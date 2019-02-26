package com.example.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


public class Hello extends CordovaPlugin {

    private static final String TAG = "FirebaseNative";
    private FirebaseDatabase database;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase-native plugin");
        this.database = FirebaseDatabase.getInstance();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("greet".equals(action)) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else if ("once".equals(action)) {

            String ref = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {

                    Log.d(TAG, "Reading from ref: " + ref);

                    database.getReference(ref).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Got value from ref: " + ref);
                            PluginResult result = transformToResult(dataSnapshot);
                            callbackContext.sendPluginResult(result);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.d(TAG, "Error from DB");
                            callbackContext.error(error.getCode());
                        }
                    });
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("on".equals(action)) {

            String ref = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {

                    Log.d(TAG, "Listening from ref: " + ref);

                    database.getReference(ref).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Got value from ref: " + ref);
                            PluginResult result = transformToResult(dataSnapshot);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.d(TAG, "Error from DB");
                            callbackContext.error(error.getCode());
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
            } catch (JSONException e) {}

            return new PluginResult(PluginResult.Status.OK, data);
        }

}
