package com.ohso.omgubuntu;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;

//@TargetApi(11)
public class ArticlesWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_OPEN_ARTICLE = "com.ohso.omgubuntu.OPEN_ARTICLE";
    public static final String ACTION_REFRESH = "com.ohso.omgubuntu.REFRESH";
    public static final String INTENT_EXTRA_APP_WIDGET_IDS = "com.ohso.omgubuntu.AppWidgetIds";
    public static RemoteViews remoteViews;
    public static RemoteViews refreshView;

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            if (Build.VERSION.SDK_INT >= 11) {
                Intent widgetIntent = new Intent(context, ArticlesWidgetService.class);
                widgetIntent.setData(Uri.parse(widgetIntent.toUri(Intent.URI_INTENT_SCHEME)));
                if (remoteViews == null) remoteViews = getRemoteViews(context, appWidgetManager, appWidgetIds);
                honeycombSetup(appWidgetManager, appWidgetId, widgetIntent);
            } else {
                if (remoteViews == null) remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_froyo);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                ArticleDataSource source = new ArticleDataSource(context);
                source.open();
                Article latestArticle = source.getLatestArticle(false);
                source.close();
                remoteViews.setTextViewText(R.id.widget_froyo_title, latestArticle.getTitle());
                Intent mainIntent = new Intent(context, ArticleActivity.class).putExtra(ArticleActivity.LATEST_ARTICLE_INTENT, true);
                PendingIntent froyoMainIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
                remoteViews.setOnClickPendingIntent(R.id.widget_froyo_container, froyoMainIntent);
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @TargetApi(11)
    public void honeycombSetup(AppWidgetManager appWidgetManager, int appWidgetId, Intent widgetIntent) {
        remoteViews.setRemoteAdapter(appWidgetId, R.id.widget_articles_list, widgetIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_articles_list_empty);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_OPEN_ARTICLE)) {
            if (MainActivity.DEVELOPER_MODE) Log.i("OMG!", "Got call to open article: " + intent.getStringExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT));
            Intent activityIntent = new Intent(context, ArticleActivity.class).putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT,
                    intent.getStringExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT));
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        } else if (intent.getAction().equals(ACTION_REFRESH)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, com.ohso.omgubuntu.ArticlesWidgetProvider.class));

            RemoteViews refreshView = new RemoteViews(context.getPackageName(), R.layout.refresh_menu_item);
            remoteViews.removeAllViews(R.id.widget_articles_refresh_container);
            remoteViews.addView(R.id.widget_articles_refresh_container, refreshView);
            manager.updateAppWidget(ids, remoteViews);

            Intent refreshIntent = new Intent(context, NotificationService.class);
            context.startService(refreshIntent);
        }
        super.onReceive(context, intent);
    }

    public static void notifyUpdate(Context context, int newArticleCount) {
        if (Build.VERSION.SDK_INT >= 11) {
            honeycombUpdate(context, newArticleCount);
        } else {
            froyoUpdate(context);
        }
    }
    private static void froyoUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, com.ohso.omgubuntu.ArticlesWidgetProvider.class));

        if (remoteViews == null) remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_froyo);
        ArticleDataSource source = new ArticleDataSource(context);
        source.open();
        Article latestArticle = source.getLatestArticle(false);
        source.close();
        remoteViews.setTextViewText(R.id.widget_froyo_title, latestArticle.getTitle());

        manager.updateAppWidget(ids, remoteViews);
    }

    @TargetApi(11)
    private static void honeycombUpdate(Context context, int newArticleCount) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, com.ohso.omgubuntu.ArticlesWidgetProvider.class));
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_articles_list);

        if (remoteViews == null) remoteViews = getRemoteViews(context, manager, ids);

        remoteViews.removeAllViews(R.id.widget_articles_refresh_container);
        remoteViews.addView(R.id.widget_articles_refresh_container, refreshView);
        remoteViews.setTextViewText(R.id.widget_articles_count, newArticleCount < 1 ? null : String.valueOf(newArticleCount));

        manager.updateAppWidget(ids, remoteViews);
    }

    @TargetApi(11)
    private static RemoteViews getRemoteViews(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent mainActivity = new Intent (context, MainActivity.class);
        PendingIntent mainActivityIntent = PendingIntent.getActivity(context, 0, mainActivity, 0);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_articles);
        remoteViews.setOnClickPendingIntent(R.id.widget_articles_logo, mainActivityIntent);
        remoteViews.setOnClickPendingIntent(R.id.widget_articles_texts, mainActivityIntent);

        refreshView = new RemoteViews(context.getPackageName(), R.layout.widget_articles_refresh);
        Intent refreshIntent = new Intent(context, ArticlesWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        refreshView.setOnClickPendingIntent(R.id.widget_articles_refresh, refreshPendingIntent);
        remoteViews.addView(R.id.widget_articles_refresh_container, refreshView);


        Intent templateIntent = new Intent(context, ArticlesWidgetProvider.class);
        templateIntent.setAction(ACTION_OPEN_ARTICLE);

        PendingIntent articlesPendingTemplate = PendingIntent.getBroadcast(context, 0, templateIntent, 0);
        remoteViews.setPendingIntentTemplate(R.id.widget_articles_list, articlesPendingTemplate);
        remoteViews.setEmptyView(R.id.widget_articles_list, R.id.widget_articles_list_empty);

        return remoteViews;
    }
}
