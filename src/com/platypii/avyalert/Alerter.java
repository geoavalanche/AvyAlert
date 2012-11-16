package com.platypii.avyalert;

import java.util.Calendar;
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
        if(advisory == null) return;

        // Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Control whether we notify or not
        if(advisory.rating == Rating.NONE) {
            Log.i("Alerter", "No rating, skipping notification");
            return;
        } else if(advisory.rating == Rating.LOW && !prefs.getBoolean("alertLow", true)) {
            return;
        } else if(advisory.rating == Rating.MODERATE && !prefs.getBoolean("alertModerate", true)) {
            return;
        } else if(advisory.rating == Rating.CONSIDERABLE && !prefs.getBoolean("alertConsiderable", true)) {
            return;
        } else if(advisory.rating == Rating.HIGH && !prefs.getBoolean("alertHigh", true)) {
            return;
        } else if(advisory.rating == Rating.EXTREME && !prefs.getBoolean("alertExtreme", true)) {
            return;
        }
        // Weekend only
        if(prefs.getBoolean("alertWeekendOnly", false) && isWeekend()) {
            return;
        }
        
        Log.i("Alerter", "Notifying user");
        
        // Intent to open the advisory in AvyAlert
        PendingIntent openAdvisory = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Avalanche Risk: " + advisory.rating);
        builder.setTicker("Avalanche Risk: " + advisory.rating);
        builder.setContentText(advisory.details);
        builder.setSmallIcon(AvalancheRisk.getImage(advisory.rating));
        builder.setAutoCancel(true);
        // builder.setVibrate(new long[] {0, 140, 70, 220});
        builder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
        if(advisory.rating == Rating.EXTREME)
            builder.setLights(AvalancheRisk.getColor(advisory.rating), 80, 150); // On for 80ms, off for 150ms
        else
            builder.setLights(AvalancheRisk.getColor(advisory.rating), 1200, 8000); // On for 1200ms, off for 8000ms
        builder.setContentIntent(openAdvisory);
        //builder.setLargeIcon(aBitmap);

        Notification noti = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(ADVISORY_NOTI, noti);
    }

    /**
     * Returns true Friday 16:00 -> Sunday 11:59
     */
    private static boolean isWeekend() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(day == Calendar.SATURDAY || day == Calendar.SUNDAY) return true; // Saturday + Sunday
        else if(day == Calendar.FRIDAY && hour >= 16) return true; // Friday after 4pm
        else return false;
    }
    
}
