package com.ohso.omgubuntu;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.Articles;


public class ArticlesFragment extends BaseFragment {
    private SharedPreferences mSharedPref;
    public ArticlesFragment() {
    }

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
        if (newData.size() >= 20) {
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
            ArticlesWidgetProvider.notifyUpdate(getActivity().getApplicationContext(), 0);
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
