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

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.actionbarsherlock.app.ActionBar;
import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.ArticleDataSource;
import com.ohso.omgubuntu.data.Articles;
import com.ohso.omgubuntu.data.Category;

public class CategoriesFragment extends BaseFragment implements ActionBar.OnNavigationListener {
    private static int lastActiveCategory = 0;
    private List<Category> categories = new ArrayList<Category>();

    @Override
    public void getData() {
        if (categories.isEmpty()) populateData();
        dataSource.open();
        Articles newData = dataSource.getArticlesWithCategory
                (categories.get(lastActiveCategory).getName(), false, currentPage);
        dataSource.close();
        adapter.clear();
        for (Article article : newData) {
            adapter.add(article);
        }
        if (newData.size() < ArticleDataSource.MAX_ARTICLES_PER_PAGE) {
            setFooterEnabled(false);
        } else {
            setFooterEnabled(true);
        }
    }

    @Override
    public void setActionBar() {
        if (categories.isEmpty()) populateData();
        actionBar.setTitle("");
        CategoryAdapter list = new CategoryAdapter(actionBar.getThemedContext(),
                R.layout.sherlock_spinner_item, categories);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(list, this);
        actionBar.setSelectedNavigationItem(lastActiveCategory);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (lastActiveCategory == itemPosition) return true;
        lastActiveCategory = itemPosition;
        if (footerView.isShown()) hidefooterView();
        currentPage = 1;
        nextPageAllowed = true;
        setFooterEnabled(false);

        dataSource.open();
        Articles articlesInCategory = dataSource.getArticlesWithCategory
                (categories.get(lastActiveCategory).getName(), false);
        dataSource.close();
        if (articlesInCategory.isEmpty()) {
            adapter.clear();
            setRefreshing();
        } else {
            adapter.clear();
            for (Article article : articlesInCategory) {
                adapter.add(article);
            }
            if (articlesInCategory.size() < ArticleDataSource.MAX_ARTICLES_PER_PAGE) {
                setFooterEnabled(false);
                setRefreshing();
            } else {
                setFooterEnabled(true);
            }
        }

        // Puts the user at the top of the list again
        if (gridView != null) gridView.setAdapter(adapter);
        return true;
    }

    @Override
    public void getNextPage() {
        new Articles().getNextCategoryPage(this, categories.get(lastActiveCategory).getPath(), ++currentPage);
    }

    @Override
    public void refreshView() {
        dataSource.open();
        Articles articles = dataSource.getArticlesWithCategory(categories.get(lastActiveCategory).getPath(),
                false, currentPage);
        dataSource.close();
        adapter.clear();
        for (Article article : articles) {
            adapter.add(article);
        }
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        new Articles().getLatestInCategory(this, categories.get(lastActiveCategory).getName());
    }

    private void populateData() {
        Resources res = getActivity().getResources();
        TypedArray data = res.obtainTypedArray(R.array.category_list);
        for (int i = 0; i < data.length(); i++) {
            int id = data.getResourceId(i, 0);
            String[] cat = res.getStringArray(id);
            if (id > 0) categories.add(new Category(res.getResourceEntryName(id), cat[0], cat[1]));
        }
    }

    @Override
    protected void getNewData() {
        new Articles().getLatestInCategory(this, categories.get(lastActiveCategory).getName());
    }
}
