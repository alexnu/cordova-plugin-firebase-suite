package com.alexnu.firebasenative;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;


public class AuthStatusListener implements AuthStateListener {

    private static final String TAG = "FirebaseAuthStatusListener";

    private CallbackContext callbackContext;

    public AuthStatusListener(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {
        Log.d(TAG, "Auth state changed");
        PluginResult pluginResult = ProfileMapper.getProfileResult(auth.getCurrentUser(), null);
        pluginResult.setKeepCallback(true);
        this.callbackContext.sendPluginResult(pluginResult);
    }

}
