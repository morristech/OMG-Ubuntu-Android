package com.ohso.omgubuntu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.webkit.WebView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CommentsActivity extends SherlockFragmentActivity {
    public static final String COMMENTS_URL = "com.ohso.omgubuntu.commentsUrl";
    public static final String COMMENTS_IDENTIFIER = "com.ohso.omgubuntu.commentsIdentifier";
    private FragmentManager mFragmentManager;

    // Need to hold activity-wide references to the WebViews for setting content and onBackPressed() state
    private WebView commentView;
    protected static WebView popupView;

    protected String articlePath;
    protected String commentIdentifier;

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

        popupView = new WebView(this);
        articlePath = getIntent().getStringExtra(COMMENTS_URL);
        if (articlePath == null) finish();

        commentIdentifier = getIntent().getStringExtra(COMMENTS_IDENTIFIER);
        mFragmentManager = getSupportFragmentManager();

        // Setting up the default Disqus WebView
        CommentsFragment commentsView = new CommentsFragment();

        mFragmentManager.beginTransaction().replace(R.id.comments_fragment_container, commentsView).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.activity_comments_menu_external:
                Intent externalIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(R.string.base_url) + articlePath + "#disqus_thread"));
                startActivity(externalIntent);
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_comments, menu);
        return true;
    }

    /*public static class WebViewFragment extends SherlockFragment {
        private WebView mWebView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Log.i("OMG!", "onCreateView called");
            RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.comments_fragment_webview, container, false);
            v.addView(mWebView);
            return v;
        }

        public void setWebView(WebView webView) { mWebView = webView; }

        public WebView getWebView() { Log.i("OMG!", "getWebView called"); return mWebView; }

    }*/

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
