#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <GoogleSignIn/GoogleSignIn.h>

@interface FirebaseAuthGooglePlugin : CDVPlugin <GIDSignInDelegate, GIDSignInUIDelegate>

- (void)signIn:(CDVInvokedUrlCommand *)command;

@end
