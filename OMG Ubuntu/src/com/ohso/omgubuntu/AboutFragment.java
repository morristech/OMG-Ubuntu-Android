package com.ohso.omgubuntu;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;


public class AboutFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle("About OMG! Ubuntu!");
        TextView omgCopy = (TextView) view.findViewById(R.id.activity_about_omg_copy);
        omgCopy.setText(Html.fromHtml(getString(R.string.activity_about_omg_ubuntu)));
        omgCopy.setMovementMethod(LinkMovementMethod.getInstance());
        TextView ohsoCopy = (TextView) view.findViewById(R.id.activity_about_ohso_copy);
        ohsoCopy.setText(Html.fromHtml(getString(R.string.activity_about_ohso)));
        ohsoCopy.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }
}
