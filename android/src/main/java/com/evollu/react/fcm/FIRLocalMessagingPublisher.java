package com.evollu.react.fcm;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FIRLocalMessagingPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new FIRLocalMessagingHelper((Application) context.getApplicationContext()).sendNotification(intent.getExtras());
    }
}