package com.ohso.omgubuntu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
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
public abstract class BaseFragment extends SherlockFragment implements OnTouchListener,
        OnArticlesLoaded, OnScrollListener, OnNextPageLoaded, OnClickListener, OnItemClickListener {
    public static final String FORCE_REFRESH = "com.ohso.omgubuntu.BaseFragment.forceRefresh";
    private boolean onStartRefresh = true;

    // Max # of pages to allow "more articles" from
    private final int MAXIMUM_PAGED = 5;

    protected ArticleDataSource dataSource;
    protected ArticleAdapter adapter;
    protected HeterogeneousGridView gridView;
    protected ImageHandler imageHandler;
    protected ActionBar actionBar;
    protected MenuItem refresh;

    protected TextView footerView;
    protected int currentPage = 1;
    protected boolean nextPageAllowed = false;
    protected boolean footerEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Reset the default action bar since it's global and setActionBar() would otherwise change things permanently
        dataSource = new ArticleDataSource(getActivity());
        actionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        ((BaseActivity) getActivity()).getDefaultActionBar();
        setActionBar();

        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_base_view, null, false);

        footerView = (TextView) inflater.inflate(R.layout.activity_main_footer, container, false);
        footerView.setVisibility(TextView.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        footerView.setLayoutParams(params);
        layout.addView(footerView);
        footerView.setOnClickListener(this);

        int columnNumber = getColumnByScreenSize();

        adapter = new ArticleAdapter(getActivity(), R.layout.article_row, R.id.article_row_text_title, new Articles(),
                columnNumber, footerView);
        imageHandler = ((BaseActivity) getActivity()).getImageHandler();
        adapter.setImageHandler(imageHandler);


        gridView = (HeterogeneousGridView) layout.findViewById(R.id.fragment_base_gridview);
        gridView.setAdapter(adapter);
        gridView.setBackgroundResource(R.drawable.list_bg);
        gridView.setNumColumns(columnNumber);
        gridView.setScrollContainer(false);
        gridView.setSelector(R.drawable.list_view_selector);
        gridView.setOnTouchListener(this);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        registerForContextMenu(gridView);

        getData();
        if (adapter.isEmpty() || (onStartRefresh == true &&
                getActivity().getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0)
                .getBoolean(SettingsFragment.STARTUP_CHECK_ENABLED, true))) {
            onStartRefresh = false;
            setRefreshing();
            getNewData();
        }

        if (adapter.getCount() >= ArticleDataSource.MAX_ARTICLES_PER_PAGE && footerEnabled) {
            nextPageAllowed = true;
            adapter.setFooterEnabled(true);
        }

        return layout;
    }

    public static int getColumnByScreenSize() {
        int columnNumber = 1;
        final int sizeMask = OMGUbuntuApplication.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        final int orientation = OMGUbuntuApplication.getContext().getResources().getConfiguration().orientation;
        switch(sizeMask) {
            case(Configuration.SCREENLAYOUT_SIZE_LARGE):
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    columnNumber = 2;
                }
                break;
            case(Configuration.SCREENLAYOUT_SIZE_XLARGE):
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    columnNumber = 3;
                } else {
                    columnNumber = 2;
                }
                break;
        }

        return columnNumber;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (footerView.isShown()) footerView.setVisibility(TextView.GONE);
            gridView.setNumColumns(getColumnByScreenSize());
            adapter.setColumns(getColumnByScreenSize());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int pos, long id) {
        Article clicked = adapter.getItem((int) id);
        ((MainActivity) getActivity()).openArticle(clicked.getPath());
    }

    @Override
    public void onClick(View v) {
        footerView.setEnabled(false);
        if (nextPageAllowed && currentPage < MAXIMUM_PAGED) {
            ((TextView) v).setText("Loading...");
            if (refresh != null) refresh.setActionView(R.layout.refresh_menu_item);
            getNextPage();
        } else {
            Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.omgubuntu.co.uk"));
            external.addCategory(Intent.CATEGORY_BROWSABLE);
            Intent chooser = Intent.createChooser(external, getResources().getString(R.string.activity_main_footer_dialog));
            startActivity(chooser);
        }
    }

    /**
     * Gets the next page.
     * When overriding, don't call super, as this function will call getNextPage()
     */
    public void getNextPage() {
        new Articles().getNextPage(this, ++currentPage);
    }

    /**
     * Refreshes the dataset.
     * When overriding, don't call super, as this function will call getArticles(int limit)
     */
    public void refreshView() {
        dataSource.open();
        Articles articles = dataSource.getArticles(false, currentPage);
        dataSource.close();
        adapter.clear();
        for (Article article: articles) {
            adapter.add(article);
        }
    }

    @Override
    public void nextPageLoaded(Articles result) {
        footerView.setEnabled(true);
        checkFooterView();
        if (refresh != null) refresh.setActionView(null);
        if (currentPage == MAXIMUM_PAGED) {
            footerView.setText(R.string.activity_main_footer_over);
            nextPageAllowed = false;
        } else {
            footerView.setText(getResources().getString(R.string.activity_main_footer_text));
        }
        for (Article article : result) {
            adapter.add(article);
        }
        if (result.size() < ArticleDataSource.MAX_ARTICLES_PER_PAGE) {
            nextPageAllowed = false;
        }
        gridView.onLayout(true, 0, 0, gridView.getRight(), gridView.getBottom());
    }


    @Override
    public void nextPageError() {
        footerView.setEnabled(true);
        footerView.setText(R.string.activity_main_footer_text);
        --currentPage;
        if (refresh != null) refresh.setActionView(null);
        articlesError();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount > 0 && (firstVisibleItem + visibleItemCount >= totalItemCount)) {
            final View child = view.getChildAt((adapter.getRealCount() - 1) - firstVisibleItem);
            if (child != null) {
                if (child.getBottom() > view.getBottom() - (adapter.getFooterHeight() / 1.5)) {
                    // Above
                    if (footerView.isShown()) hidefooterView();
                } else {
                    // Below
                    if (!footerView.isShown()) showfooterView();
                }
            }
        }

    }

    public void checkFooterView() {
        if (footerView.isShown()) hidefooterView();
    }

    protected void showfooterView() {
        if(nextPageAllowed == false) {
            if (currentPage == MAXIMUM_PAGED) {
                footerView.setText(R.string.activity_main_footer_over);
            } else if (footerView.isShown()) {
                footerView.setVisibility(TextView.GONE);
                return;
            }
        }
        footerView.setVisibility(TextView.VISIBLE);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
        footerView.startAnimation(fadeInAnimation);
    }

    protected void hidefooterView() {
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_bottom);
        footerView.startAnimation(fadeInAnimation);
        footerView.setVisibility(TextView.GONE);
    }

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
        if (gridView.getChildCount() > 0 && gridView.getLastVisiblePosition() < adapter.getRealCount()) {
            if (footerView.isShown()) footerView.setVisibility(TextView.GONE);
        }
    }

    protected abstract void getData();
    protected abstract void getNewData();

    public void setActionBar() {}

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.article_row_long_press, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Article article = adapter.getItem((int) info.id);
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
        Article article = adapter.getItem(position);
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
        adapter.notifyDataSetChanged();
        return true;
    }

    protected void onUnstarred(int position) {}

    private void startShareIntent(String title, String path) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + "http://www.omgubuntu.co.uk" + path);
        Intent chooser = Intent.createChooser(shareIntent, getResources().getString(R.string.activity_main_share_intent_text));
        startActivity(chooser);
    }

    @Override
    public void articlesLoaded(Articles result) {
        checkFooterView();
        onRefreshComplete();
        dataSource.open();
        if(!result.isEmpty()) dataSource.createArticles(result, true);
        dataSource.clearArticlesOverNumberOfEntries();
        dataSource.close();
        getData();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        imageHandler.setExitTasksEarly(true);
        imageHandler.flush();
    }

    public void setAllAsRead() {
        dataSource.open();
        for (int i = 0; i < adapter.getRealCount(); i++) {
            adapter.getItem(i).setUnread(0);
            dataSource.setArticleToUnread(false, adapter.getItem(i).getPath());
        }
        dataSource.close();
        adapter.notifyDataSetChanged();
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
