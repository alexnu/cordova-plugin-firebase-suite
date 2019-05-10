package com.alexnu.firebasenative;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;
import com.facebook.FacebookException;


public class FacebookAuthPlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseFacebookAuthPlugin";

    private FirebaseAuth auth;
    private CallbackManager mCallbackManager;
    private CallbackContext callbackContext;
    Set<String> permissions = new HashSet<String>(Arrays.asList("email", "public_profile"));

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting FirebaseGoogleAuthPlugin plugin");
        this.auth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                     Log.d(TAG, "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                     Log.d(TAG, "facebook:onError", error);
                }
            });
    }

    @Override
    public boolean execute(String action, final JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("signIn".equals(action)) {
            this.callbackContext = callbackContext;
            LoginManager.getInstance().logInWithReadPermissions(cordova.getActivity(), Arrays.asList("email", "public_profile"));
            return true;

        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "firebaseAuthWithFacebook:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
            .addOnCompleteListener(cordova.getActivity(),
                new AuthSignInListener(this.callbackContext, "signInWithFacebook"));
    }
}
