package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ArticlesFragmentTab extends Fragment implements OnRefreshListener<ListView> {
	private String[] mListItems;
	private ArrayAdapter<String> mAdapter;
	private PullToRefreshListView listView;
	
	private final String RSS_URL = "http://www.omgubuntu.co.uk/";
	static final String KEY_AUTHOR = "author";
	static final String KEY_TITLE = "title";
	static final String KEY_THUMB = "thumb";
	static final String KEY_LINK = "link";

	public ArticlesFragmentTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (container == null) {
			//If the fragment frame doesn't exist, don't waste time inflating the view
			return null;
		}
		listView = new PullToRefreshListView(getActivity());
		listView.setOnRefreshListener(this);
		mListItems = getResources().getStringArray(R.array.actionbar_categories);
		
		// On first refresh, execute onRefresh.
		// On subsequent launches, populate w/saved data, then run update like above.
		// ArrayList<HashMap<String, String>> articleList = new ArrayList<HashMap<String,String>>();
		// new GetLatestArticles().execute();
		
		
		
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mListItems);
		listView.setAdapter(mAdapter);
		return listView;
	}
	
	public static ArticlesFragmentTab newInstance (String title) {
		ArticlesFragmentTab fragmentPage = new ArticlesFragmentTab();
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		
		
		
		fragmentPage.setArguments(bundle);
		return fragmentPage;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		Log.i("OMG!", "Refreshing...");
		// TODO Auto-generated method stub
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
				return;
			}
			Log.i("OMG!","Result set isn't empty, trying to add to list view");
			Log.i("OMG!", result.toString());
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
		URL url = new URL (urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		//Disable when testing
		conn.setRequestProperty("User-Agent", "feedburner"); //Lets us get past WordPress => FeedBurner plugin
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		Log.i("OMG!", "Connecting with the following: "+conn.getRequestProperties().toString());
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}

	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
