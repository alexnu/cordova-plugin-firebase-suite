#import "FirebaseAuthPlugin.h"
@import Firebase;

@implementation FirebaseAuthPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Auth plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.auth = [FIRAuth auth];
}

- (void)createUserWithEmailAndPassword:(CDVInvokedUrlCommand *)command {
    NSString* email = [command.arguments objectAtIndex:0];
    NSString* password = [command.arguments objectAtIndex:1];

    [[FIRAuth auth] createUserWithEmail:email
                               password:password
                             completion:^(FIRAuthDataResult *result, NSError *error) {
        [self.commandDelegate sendPluginResult:[self createAuthResult:result
                                                            withError:error] callbackId:command.callbackId];
    }];
}

- (void)getTokenId:(CDVInvokedUrlCommand *)command {
    FIRUser *user = [FIRAuth auth].currentUser;

    if (user) {
        [user getIDTokenWithCompletion:^(NSString *token, NSError *error) {
            CDVPluginResult *pluginResult;
            if (error) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:token];
            }

            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"User must be signed in"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)signInWithEmailAndPassword:(CDVInvokedUrlCommand *)command {
    NSString* email = [command.arguments objectAtIndex:0];
    NSString* password = [command.arguments objectAtIndex:1];

    [[FIRAuth auth] signInWithEmail:email
                           password:password
                         completion:^(FIRAuthDataResult *result, NSError *error) {
        [self.commandDelegate sendPluginResult:[self createAuthResult:result
                                                            withError:error] callbackId:command.callbackId];
    }];
}

- (CDVPluginResult*) createAuthResult:(FIRAuthDataResult*)result withError:(NSError*)error {
    CDVPluginResult *pluginResult;
    if (error) {
        NSString* initialCode = error.code;
        NSLog(@"Got error: %s", initialCode);
        NSString* finalCode;

        if ([code isEqualToString:@"FIRAuthErrorCodeInvalidEmail"]) {
            finalCode = @"auth/invalid-email";
        } else if ([code isEqualToString:@"FIRAuthErrorCodeEmailAlreadyInUse"]) {
            finalCode = @"auth/email-already-in-use";
        } else if ([code isEqualToString:@"FIRAuthErrorCodeWeakPassword"]) {
            finalCode = @"auth/weak-password";
        } else {
            finalCode = @"auth/unexpected";
        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"code": finalCode
        }];
    } else {
        pluginResult = [self getProfileResult:result.user];
    }
    return pluginResult;
}

- (CDVPluginResult*)getProfileResult:(FIRUser *)user {
    NSDictionary* result = nil;
    if (user) {
        result = @{
            @"uid": user.uid,
            @"providerId": user.providerID,
            @"displayName": user.displayName ? user.displayName : @"",
            @"email": user.email ? user.email : @"",
            @"phoneNumber": user.phoneNumber ? user.phoneNumber : @"",
            @"photoURL": user.photoURL ? user.photoURL.absoluteString : @"",
            @"emailVerified": [NSNumber numberWithBool:user.emailVerified]
        };
    }

    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
}

- (void)addAuthStateListener:(CDVInvokedUrlCommand*)command {
    if (self.authListener) {
        [[FIRAuth auth] removeAuthStateDidChangeListener:self.authListener];
        self.authListener = nil;
    }

    self.authListener = [[FIRAuth auth]
        addAuthStateDidChangeListener:^(FIRAuth *_Nonnull auth, FIRUser *_Nullable user) {
            CDVPluginResult *pluginResult = [self getProfileResult:user];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
}

- (void)removeAuthStateListener:(CDVInvokedUrlCommand*)command {
    if (self.authListener) {
        [[FIRAuth auth] removeAuthStateDidChangeListener:self.authListener];
        self.authListener = nil;
    }
}

- (void)signOut:(CDVInvokedUrlCommand*)command {
    NSError *signOutError;
    CDVPluginResult *pluginResult;

    if ([[FIRAuth auth] signOut:&signOutError]) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:signOutError.localizedDescription];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
