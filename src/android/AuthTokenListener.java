package com.alexnu.plugin;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

public class AuthTokenListener implements OnCompleteListener<GetTokenResult> {

    private static final String TAG = "AuthTokenListener";

    private CallbackContext callbackContext;
    private String action;

    public AuthTokenListener(CallbackContext callbackContext, String action) {
        this.callbackContext = callbackContext;
        this.action = action;
    }

    @Override
    public void onComplete(@NonNull Task<GetTokenResult> task) {
        if (task.isSuccessful()) {
            Log.d(TAG, this.action + ":success");
            PluginResult okResult = new PluginResult(PluginResult.Status.OK, task.getResult().getToken());
            callbackContext.sendPluginResult(okResult);
        } else {
            Log.w(TAG, this.action + ":failure", task.getException());
            callbackContext.error(task.getException().getMessage());
        }
    }
}
