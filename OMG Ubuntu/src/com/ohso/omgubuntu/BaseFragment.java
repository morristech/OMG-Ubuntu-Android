package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Sam Tran <samvtran@gmail.com>
 */
public class BaseFragment extends Fragment {
    private String title;

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
}
