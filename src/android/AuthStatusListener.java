package com.alexnu.plugin;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONException;
import org.json.JSONObject;


public class AuthStatusListener implements AuthStateListener {

    private static final String TAG = "FirebaseAuthStatusListener";

    private CallbackContext callbackContext;

    public AuthStatusListener(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {
        Log.d(TAG, "Auth state changed");
        PluginResult pluginResult = getProfileResult(auth.getCurrentUser());
        pluginResult.setKeepCallback(true);
        this.callbackContext.sendPluginResult(pluginResult);
    }

    public static PluginResult getProfileResult(FirebaseUser user) {
        if (user == null) {
            Log.d(TAG, "User is not logged in");
            return new PluginResult(PluginResult.Status.OK, (String) null);
        }

        Log.d(TAG, "User is logged in");
        JSONObject result = new JSONObject();

        try {
            result.put("uid", user.getUid());
            result.put("displayName", user.getDisplayName());
            result.put("email", user.getEmail());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("photoURL", user.getPhotoUrl());
            result.put("providerId", user.getProviderId());
            result.put("emailVerified", user.isEmailVerified());

            return new PluginResult(PluginResult.Status.OK, result);
        } catch (JSONException e) {
            Log.e(TAG, "Fail to process getProfileData", e);

            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }
    }

}
