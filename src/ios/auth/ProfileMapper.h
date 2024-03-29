#import <Cordova/CDV.h>
@import Firebase;

@interface ProfileMapper : NSObject

+ (CDVPluginResult*)createAuthResult:(FIRAuthDataResult*)result withError:(NSError*)error;
+ (CDVPluginResult*)getProfileResult:(FIRUser*)user;

@end
