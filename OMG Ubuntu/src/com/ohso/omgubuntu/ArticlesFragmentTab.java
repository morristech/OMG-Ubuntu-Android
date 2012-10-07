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

public class ArticlesFragmentTab extends BaseFragment implements OnRefreshListener<ListView>, OnNavigationListener, OnScrollListener {
	private BasePullToRefreshListView listView;
	private final String RSS_URL = "http://www.omgubuntu.co.uk/";
	private ActionBar actionBar;
	private ArrayAdapter<CharSequence> list;
	//private GestureDetector gestureDetector;
	private int savedListPosition = 0;

	public ArticlesFragmentTab() {
		// TODO Auto-generated constructor stub
		setTitle("Articles");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			//If the fragment frame doesn't exist, don't waste time inflating the view
			return null;
		}
		//gestureDetector = new GestureDetector(getActivity(), this);
		listView = new BasePullToRefreshListView(getActivity());
		listView.setOnRefreshListener(this);
		listView.setOnScrollListener(this);
//		mListItems = listView.getResources().getStringArray(R.array.actionbar_categories);
//		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mListItems);
//		listView.setAdapter(mAdapter);
		return listView;
	}




	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		Log.i("OMG!", "Refreshing...");
		new GetLatestArticles().execute();
	}

	private class GetLatestArticles extends AsyncTask<Void, Void, RSSItems> {

		@Override
		protected RSSItems doInBackground(Void... params) {
			try {
				return loadXmlFromNetwork(RSS_URL+"feed");
			}
			catch (IOException e) {
				Log.e("OMG!", getResources().getString(R.string.connection_error)+e.toString());
				return null;
			}
			catch (XmlPullParserException e) {
				Log.e("OMG!", getResources().getString(R.string.xml_error)+e.toString());
				return null;
			}

		}

		protected void onPostExecute(RSSItems result) {
			listView.onRefreshComplete();
			if (result == null || result.size() == 0) {
				Log.d("OMG!","Empty results, not adding!");
				Toast error = Toast.makeText(getActivity(), "Could not refresh feed at this time. Please try again later.", Toast.LENGTH_SHORT);
				try {
					((TextView)((LinearLayout)error.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				error.show();
				return;
			}
			Log.i("OMG!","Result set isn't empty, trying to add to list view");
			ArticleAdapter adapter = new ArticleAdapter(getActivity(), result);
			listView.setAdapter(adapter);
		}
	}

	private RSSItems loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
		InputStream stream = null;
		OMGUbuntuParser omgParser = new OMGUbuntuParser();
		RSSItems articles = null;
		try {
			stream = downloadUrl(urlString);
			articles = omgParser.parse(stream);
		}
		catch (XmlPullParserException e) {
			Log.e("OMG!","XML Exception from loadXmlFromNetwork! "+e.toString());
		}
		catch (IOException e) {
			Log.e("OMG!","IOException from loadXmlFromNetwork "+e.toString());
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}

		return articles;
	}

	private InputStream downloadUrl(String urlString) throws IOException {
		// TODO replace this!
		URL url = new URL ("http://192.168.1.115:5000");
		//URL url = new URL (urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "feedburner"); //Lets us get past WordPress => FeedBurner plugin
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
		if (actionBar == null) {
			actionBar = MainActivity.actionBar;
		}
		actionBar.setTitle(null);
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    if(list == null) {
	    	list = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.actionbar_categories, R.layout.sherlock_spinner_item);
	    	list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
	    }
	    actionBar.setListNavigationCallbacks(list, this);
	    actionBar.setSelectedNavigationItem(savedListPosition);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	 *
	 * We want to disable the overscroll whilst we fling to the top, so it stops at the top of the list.
	 * Otherwise, you'd get a bounce at the top that'll reveal the refresh even though we're not explicitly refreshing yet.
	 *
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_FLING) {
			listView.setMode(Mode.DISABLED);
		}
		if(scrollState == SCROLL_STATE_IDLE) {
			listView.setMode(Mode.BOTH);
		}

	}




























}
