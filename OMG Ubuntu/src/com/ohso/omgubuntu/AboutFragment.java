package com.ohso.omgubuntu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;


public class AboutFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle("About OMG! Ubuntu!");
        return view;
    }
}
