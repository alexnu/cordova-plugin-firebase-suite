#import "FirebaseDatabasePlugin.h"
@import Firebase;

@implementation FirebaseDatabasePlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Native plugin");

    self.listeners = [NSMutableDictionary dictionary];

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.database = [FIRDatabase database];
    self.database.persistenceEnabled = YES;
}

- (void)on:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];

    if ([self.listeners objectForKey:path]) {
        NSLog(@"Listener already exists for path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Listener already exists for path"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    NSLog(@"Listening from path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

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

    FIRDatabaseHandle listener = [ref observeEventType:FIRDataEventTypeValue withBlock:handler withCancelBlock:errorHandler];
    [self.listeners setObject:@(listener) forKey:path];
}

- (void)off:(CDVInvokedUrlCommand *)command {

    NSString *path = [command argumentAtIndex:0];
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];
    FIRDatabaseHandle listener = [self.listeners objectForKey:path];

    if (listener) {
        NSLog(@"Removing listener from path %@", path);
        [ref removeAllObservers];
        [self.listeners removeObjectForKey:path];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        NSLog(@"No listener found for path %@", path);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No listener found for path %@"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)once:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];

    NSLog(@"Reading from path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

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

- (void)generateKey:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];

    NSLog(@"Generating key to path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];
    NSString *key = [ref childByAutoId].key;
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:key];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)push:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];
    id value = [command argumentAtIndex:1];

    NSLog(@"Pushing to path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

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

- (void)set:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];
    id value = [command argumentAtIndex:1];

    NSLog(@"Setting path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

    [ref setValue:value withCompletionBlock:^(NSError *error, FIRDatabaseReference *ref) {
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

- (void)update:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];
    id value = [command argumentAtIndex:1];

    NSLog(@"Updating path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

    [ref updateChildValues:value withCompletionBlock:^(NSError *error, FIRDatabaseReference *ref) {
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

- (void)remove:(CDVInvokedUrlCommand *)command {
    NSString *path = [command argumentAtIndex:0];

    NSLog(@"Removing path %@", path);
    FIRDatabaseReference *ref = [self.database referenceWithPath:path];

    [ref removeValueWithCompletionBlock:^(NSError *error, FIRDatabaseReference *ref) {
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

@end
