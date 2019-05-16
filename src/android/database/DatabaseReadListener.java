package com.alexnu.firebasenative;

import android.util.Log;
import org.apache.cordova.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class DatabaseReadListener implements ValueEventListener {

    private static final String TAG = "DatabaseReadListener";

    private CallbackContext callbackContext;
    private boolean keepCallback;
    private Gson gson;

    public DatabaseReadListener(CallbackContext callbackContext, boolean keepCallback) {
        this.callbackContext = callbackContext;
        this.keepCallback = keepCallback;
        this.gson = new Gson();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d(TAG, "Got value from key: " + dataSnapshot.getKey());
        PluginResult result = transformToResult(dataSnapshot);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.d(TAG, "Error from DB");
        callbackContext.error(error.getMessage());
    }

    private PluginResult transformToResult(DataSnapshot dataSnapshot) {
        JSONObject data = new JSONObject();
        Object value = dataSnapshot.getValue(false);

        try {
            data.put("priority", dataSnapshot.getPriority());
            data.put("key", dataSnapshot.getKey());
            if (value instanceof Map) {
                value = new JSONObject(this.gson.toJson(value));
            } else if (value instanceof List) {
                value = new JSONArray(this.gson.toJson(value));
            }
            data.put("value", value);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
        pluginResult.setKeepCallback(keepCallback);
        return pluginResult;
    }
}
