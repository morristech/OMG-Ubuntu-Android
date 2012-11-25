package com.ohso.omgubuntu;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class PopupFragment extends SherlockFragment {
    protected WebView popupView;
    private FragmentManager mFragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("OMG!", "onCreateView called on popup");
        mFragmentManager = getActivity().getSupportFragmentManager();
        //popupView = new WebView(getActivity());
        popupView = ((CommentsActivity) getActivity()).popupView;
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

        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.comments_fragment_webview, container, false);
        v.addView(popupView);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i("OMG!", "onAttach called on popup");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("OMG!", "onCreate called on popup");
        super.onCreate(savedInstanceState);
    }

}
