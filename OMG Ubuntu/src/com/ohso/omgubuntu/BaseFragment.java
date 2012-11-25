package com.ohso.omgubuntu;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.omgubuntu.sqlite.Articles.OnArticlesLoaded;
import com.ohso.omgubuntu.sqlite.Articles.OnNextPageLoaded;
import com.ohso.util.ImageHandler;

/**
 * @author Sam Tran <samvtran@gmail.com>
 */
public abstract class BaseFragment extends SherlockListFragment implements OnTouchListener,
        OnArticlesLoaded, OnScrollListener, OnNextPageLoaded, OnClickListener {
    public static final String FORCE_REFRESH = "com.ohso.omgubuntu.BaseFragment.forceRefresh";

    // Max # of pages to allow "more articles"
    private final int MAXIMUM_PAGED = 5;

    protected Articles articles = new Articles();
    protected ArticleDataSource dataSource;
    protected ImageHandler imageHandler;
    protected ActionBar actionBar;
    protected MenuItem refresh;

    private TextView footerView;
    private int currentPage = 1;
    protected boolean pagedEnabled = false;
    private boolean nextPageAllowed = true;


    // Updates the last active article when you return from ArticleActivity
    // e.g., to reflect (un)starring and unmarking the unread marker
    private int lastActiveArticlePosition = -1;
    // Marks the list position to restore state upon returning to the fragment
    private int mCurrentPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCurrentPosition = getListView().getFirstVisiblePosition();
        outState.putInt("currentPosition", mCurrentPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt("currentPosition", 0);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Reset the default action bar since it's global and setActionBar() would otherwise change things permanently
        actionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        ((BaseActivity) getActivity()).getDefaultActionBar();
        setActionBar();

        setListAdapter(new ArticleAdapter(getActivity(),
                R.layout.article_row, R.id.article_row_text_title, articles));
        imageHandler = ((BaseActivity) getActivity()).getImageHandler();
        ((ArticleAdapter) getListAdapter()).setImageHandler(imageHandler);

        // TODO make this a normal layout
        ListView listView = new ListView(getActivity());
        listView.setId(android.R.id.list);
        listView.setBackgroundResource(R.drawable.list_bg);
        listView.setSelector(R.drawable.list_view_selector);
        listView.setDividerHeight(0);
        listView.setItemsCanFocus(true);
        listView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        listView.setOnTouchListener(this);
        listView.setOnScrollListener(this);
        listView.setOnTouchListener(this);
        registerForContextMenu(listView);

        dataSource = new ArticleDataSource(getActivity());
        getData();

        if (articles.size() >= 15 && pagedEnabled) {
            footerView = (TextView) inflater.inflate(R.layout.activity_main_footer, null, false);
            footerView.setOnClickListener(this);
            listView.addFooterView(footerView, null, false);
        }

        //TODO also trigger refresh if first article is ages old.
        if (articles.isEmpty()) {
            setRefreshing();
            getNewData();
        }
        return listView;
    }



    @Override
    public void onClick(View v) {
        if (nextPageAllowed && currentPage < MAXIMUM_PAGED) {
            ((TextView) v).setText("Loading...");
            articles.getNextPage(this, ++currentPage);
        } else {
            Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.omgubuntu.co.uk"));
            external.addCategory(Intent.CATEGORY_BROWSABLE);
            Intent chooser = Intent.createChooser(external, "Visit OMG! Ubuntu! via");
            startActivity(chooser);
        }
    }

    @Override
    public void nextPageLoaded(Articles result) {
        if (currentPage == MAXIMUM_PAGED) {
            footerView.setText(R.string.activity_main_footer_over);
            nextPageAllowed = false;
        } else {
            footerView.setText(getResources().getString(R.string.activity_main_footer_text));
        }
        articles.addAll(result);
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void nextPageError() {
        articlesError();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    /*
     * Pause the image handler for buttery smooth flinging.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING) {
            imageHandler.setPauseWork(true);
        } else {
            imageHandler.setPauseWork(false);
        }
    }

    protected abstract void getData();
    protected abstract void getNewData();

    public void setActionBar() {}

    public void setData(Articles articles) { this.articles = articles; }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getActivity() instanceof BaseActivity) {
            if (BaseActivity.isSidebarActive()) {
                ((BaseActivity) getActivity()).toggleSidebarFragment();
                return true;
            } else if (!((BaseActivity) getActivity()).sidebarFragmentTransitionComplete) {
                if (MainActivity.DEVELOPER_MODE) Log.i("OMG!", "Transition not yet complete!");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Article clicked = articles.get((int) id);
        lastActiveArticlePosition = (int) id;
        ((MainActivity) getActivity()).openArticle(clicked.getPath());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.article_row_long_press, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_main, menu);
        refresh = menu.findItem(R.id.activity_main_menu_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(BaseActivity.isSidebarActive()) ((BaseActivity) getActivity()).toggleSidebarFragment();
        switch(item.getItemId()) {
            case R.id.activity_main_menu_refresh:
                setRefreshing();
                return true;
            case R.id.activity_main_menu_read_all:
                DialogFragment fragment = new AlertDialogFragment();
                fragment.setTargetFragment(this, 0);
                fragment.show(getSherlockActivity().getSupportFragmentManager(), "mark_as_read");
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                onUnstarred(position);
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
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
        return true;
    }

    protected void onUnstarred(int position) {}

    private void startShareIntent(String title, String path) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + "http://www.omgubuntu.co.uk" + path);
        Intent chooser = Intent.createChooser(shareIntent, "Share this article via");
        startActivity(chooser);
    }

    @Override
    public void articlesLoaded(Articles result) {
        // TODO Should we also catch result.size() == 0?
        onRefreshComplete();
        dataSource.open();
        if(!result.isEmpty()) dataSource.createArticles(result, true);
        dataSource.clearArticlesOverNumberOfEntries();
        dataSource.close();
        getData();
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void articlesError() {
        onRefreshComplete();
        Log.e("OMG!", "FEED ERROR!");
        Toast error = Toast.makeText(getActivity(),
                "Couldn't refresh the feed. Try again in a few moments.", Toast.LENGTH_SHORT);
        try {
            ((TextView) ((LinearLayout) error.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        error.show();
    }

    public void setRefreshing() {
        /*
         * 4.2 currently calls onCreateView() before onCreateOptionsMenu(), so this is still null in cases where
         * setRefreshing() is called b/c the dataset is empty.
         */
        if (refresh != null) refresh.setActionView(R.layout.refresh_menu_item);
    }

    public void onRefreshComplete() {
        /*
         * As per the  4.2 bug above, this will also lead to a NullPointerException if onCreateView() still hasn't
         * been called before the result set gets returned. Especially a problem on the starred page that has no
         * network activity to give onCreateView() to run.
         */
        if (refresh != null) refresh.setActionView(null);
    }
    @Override
    public void onResume() {
        super.onResume();
        imageHandler.setExitTasksEarly(false);
        if (lastActiveArticlePosition != -1) {
            dataSource.open();
            articles.set(lastActiveArticlePosition,
                    dataSource.getArticle(articles.get(lastActiveArticlePosition).getPath(), false));
            dataSource.close();
            ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
            lastActiveArticlePosition = -1;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        imageHandler.setExitTasksEarly(true);
        imageHandler.flush();
    }

    public void setAllAsRead() {
        dataSource.open();
        for (Article article : articles) {
            article.setUnread(0);
            dataSource.setArticleToUnread(false, article.getPath());
        }
        dataSource.close();
        ((ArticleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public static class AlertDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Mark all as read?");
            builder.setPositiveButton("Mark", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((BaseFragment) getTargetFragment()).setAllAsRead();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return builder.create();
        }

    }

}
