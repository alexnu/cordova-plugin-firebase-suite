package com.alexnu.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

        this.database = FirebaseDatabase.getInstance();
        this.database.setPersistenceEnabled(true);
        this.listeners = new HashMap<String, ValueEventListener>();
        this.gson = new Gson();
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("once".equals(action)) {

            final String path = data.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Reading from path: " + path);
                    database.getReference(path).addListenerForSingleValueEvent(
                        new DatabaseReadListener(callbackContext, false));
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("on".equals(action)) {

            final String path = data.getString(0);
            if (listeners.containsKey(path)) {
                Log.d(TAG, "Listener already exists for path " + path);
                callbackContext.error("Listener already exists for " + path);
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Listening from path: " + path);
                    ValueEventListener listener = new DatabaseReadListener(callbackContext, true);
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
                Log.d(TAG, "No listener found for " + path);
                callbackContext.error("No listener found for " + path);
            } else {
                Log.d(TAG, "Removing listener from path: " + path);
                database.getReference(path).removeEventListener(listener);
                listeners.remove(path);
                PluginResult noResult = new PluginResult(PluginResult.Status.OK, "");
                callbackContext.sendPluginResult(noResult);
            }

            return true;

        } else if ("generateKey".equals(action)) {

            final String path = data.getString(0);
            String key = database.getReference(path).push().getKey()

            PluginResult result = new PluginResult(PluginResult.Status.OK, key);
            callbackContext.sendPluginResult(result);
            
            return true;

        } else if ("push".equals(action)) {

            final String path = data.getString(0);
            final Object value = data.get(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Pushing to path: " + path);
                    database.getReference(path).push().setValue(toSettable(value))
                        .addOnCompleteListener(new DatabaseWriteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("set".equals(action)) {

            final String path = data.getString(0);
            final Object value = data.get(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Setting path: " + path);
                    database.getReference(path).setValue(toSettable(value))
                        .addOnCompleteListener(new DatabaseWriteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("update".equals(action)) {

            final String path = data.getString(0);
            final Object value = data.get(1);

            if (!(value instanceof JSONObject)) {
                callbackContext.error("Value should be json");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Updating path: " + path);
                    Map<String, Object> mapValue = gson.fromJson(value.toString(), settableType);

                    database.getReference(path).updateChildren(mapValue)
                        .addOnCompleteListener(new DatabaseWriteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("remove".equals(action)) {

            final String path = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Removing path: " + path);
                    database.getReference(path).removeValue()
                        .addOnCompleteListener(new DatabaseWriteListener(callbackContext, action));
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

    private Object toSettable(Object value) {
        Object result = value;

        if (value instanceof JSONObject) {
            result = this.gson.fromJson(value.toString(), settableType);
        }

        return result;
    }

}
