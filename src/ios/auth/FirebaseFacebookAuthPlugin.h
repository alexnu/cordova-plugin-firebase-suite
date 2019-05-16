#import <Foundation/Foundation.h>
#import <FBSDKCoreKit/FBSDKCoreKit.h>
#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <FBSDKShareKit/FBSDKShareKit.h>
#import <Cordova/CDV.h>
#import "AppDelegate.h"

@interface FirebaseFacebookAuthPlugin : CDVPlugin

- (void)login:(CDVInvokedUrlCommand *)command;

@property (strong, nonatomic) FBSDKLoginManager *loginManager;

@end
