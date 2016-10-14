package com.evollu.react.fcm;

import java.util.Map;
import android.content.Intent;
import android.util.Log;
import me.leolin.shortcutbadger.ShortcutBadger;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");
        Intent i = new Intent("com.evollu.react.fcm.ReceiveNotification");
        i.putExtra("data", remoteMessage);
        handleBadge(remoteMessage);
        sendOrderedBroadcast(i, null);
    }

    public void handleBadge(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() == null) {
            return;
        }

        Map data = remoteMessage.getData();
        if (data.get("badge") == null) {
            return;
        }

        try {
            int badgeCount = Integer.parseInt((String)data.get("badge"));
            if (badgeCount == 0) {
                ShortcutBadger.removeCount(this);
                Log.d(TAG, "Remove count");
            } else {
                ShortcutBadger.applyCount(this, badgeCount);
                Log.d(TAG, "Apply count: " + badgeCount);
            }
        } catch (Exception e) {
            Log.e(TAG, "Badge count needs to be an integer", e);
        }
    }
}
