#import "FirebaseAuthPlugin.h"
#import "ProfileMapper.h"
@import Firebase;

@implementation FirebaseAuthPlugin

- (void)pluginInitialize {
    NSLog(@"Starting FirebaseAuth plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.auth = [FIRAuth auth];
    self.responsePending = NO;
}

- (void)getCurrentUser:(CDVInvokedUrlCommand *)command {
    FIRUser *user = [FIRAuth auth].currentUser;
    CDVPluginResult *pluginResult = [ProfileMapper getProfileResult:user];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)createUserWithEmailAndPassword:(CDVInvokedUrlCommand *)command {
    NSString* email = [command.arguments objectAtIndex:0];
    NSString* password = [command.arguments objectAtIndex:1];

    [[FIRAuth auth] createUserWithEmail:email
                               password:password
                             completion:^(FIRAuthDataResult *result, NSError *error) {
        [self.commandDelegate sendPluginResult:[ProfileMapper createAuthResult:result
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
        [self.commandDelegate sendPluginResult:[ProfileMapper createAuthResult:result
                                                                     withError:error] callbackId:command.callbackId];
    }];
}

- (void)addAuthStateListener:(CDVInvokedUrlCommand*)command {
    if (self.authListener) {
        [[FIRAuth auth] removeAuthStateDidChangeListener:self.authListener];
        self.authListener = nil;
    }

    self.authListener = [[FIRAuth auth]
        addAuthStateDidChangeListener:^(FIRAuth *_Nonnull auth, FIRUser *_Nullable user) {
            if (user) {
                NSLog(@"User is logged in");
                [self respondToAuthState:command withUser:user];
            } else {
                NSLog(@"User is logged out, registering callback");
                self.responsePending = YES;
                dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 1);
                dispatch_after(delay, dispatch_get_main_queue(), ^(void){
                    if (self.responsePending == YES) {
                        NSLog(@"User is still logged out, executing callback");
                        [self respondToAuthState:command withUser:user];
                    }
                });
            }
        }];
}

- (void)respondToAuthState:(CDVInvokedUrlCommand*)command withUser:(FIRUser*)user {
    self.responsePending = NO;
    CDVPluginResult *pluginResult = [ProfileMapper getProfileResult:user];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
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
