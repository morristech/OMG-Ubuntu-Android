package com.ohso.omgubuntu;

import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class CommentsActivity extends SherlockFragmentActivity {
    public static final String COMMENTS_URL = "com.ohso.omgubuntu.commentsUrl";
    private FragmentManager mFragmentManager;

    // Need to hold activity-wide references to the WebViews for setting content and onBackPressed() state
    private WebView commentView;
    private WebView popupView;

    private String mArticlePath;

    // We need to make sure a popup that's activated is destroyed instead of just losing focus onbackpressed()
    @Override
    public void onBackPressed() {
        Log.i("OMG!", "Back pressed.");
        if (popupView != null && !popupView.isFocused()) {
            Log.i("OMG!", "popup destroyed");
            popupView.destroy();
        }
        super.onBackPressed();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.comments_fragment);
        setTitle("Comments");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mArticlePath = getIntent().getStringExtra(COMMENTS_URL);
        mFragmentManager = getSupportFragmentManager();

        // Setting up the default Disqus WebView
        commentView = new WebView(this);
        WebViewFragment commentsView = new WebViewFragment();
        commentsView.setWebView(commentView);

        mFragmentManager.beginTransaction().replace(R.id.comments_fragment_container, commentsView).commit();

        commentView.getSettings().setJavaScriptEnabled(true);
        commentView.getSettings().setSupportMultipleWindows(true);
        commentView.setWebChromeClient(new WebFragmentClient(this));
        commentView.setWebViewClient(new WebClient(this));

        if (mArticlePath == null) finish();
        setContents(mArticlePath);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private void setContents(String articlePath) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><head>");
        content.append("<script type=\"text/javascript\">var disqus_shortname='omgubuntu'; ");
        content.append("var disqus_url='" + getResources().getString(R.string.base_url) + articlePath + "'; ");
        content.append("</script> ");
        content.append("</head>");
        content.append("<body><div id='disqus_thread'>Loading...</div>");
        content.append("<script type=\"text/javascript\" src=\"http://omgubuntu.disqus.com/embed.js\"></script>");
        content.append("</body></html>");
        commentView.loadDataWithBaseURL(getResources().getString(R.string.base_url) + articlePath,
                content.toString(), "text/html", "UTF-8", getResources().getString(R.string.base_url) + articlePath);
    }

    private class WebFragmentClient extends WebChromeClient {
        private Context mContext;
        public WebFragmentClient(Context context) {
            mContext = context;
        }
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d("OMG!", consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Log.i("OMG!", "Creating a window!");
            WebViewFragment popupFragment = new WebViewFragment();
            popupView = new WebView(mContext);
            popupView.getSettings().setJavaScriptEnabled(true);
            popupView.setWebChromeClient(new WebChromeClient() {

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.i("OMG!", consoleMessage.message());
                    return super.onConsoleMessage(consoleMessage);
                }

                @Override
                public void onCloseWindow(WebView window) {
                    Log.i("OMG!", "Popup should close window");
                    mFragmentManager.popBackStack();
                    super.onCloseWindow(window);
                    window.destroy();
                }

            });
            popupView.setWebViewClient(new WebClient(mContext));

            popupFragment.setWebView(popupView);

            ((WebView.WebViewTransport) resultMsg.obj).setWebView(popupFragment.getWebView());
            FragmentTransaction trans = mFragmentManager.beginTransaction();
            trans.add(R.id.comments_fragment_container, popupFragment);
            trans.addToBackStack(null);
            trans.commit();
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.i("OMG!", "Window closed.");
            super.onCloseWindow(window);
            mFragmentManager.popBackStack();
        }

    }

    private class WebClient extends WebViewClient {
        private Context mContext;
        public WebClient(Context context) {
            mContext = context;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i("OMG!", "TEMP onPageStarted " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("OMG!", "Page finished! " + url);
            URL requestUrl = null;
            try {
                requestUrl = new URL(url);
            } catch (MalformedURLException e) {}
            if (requestUrl == null) return;
            if (requestUrl.getHost().equals("disqus.com") && requestUrl.getPath().startsWith("/logout")) {
                Log.i("OMG!", "Logout action, so refreshing now that we're logged out");
                setContents(mArticlePath);
            }

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("OMG!", "shouldOVerrideUrlLoading");
            Uri requestUrl = Uri.parse(url);
            if (requestUrl != null ) {
                Log.i("OMG!", "Request on: " + requestUrl.getHost());
                if (requestUrl.getHost().equals("disqus.com")) {
                    // Check for /logout and let it go through in a new popup, then onPageFinish should popbackstack and reload page
                    if(requestUrl.getPath().startsWith("/logout")) {
                        Log.i("OMG!", "Got logout action!");
                        commentView.loadUrl(url);
                        return true;
                    }
                } else if (requestUrl.getHost().equals("redirect.disqus.com")) {
                    // Open a popup - if it leads to an article, finish() and open Article intent
                    // else, popbackstack and open in browser.
                    String alsoOnPath = Uri.parse(requestUrl.getQueryParameter("url")).getPath().toString();
                    alsoOnPath = alsoOnPath.substring(0, alsoOnPath.indexOf(":"));
                    Log.i("OMG!", "Looking to open: " + alsoOnPath);
                    Intent intent = new Intent(mContext, ArticleActivity.class).putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, alsoOnPath);
                    startActivity(intent);
                    return true;
                } else if (requestUrl.getHost().equals("www.omgubuntu.co.uk") && requestUrl.getPath().startsWith("/2")) {
                    Log.i("OMG!", "We have an article!");
                    Intent intent = new Intent(mContext, ArticleActivity.class).putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, requestUrl.getPath());
                    startActivity(intent);
                    return true;
                } else if (requestUrl.getPath().endsWith(".rss")) {
                    // Send to external with warning
                    /*
                     * Affects: http://omgubuntu.disqus.com/.../latest.rss
                     *
                     */
                    ExternalLinkFragment.newInstance(requestUrl.toString()).show(mFragmentManager, "external_link");
                    return true;
                }
            }
            Log.i("OMG!", "URL: " + url);
            return false;
        }

    }

    public static class WebViewFragment extends SherlockFragment {
        private WebView mWebView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Log.i("OMG!", "onCreateView called");
            RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.comments_fragment_webview, container, false);
            v.addView(mWebView);
            //return mWebView;
            return v;
        }

        public void setWebView(WebView webView) { mWebView = webView; }

        public WebView getWebView() { Log.i("OMG!", "getWebView called"); return mWebView; }

    }

    public static class ExternalLinkFragment extends DialogFragment {
        public static final String EXTERNAL_LINK_FRAGMENT_LINK =
                "com.ohso.omgubuntu.CommentsActivity.ExternalLinkFragmentLink";

        public static ExternalLinkFragment newInstance(String externalUrl) {
            ExternalLinkFragment fragment = new ExternalLinkFragment();
            Bundle args = new Bundle();
            args.putString(EXTERNAL_LINK_FRAGMENT_LINK, externalUrl);
            fragment.setArguments(args);
            return fragment;
        }

        private String getExternalLink() { return getArguments().getString(EXTERNAL_LINK_FRAGMENT_LINK); }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.external_link_error));
            builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(getExternalLink()));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser(external, getResources().getString(R.string.external_link_dialog));
                    startActivity(chooser);
                    //getActivity().finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();

        }

    }

}
