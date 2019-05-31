package com.alexnu.firebasenative;

import android.util.Log;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.apache.cordova.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.firebase.auth.FirebaseAuth;


import java.io.File;


public class FirebaseStoragePlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseStorage";
    private FirebaseStorage storage;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase-storage plugin");
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("putFile".equals(action)) {

            final String remotePath = data.getString(0);
            final String localPath = data.getString(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Uploading file from " + localPath + " to " + remotePath);
                    Log.d(TAG, "Auth status " + FirebaseAuth.getInstance().getCurrentUser().toString());

                    final StorageReference storageRef = storage.getReference().child(remotePath);
                    Uri file = Uri.fromFile(new File(localPath));
                    UploadTask uploadTask = storageRef.child(remotePath).putFile(file);

                    // Listen for state changes, errors, and completion of the upload.
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            callbackContext.sendPluginResult(transformProgressToResult(taskSnapshot));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            callbackContext.error(exception.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful uploads on complete
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "putFile:onSuccess: uri= "+ uri.toString());
                                    callbackContext.sendPluginResult(transformSuccessToResult(uri));
                                }
                            });
                        }
                    });
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else {

            return false;

        }
    }

    private PluginResult transformProgressToResult(UploadTask.TaskSnapshot taskSnapshot) {
        JSONObject data = new JSONObject();
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

        try {
            data.put("progress", progress);
            data.put("completed", false);
            data.put("downloadUrl", null);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
        pluginResult.setKeepCallback(true);
        return pluginResult;
    }

    private PluginResult transformSuccessToResult(Uri uri) {
            JSONObject data = new JSONObject();

            try {
                data.put("progress", 100.0);
                data.put("completed", true);
                data.put("downloadUrl", uri.toString());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
            return pluginResult;
        }
}
