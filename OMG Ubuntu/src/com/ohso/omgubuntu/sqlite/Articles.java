package com.ohso.omgubuntu.sqlite;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;
import com.ohso.util.rss.FeedParser;

public class Articles extends ArrayList<Article> {
    private static final long serialVersionUID = 1L;
    private Context context = OMGUbuntuApplication.getContext();
    private OnArticlesLoaded mCallback;

    public Articles() {}

    public void getLatest(OnArticlesLoaded caller) {
        mCallback = caller;
        new getArticlesAsync().execute();
    }

    private class getArticlesAsync extends AsyncTask<Void, Void, Articles> {
        @Override
        protected Articles doInBackground(Void... params) {
            try {
                return loadXmlFromNetwork("feed");
            } catch (IOException e) {
                Log.e("OMG!", context.getResources().getString(R.string.connection_error) + e.toString());
                return null;
            } catch (XmlPullParserException e) {
                Log.e("OMG!", context.getResources().getString(R.string.xml_error) + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Articles result) {
            if(result == null) mCallback.articlesError();
            else mCallback.articlesLoaded(result);
            super.onPostExecute(result);
        }
    }

    private Articles loadXmlFromNetwork(String urlFragment) throws XmlPullParserException, IOException {
        InputStream stream = null;
        FeedParser omgParser = new FeedParser();
        Articles articles = null;
        try {
            stream = downloadUrl(urlFragment);
            articles = omgParser.parseArticles(stream);
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
        //URL url = new URL("http://192.168.1.115:5000");
        URL url = new URL(context.getResources().getString(R.string.rss_base_url)+ urlFragment + URLDecoder.decode(context.getResources().getString(R.string.rss_query_params), "UTF-8"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if(context.getResources().getString(R.string.rss_user_agent).length() > 0) {
            conn.setRequestProperty("User-Agent", context.getResources().getString(R.string.rss_user_agent));
        }
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    public interface OnArticlesLoaded {
        void articlesLoaded(Articles result);
        void articlesError();
    }
}
