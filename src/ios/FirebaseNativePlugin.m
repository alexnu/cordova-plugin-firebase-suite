#import "FirebaseNativePlugin.h"
@import Firebase;

@implementation FirebaseNativePlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Native plugin");

    self.listeners = [NSMutableDictionary dictionary];

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    [FIRDatabase database].persistenceEnabled = YES;
}

- (void)push:(CDVInvokedUrlCommand *)command {
    FIRDatabase* database = [FIRDatabase database];
    NSString *path = [command argumentAtIndex:0];
    id value = [command argumentAtIndex:1];

    NSLog(@"Pushing to path %@", path);
    FIRDatabaseReference *ref = [database referenceWithPath:path];

    [[ref childByAutoId] setValue:value withCompletionBlock:^(NSError *error, FIRDatabaseReference *ref) {
        dispatch_async(dispatch_get_main_queue(), ^{
            CDVPluginResult *pluginResult;
            if (error) {
                NSLog(@"Error while writing to DB");
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                        @"code": @(error.code),
                        @"message": error.description
                }];
            } else {
                NSLog(@"Write was successful");
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"%@", path]];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        });
    }];
}

- (void)on:(CDVInvokedUrlCommand *)command {
    FIRDatabase* database = [FIRDatabase database];
    NSString *path = [command argumentAtIndex:0];

    if ([self.listeners objectForKey:path]) {
        NSLog(@"Listener already exists for path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Listener already exists for path"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    NSLog(@"Listening from path %@", path);
    FIRDatabaseReference *ref = [database referenceWithPath:path];

    id handler = ^(FIRDataSnapshot *_Nonnull snapshot) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"Got value from path %@", path);
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"key": snapshot.key,
                @"value": snapshot.value,
                @"priority": snapshot.priority
            }];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        });
    };

    id errorHandler = ^(NSError * _Nonnull error) {
        NSLog(@"Error while reading from path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    FIRDatabaseHandle handle = [ref observeEventType:FIRDataEventTypeValue withBlock:handler withCancelBlock:errorHandler];
    [self.listeners setObject:@(handle) forKey:path];
}

- (void)once:(CDVInvokedUrlCommand *)command {
    FIRDatabase* database = [FIRDatabase database];
    NSString *path = [command argumentAtIndex:0];

    NSLog(@"Reading from path %@", path);
    FIRDatabaseReference *ref = [database referenceWithPath:path];

    id handler = ^(FIRDataSnapshot *_Nonnull snapshot) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"Got value from path %@", path);
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"key": snapshot.key,
                @"value": snapshot.value,
                @"priority": snapshot.priority
            }];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        });
    };

    id errorHandler = ^(NSError * _Nonnull error) {
        NSLog(@"Error while reading from path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [ref observeSingleEventOfType:FIRDataEventTypeValue withBlock:handler withCancelBlock:errorHandler];
}

@end
