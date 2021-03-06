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
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ohso.omgubuntu.SettingsFragment.PreferenceItem;

public class SettingsAdapter extends ArrayAdapter<PreferenceItem> {
    private LayoutInflater mInflater;
    private SharedPreferences mSharedPrefs;
    public SettingsAdapter(Context context, int textViewResourceId, List<PreferenceItem> objects) {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        mSharedPrefs = context.getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.fragment_settings_row, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.fragment_settings_row_title);
        title.setTextColor(convertView.getResources().getColor(android.R.color.primary_text_light));
        TextView description = (TextView) convertView.findViewById(R.id.fragment_settings_row_description);
        description.setTextColor(convertView.getResources().getColor(android.R.color.primary_text_light));
        LinearLayout widgetContainer = (LinearLayout) convertView.findViewById(R.id.fragment_settings_widget_container);

        title.setText(getItem(position).title);
        description.setText(getItem(position).description);

        if (getItem(position).type.equals("checkbox")) {
            CheckBox box = (CheckBox) mInflater.inflate(R.layout.fragment_settings_checkbox, null);
            boolean value = mSharedPrefs.getBoolean(getItem(position).preference_key,
                    convertView.getResources().getBoolean(getItem(position).default_value_resource));
            box.setChecked(value);

            if(getItem(position).dependency != -1) {
                final PreferenceItem dependency = getItem(getItem(position).dependency);
                boolean dependencyValue = mSharedPrefs.getBoolean(dependency.preference_key,
                        convertView.getResources().getBoolean(dependency.default_value_resource));
                if (!dependencyValue) {
                    title.setTextColor(convertView.getResources().getColor(R.color.Grey_9));
                    description.setTextColor(convertView.getResources().getColor(R.color.Grey_9));
                    box.setEnabled(false);
                }
            }

            // TODO Figure out what's going on here
            widgetContainer.removeAllViews();
            widgetContainer.addView(box);
        }
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position).dependency != -1) {
            final PreferenceItem dependency = getItem(getItem(position).dependency);
            boolean dependencyValue = mSharedPrefs.getBoolean(dependency.preference_key,
                    getContext().getResources().getBoolean(dependency.default_value_resource));
            if (dependencyValue) {
                return super.isEnabled(position);
            } else {
                return false;
            }

        } else {
            return super.isEnabled(position);
        }
    }
}
