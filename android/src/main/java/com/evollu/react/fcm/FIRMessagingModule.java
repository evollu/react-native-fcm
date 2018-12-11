package com.evollu.react.fcm;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.content.ContentResolver;
import android.media.AudioAttributes;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;

import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.firebase.FirebaseApp;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FIRMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {
    private final static String TAG = FIRMessagingModule.class.getCanonicalName();
    private FIRLocalMessagingHelper mFIRLocalMessagingHelper;
    private BadgeHelper mBadgeHelper;

    public FIRMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mFIRLocalMessagingHelper = new FIRLocalMessagingHelper((Application) reactContext.getApplicationContext());
        mBadgeHelper = new BadgeHelper(reactContext.getApplicationContext());
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
        registerTokenRefreshHandler();
        registerMessageHandler();
        registerLocalMessageHandler();
    }

    @Override
    public String getName() {
        return "RNFIRMessaging";
    }

    @ReactMethod
    public void getInitialNotification(Promise promise){
        Activity activity = getCurrentActivity();
        if(activity == null){
            promise.resolve(null);
            return;
        }
        promise.resolve(parseIntent(activity.getIntent()));
    }

    @ReactMethod
    public void requestPermissions(Promise promise){
        if(NotificationManagerCompat.from(getReactApplicationContext()).areNotificationsEnabled()){
            promise.resolve(true);
        } else {
            promise.reject(null, "Notification disabled");
        }
    }

    @ReactMethod
    public void createNotificationChannel(ReadableMap details, Promise promise){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mngr = (NotificationManager) getReactApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            String id = details.getString("id");
            String name = details.getString("name");
            int importance;
            if (details.hasKey("priority")) {
                String priority = details.getString("priority");
                switch (priority) {
                    case "min":
                        importance = NotificationManager.IMPORTANCE_MIN;
                        break;
                    case "low":
                        importance = NotificationManager.IMPORTANCE_LOW;
                        break;
                    case "high":
                        importance = NotificationManager.IMPORTANCE_HIGH;
                        break;
                    case "max":
                        importance = NotificationManager.IMPORTANCE_MAX;
                        break;
                    default:
                        importance = NotificationManager.IMPORTANCE_DEFAULT;
                }
            }
            else {
                importance = NotificationManager.IMPORTANCE_DEFAULT;
            }
            if (mngr.getNotificationChannel(id) != null) {
                promise.resolve(null);
                return;
            }
            
            NotificationChannel channel = new NotificationChannel(
                    id,
                    name,
                    importance);
            // Configure the notification channel.
            if(details.hasKey("description")){
                channel.setDescription(details.getString("description"));
            }
            if (details.hasKey("sound")) {
                String sound = details.getString("sound");
                AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

                Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://"
                    + getReactApplicationContext().getPackageName()
                    + "/raw/"
                    + sound
                );
                channel.setSound(soundUri, attributes);
            }
  
            mngr.createNotificationChannel(channel);
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void deleteNotificationChannel(String id, Promise promise) {
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		    NotificationManager mngr = (NotificationManager) getReactApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		    mngr.deleteNotificationChannel(id);
	    }
	    promise.resolve(null);
    }

    @ReactMethod
    public void getFCMToken(Promise promise) {
        try {
            Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
            promise.resolve(FirebaseInstanceId.getInstance().getToken());
        } catch (Throwable e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
    }

    @ReactMethod
    public void getEntityFCMToken(Promise promise) {
        try {
            String senderId = FirebaseApp.getInstance().getOptions().getGcmSenderId();
            String token = FirebaseInstanceId.getInstance().getToken(senderId, "FCM");
            Log.d(TAG, "Firebase token: " + token);
            promise.resolve(token);
        } catch (Throwable e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
    }

    @ReactMethod
    public void deleteEntityFCMToken(Promise promise) {
        try {
            String senderId = FirebaseApp.getInstance().getOptions().getGcmSenderId();
            FirebaseInstanceId.getInstance().deleteToken(senderId, "FCM");
            promise.resolve(null);
        } catch (Throwable e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
    }

    @ReactMethod
    public void deleteInstanceId(Promise promise){
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            promise.resolve(null);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
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
    public void removeDeliveredNotification(String notificationID) {
        mFIRLocalMessagingHelper.removeDeliveredNotification(notificationID);
    }

    @ReactMethod
    public void removeAllDeliveredNotifications(){
        mFIRLocalMessagingHelper.removeAllDeliveredNotifications();
    }

    @ReactMethod
    public void subscribeToTopic(String topic, Promise promise){
	    try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
            promise.resolve(null);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
    }

    @ReactMethod
    public void unsubscribeFromTopic(String topic, Promise promise){
	    try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            promise.resolve(null);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(null,e.getMessage());
        }
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

    @ReactMethod
    public void setBadgeNumber(int badgeNumber) {
        mBadgeHelper.setBadgeCount(badgeNumber);
    }

    @ReactMethod
    public void getBadgeNumber(Promise promise) {
        promise.resolve(mBadgeHelper.getBadgeCount());
    }

    private void sendEvent(String eventName, Object params) {
        getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }

    private void registerTokenRefreshHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.FCMRefreshToken");
        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    String token = intent.getStringExtra("token");
                    sendEvent("FCMTokenRefreshed", token);
                }
            }
        }, intentFilter);
    }

    @ReactMethod
    public void send(String senderId, ReadableMap payload) throws Exception {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        RemoteMessage.Builder message = new RemoteMessage.Builder(senderId + "@gcm.googleapis.com")
        .setMessageId(UUID.randomUUID().toString());

        ReadableMapKeySetIterator iterator = payload.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            String value = getStringFromReadableMap(payload, key);
            message.addData(key, value);
        }
        fm.send(message.build());
    }

    private String getStringFromReadableMap(ReadableMap map, String key) throws Exception {
        switch (map.getType(key)) {
            case String:
                return map.getString(key);
            case Number:
                try {
                    return String.valueOf(map.getInt(key));
                } catch (Exception e) {
                    return String.valueOf(map.getDouble(key));
                }
            case Boolean:
                return String.valueOf(map.getBoolean(key));
            default:
                throw new Exception("Unknown data type: " + map.getType(key).name() + " for message key " + key );
        }
    }

    private void registerMessageHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.ReceiveNotification");

        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    RemoteMessage message = intent.getParcelableExtra("data");
                    WritableMap params = Arguments.createMap();
                    WritableMap fcmData = Arguments.createMap();

                    if (message.getNotification() != null) {
                        Notification notification = message.getNotification();
                        fcmData.putString("title", notification.getTitle());
                        fcmData.putString("body", notification.getBody());
                        fcmData.putString("color", notification.getColor());
                        fcmData.putString("icon", notification.getIcon());
                        fcmData.putString("tag", notification.getTag());
                        fcmData.putString("action", notification.getClickAction());
                    }
                    params.putMap("fcm", fcmData);
                    params.putString("collapse_key", message.getCollapseKey());
                    params.putString("from", message.getFrom());
                    params.putString("google.message_id", message.getMessageId());
                    params.putDouble("google.sent_time", message.getSentTime());

                    if(message.getData() != null){
                        Map<String, String> data = message.getData();
                        Set<String> keysIterator = data.keySet();
                        for(String key: keysIterator){
                            params.putString(key, data.get(key));
                        }
                    }
                    sendEvent("FCMNotificationReceived", params);

                }
            }
        }, intentFilter);
    }

    private void registerLocalMessageHandler() {
        IntentFilter intentFilter = new IntentFilter("com.evollu.react.fcm.ReceiveLocalNotification");

        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    sendEvent("FCMNotificationReceived", Arguments.fromBundle(intent.getExtras()));
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
        mFIRLocalMessagingHelper.setApplicationForeground(true);
    }

    @Override
    public void onHostPause() {
        mFIRLocalMessagingHelper.setApplicationForeground(false);
    }

    @Override
    public void onHostDestroy() {

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent){
        sendEvent("FCMNotificationReceived", parseIntent(intent));
    }
}
