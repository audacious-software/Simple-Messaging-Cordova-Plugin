package org.apache.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.util.Log;

public class SimpleMessagingUtils extends CordovaPlugin {
    private CordovaInterface mCordovaInterface = null;

    private final ActivityResultLauncher<String> mRequestPermissionLauncher;

    private CallbackContext mLatestCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.mCordovaInterface = cordova;

        final SimpleMessagingUtils me = this;

        final Activity activity = this.mCordovaInterface.getActivity();

        this.mRequestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<boolean>() {
            @Override
            public void onActivityResult(boolean isGranted) {
                if (isGranted) {
                    me.mCordovaInterface.getThreadPool().execute(new Runnable() {
                        public void run() {
                            me.mLatestCallbackContext.success();
                        }
                    });
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("fetchPermissions".equals(action)) {
            this.mLatestCallbackContext = callbackContext;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    this.mLatestCallbackContext.success();

                    return true;
                // } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // TODO: display an educational UI explaining to the user the features that will be enabled
                    //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                    //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                    //       If the user selects "No thanks," allow the user to continue without notifications.
                } else {
                    // Directly ask for the permission

                    this.mCordovaInterface.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            this.mRequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        }
                    });
                }
            }

            return true;
        } else if ("fetchDeviceToken".equals(action)) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                  if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching Firebase Cloud Messaging registration token failed", task.getException());

                    String errorMessage = "Fetching FCM registration token failed: " + task.getException();

                    callbackContext.error(errorMessage);

                    return;
                  }
        
                  String token = task.getResult();

                  callbackContext.success("fcm:" + token);
                }
            });

            return true;
        }
         
        return false;
    }
}