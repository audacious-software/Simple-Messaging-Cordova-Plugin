package org.apache.cordova.plugin;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import android.app.Activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.FirebaseApp;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SimpleMessagingUtils extends CordovaPlugin {
    private CordovaInterface mCordovaInterface = null;

    private ActivityResultLauncher<String> mRequestPermissionLauncher = null;

    private CallbackContext mLatestCallbackContext;

    private static final String TAG = "SIMPLE-MESSAGING";

    private CallbackContext mCallbackContext = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.mCordovaInterface = cordova;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        final SimpleMessagingUtils me = this;

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                if (me.mCallbackContext != null) {
                    me.mCallbackContext.success(0);
                }

                return;
            }
        }

        if (me.mCallbackContext != null) {
            me.mCallbackContext.success(1);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.e(TAG, "SimpleMessagingUtils.execute: " + action + " -- " + args);

        final Activity activity = this.mCordovaInterface.getActivity();
        final SimpleMessagingUtils me = this;

        if ("fetchPermissions".equals(action)) {
            this.mCallbackContext = callbackContext;

            cordova.requestPermission(this, 9999, Manifest.permission.POST_NOTIFICATIONS);

            return true;
        } else if ("fetchDeviceToken".equals(action)) {
            FirebaseApp.initializeApp(activity);

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

                  Log.e(TAG, "TOKEN: " + token);

                  callbackContext.success("fcm:" + token);
                }
            });

            return true;
        } else if ("fetchDeviceTokenAndTransmit".equals(action)) {
            FirebaseApp.initializeApp(activity);

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

                  try {
                    String endpointUrl = args.getString(0);
                    String username = args.getString(1);

                    Log.e(TAG, "TOKEN: " + token);

                    OkHttpClient client = new OkHttpClient();

                    RequestBody formBody = new FormBody.Builder()
                        .add("identifier", username)
                        .add("platform", "android")
                        .add("token", token)
                        .build();

                    Request request = new Request.Builder()
                        .url(endpointUrl)
                        .post(formBody)
                        .build();

                    client.newCall(request).enqueue(new Callback() {
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();

                            callbackContext.error("Unable to register token with server.");
                        }

                        public void onResponse(Call call, Response response) throws IOException {
                            Log.e(TAG, "response: " + response);

                            if (!response.isSuccessful()) {
                                Log.e(TAG, "Unexpected code " + response.code());
                                Log.e(TAG, "BODY " + response.body().string());

                                callbackContext.error("Unexpected code " + response);

                                return;
                            }

                            try {
                                ResponseBody responseBody = response.body();

                                String responseString = responseBody.string();

                                Log.e(TAG, responseString);

                                try {
                                    JSONObject responseJson = new JSONObject(responseString);

                                    if (responseJson.getBoolean("success")) {
                                        callbackContext.success("Token successfully registered with the server.");

                                        return;
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }

                                callbackContext.error("Unexpected result: " + responseString);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            return true;
        }


        return false;
    }
}