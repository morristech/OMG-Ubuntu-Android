package com.ohso.omgubuntu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CategoriesFragmentTab extends BaseFragment {
    public CategoriesFragmentTab() { setTitle("Categories"); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.tab_fragment_categories, container, false);
    }
}
