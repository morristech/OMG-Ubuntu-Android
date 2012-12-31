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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SidebarFragment extends SherlockListFragment {
    private OnSidebarClickListener mCallback;
    private static String sActiveFragment = "sidebar_home";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sidebar, container, false);
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
