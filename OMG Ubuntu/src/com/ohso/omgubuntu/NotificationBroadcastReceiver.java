package com.ohso.omgubuntu;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;
import com.ohso.omgubuntu.sqlite.Articles;

@TargetApi(11)
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final int mId = 0;
    public NotificationBroadcastReceiver() {
    }

    //TODO ringtone and/or vibration
    // TODO update widgets
    @Override
    public void onReceive(Context context, Intent intent) {
        String lastPath = intent.getExtras().getString("last_path");

        ArticleDataSource dataSource = new ArticleDataSource(context);
        dataSource.open();
        Articles articles = dataSource.getArticlesSince(lastPath, true, true, false);
        dataSource.close();

        ArticlesWidgetProvider.notifyUpdate(context, articles.size());

        if (articles.size() < 1) {
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
                    .setOnlyAlertOnce(true).setAutoCancel(true);

            Intent notify = new Intent(context, ArticleActivity.class);
            notify.putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, article.getPath());

            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notify);

        } else if (articles.size() > 1) {
            mBuilder.setSmallIcon(R.drawable.ic_stat_bubble)
                    .setContentTitle(context.getString(R.string.new_article_notification_singular))
                    .setContentText(articles.get(0).getTitle())
                    .setContentInfo(String.valueOf(articles.size()))
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
