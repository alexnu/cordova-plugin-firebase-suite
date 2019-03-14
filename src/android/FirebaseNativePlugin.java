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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
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

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private AuthListener authListener;
    private Map<String, ValueEventListener> listeners;

    private Gson gson;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase-native plugin");

        this.auth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
        this.database.setPersistenceEnabled(true);

        this.authListener = new AuthListener();
        this.auth.addAuthStateListener(this.authListener);
        this.listeners = new HashMap<>();

        this.gson = new Gson();
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
                Log.d(TAG, "Listener already exists for path " + path);
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
                                PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
                                callbackContext.sendPluginResult(okResult);
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

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("set".equals(action)) {

            String path = data.getString(0);
            Object value = data.get(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Setting path: " + path);
                    database.getReference(path).setValue(toSettable(value))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Write was successful");
                                PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
                                callbackContext.sendPluginResult(okResult);
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

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("update".equals(action)) {

            String path = data.getString(0);
            Object value = data.get(1);

            if (!(value instanceof JSONObject)) {
                callbackContext.error("Value should be json");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Updating path: " + path);
                    Map<String, Object> mapValue = gson.fromJson(value.toString(), settableType);

                    database.getReference(path).updateChildren(mapValue)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Write was successful");
                                PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
                                callbackContext.sendPluginResult(okResult);
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

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("remove".equals(action)) {

            String path = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Removing path: " + path);
                    database.getReference(path).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Write was successful");
                                PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
                                callbackContext.sendPluginResult(okResult);
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

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("signInWithEmailAndPassword".equals(action)) {

            String email = data.getString(0);
            String password = data.getString(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Signing in with email");

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
                                    callbackContext.sendPluginResult(okResult);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    callbackContext.error(task.getException().getMessage());
                                }
                            }
                        });
                }
            });

            PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
            noResult.setKeepCallback(true);
            callbackContext.sendPluginResult(noResult);

            return true;

        } else if ("addAuthStateListener".equals(action)) {

            this.authListener.setCallbackContext(callbackContext);
            PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
            callbackContext.sendPluginResult(okResult);
            return true;

        } else if ("removeAuthStateListener".equals(action)) {

            this.authListener.setCallbackContext(null);
            PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
            callbackContext.sendPluginResult(okResult);
            return true;

        } else if ("signOut".equals(action)) {

            this.auth.signOut();
            PluginResult okResult = new PluginResult(PluginResult.Status.OK, "");
            callbackContext.sendPluginResult(okResult);
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
