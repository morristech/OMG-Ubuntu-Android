package com.ohso.omgubuntu;

import android.app.Application;
import android.content.Context;

public class OMGUbuntuApplication extends Application {
    private static Context mContext;
    public static String PREFS_FILE = "OMGUbuntuPrefs";

    // TODO register AlarmManager for NotificationService
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }


}
