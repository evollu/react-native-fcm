package com.evollu.react.fcm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import static com.facebook.react.common.ReactConstants.TAG;

public class SendNotificationTask extends AsyncTask<Void, Void, Void> {
    private static final long DEFAULT_VIBRATION = 300L;

    private Context mContext;
    private Bundle bundle;
    private SharedPreferences sharedPreferences;
    private Boolean mIsForeground;

    SendNotificationTask(Context context, SharedPreferences sharedPreferences, Boolean mIsForeground, Bundle bundle){
        this.mContext = context;
        this.bundle = bundle;
        this.sharedPreferences = sharedPreferences;
        this.mIsForeground = mIsForeground;
    }

    protected Void doInBackground(Void... params) {
        try {
            String intentClassName = getMainActivityClassName();
            if (intentClassName == null) {
                return null;
            }

            String body = bundle.getString("body");
            if (body == null) {
                return null;
            }
            body = URLDecoder.decode( body, "UTF-8" );

            Resources res = mContext.getResources();
            String packageName = mContext.getPackageName();

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = mContext.getApplicationInfo();
                title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
            }
            title = URLDecoder.decode( title, "UTF-8" );

            String ticker = bundle.getString("ticker");
            if (ticker != null) ticker = URLDecoder.decode( ticker, "UTF-8" );

            String subText = bundle.getString("sub_text");
            if (subText != null) subText = URLDecoder.decode( subText, "UTF-8" );

            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, bundle.getString("channel"))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setTicker(ticker)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setAutoCancel(bundle.getBoolean("auto_cancel", true))
                    .setNumber(bundle.getInt("number", (int)bundle.getDouble("number")))
                    .setSubText(subText)
                    .setVibrate(new long[]{0, DEFAULT_VIBRATION})
                    .setExtras(bundle.getBundle("data"));

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                String group = bundle.getString("group");
                if (group != null) group = URLDecoder.decode( group, "UTF-8" );
                notification.setGroup(group);
                if (bundle.containsKey("groupSummary") && bundle.getBoolean("groupSummary")) {
                    notification.setGroupSummary(true);
                }
            }

            if (bundle.containsKey("ongoing") && bundle.getBoolean("ongoing")) {
                notification.setOngoing(bundle.getBoolean("ongoing"));
            }

            //priority
            String priority = bundle.getString("priority", "");
            switch(priority) {
                case "min":
                    notification.setPriority(NotificationCompat.PRIORITY_MIN);
                    break;
                case "high":
                    notification.setPriority(NotificationCompat.PRIORITY_HIGH);
                    break;
                case "max":
                    notification.setPriority(NotificationCompat.PRIORITY_MAX);
                    break;
                default:
                    notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            }

            //icon
            String smallIcon = bundle.getString("icon", "ic_launcher");
            int smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
            if(smallIconResId == 0){
                smallIconResId = res.getIdentifier(smallIcon, "drawable", packageName);
            }
            if(smallIconResId != 0){
                notification.setSmallIcon(smallIconResId);
            }

            //large icon
            String largeIcon = bundle.getString("large_icon");
            if(largeIcon != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                if (largeIcon.startsWith("http://") || largeIcon.startsWith("https://")) {
                    Bitmap bitmap = getBitmapFromURL(largeIcon);
                    notification.setLargeIcon(bitmap);
                } else {
                    int largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
                    Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

                    if (largeIconResId != 0) {
                        notification.setLargeIcon(largeIconBitmap);
                    }
                }
            }

            //big text
            String bigText = bundle.getString("big_text");
            if(bigText != null){
                bigText = URLDecoder.decode( bigText, "UTF-8" );
                notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
            }

            //picture
            String picture = bundle.getString("picture");

            if(picture!=null){
                NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();

                if (picture.startsWith("http://") || picture.startsWith("https://")) {
                    Bitmap bitmap = getBitmapFromURL(picture);
                    bigPicture.bigPicture(bitmap);
                } else {
                    int pictureResId = res.getIdentifier(picture, "mipmap", packageName);
                    Bitmap pictureResIdBitmap = BitmapFactory.decodeResource(res, pictureResId);

                    if (pictureResId != 0) {
                        bigPicture.bigPicture(pictureResIdBitmap);
                    }
                }
                // setBigContentTitle and setSummaryText overrides current title with body and subtext
		// that cause to display duplicated body in subtext when picture has specified
                notification.setStyle(bigPicture);
            }

            //sound
            String soundName = bundle.getString("sound");
            if (soundName != null) {
                if (soundName.equalsIgnoreCase("default")) {
                    notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                } else {
                    int soundResourceId = res.getIdentifier(soundName, "raw", packageName);
                    if (soundResourceId == 0) {
                        soundName = soundName.substring(0, soundName.lastIndexOf('.'));
                        soundResourceId = res.getIdentifier(soundName, "raw", packageName);
                    }
                    notification.setSound(Uri.parse("android.resource://" + packageName + "/" + soundResourceId));
                }
            }

            //color
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                }
            }

            //vibrate
            if(bundle.containsKey("vibrate")){
                long vibrate = Math.round(bundle.getDouble("vibrate", DEFAULT_VIBRATION));
                if(vibrate > 0){
                    notification.setVibrate(new long[]{0, vibrate});
                }else{
                    notification.setVibrate(null);
                }
            }

            //lights
            if (bundle.getBoolean("lights")) {
                notification.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
            }

            if(bundle.containsKey("fire_date")) {
                Log.d(TAG, "broadcast intent if it is a scheduled notification");
                Intent i = new Intent("com.evollu.react.fcm.ReceiveLocalNotification");
                i.putExtras(bundle);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
            }

            if(!mIsForeground || bundle.getBoolean("show_in_foreground")){
                Intent intent = new Intent();
                intent.setClassName(mContext, intentClassName);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtras(bundle);

                String clickAction = bundle.getString("click_action");
                if (clickAction != null) clickAction = URLDecoder.decode( clickAction, "UTF-8" );

                intent.setAction(clickAction);

                int notificationID = bundle.containsKey("id") ? bundle.getString("id", "").hashCode() : (int) System.currentTimeMillis();
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                notification.setContentIntent(pendingIntent);

                if (bundle.containsKey("android_actions")) {
                    String androidActions = bundle.getString("android_actions");
                    androidActions = URLDecoder.decode( androidActions, "UTF-8" );

                    WritableArray actions = ReactNativeJson.convertJsonToArray(new JSONArray(androidActions));
                    for (int a = 0; a < actions.size(); a++) {
                        ReadableMap action = actions.getMap(a);
                        String actionTitle = action.getString("title");
                        String actionId = action.getString("id");
                        Intent actionIntent = new Intent();
                        actionIntent.setClassName(mContext, intentClassName);
                        actionIntent.setAction("com.evollu.react.fcm." + actionId + "_ACTION");
                        actionIntent.putExtras(bundle);
                        actionIntent.putExtra("_actionIdentifier", actionId);
                        actionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingActionIntent = PendingIntent.getActivity(mContext, notificationID, actionIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        notification.addAction(0, actionTitle, pendingActionIntent);
                    }
                }

                Notification info = notification.build();

                NotificationManagerCompat.from(mContext).notify(notificationID, info);
            }

            if(bundle.getBoolean("wake_screen", false)){
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                if(pm != null && !pm.isScreenOn())
                {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"FCMLock");
                    wl.acquire(5000);
                }
            }

            //clear out one time scheduled notification once fired
            if(!bundle.containsKey("repeat_interval") && bundle.containsKey("fire_date")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(bundle.getString("id"));
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to send local notification", e);
        }
        return null;
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getMainActivityClassName() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        return launchIntent != null ? launchIntent.getComponent().getClassName() : null;
    }
}
