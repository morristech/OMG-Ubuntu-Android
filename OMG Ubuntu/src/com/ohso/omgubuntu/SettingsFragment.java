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

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SettingsFragment extends SherlockListFragment {
    public static final String NOTIFICATIONS_ENABLED = "com.ohso.omgubuntu.SettingsFragment.NOTIFICATIONS_ENABLED";
    public static final String STARTUP_CHECK_ENABLED = "com.ohso.omgubuntu.SettingsFragment.STARTUP_CHECK_ENABLED";
    private SharedPreferences mSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = getActivity().getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle(getResources().getString(R.string.title_settings));

        View v= inflater.inflate(R.layout.fragment_settings, container, false);

        final List<PreferenceItem> preferenceItems = new ArrayList<PreferenceItem>();
        preferenceItems.add(new PreferenceItem("checkbox", getString(R.string.pref_notifications_enabled_title),
                getString(R.string.pref_notifications_enabled_description),
                NOTIFICATIONS_ENABLED, R.bool.pref_notifications_enabled_default));
        preferenceItems.add(new PreferenceItem("checkbox", getString(R.string.pref_startup_title),
                getString(R.string.pref_startup_description), STARTUP_CHECK_ENABLED, R.bool.pref_startup_default));
        setListAdapter(new SettingsAdapter(getActivity(), R.id.fragment_settings_row_title, preferenceItems));

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        PreferenceItem item = (PreferenceItem) l.getItemAtPosition((int) id);
        if (item.type.equals("checkbox")) {
            Editor editor = mSharedPrefs.edit();
            editor.putBoolean(item.preference_key,
                    !mSharedPrefs.getBoolean(item.preference_key,
                            getResources().getBoolean(item.default_value_resource)));
            editor.commit();
        }
        ((SettingsAdapter) getListAdapter()).notifyDataSetChanged();
        if (item.title.equals(getResources().getString(R.string.pref_notifications_enabled_title))) {
            Log.i("OMG!", "Notifications are now set to " + mSharedPrefs.getBoolean(item.preference_key,
                    getResources().getBoolean(item.default_value_resource)));
            if(mSharedPrefs.getBoolean(item.preference_key,
                    getResources().getBoolean(item.default_value_resource))) { //Notifications enabled
                if (!NotificationService.isNotificationAlarmActive()) {
                    NotificationAlarmGenerator.generateAlarm(getActivity());
                }
            } else { //Notifications disabled
                NotificationAlarmGenerator.cancelAlarm(getActivity());
            }
        } else if (item.title.equals(getResources().getString(R.string.pref_startup_title))) {
            Log.i("OMG!", "Startup check is now set to " + mSharedPrefs.getBoolean(item.preference_key,
                    getResources().getBoolean(item.default_value_resource)));
        }
    }

    public class PreferenceItem {
        public final String type;
        public final String title;
        public final String description;
        public final String preference_key;
        public final int default_value_resource;
        public PreferenceItem(String type, String title, String description,
                String preference_key, int default_value_resource) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.preference_key = preference_key;
            this.default_value_resource = default_value_resource;
        }
    }

}
