#import "FirebaseAuthGooglePlugin.h"
#import "ProfileMapper.h"
#import <Crashlytics/Crashlytics.h>
@import Firebase;

@implementation FirebaseAuthGooglePlugin

- (void)pluginInitialize {

    NSLog(@"Starting Firebase Google auth plugin");

    [GIDSignIn sharedInstance].clientID = [FIRApp defaultApp].options.clientID;
    [GIDSignIn sharedInstance].uiDelegate = self.viewController;
    [GIDSignIn sharedInstance].delegate = self;
}

- (void)signIn:(CDVInvokedUrlCommand *)command {

    self.eventCallbackId = command.callbackId;
    [[GIDSignIn sharedInstance] signOut];
    [[GIDSignIn sharedInstance] signIn];
}

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {

    if (error == nil) {
        GIDAuthentication *authentication = user.authentication;
        FIRAuthCredential *credential = [FIRGoogleAuthProvider credentialWithIDToken:authentication.idToken
                                                                         accessToken:authentication.accessToken];
        [[FIRAuth auth] signInWithCredential:credential
                                  completion:^(FIRAuthDataResult *result, NSError *error) {
            [self.commandDelegate sendPluginResult:[ProfileMapper createAuthResult:result
                                                                         withError:error] callbackId:self.eventCallbackId];
        }];
    } else if (error.code == kGIDSignInErrorCodeCanceled) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
             @"code": @"auth/cancelled-popup-request"
         }];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
    } else {
        [CrashlyticsKit recordError:error];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
             @"code": @"auth/general-error",
             @"message": error.localizedDescription
         }];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
    }

}

- (void)signIn:(GIDSignIn *)signIn didDisconnectWithUser:(GIDGoogleUser *)user withError:(NSError *)error {

    NSDictionary *message = nil;
    if (error == nil) {
        GIDProfileData *profile = user.profile;
        message = @{
                @"type": @"signoutsuccess"
        };
    } else {
        message = @{
                @"type": @"signoutfailure",
                @"data": @{

                        @"code": [NSNumber numberWithInteger:error.code],
                        @"message": error.description ? error.description : [NSNull null]
                }
        };
    }

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
}

@end
