package com.ohso.omgubuntu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainPager extends FragmentPagerAdapter {
    private List<BaseFragment> fragmentTabs = new ArrayList<BaseFragment>(Arrays.asList(
                                                         new CategoriesFragmentTab(),
                                                         new ArticlesFragmentTab(),
                                                         new StarredFragmentTab()));

    public MainPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return fragmentTabs.size();
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragmentTabs.get(arg0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTabs.get(position).getTitle();
    }

    public void getActionBar(int position) {
        fragmentTabs.get(position).getActionBar();
    }
}
