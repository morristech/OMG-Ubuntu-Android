package com.ohso.omgubuntu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;

public class StarredFragment extends BaseFragment {
    public StarredFragment() {
        setFooterEnabled(false);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle("Starred");
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

    //TODO onstar onunstar
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
