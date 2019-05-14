#import <Cordova/CDV.h>
@import Firebase;

@interface MyClass : NSObject

+ (CDVPluginResult*)getProfileResult:(FIRUser*)user withInfo:(FIRAdditionalUserInfo*_Nullable)additionalUserInfo;

@end
