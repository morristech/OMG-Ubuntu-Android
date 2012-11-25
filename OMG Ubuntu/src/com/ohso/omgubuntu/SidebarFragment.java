package com.ohso.omgubuntu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SidebarFragment extends SherlockListFragment {
    private OnSidebarClickListener mCallback;
    private static String           sActiveFragment = "sidebar_home";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = new ContextThemeWrapper(getActivity(), R.style.SidebarTheme_Styled);
        LayoutInflater localInflator = inflater.cloneInContext(context);
        View v = localInflator.inflate(R.layout.fragment_sidebar, container, false);
        final List<SidebarItem> sidebarItems = new ArrayList<SidebarItem>();
        TypedArray items = getResources().obtainTypedArray(R.array.sidebar_list);
        for (int i = 0; i < items.length(); i++) {
            int id = items.getResourceId(i, 0);
            TypedArray sidebarItem = getResources().obtainTypedArray(id);
            sidebarItems.add(new SidebarItem(getResources().getResourceEntryName(id), sidebarItem));

        }
        SidebarAdapter sidebarAdapter = new SidebarAdapter(getActivity(), R.id.sidebar_item_title, sidebarItems);
        setListAdapter(sidebarAdapter);
        ((ListView) v.findViewById(android.R.id.list)).setSelector(R.drawable.list_view_selector);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        SidebarItem item = (SidebarItem) l.getItemAtPosition(position);
        boolean onActiveActivity = false;
        if (isActiveFragment(item.name)) onActiveActivity = true;
        mCallback.onSidebarItemClicked(item.name, onActiveActivity);

    }

    public void setActiveFragment(String name) {
        sActiveFragment = name;
        ((SidebarAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public String getActiveFragment() {
        return sActiveFragment;
    }

    public static boolean isActiveFragment(String name) {
        return sActiveFragment.equals(name);
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

    public class SidebarItem {
        public final String name;
        public final String title;
        public final Drawable icon;
        public SidebarItem(String name, TypedArray arrayRes) {
            this.name = name;
            title = arrayRes.getString(0);
            icon = arrayRes.getDrawable(1);
        }
    }
}
