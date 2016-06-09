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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import android.os.Bundle;
import android.util.Log;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FIRMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final static String TAG = FIRMessagingModule.class.getCanonicalName();
    Intent mIntent;

    public FIRMessagingModule(ReactApplicationContext reactContext, Intent intent) {
        super(reactContext);

        mIntent = intent;

        getReactApplicationContext().addLifecycleEventListener(this);
        registerTokenRefreshHandler();
        registerMessageHandler();
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();
        if (mIntent != null) {
            if(mIntent.getExtras() != null) {
                Map<String, Object> extra = new HashMap<>();
                Bundle data = mIntent.getExtras();
                Set<String> keysIterator = data.keySet();
                for(String key: keysIterator){
                    extra.put(key, data.get(key));
                }
                constants.put("initialData", extra);
            }
            constants.put("initialAction", mIntent.getAction());
        }
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
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.ReceiveNotification");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                RemoteMessage message = intent.getParcelableExtra("data");
                WritableMap params = Arguments.createMap();
                if(message.getData() != null){
                    Map data = message.getData();
                    Set<String> keysIterator = data.keySet();
                    for(String key: keysIterator){
                        params.putString(key, (String) data.get(key));
                    }
                    sendEvent("FCMNotificationReceived", params);
                    abortBroadcast();
                }

            }
            }
        }, intentFilter);
    }

    @Override
    public void onHostResume() {
        Intent newIntent = getCurrentActivity().getIntent();
        if(newIntent != mIntent && newIntent != null){
            Bundle extras = newIntent.getExtras();
            if (extras != null) {
                WritableMap params = Arguments.fromBundle(extras);
                WritableMap fcm = Arguments.createMap();
                fcm.putString("action", newIntent.getAction());
                params.putMap("fcm", fcm);
                sendEvent("FCMNotificationReceived", params);
            }
        }
        mIntent = newIntent;
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }
}
