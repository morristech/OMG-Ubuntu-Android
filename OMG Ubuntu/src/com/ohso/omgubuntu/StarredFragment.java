/*
 * Copyright (C) 2012 Ohso Ltd
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.Articles;

public class StarredFragment extends BaseFragment {
    public StarredFragment() {
        setFooterEnabled(false);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_starred));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void getData() {
        dataSource.open();
        Articles newData = dataSource.getStarredArticles(false);
        dataSource.close();
        adapter.clear();
        for (Article article : newData) {
            adapter.add(article);
        }
    }

    @Override
    public void setActionBar() {
        actionBar.setTitle("Starred");
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        dataSource.open();
        Articles newData = dataSource.getStarredArticles(false);
        dataSource.close();
        adapter.clear();
        for (Article article : newData) {
            adapter.add(article);
        }
        onRefreshComplete();
    }

    @Override
    protected void onUnstarred(int position) {
        adapter.remove(adapter.getItem(position));
    }

    @Override
    public void refreshView() {
        getNewData();
    }

    @Override
    protected void getNewData() {
        dataSource.open();
        Articles newData = dataSource.getStarredArticles(false);
        dataSource.close();
        adapter.clear();
        for(Article article : newData) {
            adapter.add(article);
        }
        onRefreshComplete();
    }
}
