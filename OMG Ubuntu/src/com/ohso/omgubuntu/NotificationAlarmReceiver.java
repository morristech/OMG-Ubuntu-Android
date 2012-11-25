package com.ohso.omgubuntu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ACTION = "com.ohso.omgubuntu.BroadcastReceiver.NOTIFICATION_ACTION";

    private static WakeLock wakeLock;
    public NotificationAlarmReceiver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Received call to start background intent");
        PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OmgNotificationLock");
        wakeLock.acquire();
        Intent notification = new Intent(context, NotificationService.class);
        context.startService(notification);
    }

    public static WakeLock getWakeLock() { return wakeLock; }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            try {
                wakeLock.release();
            } catch (Throwable e) {} // Shouldn't worry
        } else {
            // This usually happens if we're already awake and there isn't a wakelock anyway
            if (MainActivity.DEVELOPER_MODE) Log.i("OMG!", "Wakelock already released");
        }
    }

}
