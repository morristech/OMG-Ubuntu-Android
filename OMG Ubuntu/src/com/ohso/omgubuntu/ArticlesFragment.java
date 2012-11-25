package com.ohso.omgubuntu;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;


public class ArticlesFragment extends BaseFragment {
    private SharedPreferences mSharedPref;
    public ArticlesFragment() {
        pagedEnabled = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPref = getActivity().getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
    }

    @Override
    protected void getData() {
        dataSource.open();
        Articles newData = dataSource.getArticles(false);
        dataSource.close();
        articles.clear();
        for (Article article : newData) {
            articles.add(article);
        }
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        String lastPath = mSharedPref.getString(NotificationService.LAST_NOTIFIED_PATH, null);
        if (lastPath != null) {
            Editor editor = mSharedPref.edit();
            editor.putString(NotificationService.LAST_NOTIFIED_PATH, null);
            editor.commit();
            ArticlesWidgetProvider.notifyUpdate(getActivity().getApplicationContext(), 0);
        }
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        articles.getLatest(this);

    }

    @Override
    protected void getNewData() {
        articles.getLatest(this);
    }

}
