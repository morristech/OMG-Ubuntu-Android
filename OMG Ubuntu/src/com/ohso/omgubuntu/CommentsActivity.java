/*
 * Copyright (C) 2012 Ohso Ltd
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    public static final String COMMENTS_URL = "com.ohso.omgubuntu.CommentsActivity.COMMENTS_URL";
    public static final String COMMENTS_IDENTIFIER = "com.ohso.omgubuntu.CommentsActivity.COMMENTS_IDENTIFIER";
    private FragmentManager mFragmentManager;

    // Need to hold activity-wide references to the WebViews for setting content and onBackPressed() state
    private WebView commentView;
    private WebView popupView;

    private String mArticlePath;
    private String mIdentifier;

    // We need to make sure a popup that's activated is destroyed instead of just losing focus onBackPressed()
    @Override
    public void onBackPressed() {
        if (popupView != null && !popupView.isFocused()) {
            popupView.destroy();
        }
        super.onBackPressed();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.fragment_comments);
        setTitle("Comments");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mArticlePath = getIntent().getStringExtra(COMMENTS_URL);
        mIdentifier = getIntent().getStringExtra(COMMENTS_IDENTIFIER);
        if (mArticlePath == null) finish();
        mFragmentManager = getSupportFragmentManager();

        // Setting up the default Disqus WebView


        WebViewFragment commentsFragment;

        if (instance != null) {
            commentsFragment = (WebViewFragment) getSupportFragmentManager().findFragmentByTag("comments");
            commentView = commentsFragment.getWebView();
        } else {
            commentsFragment = new WebViewFragment();
            mFragmentManager.beginTransaction().replace(R.id.comments_fragment_container, commentsFragment, "comments").commit();
            commentView = new WebView(this);
            commentsFragment.setWebView(commentView);
        }
        if (commentView == null) commentView = new WebView(this);
        commentView.getSettings().setJavaScriptEnabled(true);
        commentView.getSettings().setSupportMultipleWindows(true);
        commentView.setWebChromeClient(new WebFragmentClient(this));
        commentView.setWebViewClient(new WebClient(this));
        if (instance == null) setContents(mArticlePath);
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
        content.append("var disqus_identifier='" + mIdentifier + " " +
                getResources().getString(R.string.base_url) + "/?p=" + mIdentifier + "';");
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

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            WebViewFragment popupFragment = new WebViewFragment();
            popupView = new WebView(mContext);
            popupView.getSettings().setJavaScriptEnabled(true);
            popupView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onCloseWindow(WebView window) {
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
            trans.commitAllowingStateLoss();
            //trans.commit();
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
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
        public void onPageFinished(WebView view, String url) {
            URL requestUrl = null;
            try {
                requestUrl = new URL(url);
            } catch (MalformedURLException e) {}
            if (requestUrl == null) return;
            if (requestUrl.getHost().equals("disqus.com") && requestUrl.getPath().startsWith("/logout")) {
                setContents(mArticlePath);
            }

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri requestUrl = Uri.parse(url);
            if (requestUrl != null ) {
                if (requestUrl.getHost().equals("disqus.com")) {
                    // Check for /logout and let it go through in a new popup,
                    // then onPageFinish should popbackstack and reload page
                    if(requestUrl.getPath().startsWith("/logout")) {
                        commentView.loadUrl(url);
                        return true;
                    }
                } else if (requestUrl.getHost().equals("redirect.disqus.com")) {
                    // Open a popup - if it leads to an article, finish() and open Article intent
                    // else, popbackstack and open in browser.
                    String alsoOnPath = Uri.parse(requestUrl.getQueryParameter("url")).getPath().toString();
                    alsoOnPath = alsoOnPath.substring(0, alsoOnPath.indexOf(":"));
                    Intent intent = new Intent(mContext, ArticleActivity.class)
                        .putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, alsoOnPath);
                    startActivity(intent);
                    return true;
                } else if (requestUrl.getHost().equals("www.omgubuntu.co.uk")
                        && requestUrl.getPath().startsWith("/2")) {
                    Intent intent = new Intent(mContext, ArticleActivity.class)
                        .putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, requestUrl.getPath());
                    startActivity(intent);
                    return true;
                } else if (requestUrl.getPath().endsWith(".rss")) {
                    // Send to external with warning
                    // Affects: http://omgubuntu.disqus.com/.../latest.rss
                    ExternalLinkFragment.newInstance(requestUrl.toString()).show(mFragmentManager, "external_link");
                    return true;
                }
            }
            return false;
        }

    }

    public static class WebViewFragment extends SherlockFragment {
        private WebView mWebView;

        public WebViewFragment() {
            setRetainInstance(true);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_comments_webview, container, false);
            if (mWebView == null) {
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
                return v;
            } else if (mWebView.getParent() != null) {
                ((RelativeLayout) mWebView.getParent()).removeView(mWebView);
            }
            v.addView(mWebView);
            return v;
        }

        public void setWebView(WebView webView) { mWebView = webView; }

        public WebView getWebView() { return mWebView; }

    }

    public static class ExternalLinkFragment extends DialogFragment {
        public static final String EXTERNAL_LINK_FRAGMENT_LINK =
                "com.ohso.omgubuntu.CommentsActivity.ExternalLinkFragment.EXTERNAL_LINK_FRAGMENT_LINK";

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
            builder.setPositiveButton(getResources().getString(R.string.dialog_fragment_open),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(getExternalLink()));
                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                    Intent chooser = Intent.createChooser(external,
                            getResources().getString(R.string.external_link_dialog));
                    startActivity(chooser);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.dialog_fragment_cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();

        }

    }

}
