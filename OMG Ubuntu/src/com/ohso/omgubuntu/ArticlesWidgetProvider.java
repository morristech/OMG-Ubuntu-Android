package com.ohso.omgubuntu;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

@TargetApi(11)
public class ArticlesWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_OPEN_ARTICLE = "com.ohso.omgubuntu.OPEN_ARTICLE";
    public static final String ACTION_REFRESH = "com.ohso.omgubuntu.REFRESH";
    public static final String INTENT_EXTRA_APP_WIDGET_IDS = "com.ohso.omgubuntu.AppWidgetIds";
    public static RemoteViews remoteViews;
    public static RemoteViews refreshView;

    // TODO Refresh button
    public ArticlesWidgetProvider() {}

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent widgetIntent = new Intent(context, ArticlesWidgetService.class);
        widgetIntent.setData(Uri.parse(widgetIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (remoteViews == null) remoteViews = getRemoteViews(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            remoteViews.setRemoteAdapter(appWidgetId, R.id.widget_articles_list, widgetIntent);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_articles_list_empty);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
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
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, com.ohso.omgubuntu.ArticlesWidgetProvider.class));
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_articles_list);

        if (remoteViews == null) remoteViews = getRemoteViews(context, manager, ids);

        remoteViews.removeAllViews(R.id.widget_articles_refresh_container);
        remoteViews.addView(R.id.widget_articles_refresh_container, refreshView);
        remoteViews.setTextViewText(R.id.widget_articles_count, newArticleCount < 1 ? null : String.valueOf(newArticleCount));

        // Debug line that shows 0 if no results are returned rather than no textview at all
        //remoteViews.setTextViewText(R.id.widget_articles_count, String.valueOf(newArticleCount));
        manager.updateAppWidget(ids, remoteViews);
    }

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
