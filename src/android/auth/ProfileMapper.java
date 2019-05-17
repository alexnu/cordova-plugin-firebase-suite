import org.apache.cordova.PluginResult;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AdditionalUserInfo;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileMapper {

    public static PluginResult getProfileResult(FirebaseUser user, AdditionalUserInfo info) {
            if (user == null) {
                Log.d(TAG, "User is not logged in");
                return new PluginResult(PluginResult.Status.OK, (String) null);
            }

            Log.d(TAG, "User is logged in");
            JSONObject result = new JSONObject();

            try {
                result.put("uid", user.getUid());
                result.put("displayName", user.getDisplayName());
                result.put("email", user.getEmail());
                result.put("phoneNumber", user.getPhoneNumber());
                result.put("photoURL", user.getPhotoUrl());
                result.put("providerId", user.getProviderId());
                result.put("emailVerified", user.isEmailVerified());
                result.put("newUser", info != null && info.isNewUser() ? true : false);

                return new PluginResult(PluginResult.Status.OK, result);
            } catch (JSONException e) {
                Log.e(TAG, "Fail to process getProfileData", e);

                return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            }
        }
}
