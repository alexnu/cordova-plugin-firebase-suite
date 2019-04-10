#import <Cordova/CDV.h>
@import Firebase;

@interface FirebaseAuthPlugin : CDVPlugin

- (void)signInWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)createUserWithEmailAndPassword:(CDVInvokedUrlCommand *)command;
- (void)getTokenId:(CDVInvokedUrlCommand *)command;
- (void)addAuthStateListener:(CDVInvokedUrlCommand *)command;
- (void)removeAuthStateListener:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;

@property(strong) FIRAuth* auth;
@property(strong, nonatomic) FIRAuthStateDidChangeListenerHandle authListener;

@end
