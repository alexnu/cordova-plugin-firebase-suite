#import "FirebaseAuthGooglePlugin.h"
@import Firebase;

@implementation FirebaseAuthGooglePlugin

- (void)pluginInitialize {

    NSLog(@"Starting Firebase Native plugin");

    [GIDSignIn sharedInstance].clientID = [FIRApp defaultApp].options.clientID;
    [GIDSignIn sharedInstance].uiDelegate = self.viewController;
    [GIDSignIn sharedInstance].delegate = self;
}

- (void)signIn:(CDVInvokedUrlCommand *)command {

    BOOL silent = [command.arguments[0] boolValue];
    if(silent == YES) {
        [[GIDSignIn sharedInstance] signInSilently];
    } else {
        [[GIDSignIn sharedInstance] signIn];
    }
}

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {

    NSDictionary *message = nil;
    if (error == nil) {
        GIDAuthentication *authentication = user.authentication;
        FIRAuthCredential *credential = [FIRGoogleAuthProvider credentialWithIDToken:authentication.idToken
                                                                         accessToken:authentication.accessToken];
        [[FIRAuth auth] signInWithCredential:credential
                                  completion:[self handleLogin]];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
                @"type": @"signinfailure",
                @"data": @{
                        @"code": @(error.code),
                        @"message": error.description
                }
        }];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
    }

}

- (void (^)(FIRUser *, NSError *))handleLogin {
    return ^(FIRUser *user, NSError *error) {

        if (error == nil) {
            FIRUser *currentUser = [FIRAuth auth].currentUser;
            [currentUser getTokenWithCompletion:^(NSString *_Nullable idToken,
                                                  NSError *_Nullable error) {

                                         NSDictionary *message;

                                         if (error) {
                                             message = @{
                                                         @"type": @"signinfailure",
                                                         @"data": @{
                                                                 @"code": [NSNumber numberWithInteger:error.code],
                                                                 @"message": error.description == nil ? [NSNull null] : error.description
                                                                 }
                                                         };
                                         } else {

                                            message = @{
                                                         @"type": @"signinsuccess",
                                                         @"data": @{
                                                                 @"token": idToken,
                                                                 @"id": user.uid == nil ? [NSNull null] : user.uid,
                                                                 @"name": user.displayName == nil ? [NSNull null] : user.displayName,
                                                                 @"email": user.email == nil ? [NSNull null] : user.email,
                                                                 @"photoUrl": user.photoURL == nil ? [NSNull null] : [user.photoURL absoluteString]
                                                                 }
                                                         };
                                         }

                                         CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
                                         [pluginResult setKeepCallbackAsBool:YES];
                                         [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];

                                     }];
        } else {
            NSDictionary *message = @{
                    @"type": @"signinfailure",
                    @"data": @{
                            @"code": [NSNumber numberWithInteger:error.code],
                            @"message": error.description == nil ? [NSNull null] : error.description
                    }
            };

            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
        }
    };
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
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.eventCallbackId];
}

@end
