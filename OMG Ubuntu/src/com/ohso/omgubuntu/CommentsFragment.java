package com.ohso.omgubuntu;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.CommentsActivity.ExternalLinkFragment;

public class CommentsFragment extends SherlockFragment {
    private String articlePath;
    private String commentIdentifier;
    private WebView commentView;
    //private WebView popupView;
    private FragmentManager mFragmentManager;
    public CommentsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        articlePath = ((CommentsActivity) getActivity()).articlePath;
        commentIdentifier = ((CommentsActivity) getActivity()).commentIdentifier;

        mFragmentManager = getActivity().getSupportFragmentManager();

        commentView = new WebView(getActivity());
        commentView.getSettings().setJavaScriptEnabled(true);
        commentView.getSettings().setSupportMultipleWindows(true);
        commentView.setWebChromeClient(new WebFragmentClient(getActivity()));
        commentView.setWebViewClient(new WebClient(getActivity()));

        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.comments_fragment_webview, container, false);
        v.addView(commentView);
        setContents(articlePath, commentIdentifier);
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.activity_comments_menu_refresh:
                setContents(articlePath, commentIdentifier);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setContents(String articlePath, String identifier) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><head>");
        content.append("<script type=\"text/javascript\">var disqus_shortname='omgubuntu'; ");
        content.append("var disqus_url='" + getResources().getString(R.string.base_url) + articlePath + "'; ");
        content.append("var disqus_identifier='" + identifier + " "
                + getResources().getString(R.string.base_url) + "/?p=" + identifier + "'; ");
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
            PopupFragment popupFragment = new PopupFragment();
            FragmentTransaction trans = mFragmentManager.beginTransaction();
            trans.add(R.id.comments_fragment_container, popupFragment);
            trans.addToBackStack(null);
            trans.commit();

            Log.i("OMG!", "Accessing popupView now");
            ((WebView.WebViewTransport) resultMsg.obj).setWebView(CommentsActivity.popupView);

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
                setContents(articlePath, commentIdentifier);
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

}
