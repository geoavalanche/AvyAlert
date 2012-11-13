package com.platypii.avyalert;

import com.platypii.avyalert.AvalancheRisk.Rating;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;


public class Alerter {

    private static final int ADVISORY_NOTI = 0; // Tag for the advisory notification

    /**
     * Notify the user of advisory
     */
    public static void notifyUser(Context context, Advisory advisory) {
        Log.i("Alerter", "Notifying user");

        // Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Control whether we notify or not
        if(advisory.rating == Rating.NONE) {
            Log.i("Alerter", "Rating unknown, skipping notification");
            return;
        } else if(advisory.rating == Rating.LOW && !prefs.getBoolean("enable_low", true)) {
            return;
        } else if(advisory.rating == Rating.MODERATE && !prefs.getBoolean("enable_moderate", true)) {
            return;
        } else if(advisory.rating == Rating.CONSIDERABLE && !prefs.getBoolean("enable_considerable", true)) {
            return;
        } else if(advisory.rating == Rating.HIGH && !prefs.getBoolean("enable_high", true)) {
            return;
        } else if(advisory.rating == Rating.EXTREME && !prefs.getBoolean("enable_extreme", true)) {
            return;
        }
        
        // Intent to open the advisory in AvyAlert
        PendingIntent openAdvisory = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        Builder builder = new NotificationCompat.Builder(context)
        .setContentTitle("Avalanche Risk: " + advisory.rating)
        .setTicker("Avalanche Risk: " + advisory.rating)
        .setContentText(advisory.getDetails())
        .setSmallIcon(R.drawable.ic_launcher)
        .setAutoCancel(true)
        .setDefaults(Notification.FLAG_SHOW_LIGHTS)
        .setLights(AvalancheRisk.getColor(advisory.rating), 300, 1000) // On for 300ms, off for 1000ms
        .setContentIntent(openAdvisory);
        //.setLargeIcon(aBitmap)
        
        if(prefs.getBoolean("enable_vibrate", true)) {
            builder.setVibrate(new long[] {0, 100, 50, 180});
        }

        Notification noti = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(ADVISORY_NOTI, noti);
    }
    
}
