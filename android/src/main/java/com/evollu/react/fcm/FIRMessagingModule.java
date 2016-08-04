package com.evollu.react.fcm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import android.os.Bundle;
import android.util.Log;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FIRMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {
    private final static String TAG = FIRMessagingModule.class.getCanonicalName();
    Intent initIntent;

    public FIRMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
        registerTokenRefreshHandler();
        registerMessageHandler();
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();
        return constants;
    }

    @Override
    public String getName() {
        return "RNFIRMessaging";
    }

    @ReactMethod
    public void requestPermissions(){
    }

    @ReactMethod
    public void getFCMToken(Promise promise) {
        Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
        promise.resolve(FirebaseInstanceId.getInstance().getToken());
    }

    @ReactMethod
    public void subscribeToTopic(String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    @ReactMethod
    public void unsubscribeFromTopic(String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
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

                    sendEvent("FCMTokenRefreshed", token);
                    abortBroadcast();
                }
            }
        }, intentFilter);
    }

    private void registerMessageHandler() {
        Log.d(TAG, "registerMessageHandler");
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.ReceiveNotification");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    RemoteMessage message = intent.getParcelableExtra("data");
                    String title = message.getNotification().getTitle();
                    String body = message.getNotification().getBody();
                    WritableMap params = Arguments.createMap();
                    if (message.getData() != null){
                        Map data = message.getData();
                        WritableMap wrapper = Arguments.createMap();
                        Set<String> keysIterator = data.keySet();
                        for(String key: keysIterator){
                            wrapper.putString(key, (String) data.get(key));
                        }
                        params.putMap("data", wrapper);
                    }
                    if (title != null) {
                        params.putString("title", title);
                    }
                    if (body != null) {
                        params.putString("body", body);
                    }
                    Log.d(TAG, "FCMNotificationReceived: " + String.valueOf(params));
                    sendEvent("FCMNotificationReceived", params);
                    abortBroadcast();
                }
            }
        }, intentFilter);
    }

    private WritableMap parseIntent(Intent intent){
        WritableMap params;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            try {
                params = Arguments.fromBundle(extras);
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
                params = Arguments.createMap();
            }
        } else {
            params = Arguments.createMap();
        }
        WritableMap fcm = Arguments.createMap();
        fcm.putString("action", intent.getAction());
        params.putMap("fcm", fcm);
        return params;
    }

    @Override
    public void onHostResume() {
        if (initIntent == null){
            //the first intent is initial intent that opens the app
            Intent newIntent = getCurrentActivity().getIntent();
            sendEvent("FCMInitData", parseIntent(newIntent));
            initIntent = newIntent;
        }
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent){
        sendEvent("FCMNotificationReceived", parseIntent(intent));
    }
}
