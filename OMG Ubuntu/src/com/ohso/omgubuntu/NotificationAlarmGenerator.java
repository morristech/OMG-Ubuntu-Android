/*
 * Copyright (C) 2012 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.omgubuntu;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationAlarmGenerator extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Received boot completed message from Android");
        generateAlarm(context);
    }

    public static void generateAlarm(Context context) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Alarm isn't active, so setting up alarm now");
        Intent notificationIntent = new Intent(NotificationAlarmReceiver.NOTIFICATION_ACTION);
        boolean isActive = (PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (isActive) {
            Log.i("OMG!", "Alarm already exists, not recreating");
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 30);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 30, pendingIntent);
        // Debug line
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 1, pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Attempting to cancel alarms");
        Intent notificationIntent = new Intent(NotificationAlarmReceiver.NOTIFICATION_ACTION);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            alarm.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("OMG!", "Alarm cancelled.");
        } catch (Exception e) {
            if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "No alarm was cancelled. " + e.toString());
        }
    }
}
