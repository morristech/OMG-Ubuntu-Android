package com.ohso.omgubuntu;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationAlarmGenerator extends BroadcastReceiver {
    // TODO set up sharedPrefs for 30 minutes, 1 hour, 6 hours, 12 hours, 24 hours
    public NotificationAlarmGenerator() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Received boot completed message from Android");
        generateAlarm(context);
    }

    public static void generateAlarm(Context context) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Alarm isn't active, so setting up alarm now");
        Intent notificationIntent = new Intent(NotificationAlarmReceiver.NOTIFICATION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 30);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 30, pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Attempting to cancel alarms");
        Intent notificationIntent = new Intent(NotificationAlarmReceiver.NOTIFICATION_ACTION);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            alarm.cancel(pendingIntent);
            pendingIntent.cancel();
        } catch (Exception e) {
            if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "No alarm was cancelled. " + e.toString());
        }
    }
}
