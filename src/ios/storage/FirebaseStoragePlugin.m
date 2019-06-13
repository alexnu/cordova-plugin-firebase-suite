#import "FirebaseStoragePlugin.h"
@import Firebase;

@implementation FirebaseStoragePlugin

- (void)pluginInitialize {
    NSLog(@"Starting FirebaseStorage plugin");

    self.uploadTasks = [NSMutableDictionary dictionary];

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.storage = [FIRStorage storage];
}

- (void)putFile:(CDVInvokedUrlCommand *)command {

    NSString *remotePath = [command argumentAtIndex:0];
    NSString *fileUri = [command argumentAtIndex:1];

    if ([self.uploadTasks objectForKey:remotePath]) {
        NSLog(@"Upload task already exists for path %@", remotePath);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Upload task already exists for path"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    // File located on disk
    NSURL *localFile = [NSURL fileURLWithPath:fileUri];

    // Create a reference to the file you want to upload
    FIRStorageReference *storageRef = [[storage reference] child:remotePath];

    // Upload the file to the path
    FIRStorageUploadTask *uploadTask = [storageRef putFile:localFile metadata:nil completion:^(FIRStorageMetadata *metadata, NSError *error) {
      [self.uploadTasks removeObjectForKey:remotePath];
      if (error != nil) {
        // Uh-oh, an error occurred!
        NSLog(@"Upload failed");
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
      } else {
        // You can access download URL after upload.
        [storageRef downloadURLWithCompletion:^(NSURL * _Nullable URL, NSError * _Nullable error) {
          if (error != nil) {
            // Uh-oh, an error occurred!
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
          } else {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"progress": @100.0,
                @"completed": YES,
                @"downloadUrl": [URL absoluteString]
            }];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
          }
        }];
      }
    }];

    // Add a progress observer to an upload task
    FIRStorageHandle observer = [uploadTask observeStatus:FIRStorageTaskStatusProgress
        handler:^(FIRStorageTaskSnapshot *snapshot) {
            double percentComplete = 100.0 * (snapshot.progress.completedUnitCount) / (snapshot.progress.totalUnitCount);
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"progress": percentComplete,
                @"completed": NO,
                @"downloadUrl": @""
            }];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];

    [self.uploadTasks setObject:@(uploadTask) forKey:remotePath];
}

- (void)cancelUpload:(CDVInvokedUrlCommand *)command {

    NSString *path = [command argumentAtIndex:0];
    FIRStorageUploadTask uploadTask = [self.uploadTasks objectForKey:path];

    if (uploadTask) {
        NSLog(@"Cancelling upload task from path %@", path);
        [uploadTask cancel];
        [self.listeners removeObjectForKey:path];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        NSLog(@"No upload task found for path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No upload task found for path %@"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)deleteFile:(CDVInvokedUrlCommand *)command {

    NSString *path = [command argumentAtIndex:0];

    // Create a reference to the file to delete
    FIRStorageReference *storageRef = [[storage reference] child:path];

    // Delete the file
    [storageRef deleteWithCompletion:^(NSError *error){
      if (error != nil) {
        // Uh-oh, an error occurred!
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
      } else {
        // File deleted successfully
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
      }
    }];
}

@end
