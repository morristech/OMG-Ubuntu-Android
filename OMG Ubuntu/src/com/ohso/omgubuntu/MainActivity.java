package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends BaseActivity implements OnNavigationListener, ViewPager.OnPageChangeListener {
    public static MainPager pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);


        pagerAdapter = new MainPager(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
        viewPager.setAdapter(pagerAdapter);

        TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.activity_main_tabs);
        tabIndicator.setViewPager(viewPager);
        tabIndicator.setOnPageChangeListener(this);
        tabIndicator.setCurrentItem(1);
    }

    protected int getLayoutResourceId() { return R.layout.activity_main; }

    @Override
    protected int getMenuId() { return R.menu.activity_main; }

    @Override
    protected int getSidebarResourceContainer() { return R.id.activity_main_super; }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO actually implement this at some point
        return false;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}

    @Override
    public void onPageSelected(int arg0) {
        pagerAdapter.getActionBar(arg0);
    }

}
