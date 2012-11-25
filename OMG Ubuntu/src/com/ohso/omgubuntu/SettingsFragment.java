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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;


public class SettingsFragment extends SherlockListFragment {
    public static final String NOTIFICATIONS_ENABLED = "notifications_enabled";
    private SharedPreferences mSharedPrefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = getActivity().getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle("Settings");

        View v= inflater.inflate(R.layout.fragment_settings, null);

        final List<PreferenceItem> preferenceItems = new ArrayList<PreferenceItem>();
        preferenceItems.add(new PreferenceItem("checkbox", getString(R.string.pref_notifications_enabled_title),
                getString(R.string.pref_notifications_enabled_description),
                NOTIFICATIONS_ENABLED, R.bool.pref_notifications_enabled_default));
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
                    !mSharedPrefs.getBoolean(item.preference_key, getResources().getBoolean(item.default_value_resource)));
            editor.commit();
        }
        Log.i("OMG!", "Notifications are now set to " + mSharedPrefs.getBoolean(item.preference_key, getResources().getBoolean(item.default_value_resource)));
        ((ArrayAdapter<PreferenceItem>) getListAdapter()).notifyDataSetChanged();
        if (item.title.equals(getResources().getString(R.string.pref_notifications_enabled_title))) {
            if(mSharedPrefs.getBoolean(item.preference_key, getResources().getBoolean(item.default_value_resource))) { //Notifications enabled
                if (!NotificationService.isNotificationAlarmActive()) {
                    NotificationAlarmGenerator.generateAlarm(getActivity());
                }
            } else { //Notifications disabled
                NotificationAlarmGenerator.cancelAlarm(getActivity());
            }
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