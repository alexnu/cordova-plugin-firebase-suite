#import <Cordova/CDV.h>
@import Firebase;

@interface FirebaseDatabasePlugin : CDVPlugin

- (void)once:(CDVInvokedUrlCommand *)command;
- (void)on:(CDVInvokedUrlCommand *)command;
- (void)off:(CDVInvokedUrlCommand *)command;
- (void)generateKey:(CDVInvokedUrlCommand *)command;
- (void)push:(CDVInvokedUrlCommand *)command;
- (void)set:(CDVInvokedUrlCommand *)command;
- (void)update:(CDVInvokedUrlCommand *)command;
- (void)remove:(CDVInvokedUrlCommand *)command;

@property(strong) NSMutableDictionary *listeners;
@property(strong) FIRDatabase* database;

@end
