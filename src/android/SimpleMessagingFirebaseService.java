package org.apache.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

import android.util.Log;

public class SimpleMessagingFirebaseService  extends FirebaseMessagingService {
    private static final String TAG = "Simple Messaging";

    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        // sendRegistrationToServer(token);
    }
}