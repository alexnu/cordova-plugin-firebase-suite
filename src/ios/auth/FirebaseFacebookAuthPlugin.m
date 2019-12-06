#import "FirebaseFacebookAuthPlugin.h"
#import "ProfileMapper.h"
#import <Crashlytics/Crashlytics.h>
@import Firebase;

@implementation FirebaseFacebookAuthPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Facebook auth plugin");

    self.loginManager = [[FBSDKLoginManager alloc] init];
}

- (void)signIn:(CDVInvokedUrlCommand *)command {
    NSLog(@"Login with facebook");
    CDVPluginResult *pluginResult;

    // this will prevent from being unable to login after updating plugin or changing permissions
    // without refreshing there will be a cache problem. This simple call should fix the problems
    [FBSDKAccessToken refreshCurrentAccessToken:nil];

    FBSDKLoginManagerLoginResultBlock loginHandler = ^void(FBSDKLoginManagerLoginResult *result, NSError *error) {
        if (error) {
            // If the SDK has a message for the user, surface it.
            [CrashlyticsKit recordError:error];
            NSString *errorMessage = error.userInfo[FBSDKErrorLocalizedDescriptionKey] ?: @"There was a problem logging you in.";
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                @"code": @"auth/general-error",
                @"message": errorMessage
            }];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else if (result.isCancelled) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                @"code": @"auth/cancelled-popup-request"
            }];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
         } else if (result.declinedPermissions.count > 0) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                @"code": @"auth/permission-not-granted"
            }];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            FIRAuthCredential *credential = [FIRFacebookAuthProvider
                credentialWithAccessToken:[FBSDKAccessToken currentAccessToken].tokenString];

            [[FIRAuth auth] signInWithCredential:credential
                                      completion:^(FIRAuthDataResult *result, NSError *error) {
                [self.commandDelegate sendPluginResult:[ProfileMapper createAuthResult:result
                                                                             withError:error]
                                            callbackId:command.callbackId];
            }];
        }
    };

    [self.loginManager logOut];
    [self.loginManager logInWithPermissions:@[@"public_profile", @"email"]
                             fromViewController:[self topMostController]
                                        handler:loginHandler];
}

- (UIViewController*) topMostController {
    UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;

    while (topController.presentedViewController) {
        topController = topController.presentedViewController;
    }

    return topController;
}

@end
