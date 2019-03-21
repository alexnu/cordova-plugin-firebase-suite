package com.alexnu.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.firebase.auth.FirebaseAuth;


public class FirebaseAuthPlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseAuth";

    private FirebaseAuth auth;
        private AuthStatusListener authStatusListener;


    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting FirebaseAuth plugin");

        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("signInWithEmailAndPassword".equals(action)) {

            String email = data.getString(0);
            String password = data.getString(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Signing in with email");

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(cordova.getActivity(),
                            new AuthCompleteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("createUserWithEmailAndPassword".equals(action)) {

            String email = data.getString(0);
            String password = data.getString(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Creating account");

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(cordova.getActivity(),
                            new AuthCompleteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("addAuthStateListener".equals(action)) {

            if (this.authStatusListener != null) {
                this.auth.removeAuthStateListener(this.authStatusListener);
                this.authStatusListener = null;
            }

            this.authStatusListener = new AuthStatusListener(callbackContext);
            this.auth.addAuthStateListener(this.authStatusListener);

            return true;

        } else if ("removeAuthStateListener".equals(action)) {

            if (this.authStatusListener != null) {
                this.auth.removeAuthStateListener(this.authStatusListener);
                this.authStatusListener = null;
            }

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

}
