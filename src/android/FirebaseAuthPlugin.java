package com.alexnu.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
    public boolean execute(final String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("signInWithEmailAndPassword".equals(action)) {

            final String email = data.getString(0);
            final String password = data.getString(1);

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

            final String email = data.getString(0);
            final String password = data.getString(1);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Creating account");

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(cordova.getActivity(),
                            new AuthCompleteListener(callbackContext, action));
                }
            });

            return true;

        } else if ("getTokenId".equals(action)) {

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.d(TAG, "Getting token id");
                    FirebaseUser user = auth.getCurrentUser();

                    if (user == null) {
                        callbackContext.error("User is not authorized");
                    } else {
                        auth.getCurrentUser().getIdToken(false)
                            .addOnCompleteListener(new AuthTokenListener(callbackContext, action));
                    }
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
