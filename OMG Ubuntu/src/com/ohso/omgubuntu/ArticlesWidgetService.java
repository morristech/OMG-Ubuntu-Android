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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ohso.omgubuntu.data.ArticleDataSource;
import com.ohso.omgubuntu.data.Articles;

@TargetApi(11)
public class ArticlesWidgetService extends RemoteViewsService {

    public ArticlesWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ArticlesWidgetFactory(this.getApplicationContext(), intent);
    }

    class ArticlesWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private ArticleDataSource mDataSource;
        private Articles articles;
        public ArticlesWidgetFactory(Context applicationContext, Intent intent) {
            mContext = applicationContext;
            mDataSource = new ArticleDataSource(mContext);
        }

        @Override
        public void onCreate() {
            mDataSource.open();
            articles = mDataSource.getArticles(false);
            mDataSource.close();
        }

        @Override
        public int getCount() {
            return articles.size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_articles_row);
            rv.setTextViewText(android.R.id.text1, articles.get(position).getTitle());
            rv.setTextViewText(android.R.id.text2, articles.get(position).getSummary());

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, articles.get(position).getPath());
            rv.setOnClickFillInIntent(R.id.widget_articles_row_container, fillInIntent);
            return rv;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onDataSetChanged() {
            mDataSource.open();
            articles = mDataSource.getArticles(false);
            mDataSource.close();
        }

        @Override
        public void onDestroy() {}

    }
}
