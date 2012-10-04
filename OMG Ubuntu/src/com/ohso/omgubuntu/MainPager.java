package com.ohso.omgubuntu;
import java.util.ArrayList;
import java.util.Arrays;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


public class MainPager extends FragmentPagerAdapter {
	private static final int NUMBER_OF_PAGES = 2;
	private ArrayList<Fragment> fragmentTabs = new ArrayList<Fragment>(Arrays.asList(
		ArticlesFragmentTab.newInstance("Articles"),
		AuthorsFragmentTab.newInstance("Authors")
	));
	public MainPager(FragmentManager fm) { super(fm); }

	@Override
	public int getCount() { return NUMBER_OF_PAGES; }

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		//super.destroyItem(container, position, object);
		Log.i("OMG!", "Destroying a view: "+object.toString());
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public Fragment getItem(int arg0) {	return fragmentTabs.get(arg0); }
}
