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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Article.OnArticleLoaded;
import com.ohso.omgubuntu.sqlite.ArticleDataSource;

public class ArticleActivity extends SherlockFragmentActivity implements OnArticleLoaded {
    final Context ctx = this;
    private ActionBar actionBar;
    private ArticleDataSource articleSource;
    private String activeArticle;
    private Article currentArticle;
    private WebView webview;
    private TextView titleView;
    private TextView byline;
    private TextView dateView;
    private MenuItem refresh;

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
        titleView = (TextView) findViewById(R.id.activity_article_title);
        dateView = (TextView) findViewById(R.id.activity_article_date);
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(robotoLight);
        byline = (TextView) findViewById(R.id.activity_article_byline);

        URL article_uri = null;
        try {
           article_uri = new URL(getIntent().getDataString());
        } catch (MalformedURLException e) {
            // We want to know if there's a URL or not
        }

        if (article_uri == null) { // We're opening from the application
            activeArticle = getIntent().getExtras().getString("article_path");
        } else { // We're opening from an external application
            Log.i("OMG!", "Using article "+ article_uri.getPath());
            activeArticle = article_uri.getPath();
        }

        articleSource = new ArticleDataSource(this);

        openArticle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_article, menu);
        refresh = menu.findItem(R.id.activity_article_menu_refresh);
        // TODO show star/unstarred
        if (currentArticle.isStarred()) {
            menu.findItem(R.id.activity_article_menu_starred).setVisible(true);
        } else {
            menu.findItem(R.id.activity_article_menu_unstarred).setVisible(true);
        }
        return true;
    }

    private void createShareIntent(String title, String path) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + "http://www.omgubuntu.co.uk" + path);
        Intent chooser = Intent.createChooser(shareIntent, "Share this article via");
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openArticle() {
        //final ArticleDataSource articleSource = new ArticleDataSource(this);
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
                    DialogFragment fragment = new AlertDialogFragment();
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
    }

    private void setContents(Article article) {
        titleView.setText(article.getTitle());
        byline.setText(article.getAuthor());
        CharSequence date = DateUtils.getRelativeTimeSpanString(article.getDate(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        dateView.setText(date);
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><head><link rel='stylesheet' type='text/css' href='style.css'></head>");
        content.append("<body><div class='post'>");
        content.append(article.getContent());
        content.append("</div></body></html>");
        webview.loadDataWithBaseURL("file:///android_asset/", content.toString(), "text/html", "UTF-8", getResources().getString(R.string.rss_base_url) + article.getPath().substring(1));
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
        Article article = articleSource.updateArticle(result);
        currentArticle = article;
        setContents(article);
        articleSource.close();

    }

    @Override
    public void articleError() {
        DialogFragment fragment = new AlertDialogFragment();
        fragment.show(getSupportFragmentManager(), "article_error");
    }

    private class AlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Couldn't get the article. Would you like to open it in an external browser?");
            builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.omgubuntu.co.uk" + activeArticle));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser(external, "Reopen this article in");
                    startActivity(chooser);
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            return builder.create();
        }

    }


}
