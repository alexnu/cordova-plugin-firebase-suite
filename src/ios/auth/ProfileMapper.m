#import "ProfileMapper.h"
@import Firebase;

@implementation ProfileMapper

+ (CDVPluginResult*) createAuthResult:(FIRAuthDataResult*)result withError:(NSError*)error {
    CDVPluginResult *pluginResult;
    if (error) {
        NSLog(@"Got error: %d", error.code);
        NSString* finalCode;

        if (error.code == FIRAuthErrorCodeInvalidEmail) {
            finalCode = @"auth/invalid-email";
        } else if (error.code == FIRAuthErrorCodeEmailAlreadyInUse) {
            finalCode = @"auth/email-already-in-use";
        } else if (error.code == FIRAuthErrorCodeWeakPassword) {
            finalCode = @"auth/weak-password";
        } else if (error.code == FIRAuthErrorCodeAccountExistsWithDifferentCredential) {
            finalCode = @"auth/email-already-in-use";
        } else if (error.code == FIRAuthErrorCodeUserDisabled) {
            finalCode = @"auth/invalid-email";
        } else if (error.code == FIRAuthErrorCodeWrongPassword) {
            finalCode = @"auth/wrong-password";
        } else {
            finalCode = @"auth/general-error";
        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"code": finalCode,
            @"message": error.localizedDescription
        }];
    } else {
        pluginResult = [ProfileMapper getProfileResult:result.user];
    }
    return pluginResult;
}

+ (CDVPluginResult*)getProfileResult:(FIRUser*)user {
    NSDictionary* response = nil;
    if (user) {
        response = @{
            @"uid": user.uid,
            @"providerId": user.providerID,
            @"displayName": user.displayName ? user.displayName : @"",
            @"email": user.email ? user.email : @"",
            @"phoneNumber": user.phoneNumber ? user.phoneNumber : @"",
            @"photoURL": user.photoURL ? user.photoURL.absoluteString : @"",
            @"emailVerified": [NSNumber numberWithBool:user.emailVerified]
        };
    }

    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
}

@end
