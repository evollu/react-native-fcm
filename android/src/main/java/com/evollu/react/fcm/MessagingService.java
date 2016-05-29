package com.evollu.react.fcm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;;import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");
        Intent i = new Intent("com.evollu.fcm.ReceiveNotification");
        if(remoteMessage.getData() != null){
            i.putExtra("data", convertJSON(remoteMessage.getData()));
        }
        if(remoteMessage.getNotification() != null){
            i.putExtra("notification", buildNotificationJSON(remoteMessage.getNotification()));
        }
        sendOrderedBroadcast(i, null);
    }

    private String buildNotificationJSON(RemoteMessage.Notification notification){
        JSONObject json = new JSONObject();
        try{
            json.put("title", notification.getTitle());
            json.put("body", notification.getBody());
            json.put("icon", notification.getIcon());
            json.put("sound", notification.getSound());
            json.put("color", notification.getColor());
        } catch( JSONException e){
            return null;
        }
        return json.toString();
    }

    private String convertJSON(Map data) {
        JSONObject json = new JSONObject();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    json.put(key, JSONObject.wrap(data.get(key)));
                } else {
                    json.put(key, data.get(key));
                }
            } catch(JSONException e) {
                return null;
            }
        }
        return json.toString();
    }
}
