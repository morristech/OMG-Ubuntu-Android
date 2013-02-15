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
package com.ohso.omgubuntu;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.CommentsActivity.ExternalLinkFragment;
import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.Article.OnArticleLoaded;
import com.ohso.omgubuntu.data.ArticleDataSource;

public class ArticleActivity extends SherlockFragmentActivity implements OnArticleLoaded {
    private ActionBar actionBar;
    private ArticleDataSource articleSource;
    private String activeArticle;
    private Article currentArticle;
    private WebView webview;
    private MenuItem refresh;

    public static final String INTERNAL_ARTICLE_PATH_INTENT = "com.ohso.omgubuntu.ArticleActivity.ARTICLE_PATH";
    public static final String LATEST_ARTICLE_INTENT = "com.ohso.omgubuntu.ArticleActivity.LATEST_ARTICLE";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_article);
        webview = (WebView) findViewById(R.id.activity_article_webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new ArticleWebViewClient(this));

        articleSource = new ArticleDataSource(this);

        URL article_uri = null;
        try {
           article_uri = new URL(getIntent().getDataString());
        } catch (MalformedURLException e) {}

        if (article_uri == null) { // We're opening from the application
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if(getIntent().getExtras().getBoolean(LATEST_ARTICLE_INTENT, false)) { //Latest article intent
                articleSource.open();
                Article article = articleSource.getLatestArticle(false);
                articleSource.close();
                if (article == null) finish();
                activeArticle = article.getPath();
            } else {
                activeArticle = getIntent().getExtras().getString(INTERNAL_ARTICLE_PATH_INTENT);
                SharedPreferences sharedPref = getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
                String lastPath = sharedPref.getString(NotificationService.LAST_NOTIFIED_PATH, null);
                if (lastPath != null && lastPath.equals(activeArticle)) {
                    Editor editor = sharedPref.edit();
                    editor.putString(NotificationService.LAST_NOTIFIED_PATH, null);
                    editor.commit();
                    ArticlesWidgetProvider.notifyUpdate(0);
                }
            }
        } else { // We're opening from an external application
            activeArticle = article_uri.getPath();
        }
        openArticle();
    }

    private class ArticleWebViewClient extends WebViewClient {
        private Context mContext;
        public ArticleWebViewClient(Context context) {
            mContext = context;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri requestUrl = Uri.parse(url);
            if((requestUrl.getScheme() != null) && requestUrl.getScheme().equals("internal")
                    && (requestUrl.getHost() != null) && requestUrl.getHost().equals("app-comments")) {
                Intent commentIntent = new Intent(mContext, CommentsActivity.class);
                commentIntent.putExtra(CommentsActivity.COMMENTS_URL, currentArticle.getPath());
                commentIntent.putExtra(CommentsActivity.COMMENTS_IDENTIFIER, currentArticle.getIdentifier());
                startActivity(commentIntent);
            } else if ((requestUrl.getHost() != null) && requestUrl.getHost().equals("www.omgubuntu.co.uk") &&
                    (requestUrl.getPath() != null) && requestUrl.getPath().startsWith("/2")) {
                String requestPath = requestUrl.getPath();
                if (requestPath.substring(requestPath.length() - 1).equals("/")) {
                    requestPath = requestPath.substring(0, requestPath.length() - 1);
                }
                Intent articleIntent = new Intent(mContext, ArticleActivity.class);
                articleIntent.putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, requestPath);
                startActivity(articleIntent);
            } else {
                ExternalLinkFragment fragment = ExternalLinkFragment.newInstance(url);
                fragment.show(getSupportFragmentManager(), "external_article_link");
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_article, menu);
        refresh = menu.findItem(R.id.activity_article_menu_refresh);
        if (currentArticle != null && currentArticle.isStarred()) {
            menu.findItem(R.id.activity_article_menu_starred).setVisible(true);
        } else {
            menu.findItem(R.id.activity_article_menu_unstarred).setVisible(true);
        }
        return true;
    }

    private void createShareIntent(String title, String path) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + getResources().getString(R.string.base_url) + path);
        Intent chooser = Intent.createChooser(shareIntent, getResources().getString(R.string.activity_main_share_intent_text));
        startActivity(chooser);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.activity_article_menu_refresh:
                refreshArticle();
                return true;
            case R.id.activity_article_menu_share:
                createShareIntent(currentArticle.getTitle(), currentArticle.getPath());
                return true;
            case R.id.activity_article_menu_starred: //unstarring
                currentArticle.setStarred(0);
                articleSource.open();
                articleSource.setArticleToStarred(false, currentArticle.getPath());
                articleSource.close();
                invalidateOptionsMenu();
                return true;
            case R.id.activity_article_menu_unstarred: //starring
                currentArticle.setStarred(1);
                articleSource.open();
                articleSource.setArticleToStarred(true, currentArticle.getPath());
                articleSource.close();
                invalidateOptionsMenu();
                return true;
            case R.id.activity_article_menu_comments:
                Intent commentIntent = new Intent(this, CommentsActivity.class);
                commentIntent.putExtra(CommentsActivity.COMMENTS_URL, currentArticle.getPath());
                commentIntent.putExtra(CommentsActivity.COMMENTS_IDENTIFIER, currentArticle.getIdentifier());
                startActivity(commentIntent);
                return true;
            case R.id.activity_article_menu_external:
                Intent externalIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(R.string.base_url) + currentArticle.getPath()));
                startActivity(externalIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openArticle() {
        articleSource.open();
        Article article = articleSource.getArticle(activeArticle, true);
        if (article == null) {
            OnArticleLoaded newArticleLoad = new OnArticleLoaded() {
                @Override
                public void articleLoaded(Article article) {
                    currentArticle = article;
                    article.setUnread(0);
                    articleSource.createArticle(article, true, true);
                    setContents(article);
                    articleSource.close();
                }
                @Override
                public void articleError() {
                    DialogFragment fragment = AlertDialogFragment.newInstance(activeArticle);
                    fragment.show(getSupportFragmentManager(), "article_error");
                    articleSource.close();
                }
            };
            new Article().getLatest(newArticleLoad, activeArticle);
        } else {
            currentArticle = article;
            articleSource.close();
            setContents(article);
            if (article.isUnread()) {
                articleSource.open();
                articleSource.setArticleToUnread(false, article.getPath());
                articleSource.close();
            }

        }
        invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setContents(Article article) {
        //titleView.setText(article.getTitle());
        //byline.setText(article.getAuthor());
        CharSequence date = DateUtils.getRelativeTimeSpanString(article.getDate(), new Date().getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        //dateView.setText(date);
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><head><link rel='stylesheet' type='text/css' href='style.css'></head><body>");
        content.append("<h1 id='article-title'>" + article.getTitle() + "</h1>");
        content.append("<div class='metadata'>");
        content.append("<span class='author'>"+ article.getAuthor() +"</span>");
        content.append("<span class='date'>" + date + "</span></div>");
        content.append("<div class='post'>");
        content.append(article.getContent());
        content.append("<h2 class='internal-comments-link'><a href='internal://app-comments'>"+
                getResources().getString(R.string.activity_article_comment_text) +"</a></h2></div></body></html>");
        webview.loadDataWithBaseURL("file:///android_asset/", content.toString(), "text/html", "UTF-8",
                getResources().getString(R.string.base_url) + article.getPath());
    }

    private void refreshArticle() {
        refresh.setActionView(R.layout.refresh_menu_item);
        currentArticle.getLatest(this, activeArticle);
    }

    @Override
    public void articleLoaded(Article result) {
        refresh.setActionView(null);
        ArticleDataSource articleSource = new ArticleDataSource(this);
        articleSource.open();
        result.setUnread(0);
        Article article = articleSource.updateArticle(result);
        currentArticle = article;
        setContents(article);
        articleSource.close();

    }

    @Override
    public void articleError() {
        DialogFragment fragment = AlertDialogFragment.newInstance(activeArticle);
        fragment.show(getSupportFragmentManager(), "article_error");
    }

    public static class AlertDialogFragment extends DialogFragment {
        private static final String ACTIVE_ARTICLE = "com.ohso.omgubuntu.ArticleActivity.activeArticle";

        public static AlertDialogFragment newInstance(String activeArticle) {
            AlertDialogFragment fragment = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString(ACTIVE_ARTICLE, activeArticle);
            fragment.setArguments(args);
            return fragment;
        }

        public String getArticlePath() {
            return getArguments().getString(ACTIVE_ARTICLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.article_fetch_error));
            builder.setPositiveButton(getResources().getString(R.string.dialog_fragment_open),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getResources().getString(R.string.base_url) + getArticlePath()));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser
                            (external, getResources().getString(R.string.article_fetch_error_dialog));
                    startActivity(chooser);
                    getActivity().finish();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.dialog_fragment_cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            return builder.create();
        }
    }
}
