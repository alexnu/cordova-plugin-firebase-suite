package com.alexnu.plugin;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


public class AuthSignInListener implements OnCompleteListener<AuthResult> {

    private static final String TAG = "AuthSignInListener";

    private CallbackContext callbackContext;
    private String action;

    public AuthSignInListener(CallbackContext callbackContext, String action) {
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

            JSONObject error = new JSONObject();
            Exception exception = task.getException();

            if (exception instanceof FirebaseAuthWeakPasswordException) {
                data.put("priority", "auth/weak-password");
            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                data.put("priority", "auth/invalid-email");
            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                data.put("priority", "auth/email-already-in-use");
            } else {
                data.put("priority", "auth/unexpected");
            }

            callbackContext.error(error);
        }
    }
}
