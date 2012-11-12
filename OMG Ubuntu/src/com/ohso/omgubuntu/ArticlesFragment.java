package com.ohso.omgubuntu;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.omgubuntu.sqlite.Articles.OnArticlesLoaded;
import com.ohso.util.ImageHandler;

public class ArticlesFragment extends BaseFragment implements OnRefreshListener<ListView>,
        OnItemClickListener, OnArticlesLoaded {
//    private BasePullToRefreshListView  listView;
    private Articles articles = new Articles();
    private ArticleAdapter mAdapter;
    private ImageHandler imageHandler;
    private ArticleDataSource dataSource;
    private int lastActiveArticlePosition = -1;

    public ArticlesFragment() {
        setTitle("Articles");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = new BasePullToRefreshListView(getActivity());
        listView.setOnRefreshListener(this);
        listView.setOnScrollListener(this);
        listView.setDisableScrollingWhileRefreshing(true);
        listView.setBackgroundResource(R.drawable.list_bg);
        listView.getRefreshableView().setSelector(R.drawable.list_view_selector);
        listView.getRefreshableView().setDividerHeight(0);
        listView.getRefreshableView().setItemsCanFocus(true);
        listView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(this);
        listView.getRefreshableView().setOnTouchListener(this);
        registerForContextMenu(listView);
        imageHandler = new ImageHandler(getActivity());
        dataSource = new ArticleDataSource(getActivity());
        dataSource.open();
        articles = dataSource.getArticles(false);
        dataSource.close();

        //TODO also trigger refresh if first article is ages old.
        if (articles.isEmpty()) {
            listView.setRefreshing();
            articles.getLatest(this);
        }

        mAdapter = new ArticleAdapter(getActivity(), R.layout.article_row, R.id.article_row_text_title, articles);
        mAdapter.setImageHandler(imageHandler);
        listView.setAdapter(mAdapter);
        return listView;
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
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        articles.getLatest(this);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.article_row_long_press, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        //Article article = mAdapter.getItem(info.position);
        Article article = articles.get((int) info.id);
        if(article.isStarred()) {
            menu.findItem(R.id.article_row_unstar).setVisible(true);
        } else {
            menu.findItem(R.id.article_row_star).setVisible(true);
        }

        if(article.isUnread()) {
            menu.findItem(R.id.article_mark_as_read).setVisible(true);
        } else {
            menu.findItem(R.id.article_mark_as_unread).setVisible(true);
        }

    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = (int) info.id;
        Article article = articles.get(position);
        dataSource.open();
        switch(item.getItemId()) {
            case R.id.article_row_star:
                article.setStarred(1);
                dataSource.setArticleToStarred(true, article.getPath());
                break;
            case R.id.article_row_unstar:
                article.setStarred(0);
                dataSource.setArticleToStarred(false, article.getPath());
                break;
            case R.id.article_mark_as_read:
                article.setUnread(0);
                dataSource.setArticleToUnread(false, article.getPath());
                break;
            case R.id.article_mark_as_unread:
                article.setUnread(1);
                dataSource.setArticleToUnread(true, article.getPath());
                break;
            case R.id.article_share:
                startShareIntent(article.getTitle(), article.getPath());
                break;
            default:
                dataSource.close();
                return super.onContextItemSelected(item);
        }
        dataSource.close();
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private void startShareIntent(String title, String path) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + "http://www.omgubuntu.co.uk" + path);
        Intent chooser = Intent.createChooser(shareIntent, "Share this article via");
        startActivity(chooser);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("OMG!", "resuming...");
        if (lastActiveArticlePosition > 0) {
            Log.i("OMG!", "Updating article at position " + lastActiveArticlePosition);
            dataSource.open();
            articles.set(lastActiveArticlePosition,
                    dataSource.getArticle(articles.get(lastActiveArticlePosition).getPath(), false));
            dataSource.close();
            mAdapter.notifyDataSetChanged();
            lastActiveArticlePosition = -1;
        }
    }

    @Override
    public void articlesLoaded(Articles result) {
        // TODO Should we also catch result.size() == 0?
        listView.onRefreshComplete();
        dataSource.open();
        dataSource.createArticles(result, true);
        articles = dataSource.getArticles(false);
        dataSource.clearArticlesOverNumberOfEntries();
        dataSource.close();
        mAdapter.clear();
        mAdapter.addAll(articles);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
        Article clicked = articles.get((int) id);
        Log.i("OMG!", "Click on " + clicked.getTitle());
        lastActiveArticlePosition = (int) id;
        ((MainActivity) getActivity()).openArticle(clicked.getPath());
    }

    @Override
    public void articlesError() {
        listView.onRefreshComplete();
        Toast error = Toast.makeText(getActivity(),
                "Couldn't refresh the feed. Try again in a few moments.", Toast.LENGTH_SHORT);
        try {
            ((TextView) ((LinearLayout) error.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        error.show();
    }

}
