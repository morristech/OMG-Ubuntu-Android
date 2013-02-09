/*
 * Copyright (C) 2012 - 2013 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.omgubuntu.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ohso.omgubuntu.MainActivity;
import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;
import com.ohso.util.UrlFactory;
import com.ohso.util.rss.FeedParser;

public class Article extends BaseTableObject {
    private Context context = OMGUbuntuApplication.getContext();
    private OnArticleLoaded mCallback;
    private String articleTitle;
    private String path;
    private String author;
    private String thumb;
    private long date;
    private int starred;
    private int unread;
    private String summary;
    private String content;
    private long createdAt;
    private String identifier;
    private List<String> categories = new ArrayList<String>();
    public Article() {
        setTitle(null);
        setAuthor(null);
        setPath(null);
        setThumb(null);
        setStarred(0);
        setUnread(1);
        setIdentifier(null);
        setDate(System.currentTimeMillis());
    }

    public void setTitle(String title) { this.articleTitle = title; }
    public String getTitle() { return articleTitle; }

    public void setPath(String path) { this.path = path; }
    public String getPath() { return path; }

    public void setAuthor(String author) { this.author = author; }
    public String getAuthor() { return author; }

    public void setThumb(String thumb) { this.thumb = thumb; }
    public String getThumb() { return thumb; }

    public void setDate(long date) { this.date = date; }
    public long getDate() { return date; }

    public void setStarred(int starred) { this.starred = starred; }
    public int getStarred() { return starred; }
    public boolean isStarred() {
        if (starred == 1) return true;
        else return false;
    }

    public void setUnread(int unread) { this.unread = unread; }
    public int getUnread() { return unread; }
    public boolean isUnread() {
        if (unread == 1) return true;
        else return false;
    }

    public void addCategory(String category) { this.categories.add(category); }
    public List<String> getCategories() { return categories; }

    public void setSummary(String summary) { this.summary = summary; }
    public String getSummary() {
        String formattedSummary = summary;
        if(formattedSummary.length() > 0 && formattedSummary.substring(0,1).matches("\\W")) {
            if (MainActivity.DEVELOPER_MODE) Log.d("OMG!", "Got non-word character in summary. Stripping");
            formattedSummary = formattedSummary.substring(1);
        }
        return formattedSummary;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCreatedAtNow() { this.createdAt = new Date().getTime(); }
    public long getCreatedAt() { return createdAt; }

    @Override
    public void setSQL() {
        title = "article";
        primaryId = "path";
        primaryIdType= "TEXT";
        primaryIdAutoIncrement = false;
        addColumn(new Column("title", "TEXT"));
        addColumn(new Column("author", "TEXT"));
        addColumn(new Column("thumb", "TEXT"));
        addColumn(new Column("date", "INTEGER"));
        addColumn(new Column("starred", "INTEGER"));
        addColumn(new Column("unread", "INTEGER"));
        addColumn(new Column("summary", "TEXT"));
        addColumn(new Column("content", "TEXT"));
        addColumn(new Column("created_at", "INTEGER"));
        addColumn(new Column("identifier", "TEXT"));
    }

    public void getLatest(OnArticleLoaded caller, String path) {
        mCallback = caller;
        if (OMGUbuntuApplication.isNetworkAvailable()) {
            new getArticleAsync().execute(path);
        } else {
            mCallback.articleError();
        }
    }

    private class getArticleAsync extends AsyncTask<String, Void, Article> {
        @Override
        protected Article doInBackground(String... params) {
            String path = params[0];
            if (path == null) return null;
            try {
                return loadXmlFromNetwork(path);
            } catch (IOException e) {
                Log.e("OMG!", context.getResources().getString(R.string.connection_error) + e.toString());
                return null;
            } catch (XmlPullParserException e) {
                Log.e("OMG!", context.getResources().getString(R.string.xml_error) + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Article result) {
            if (result == null) {
                mCallback.articleError();
            } else {
                mCallback.articleLoaded(result);
            }
            super.onPostExecute(result);
        }
    }

    private Article loadXmlFromNetwork(String urlFragment) throws XmlPullParserException, IOException {
        InputStream stream = null;
        FeedParser omgParser = new FeedParser();
        Article article = null;
        try {
            stream = downloadUrl(urlFragment);
            article = omgParser.parseArticle(stream);
        } catch (XmlPullParserException e) {
            Log.e("OMG!", "XML Exception from loadXmlFromNetwork! " + e.toString());
        } catch (IOException e) {
            Log.e("OMG!", "IOException from loadXmlFromNetwork " + e.toString());
        } finally {
            if (stream != null) stream.close();
        }

        return article;
    }

    private InputStream downloadUrl(String urlFragment) throws IOException {
        URL url = new URL(UrlFactory.forArticle(urlFragment));
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

    public interface OnArticleLoaded {
        void articleLoaded(Article result);
        void articleError();
    }

    public static class Compare implements Comparator<Article> {
        @Override
        public int compare(Article lhs, Article rhs) {
            if (lhs.getDate() < rhs.getDate()) {
                return -1;
            } else if (lhs.getDate() > rhs.getDate()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
