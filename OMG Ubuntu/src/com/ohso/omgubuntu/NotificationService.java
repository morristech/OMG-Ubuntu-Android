package com.ohso.omgubuntu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.omgubuntu.sqlite.Articles.OnArticlesLoaded;

public class NotificationService extends IntentService implements OnArticlesLoaded {
    private static final String NOTIFICATION_SERVICE_NAME = "OMGNotificationService";
    public static final String LAST_NOTIFIED_PATH = "NotificationServiceLastNotifiedPath";
    public static final String NOTIFICATION_ACTION =
            "com.ohso.omgubuntu.NotificationService.NEW_ARTICLES";

    public static boolean isNotificationAlarmActive() {
        boolean isUp = (PendingIntent.getBroadcast(OMGUbuntuApplication.getContext(), 0,
                new Intent(NOTIFICATION_ACTION), PendingIntent.FLAG_NO_CREATE) != null);
        if (isUp) Log.i("OMG!", "Alarm is already active!");
        return isUp;
    }

    public NotificationService() {
        super(NOTIFICATION_SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0)
                .getBoolean(SettingsFragment.NOTIFICATIONS_ENABLED,
                getResources().getBoolean(R.bool.pref_notifications_enabled_default))) {
            Log.i("OMG!", "Cancelling alarms since it's no longer enabled!");
            NotificationAlarmGenerator.cancelAlarm(getApplicationContext());
            return;
        }
        Articles articles = new Articles();
        articles.getLatest(this);
    }

    @Override
    public void articlesLoaded(Articles result) {
        List<Article> newArticles = new ArrayList<Article>();
        ArticleDataSource dataSource = new ArticleDataSource(getApplicationContext());
        dataSource.open();
        for (Article article : result) {
            if(dataSource.createArticle(article, false, false)) {
                newArticles.add(article);
            }
        }
        dataSource.close();

        // Comment this out for debugging
        if(newArticles.size() < 1) {
            ArticlesWidgetProvider.notifyUpdate(getApplicationContext(), 0);
            NotificationAlarmReceiver.releaseWakeLock();
            return;
        }

        Collections.sort(newArticles, new Article.Compare());

        SharedPreferences sharedPref = getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
        String lastPath = sharedPref.getString(LAST_NOTIFIED_PATH, null);
        String broadcastPath = null;

        if (lastPath != null) {
            broadcastPath = lastPath;
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            // Uncomment next line and comment the one after for debugging
            //broadcastPath = result.get(2).getPath();
            broadcastPath = newArticles.get(0).getPath();
            editor.putString(LAST_NOTIFIED_PATH, broadcastPath);
            editor.commit();
        }


        broadcastArticlesNotification(broadcastPath);
    }

    @Override
    public void articlesError() {
        // Silently fail for now
    }

    private void broadcastArticlesNotification(String lastPath) {
        Intent broadcast = new Intent();
        broadcast.setAction(NOTIFICATION_ACTION);
        broadcast.addCategory(Intent.CATEGORY_DEFAULT);
        broadcast.putExtra("last_path", lastPath);
        sendBroadcast(broadcast);
    }

}
