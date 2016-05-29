package com.evollu.react.firebase;

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
import com.google.firebase.messaging.RemoteMessage;

import android.util.Log;

import android.content.Context;

import java.util.Map;
import java.util.Set;

public class FIRMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final static String TAG = FIRMessagingModule.class.getCanonicalName();

    public FIRMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);

        getReactApplicationContext().addLifecycleEventListener(this);

        registerNotificationHandler();
        registerTokenRefreshHandler();
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

    private void sendEvent(String eventName, Object params) {
    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }

    private void registerTokenRefreshHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.firebase.FCMRefreshToken");
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
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.firebase.ReceiveNotification");

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
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }
}
