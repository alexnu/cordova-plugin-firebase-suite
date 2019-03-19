#import <Cordova/CDV.h>

@interface FirebaseNativePlugin : CDVPlugin

- (void)once:(CDVInvokedUrlCommand *)command;
- (void)on:(CDVInvokedUrlCommand *)command;
- (void)off:(CDVInvokedUrlCommand *)command;
- (void)push:(CDVInvokedUrlCommand *)command;
- (void)set:(CDVInvokedUrlCommand *)command;
- (void)update:(CDVInvokedUrlCommand *)command;
- (void)remove:(CDVInvokedUrlCommand *)command;

- (void)signInWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)createUserWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)addAuthStateListener:(CDVInvokedUrlCommand *)command;
- (void)removeAuthStateListener:(CDVInvokedUrlCommand *)command;

@property(strong) NSMutableDictionary *listeners;
@property(strong) FIRAuth* auth;
@property(strong) FIRDatabase* database;
@property(strong) FIRAuthStateDidChangeListenerHandle authListener;

@end
