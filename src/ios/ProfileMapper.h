#import <Cordova/CDV.h>
@import Firebase;

@interface ProfileMapper : NSObject

+ (CDVPluginResult*)getProfileResult:(FIRUser*)user withInfo:(FIRAdditionalUserInfo*_Nullable)additionalUserInfo;

@end
