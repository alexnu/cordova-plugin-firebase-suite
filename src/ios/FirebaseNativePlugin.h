#import <Cordova/CDV.h>

@interface FirebaseNativePlugin : CDVPlugin

- (void)once:(CDVInvokedUrlCommand *)command;
- (void)on:(CDVInvokedUrlCommand *)command;
- (void)off:(CDVInvokedUrlCommand *)command;
- (void)push:(CDVInvokedUrlCommand *)command;
- (void)set:(CDVInvokedUrlCommand *)command;
- (void)update:(CDVInvokedUrlCommand *)command;
- (void)remove:(CDVInvokedUrlCommand *)command;

@property(strong) NSMutableDictionary *listeners;

@end
