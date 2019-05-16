#import <FBSDKCoreKit/FBSDKCoreKit.h>
#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <Cordova/CDV.h>

@interface FirebaseFacebookAuthPlugin : CDVPlugin

- (void)login:(CDVInvokedUrlCommand *)command;

@property (strong, nonatomic) FBSDKLoginManager *loginManager;

@end
