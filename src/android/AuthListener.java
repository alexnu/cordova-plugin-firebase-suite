package com.alexnu.plugin;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONException;
import org.json.JSONObject;


public class AuthListener implements AuthStateListener {

    private static final String TAG = "AuthListener";

    private CallbackContext callbackContext = null;

    private void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {
        if (this.callbackContext != null) {
            PluginResult pluginResult = getProfileResult(auth.getCurrentUser());
            pluginResult.setKeepCallback(true);
            this.callbackContext.sendPluginResult(pluginResult);
        }
    }

    private static PluginResult getProfileResult(FirebaseUser user) {
        if (user == null) {
            return new PluginResult(PluginResult.Status.OK, (String) null);
        }

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
