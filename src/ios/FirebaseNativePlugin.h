#import <Cordova/CDV.h>

@interface FirebaseNativePlugin : CDVPlugin

- (void)once:(CDVInvokedUrlCommand *)command;
- (void)on:(CDVInvokedUrlCommand *)command;
- (void)off:(CDVInvokedUrlCommand *)command;
- (void)push:(CDVInvokedUrlCommand *)command;

@property(strong) NSMutableDictionary *listeners;

@end
