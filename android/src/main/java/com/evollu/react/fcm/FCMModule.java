package com.evollu.react.fcm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.json.*;

import android.preference.PreferenceManager;
import android.util.Log;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;

public class FCMModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final static String TAG = FCMModule.class.getCanonicalName();

    public FCMModule(ReactApplicationContext reactContext, Intent intent) {
        super(reactContext);

        if (getReactApplicationContext().hasCurrentActivity()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(reactContext);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("GcmMainActivity", getCurrentActivity().getClass().getSimpleName());
            editor.apply();
        }

        getReactApplicationContext().addLifecycleEventListener(this);

        registerNotificationHandler();
        registerTokenRefreshHandler();
    }

    @Override
    public String getName() {
        return "FCMModule";
    }

    @ReactMethod
    public void requestPermissions(){
    }

    @ReactMethod
    public void getFCMToken(Promise promise) {
        Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
        promise.resolve(FirebaseInstanceId.getInstance().getToken());
    }

    private void sendEvent(String eventName, Object params) {
    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }

    private void registerTokenRefreshHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.FCMRefreshToken");
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    String token = intent.getStringExtra("token");

                    WritableMap params = Arguments.createMap();
                    params.putString("token", token);

                    sendEvent("FCMTokenRefreshed", params);
                    abortBroadcast();
                }
            }
        }, intentFilter);
    }

    private void registerNotificationHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.fcm.ReceiveNotification");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    WritableMap params = Arguments.createMap();

                    if(intent.getStringExtra("data") != null){
                        params.putString("data", intent.getStringExtra("data"));
                    }
                    if(intent.getStringExtra("notification") != null){
                        params.putString("notification", intent.getStringExtra("notification"));
                    }

                    sendEvent("FCMNotificationReceived", params);
                    abortBroadcast();
                }
            }
        }, intentFilter);
    }

//    @Override
//    public Map<String, Object> getConstants() {
//        final Map<String, Object> constants = new HashMap<>();
//        if (mIntent != null) {
//            Bundle bundle = mIntent.getBundleExtra("bundle");
//            String bundleString = convertJSON(bundle);
//            Log.d(TAG, "bundleString: " + bundleString);
//            constants.put("initialNotification", bundleString);
//        }
//        return constants;
//    }

//    private void sendEvent(String eventName, Object params) {
//        getReactApplicationContext()
//            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//            .emit(eventName, params);
//    }

//    private void listenGcmRegistration() {
//        IntentFilter intentFilter = new IntentFilter("RNGcmRegistrationServiceResult");
//
//        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Bundle bundle = intent.getExtras();
//                boolean success = bundle.getBoolean("success");
//                if (success) {
//                    String token = bundle.getString("token");
//                    WritableMap params = Arguments.createMap();
//                    params.putString("registrationToken", token);
//                    registrationToken = token;
//
//                    sendEvent("GCMRemoteNotificationRegistered", params);
//                } else {
//                    String message = bundle.getString("message");
//                    WritableMap params = Arguments.createMap();
//                    params.putString("error", message);
//
//                    sendEvent("GCMRemoteNotificationRegistered", params);
//
//                }
//            }
//        }, intentFilter);
//    }

//    private String convertJSON(Bundle bundle) {
//        JSONObject json = new JSONObject();
//        Set<String> keys = bundle.keySet();
//        for (String key : keys) {
//            try {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    json.put(key, JSONObject.wrap(bundle.get(key)));
//                } else {
//                    json.put(key, bundle.get(key));
//                }
//            } catch(JSONException e) {
//                return null;
//            }
//        }
//        return json.toString();
//    }

//    private void listenGcmReceiveNotification() {
//        IntentFilter intentFilter = new IntentFilter("com.oney.gcm.GCMReceiveNotification");
//
//        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, "GCMReceiveNotification BroadcastReceiver");
//
//                if (getReactApplicationContext().hasActiveCatalystInstance()) {
//                    Bundle bundle = intent.getBundleExtra("bundle");
//
//                    String bundleString = convertJSON(bundle);
//
//                    WritableMap params = Arguments.createMap();
//                    params.putString("dataJSON", bundleString);
//                    params.putBoolean("isInForeground", mIsInForeground);
//
//                    sendEvent("GCMRemoteNotificationReceived", params);
//                    abortBroadcast();
//                } else {
//                }
//            }
//        }, intentFilter);
//    }

//    @ReactMethod
//    public void stopService() {
//        if (mIntent != null) {
//            new android.os.Handler().postDelayed(new Runnable() {
//                public void run() {
//                    getReactApplicationContext().stopService(mIntent);
//                }
//            }, 1000);
//        }
//    }
//    private Class getMainActivityClass() {
//        try {
//            String packageName = getReactApplicationContext().getPackageName();
//
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getReactApplicationContext());
//            String activityString = preferences.getString("GcmMainActivity", null);
//            if (activityString == null) {
//                Log.d(TAG, "GcmMainActivity is null");
//                return null;
//            } else {
//                return Class.forName(packageName + "." + activityString);
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }
}
