/*
 * Copyright (C) 2012 - 2013 Ohso Ltd
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ACTION = "com.ohso.omgubuntu.BroadcastReceiver.NOTIFICATION_ACTION";

    private static WakeLock wakeLock;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Received call to start background intent");
        PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OmgNotificationLock");
        if (wakeLock.isHeld() == false) wakeLock.acquire();
        Intent notification = new Intent(context, NotificationService.class);
        context.startService(notification);
    }

    public static WakeLock getWakeLock() { return wakeLock; }

    public static void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
            } catch (Throwable e) {} // Shouldn't worry
        } else {
            // This usually happens if we're already awake and there isn't a wakelock anyway
            if (MainActivity.DEVELOPER_MODE) Log.i("OMG!", "Wakelock already released");
        }
    }

}
