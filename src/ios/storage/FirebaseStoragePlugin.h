#import <Cordova/CDV.h>
@import Firebase;

@interface FirebaseStoragePlugin : CDVPlugin

- (void)putFile:(CDVInvokedUrlCommand *)command;
- (void)cancelUpload:(CDVInvokedUrlCommand *)command;
- (void)deleteFile:(CDVInvokedUrlCommand *)command;

@property(strong) NSMutableDictionary *uploadTasks;
@property(strong) FIRStorage* storage;

@end
