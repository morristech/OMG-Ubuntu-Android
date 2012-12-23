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

import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ohso.omgubuntu.data.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private LayoutInflater mInflater;
    private final float height;
    private final float width;

    public CategoryAdapter(Context context, int textViewResourceId, List<Category> objects) {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                context.getResources().getDisplayMetrics());
        width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sherlock_spinner_item, null);
        }

        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText(getItem(position).getTitle());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_dropdown_item, null);
        }

        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setHeight((int) height);
        text.setWidth((int) width);
        text.setText(getItem(position).getTitle());

        return convertView;
    }

}
