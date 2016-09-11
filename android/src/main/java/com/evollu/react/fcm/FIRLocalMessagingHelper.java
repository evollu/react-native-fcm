//Credits to react-native-push-notification

package com.evollu.react.fcm;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.SharedPreferences;


public class FIRLocalMessagingHelper {
    private static final long DEFAULT_VIBRATION = 300L;
    private static final String TAG = FIRLocalMessagingHelper.class.getSimpleName();
    private final static String PREFERENCES_KEY = "ReactNativeSystemNotification";

    private Context mContext;
    private SharedPreferences sharedPreferences = null;

    public FIRLocalMessagingHelper(Application context) {
        mContext = context;
        sharedPreferences = (SharedPreferences) mContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public Class getMainActivityClass() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getScheduleNotificationIntent(Bundle bundle, Boolean storeIntent) {

        int notificationID = (int) System.currentTimeMillis();

        if (bundle.containsKey("id")) {
            notificationID = (int) bundle.getDouble("id");
            Log.w(TAG, String.valueOf(notificationID));

            if(notificationID == 0){
                String notificationIDString = bundle.getString("id");
                if (notificationIDString != null) {
                    Log.w(TAG, "'id' field set as a string instead of an int");

                    try {
                        notificationID = Integer.parseInt(notificationIDString);
                    } catch (NumberFormatException nfe) {
                        Log.w(TAG, "'id' field could not be converted to an int, ignoring it", nfe);
                    }
                }
            }
        }

        Intent notificationIntent = new Intent(mContext, FIRLocalMessagingPublisher.class);
        notificationIntent.putExtra(FIRLocalMessagingPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        if(storeIntent){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Integer.toString(notificationID), notificationIntent.getExtras().toString());
            editor.commit();
        }
        return PendingIntent.getBroadcast(mContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void sendNotification(Bundle bundle) {
        try {
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                return;
            }

            if (bundle.getString("message") == null) {
                return;
            }

            Resources res = mContext.getResources();
            String packageName = mContext.getPackageName();

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = mContext.getApplicationInfo();
                title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                    .setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true));


            String priority = bundle.getString("priority");
            switch(priority==null?"":priority) {
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
                    notification.setPriority(NotificationCompat.PRIORITY_LOW);
                break;
            }

            String group = bundle.getString("group");
            if (group != null) {
                notification.setGroup(group);
            }

            notification.setContentText(bundle.getString("message"));

            String largeIcon = bundle.getString("largeIcon");

            String subText = bundle.getString("subText");

            if (subText != null) {
                notification.setSubText(subText);
            }

            if (bundle.containsKey("number")) {
                try {
                    int number = (int) bundle.getDouble("number");
                    notification.setNumber(number);
                } catch (Exception e) {
                    String numberAsString = bundle.getString("number");
                    if(numberAsString != null) {
                        int number = Integer.parseInt(numberAsString);
                        notification.setNumber(number);
                        Log.w(TAG, "'number' field set as a string instead of an int");
                    }
                }
            }

            int smallIconResId;
            int largeIconResId;

            String smallIcon = bundle.getString("smallIcon");

            if (smallIcon != null) {
                smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
            } else {
                smallIconResId = res.getIdentifier("ic_notification", "mipmap", packageName);
            }

            if (smallIconResId == 0) {
                smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

                if (smallIconResId == 0) {
                    smallIconResId = android.R.drawable.ic_dialog_info;
                }
            }

            if (largeIcon != null) {
                largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
            } else {
                largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
            }

            Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

            if (largeIconResId != 0 && (largeIcon != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)) {
                notification.setLargeIcon(largeIconBitmap);
            }

            notification.setSmallIcon(smallIconResId);
            String bigText = bundle.getString("bigText");

            if (bigText == null) {
                bigText = bundle.getString("message");
            }

            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));

            Intent intent = new Intent(mContext, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notification.setSound(defaultSoundUri);
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                }
            }

            int notificationID = (int) System.currentTimeMillis();

            if (bundle.containsKey("id")) {
                notificationID = (int) bundle.getDouble("id");

                if(notificationID == 0){
                    String notificationIDString = bundle.getString("id");
                    if (notificationIDString != null) {
                        Log.w(TAG, "'id' field set as a string instead of an int");

                        try {
                            notificationID = Integer.parseInt(notificationIDString);
                        } catch (NumberFormatException nfe) {
                            Log.w(TAG, "'id' field could not be converted to an int, ignoring it", nfe);
                        }
                    }
                }
            }

            intent.putExtra("localNotification", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notification.setContentIntent(pendingIntent);

            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
                if (vibration == 0)
                    vibration = DEFAULT_VIBRATION;
                notification.setVibrate(new long[]{0, vibration});
            }

            Notification info = notification.build();
            info.defaults |= Notification.DEFAULT_LIGHTS;

            if (bundle.containsKey("tag")) {
                String tag = bundle.getString("tag");
                notificationManager.notify(tag, notificationID, info);
            } else {
                notificationManager.notify(notificationID, info);
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to send push notification", e);
        }
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        Double fireDateDouble = bundle.getDouble("fireDate", 0);
        if (fireDateDouble == 0) {
            return;
        }

        long fireDate = Math.round(fireDateDouble);
        long currentTime = System.currentTimeMillis();
        String repeatEvery = bundle.getString("repeatEvery");
        Log.i("ReactSystemNotification", "repeat set: " + repeatEvery);
        Log.i("ReactSystemNotification", "fireDate: " + fireDate + ", Now Time: " + currentTime);
        PendingIntent pendingIntent = getScheduleNotificationIntent(bundle, true);

        Long interval = null;
        switch (repeatEvery) {
          case "minute":
              interval = new Long(60000);
              break;

          case "hour":
              interval = AlarmManager.INTERVAL_HOUR;
              break;

          case "halfDay":
              interval = AlarmManager.INTERVAL_HALF_DAY;
              break;

          case "day":
              interval = AlarmManager.INTERVAL_DAY;
              break;
          case "week":
              interval = AlarmManager.INTERVAL_DAY * 7;
              break;
        }

        if(interval != null){
            getAlarmManager().setRepeating(AlarmManager.RTC_WAKEUP, fireDate, interval, pendingIntent);
            Log.i("ReactSystemNotification", "Set Repeat Alarm , Type: " + repeatEvery);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
            Log.i("ReactSystemNotification", "Set One-Time Alarm");
        }else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
            Log.i("ReactSystemNotification", "Set One-Time Alarm: ");
        }

    }

    public void cancelLocalNotification(double notificationID) {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel((int) notificationID);

        cancelAlarm(notificationID);
        deleteFromPreferences(Double.toString(notificationID));
    }

    public void deleteFromPreferences(String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(id);
        editor.commit();
    }

    public void cancelLocalNotifications() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        cancelAlarms();
    }

    public void cancelAlarm(double notificationID) {
          Bundle b = new Bundle();
          b.putDouble("id", notificationID);
          getAlarmManager().cancel(getScheduleNotificationIntent( b, false));
    }

    public void cancelAlarms() {
      java.util.Map<String, ?> keyMap = sharedPreferences.getAll();
      SharedPreferences.Editor editor = sharedPreferences.edit();
      for(java.util.Map.Entry<String, ?> entry:keyMap.entrySet()){
          cancelAlarm(Double.parseDouble(entry.getKey()));
      }
      editor.clear();
      editor.commit();
    }
}