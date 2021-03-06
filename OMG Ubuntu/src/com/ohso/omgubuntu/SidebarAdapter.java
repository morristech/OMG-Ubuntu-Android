/*
 * Copyright (C) 2012 - 2013 Ohso Ltd
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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ohso.omgubuntu.SidebarFragment.SidebarItem;
import com.ohso.util.ViewTagger;

public class SidebarAdapter extends ArrayAdapter<SidebarItem> {
    private LayoutInflater mInflater;
    public SidebarAdapter(Context context, int textViewResourceId, List<SidebarItem> sidebar) {
        super(context, textViewResourceId, sidebar);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sidebar_row, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.sidebar_item_title);

            /*
             * The following uses a workaround found here http://code.google.com/p/android/issues/detail?id=18273
             * setTag used a static WeakHashMap in Android < ICS, which led to memory leaks.
             * SparseArray is used in ViewTagger, as it is in Android > ICS
             */
            ViewTagger.setTag(convertView, holder);
        } else {
            holder = (ViewHolder) ViewTagger.getTag(convertView);
        }

        // Gives the active fragment its orange left border
        // notifyDataSetChanged() always calls getView(), so this changes when a click is issued from BaseFragment
        if(SidebarFragment.isActiveFragment(getItem(position).name)) {
            convertView.setBackgroundResource(R.drawable.sidebar_row_active_selector);
        } else {
            convertView.setBackgroundResource(0);
        }
        holder.title.setCompoundDrawablesWithIntrinsicBounds(getItem(position).icon, null, null, null);
        holder.title.setText(getItem(position).title);
        return convertView;
    }


    static class ViewHolder {
        TextView title;
    }
}
