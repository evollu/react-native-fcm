package com.evollu.react.fcm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import android.util.Log;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FCMModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final static String TAG = FCMModule.class.getCanonicalName();

    public FCMModule(ReactApplicationContext reactContext, Intent intent) {
        super(reactContext);

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
                    try {
                        JSONObject data = new JSONObject(intent.getStringExtra("data"));
                        Iterator<String> keysIterator = data.keys();
                        while(keysIterator.hasNext()){
                            String key = keysIterator.next();
                            params.putString(key, (String) data.get(key));
                        }
                        sendEvent("FCMNotificationReceived", params);
                    } catch (JSONException e){

                    }
                    abortBroadcast();
                }
            }
        }, intentFilter);
    }

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
