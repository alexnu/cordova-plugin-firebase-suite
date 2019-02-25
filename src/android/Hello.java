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

        if ("great".equals(action)) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else if ("once".equals(action)) {

            String ref = data.getString(0);
            Log.d(TAG, "Reading from ref: " + ref);
            this.database.getReference(ref).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String data = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "Got value from DB: " + data);
                    callbackContext.success(data);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.d(TAG, "Error from DB");
                    callbackContext.error(error.getCode());
                }
            });

        } else {

            return false;

        }
    }
}
