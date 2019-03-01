#import <Cordova/CDV.h>

@interface FirebaseNativePlugin : CDVPlugin

- (void)on:(CDVInvokedUrlCommand *)command;
- (void)push:(CDVInvokedUrlCommand *)command;

@end
