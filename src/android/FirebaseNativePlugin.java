package com.alexnu.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import java.lang.reflect.Type;


public class FirebaseNativePlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseNative";
    private final static Type settableType = new TypeToken<Map<String, Object>>() {}.getType();

    private FirebaseDatabase database;
    private Map<String, ValueEventListener> listeners;
    private Gson gson;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase-native plugin");

        this.gson = new Gson();
        this.database = FirebaseDatabase.getInstance();
        this.database.setPersistenceEnabled(true);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("once".equals(action)) {

            String path = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {

                    Log.d(TAG, "Reading from path: " + path);

                    database.getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Got value from path: " + path);
                            PluginResult result = transformToResult(dataSnapshot);
                            callbackContext.sendPluginResult(result);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.d(TAG, "Error from DB");
                            callbackContext.error(error.getMessage());
                        }
                    });
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("on".equals(action)) {

            String path = data.getString(0);
            if (listeners.containsKey(path)) {
                callbackContext.error("Listener already exists for " + path);
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {

                    Log.d(TAG, "Listening from path: " + path);

                    ValueEventListener listener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Got value from path: " + path);
                            PluginResult result = transformToResult(dataSnapshot);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.d(TAG, "Error while reading from path " + path);
                            callbackContext.error(error.getMessage());
                        }
                    };
                    database.getReference(path).addValueEventListener(listener);
                    listeners.put(path, listener);
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("off".equals(action)) {

            String path = data.getString(0);
            ValueEventListener listener = listeners.get(path);

            if (listener == null) {
                callbackContext.error("No listener found for " + path);
            } else {
                Log.d(TAG, "Removing listener from path: " + path);
                database.getReference(path).removeEventListener(listener);
                listeners.remove(path);
                PluginResult noResult = new PluginResult(PluginResult.Status.OK, "");
                callbackContext.sendPluginResult(noResult);
            }

            return true;

        } else if ("push".equals(action)) {

            String path = data.getString(0);
            Object value = data.get(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Pushing to path: " + path);
                    database.getReference(path).push().setValue(toSettable(value))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Write was successful");
                                PluginResult noResult = new PluginResult(PluginResult.Status.OK, "");
                                callbackContext.sendPluginResult(noResult);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception error) {
                                Log.d(TAG, "Error while writing to DB");
                                callbackContext.error(error.getMessage());
                            }
                        });;
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.OK, "");
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

    private Object toSettable(Object value) {
        Object result = value;

        if (value instanceof JSONObject) {
            result = this.gson.fromJson(value.toString(), settableType);
        }

        return result;
    }

}
