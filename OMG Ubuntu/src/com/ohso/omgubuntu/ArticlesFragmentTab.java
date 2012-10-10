package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.ohso.util.rss.RSSItems;

public class ArticlesFragmentTab extends BaseFragment implements OnRefreshListener<ListView>, OnNavigationListener,
        OnScrollListener {
    private BasePullToRefreshListView  listView;
    private ActionBar                  actionBar;
    private ArrayAdapter<CharSequence> list;
    /* TODO Implement proper list positioning once we can add/remove list items.
     * Easist way is probably to have a db table for visible categories and reference by name
     */
    private int                        savedListPosition = 0;

    public ArticlesFragmentTab() { setTitle("Articles"); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        listView = new BasePullToRefreshListView(getActivity());
        listView.setOnRefreshListener(this);
        listView.setOnScrollListener(this);
        // mListItems = listView.getResources().getStringArray(R.array.actionbar_categories);
        // mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mListItems);
        // listView.setAdapter(mAdapter);
        return listView;
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) { new GetLatestArticles().execute(); }

    private class GetLatestArticles extends AsyncTask<Void, Void, RSSItems> {

        @Override
        protected RSSItems doInBackground(Void... params) {
            try {
                return loadXmlFromNetwork("feed");
            } catch (IOException e) {
                Log.e("OMG!", getResources().getString(R.string.connection_error) + e.toString());
                return null;
            } catch (XmlPullParserException e) {
                Log.e("OMG!", getResources().getString(R.string.xml_error) + e.toString());
                return null;
            }

        }

        protected void onPostExecute(RSSItems result) {
            listView.onRefreshComplete();
            if (result == null || result.size() == 0) {
                Log.d("OMG!", "Empty results, not adding!");
                Toast error = Toast.makeText(getActivity(),
                        "Couldn't refresh the feed. Try again in a few moments.", Toast.LENGTH_SHORT);
                try {
                    ((TextView) ((LinearLayout) error.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                error.show();
                return;
            }
            Log.i("OMG!", "Result set isn't empty, trying to add to list view");
            ArticleAdapter adapter = new ArticleAdapter(getActivity(), result);
            listView.setAdapter(adapter);
        }
    }

    private RSSItems loadXmlFromNetwork(String urlFragment) throws XmlPullParserException, IOException {
        InputStream stream = null;
        OMGUbuntuParser omgParser = new OMGUbuntuParser();
        RSSItems articles = null;
        try {
            stream = downloadUrl(urlFragment);
            articles = omgParser.parse(stream);
        } catch (XmlPullParserException e) {
            Log.e("OMG!", "XML Exception from loadXmlFromNetwork! " + e.toString());
        } catch (IOException e) {
            Log.e("OMG!", "IOException from loadXmlFromNetwork " + e.toString());
        } finally {
            if (stream != null) stream.close();
        }

        return articles;
    }

    private InputStream downloadUrl(String urlFragment) throws IOException {
        // TODO replace this!
        URL url = new URL("http://192.168.1.115:5000");
        //url = new URL(getResources().getString(R.string.rss_base_url)+urlFragment);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if(getResources().getString(R.string.rss_user_agent).length() > 0)
            conn.setRequestProperty("User-Agent", getResources().getString(R.string.rss_user_agent));
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        savedListPosition = itemPosition;
        return true;
    }

    @Override
    public void getActionBar() {
        if (actionBar == null) actionBar = MainActivity.actionBar;

        actionBar.setTitle(null);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        if (list == null) {
            list = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.actionbar_categories,
                    R.layout.sherlock_spinner_item);
            list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        }
        actionBar.setListNavigationCallbacks(list, this);
        actionBar.setSelectedNavigationItem(savedListPosition);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    /*
     * (non-Javadoc)
     *
     * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
     *
     * We want to disable the overscroll whilst we fling to the top, so it stops at the top of the list. Otherwise,
     * you'd get a bounce at the top that'll reveal the refresh even though we're not wanting to refresh.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING) listView.setMode(Mode.DISABLED);
        if (scrollState == SCROLL_STATE_IDLE) listView.setMode(Mode.PULL_DOWN_TO_REFRESH);
    }

}
