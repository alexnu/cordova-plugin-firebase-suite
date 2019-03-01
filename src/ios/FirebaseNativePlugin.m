#import "FirebaseNativePlugin.h"
@import Firebase;

@implementation FirebaseNativePlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Native plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    [FIRDatabase database].persistenceEnabled = YES;
}

- (void)push:(CDVInvokedUrlCommand *)command {
    FIRDatabase* database = [FIRDatabase database];
    NSString *path = [command argumentAtIndex:0];
    id value = [command argumentAtIndex:1];
    FIRDatabaseReference *ref = [database referenceWithPath:path];

    [[ref childByAutoId] setValue:value withCompletionBlock:^(NSError *error, FIRDatabaseReference *ref) {
        dispatch_async(dispatch_get_main_queue(), ^{
            CDVPluginResult *pluginResult;
            if (error) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                        @"code": @(error.code),
                        @"message": error.description
                }];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"%@/%@", path, [ref key]]];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        });
    }];
}

- (void)on:(CDVInvokedUrlCommand *)command {
    FIRDatabase* database = [FIRDatabase database];
    NSString *path = [command argumentAtIndex:0];
    FIRDatabaseReference *ref = [database referenceWithPath:path];

    id handler = ^(FIRDataSnapshot *_Nonnull snapshot) {
        dispatch_async(dispatch_get_main_queue(), ^{
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"key": snapshot.key,
                @"value": snapshot.value,
                @"priority": snapshot.priority
            }];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        });
    };

    FIRDatabaseHandle handle = [ref observeEventType:FIRDataEventTypeValue withBlock:handler];
}

@end
