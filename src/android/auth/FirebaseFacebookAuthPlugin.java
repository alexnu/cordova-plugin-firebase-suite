package com.alexnu.firebasenative;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.crashlytics.android.Crashlytics;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;
import com.facebook.FacebookException;

import java.util.Arrays;


public class FirebaseFacebookAuthPlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseFacebookAuth";

    private FirebaseAuth auth;
    private CallbackManager mCallbackManager;
    private CallbackContext callbackContext;

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

                    if (loginResult.getRecentlyDeniedPermissions().size() > 0) {
                        JSONObject error = new JSONObject();
                        try {
                            error.put("code", "auth/permission-not-granted");
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        callbackContext.error(error);
                        return;
                    }

                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                     Log.w(TAG, "facebook:onCancel");
                     JSONObject error = new JSONObject();

                     try {
                         error.put("code", "auth/cancelled-popup-request");
                     } catch (JSONException e) {
                         Log.e(TAG, e.getMessage());
                     }

                     callbackContext.error(error);
                }

                @Override
                public void onError(FacebookException ex) {
                     Log.e(TAG, "facebook:onError", ex);
                     JSONObject error = new JSONObject();

                     try {
                        error.put("code", "auth/general-error");
                        error.put("message", ex.getMessage());
                     } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                     }

                     Crashlytics.logException(ex);
                     callbackContext.error(error);
                }
            });
    }

    @Override
    public boolean execute(String action, final JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("signIn".equals(action)) {
            // Set up the activity result callback to this class
            this.callbackContext = callbackContext;
            cordova.setActivityResultCallback(this);
            LoginManager.getInstance().logOut();
            LoginManager.getInstance().logInWithReadPermissions(cordova.getActivity(),
                Arrays.asList("email", "public_profile"));
            return true;

        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity returned result");

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
