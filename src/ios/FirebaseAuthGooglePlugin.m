#import "FirebaseAuthGooglePlugin.h"
@import Firebase;

@implementation FirebaseAuthGooglePlugin

- (void)pluginInitialize {

    NSLog(@"Starting Firebase Google Auth plugin");

    [GIDSignIn sharedInstance].clientID = [FIRApp defaultApp].options.clientID;
    [GIDSignIn sharedInstance].uiDelegate = self.viewController;
    [GIDSignIn sharedInstance].delegate = self;
}

- (void)signIn:(CDVInvokedUrlCommand *)command {

    self.eventCallbackId = command.callbackId;
    [[GIDSignIn sharedInstance] signIn];
}

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {

    NSDictionary *message = nil;
    if (error == nil) {
        GIDAuthentication *authentication = user.authentication;
        FIRAuthCredential *credential = [FIRGoogleAuthProvider credentialWithIDToken:authentication.idToken
                                                                         accessToken:authentication.accessToken];
        [[FIRAuth auth] signInWithCredential:credential
                                  completion:^(FIRAuthDataResult *result, NSError *error) {
            [self.commandDelegate sendPluginResult:[self createAuthResult:result
                                                                withError:error] callbackId:self.eventCallbackId];
        }];
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

- (CDVPluginResult*) createAuthResult:(FIRAuthDataResult*)result withError:(NSError*)error {
    CDVPluginResult *pluginResult;
    if (error) {
        NSLog(@"Got error: %d", error.code);
        NSString* finalCode;

        if (error.code == FIRAuthErrorCodeAccountExistsWithDifferentCredential) {
            finalCode = @"auth/email-already-in-use";
        } else {
            finalCode = @"auth/unexpected";
        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"code": finalCode
        }];
    } else {
        pluginResult = [self getProfileResult:result];
    }
    return pluginResult;
}

- (CDVPluginResult*)getProfileResult:(FIRAuthDataResult*)result {
    NSDictionary* response = nil;
    if (result) {
        response = @{
            @"uid": result.user.uid,
            @"providerId": result.user.providerID,
            @"displayName": result.user.displayName ? result.user.displayName : @"",
            @"email": result.user.email ? result.user.email : @"",
            @"phoneNumber": result.user.phoneNumber ? result.user.phoneNumber : @"",
            @"photoURL": result.user.photoURL ? result.user.photoURL.absoluteString : @"",
            @"emailVerified": [NSNumber numberWithBool:result.user.emailVerified],
            @"newUser": result.additionalUserInfo.newUser ? @"true" : @"false"
        };
    }

    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
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