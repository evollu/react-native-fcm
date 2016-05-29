package com.evollu.react.fcm;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");
        Intent i = new Intent("com.evollu.fcm.ReceiveNotification");
        if(remoteMessage.getData() != null){
            i.putExtra("data", new JSONObject(remoteMessage.getData()).toString());
            sendOrderedBroadcast(i, null);
        }
    }
}
