package com.alexnu.plugin;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;


public class FirebaseGoogleAuthPlugin extends CordovaPlugin {

    private static final String TAG = "FirebaseGoogleAuthPlugin";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackContext callbackContext;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting FirebaseGoogleAuthPlugin plugin");
        this.auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getDefaultClientId())
                        .requestEmail()
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this.cordova.getActivity(), gso);
    }

    private String getDefaultClientId() {
        Context context = cordova.getActivity().getApplicationContext();
        String packageName = context.getPackageName();
        int id = context.getResources().getIdentifier("default_web_client_id", "string", packageName);
        Log.d(TAG, "Client id: " + id + " value: " + context.getString(id));
        return context.getString(id);
    }

    @Override
    public boolean execute(String action, final JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Got new action " + action);

        if ("signIn".equals(action)) {

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            this.callbackContext = callbackContext;
            this.cordova.startActivityForResult(this, signInIntent, RC_SIGN_IN);

            return true;

        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(cordova.getActivity(),
                new AuthSignInListener(this.callbackContext, "signInWithGoogle"));
    }
}
