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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.ArticleDataSource;
import com.ohso.omgubuntu.data.Articles;

public class ArticlesFragment extends BaseFragment {
    private SharedPreferences mSharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPref = getActivity().getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
    }

    @Override
    protected void getData() {
        dataSource.open();
        Articles newData = dataSource.getArticles(false, currentPage);
        dataSource.close();
        adapter.clear();
        for (Article article : newData) {
            adapter.add(article);
        }
        if (newData.size() >= ArticleDataSource.MAX_ARTICLES_PER_PAGE) {
            setFooterEnabled(true);
        } else {
            setFooterEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String lastPath = mSharedPref.getString(NotificationService.LAST_NOTIFIED_PATH, null);
        if (lastPath != null) {
            Editor editor = mSharedPref.edit();
            editor.putString(NotificationService.LAST_NOTIFIED_PATH, null);
            editor.commit();
            ArticlesWidgetProvider.notifyUpdate(0);
        }
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        new Articles().getLatest(this);
    }

    @Override
    protected void getNewData() {
        new Articles().getLatest(this);
    }

}
