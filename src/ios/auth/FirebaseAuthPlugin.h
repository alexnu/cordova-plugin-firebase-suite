#import <Cordova/CDV.h>
@import Firebase;

@interface FirebaseAuthPlugin : CDVPlugin

- (void)getCurrentUser:(CDVInvokedUrlCommand *)command;
- (void)signInWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)createUserWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)getTokenId:(CDVInvokedUrlCommand *)command;
- (void)addAuthStateListener:(CDVInvokedUrlCommand *)command;
- (void)removeAuthStateListener:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;

- (void)respondToAuthState:(CDVInvokedUrlCommand *)command withUser:(FIRUser*)user;

@property(strong) FIRAuth* auth;
@property(strong, nonatomic) FIRAuthStateDidChangeListenerHandle authListener;
@property Boolean responsePending;

@end
