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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.ArticleDataSource;
import com.ohso.omgubuntu.data.Articles;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final int mId = 0;
    public NotificationBroadcastReceiver() {
    }

    // TODO ringtone and/or vibration
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DEVELOPER_MODE) Log.i("OMG!", "Starting NotificationBroadcastReceiver onReceive()");
        String lastPath = intent.getExtras().getString("last_path");

        ArticleDataSource dataSource = new ArticleDataSource(context);
        dataSource.open();
        Articles articles = dataSource.getArticlesSince(lastPath, true, true, false);
        dataSource.close();

        ArticlesWidgetProvider.notifyUpdate(articles.size());

        // If we have no new articles or if notifications are disabled, don't notify
        if (articles.size() < 1 ||
                !context.getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0)
                .getBoolean(SettingsFragment.NOTIFICATIONS_ENABLED,
                context.getResources().getBoolean(R.bool.pref_notifications_enabled_default))) {
            NotificationAlarmReceiver.releaseWakeLock();
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        if (articles.size() == 1) {
            Article article = articles.get(0);
            mBuilder.setSmallIcon(R.drawable.ic_stat_bubble)
                    .setContentTitle(context.getString(R.string.new_article_notification_singular))
                    .setContentText(article.getTitle())
                    .setTicker("New OMG! Ubuntu! article: " + article.getTitle())
                    .setOnlyAlertOnce(true).setAutoCancel(true);

            Intent notify = new Intent(context, ArticleActivity.class);
            notify.putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, article.getPath());

            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notify);

        } else if (articles.size() > 1) {
            mBuilder.setSmallIcon(R.drawable.ic_stat_bubble)
                    .setContentTitle(context.getString(R.string.new_article_notification_plural))
                    .setContentText(articles.get(0).getTitle())
                    .setContentInfo(String.valueOf(articles.size()))
                    .setTicker(String.valueOf(articles.size()) + " new OMG! Ubuntu! articles!")
                    .setOnlyAlertOnce(true).setAutoCancel(true);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            int numberOfTitles = 6 < articles.size() ? 6 : articles.size();
            for (int i = 0; i < numberOfTitles; i++) {
                inboxStyle.addLine(articles.get(i).getTitle());
            }
            mBuilder.setStyle(inboxStyle);

            Intent notify = new Intent(context, MainActivity.class);

            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notify);

        }

        // Common intent
        PendingIntent resultPending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPending);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());

        NotificationAlarmReceiver.releaseWakeLock();
    }

}
