package com.ohso.omgubuntu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SidebarFragment extends ListFragment {
    private OnSidebarClickListener mCallback;
    public static String           sActiveActivity = "Home";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = new ContextThemeWrapper(getActivity(), R.style.SidebarTheme_Styled);
        LayoutInflater localInflator = inflater.cloneInContext(context);

        View v = localInflator.inflate(R.layout.fragment_sidebar, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String name = (String) l.getItemAtPosition(position);
        boolean onActiveActivity = false;
        if (sActiveActivity.equals(name)) onActiveActivity = true;
        Log.i("OMG!", "Got menu item: " + name);
        mCallback.onSidebarItemClicked(name, onActiveActivity);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnSidebarClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSidebarClickListener");
        }
    }

    public interface OnSidebarClickListener {
        public void onSidebarItemClicked(String name, boolean active);
    }
}
