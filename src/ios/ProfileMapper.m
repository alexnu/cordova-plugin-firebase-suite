#import "ProfileMapper.h"
@import Firebase;

@implementation ProfileMapper

+ (CDVPluginResult*)getProfileResult:(FIRUser*)user withInfo:(FIRAdditionalUserInfo*_Nullable)additionalUserInfo {
    NSDictionary* response = nil;
    if (user) {
        response = @{
            @"uid": user.uid,
            @"providerId": user.providerID,
            @"displayName": user.displayName ? user.displayName : @"",
            @"email": user.email ? user.email : @"",
            @"phoneNumber": user.phoneNumber ? user.phoneNumber : @"",
            @"photoURL": user.photoURL ? user.photoURL.absoluteString : @"",
            @"emailVerified": [NSNumber numberWithBool:user.emailVerified],
            @"newUser": additionalUserInfo && additionalUserInfo.newUser ? @YES : @NO
        };
    }

    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
}
