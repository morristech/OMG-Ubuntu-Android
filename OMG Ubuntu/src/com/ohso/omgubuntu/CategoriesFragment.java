package com.ohso.omgubuntu;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.actionbarsherlock.app.ActionBar;
import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.omgubuntu.sqlite.Category;

public class CategoriesFragment extends BaseFragment implements ActionBar.OnNavigationListener {
    private static int lastActiveCategory = 0;
    private List<Category> categories = new ArrayList<Category>();

    @Override
    public void getData() {
        if (categories.isEmpty()) populateData();
        dataSource.open();
        Articles newData = dataSource.getArticlesWithCategory(categories.get(lastActiveCategory).getName(), false);
        dataSource.close();
        articles.clear();
        for (Article article : newData) {
            articles.add(article);
        }
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setActionBar() {
        if (categories.isEmpty()) populateData();
        actionBar.setTitle("");
        CategoryAdapter list = new CategoryAdapter(actionBar.getThemedContext(), R.layout.sherlock_spinner_item, categories);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(list, this);
        actionBar.setSelectedNavigationItem(lastActiveCategory);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        lastActiveCategory = itemPosition;
        dataSource.open();
        Articles articlesInCategory = dataSource.getArticlesWithCategory(categories.get(lastActiveCategory).getName(), false);
        dataSource.close();
        if (articlesInCategory.isEmpty()) {
            articles.clear();
            ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
            setRefreshing();
            getNewData();
        } else {
            articles.clear();
            for (Article article : articlesInCategory) {
                articles.add(article);
            }
            ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
            if (articlesInCategory.size() < 15) {
                setRefreshing();
                getNewData();
            }
        }

        return true;
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        articles.getLatestInCategory(this, categories.get(lastActiveCategory).getName());
    }

    private void populateData() {
        Resources res = getActivity().getResources();
        TypedArray data = res.obtainTypedArray(R.array.category_list);
        for (int i = 0; i < data.length(); i++) {
            int id = data.getResourceId(i, 0);
            String[] cat = res.getStringArray(id);
            if (id > 0) categories.add(new Category(res.getResourceEntryName(id), cat[0]));
        }
    }

    @Override
    protected void getNewData() {
        articles.getLatestInCategory(this, categories.get(lastActiveCategory).getName());
    }
}
