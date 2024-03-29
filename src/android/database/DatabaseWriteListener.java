package com.alexnu.firebasenative;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class DatabaseWriteListener implements OnCompleteListener<Void> {

    private static final String TAG = "DatabaseWriteListener";

    private CallbackContext callbackContext;
    private String action;

    public DatabaseWriteListener(CallbackContext callbackContext, String action) {
        this.callbackContext = callbackContext;
        this.action = action;
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.d(TAG, this.action + ":success");
            PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
            callbackContext.sendPluginResult(okResult);
        } else {
            Log.w(TAG, this.action + ":failure", task.getException());
            callbackContext.error(task.getException().getMessage());
        }
    }
}
