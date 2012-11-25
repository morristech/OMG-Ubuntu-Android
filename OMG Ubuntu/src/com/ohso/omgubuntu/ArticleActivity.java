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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.CommentsActivity.ExternalLinkFragment;
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
    public static final String INTERNAL_ARTICLE_PATH_INTENT = "article_path";

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

        titleView = (TextView) findViewById(R.id.activity_article_title);
        dateView = (TextView) findViewById(R.id.activity_article_date);
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(robotoLight);
        byline = (TextView) findViewById(R.id.activity_article_byline);

        URL article_uri = null;
        try {
           article_uri = new URL(getIntent().getDataString());
        } catch (MalformedURLException e) {}

        if (article_uri == null) { // We're opening from the application
            activeArticle = getIntent().getExtras().getString(INTERNAL_ARTICLE_PATH_INTENT);
            SharedPreferences sharedPref = getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
            String lastPath = sharedPref.getString(NotificationService.LAST_NOTIFIED_PATH, null);
            if (lastPath != null && lastPath.equals(activeArticle)) {
                Editor editor = sharedPref.edit();
                editor.putString(NotificationService.LAST_NOTIFIED_PATH, null);
                editor.commit();
                ArticlesWidgetProvider.notifyUpdate(this, 0);
            }
        } else { // We're opening from an external application
            activeArticle = article_uri.getPath();
        }

        articleSource = new ArticleDataSource(this);

        openArticle();
    }

    private class ArticleWebViewClient extends WebViewClient {
        private Context mContext;
        public ArticleWebViewClient(Context context) {
            mContext = context;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(Uri.parse(url).getScheme().equals("internal") && Uri.parse(url).getHost().equals("app-comments")) {
                Intent commentIntent = new Intent(mContext, CommentsActivity.class);
                commentIntent.putExtra(CommentsActivity.COMMENTS_URL, currentArticle.getPath());
                startActivity(commentIntent);
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

    private void setContents(Article article) {
        titleView.setText(article.getTitle());
        byline.setText(article.getAuthor());
        CharSequence date = DateUtils.getRelativeTimeSpanString(article.getDate(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        dateView.setText(date);
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><head><link rel='stylesheet' type='text/css' href='style.css'></head>");
        content.append("<body><div class='post'>");
        content.append(article.getContent());
        content.append("<h2 class='internal-comments-link'><a href='internal://app-comments'>"+
                getResources().getString(R.string.activity_article_comment_text) +"</a></h2></div></body></html>");
        webview.loadDataWithBaseURL("file:///android_asset/", content.toString(), "text/html", "UTF-8", getResources().getString(R.string.base_url) + article.getPath());
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
            builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.base_url)
                            + getArticlePath()));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser(external, getResources().getString(R.string.article_fetch_error_dialog));
                    startActivity(chooser);
                    getActivity().finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            return builder.create();
        }

    }

 /*   public static class ExternalLinkFragment extends DialogFragment {
        private static final String EXTERNAL_LINK = "com.ohso.omgubuntu.ArticleActivity.externalLink";

        public static ExternalLinkFragment newInstance(String externalLink) {
            ExternalLinkFragment fragment = new ExternalLinkFragment();
            Bundle args = new Bundle();
            args.putString(EXTERNAL_LINK, externalLink);
            fragment.setArguments(args);
            return fragment;
        }

        public String getExternalLink() {
            return getArguments().getString(EXTERNAL_LINK);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.article_external_alert));
            builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(getExternalLink()));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser(external, getResources().getString(R.string.article_external_link_dialog));
                    startActivity(chooser);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            return builder.create();
        }

    }*/


}
