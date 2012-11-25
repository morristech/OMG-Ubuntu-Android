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
        TextView description = (TextView) convertView.findViewById(R.id.fragment_settings_row_description);
        LinearLayout widgetContainer = (LinearLayout) convertView.findViewById(R.id.fragment_settings_widget_container);

        title.setText(getItem(position).title);
        description.setText(getItem(position).description);

        if (getItem(position).type.equals("checkbox")) {
            CheckBox box = (CheckBox) mInflater.inflate(R.layout.fragment_settings_checkbox, null);
            boolean value = mSharedPrefs.getBoolean(getItem(position).preference_key,
                    convertView.getResources().getBoolean(getItem(position).default_value_resource));
            box.setChecked(value);
            // TODO Figure out what's going on here
            widgetContainer.removeAllViews();
            widgetContainer.addView(box);


        }

        return convertView;
    }



}
