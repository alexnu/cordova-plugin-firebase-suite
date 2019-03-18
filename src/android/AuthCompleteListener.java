package com.alexnu.plugin;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class AuthCompleteListener implements OnCompleteListener<AuthResult> {

    private static final String TAG = "AuthCompleteListener";

    private CallbackContext callbackContext;
    private String action;

    public AuthCompleteListener(CallbackContext callbackContext, String action) {
        this.callbackContext = callbackContext;
        this.action = action;
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Log.d(TAG, this.action + ":success");
            PluginResult pluginResult = AuthStatusListener.getProfileResult(task.getResult().getUser());
            callbackContext.sendPluginResult(pluginResult);
        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, this.action + ":failure", task.getException());
            callbackContext.error(task.getException().getMessage());
        }
    }
}
