package com.ohso.omgubuntu;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.omgubuntu.sqlite.Articles.OnArticlesLoaded;
import com.ohso.util.ImageHandler;

public class StarredFragment extends BaseFragment implements OnRefreshListener<ListView>,
        OnItemClickListener, OnArticlesLoaded {
    private Articles articles = new Articles();
    private ImageHandler imageHandler;
    private String activeCategory;
    private ArticleAdapter mAdapter;
    public StarredFragment() {
        setTitle("Categories");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = new BasePullToRefreshListView(getActivity());
        listView.setOnRefreshListener(this);
        listView.setOnScrollListener(this);
        listView.setDisableScrollingWhileRefreshing(true);
        listView.setBackgroundResource(R.drawable.list_bg);
        listView.getRefreshableView().setBackgroundResource(R.drawable.list_bg);
        listView.getRefreshableView().setDividerHeight(0);
        listView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        listView.setOnItemClickListener(this);

        imageHandler = new ImageHandler(getActivity());
        ArticleDataSource dataSource = new ArticleDataSource(getActivity());
        dataSource.open();
        articles = dataSource.getArticles(false);
        dataSource.close();
        //TODO also trigger refresh if first article is ages old.
        if (articles.isEmpty()) {
            listView.setRefreshing();
            articles.getLatest(this);
            //articles.getCategory(activeCategory);
        }

        mAdapter = new ArticleAdapter(getActivity(), R.layout.article_row, R.id.article_row_text_title, articles);
        listView.setAdapter(mAdapter);
        return listView;
    }
    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

    }
    @Override
    public void articlesLoaded(Articles result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroy() {
        Log.i("OMG!", "DESTROYED");
        imageHandler.closeCache();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.i("OMG!", "DETACHED");
        imageHandler.closeCache();
        super.onDetach();
    }
    @Override
    public void articlesError() {
        // TODO Auto-generated method stub

    }


}
