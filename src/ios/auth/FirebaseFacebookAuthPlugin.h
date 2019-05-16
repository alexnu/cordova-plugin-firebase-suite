#import <FBSDKCoreKit/FBSDKCoreKit.h>
#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <Cordova/CDV.h>

@interface FirebaseFacebookAuthPlugin : CDVPlugin

- (void)signIn:(CDVInvokedUrlCommand *)command;

@property (strong, nonatomic) FBSDKLoginManager *loginManager;

@end
