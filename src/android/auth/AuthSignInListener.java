package com.alexnu.firebasenative;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONObject;
import org.json.JSONException;


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
            AuthResult result = task.getResult();
            PluginResult pluginResult = ProfileMapper.getProfileResult(result.getUser(), result.getAdditionalUserInfo());
            callbackContext.sendPluginResult(pluginResult);
        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, this.action + ":failure", task.getException());

            JSONObject error = new JSONObject();
            FirebaseAuthException exception = task.getException();

            try {
                if (exception instanceof FirebaseAuthWeakPasswordException) {
                    error.put("code", "auth/weak-password");
                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    error.put("code", "auth/invalid-email");
                } else if (exception instanceof FirebaseAuthUserCollisionException) {
                    error.put("code", "auth/email-already-in-use");
                } else {
                    error.put("code", exception.getErrorCode());
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            callbackContext.error(error);
        }
    }
}
