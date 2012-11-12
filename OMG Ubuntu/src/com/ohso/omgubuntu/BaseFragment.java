package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

/**
 * @author Sam Tran <samvtran@gmail.com>
 */
public class BaseFragment extends Fragment implements OnScrollListener, OnTouchListener {
    public String title;
    protected BasePullToRefreshListView listView;

    public BaseFragment() { setTitle(null); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // If the fragment frame doesn't exist, don't waste time inflating the view
        if (container == null) return null;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; }

    public void getActionBar() {
        try {
            ((BaseActivity)getActivity()).getDefaultActionBar();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must extend BaseActivity.");
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    /*
     * Disable the overscroll whilst flinging to the top, so it stops at the top of the list
     * intead of triggering a refresh accidentally.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING) listView.setMode(Mode.DISABLED);
        if (scrollState == SCROLL_STATE_IDLE) listView.setMode(Mode.PULL_DOWN_TO_REFRESH);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getActivity() instanceof BaseActivity) {
            if (((BaseActivity) getActivity()).sidebarFragmentActive) {
                ((BaseActivity) getActivity()).toggleSidebarFragment();
                return true;
            } else if (!((BaseActivity) getActivity()).sidebarFragmentTransitionComplete) {
                return true;
            }
        }
        return false;
    }
}
