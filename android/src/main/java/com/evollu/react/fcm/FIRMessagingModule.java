package com.evollu.react.fcm;

import android.app.Activity;
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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class FIRMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {
    private final static String TAG = FIRMessagingModule.class.getCanonicalName();
    private FIRLocalMessagingHelper mFIRLocalMessagingHelper;
    
    public FIRMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mFIRLocalMessagingHelper = new FIRLocalMessagingHelper((Application) reactContext.getApplicationContext());
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
        registerTokenRefreshHandler();
        registerMessageHandler();
    }

    @Override
    public String getName() {
        return "RNFIRMessaging";
    }

    @ReactMethod
    public void getInitialNotification(Promise promise){
        promise.resolve(parseIntent(getCurrentActivity().getIntent()));
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
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mFIRLocalMessagingHelper.sendNotification(bundle);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mFIRLocalMessagingHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void cancelLocalNotification(String notificationID) {
      mFIRLocalMessagingHelper.cancelLocalNotification(notificationID);
    }
    @ReactMethod
    public void cancelAllLocalNotifications() {
      mFIRLocalMessagingHelper.cancelAllLocalNotifications();
    }

    @ReactMethod
    public void subscribeToTopic(String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    @ReactMethod
    public void unsubscribeFromTopic(String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    @ReactMethod
    public void getScheduledLocalNotifications(Promise promise){
        ArrayList<Bundle> bundles = mFIRLocalMessagingHelper.getScheduledLocalNotifications();
        WritableArray array = Arguments.createArray();
        for(Bundle bundle:bundles){
            array.pushMap(Arguments.fromBundle(bundle));
        }
        promise.resolve(array);
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

        params.putInt("opened_from_tray", 1);
        return params;
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

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent){
        Bundle bundle = intent.getExtras();
        Boolean isLocalNotification = bundle.getBoolean("localNotification", false);
        sendEvent(isLocalNotification ? "FCMLocalNotificationReceived" : "FCMNotificationReceived", parseIntent(intent));
    }
}
