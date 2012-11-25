package com.ohso.omgubuntu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;

public class StarredFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle("Starred");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void getData() {
        Log.i("OMG!", "Getting data");
        dataSource.open();
        Articles newData = dataSource.getStarredArticles(false);
        dataSource.close();
        articles.clear();
        for (Article article : newData) {
            Log.i("OMG!", "Adding " + article.getTitle());
            articles.add(article);
        }
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
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
        articles.clear();
        for (Article article : newData) {
            articles.add(article);
        }
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
        onRefreshComplete();
    }

    //TODO onstar onunstar
    @Override
    protected void onUnstarred(int position) {
        articles.remove(position);
    }

    @Override
    protected void getNewData() {
        Log.i("OMG!", "Get new data!");
        dataSource.open();
        Articles newData = dataSource.getStarredArticles(false);
        dataSource.close();
        articles.clear();
        for(Article article : newData) {
            Log.i("OMG!", "Adding " + article.getTitle());
            articles.add(article);
        }
        Log.i("OMG!", "Notifying again!");
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
        Log.i("OMG!", "Notified!");
        onRefreshComplete();
    }
}
