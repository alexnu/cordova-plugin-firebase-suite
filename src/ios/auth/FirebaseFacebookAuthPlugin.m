#import "FirebaseFacebookAuthPlugin.h"
#import "ProfileMapper.h"
@import Firebase;

@interface FirebaseFacebookAuthPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Facebook Connect plugin");

    self.loginManager = [[FBSDKLoginManager alloc] init];
}

- (void)login:(CDVInvokedUrlCommand *)command {
    NSLog(@"Starting facebook login");
    CDVPluginResult *pluginResult;

    // this will prevent from being unable to login after updating plugin or changing permissions
    // without refreshing there will be a cache problem. This simple call should fix the problems
    [FBSDKAccessToken refreshCurrentAccessToken:nil];

    FBSDKLoginManagerRequestTokenHandler loginHandler = ^void(FBSDKLoginManagerLoginResult *result, NSError *error) {
        if (error) {
            // If the SDK has a message for the user, surface it.
            NSString *errorMessage = error.userInfo[FBSDKErrorLocalizedDescriptionKey] ?: @"There was a problem logging you in.";
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:errorMessage];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        } else if (result.isCancelled) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:@"User cancelled."];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            FIRAuthCredential *credential = [FIRFacebookAuthProvider
                credentialWithAccessToken:[FBSDKAccessToken currentAccessToken].tokenString];

            [[FIRAuth auth] signInWithCredential:credential
                                      completion:^(FIRAuthDataResult *result, NSError *error) {
                [self.commandDelegate sendPluginResult:[self createAuthResult:result
                                                                    withError:error] callbackId:self.eventCallbackId];
            }];
        }
    };

    [self.loginManager logInWithReadPermissions:@[@"public_profile", @"email"]
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
