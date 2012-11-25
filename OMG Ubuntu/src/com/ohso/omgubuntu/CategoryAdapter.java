package com.ohso.omgubuntu;

import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ohso.omgubuntu.sqlite.Category;

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
