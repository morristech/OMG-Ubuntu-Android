package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.omgubuntu.BaseActivity.ActionBarListener;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends BaseActivity implements OnNavigationListener, ViewPager.OnPageChangeListener, ActionBarListener, OnGestureListener {
	public static ActionBar actionBar;
	public static MainPager pagerAdapter;
	private GestureDetector gestureDetector;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        gestureDetector = new GestureDetector(this, this);

        pagerAdapter = new MainPager(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
        viewPager.setAdapter(pagerAdapter);


        TabPageIndicator tabIndicator = (TabPageIndicator)findViewById(R.id.activity_main_tabs);
        tabIndicator.setViewPager(viewPager);
        tabIndicator.setOnPageChangeListener(this);
        tabIndicator.setCurrentItem(1);
    }

	public boolean onActionBarItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Log.i("OMG!", "Something pressed in home view!");
		return true;
	}

	public void onGetDefaultActionBar() {
		actionBar.setTitle(R.string.app_name);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	protected int getLayoutResourceId() { return R.layout.activity_main; }


	// TODO capture onFling and disable overscroll until we're finished.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//super.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}


	@Override
	protected int getMenuId() {	return R.menu.activity_main; }

	@Override
	protected int getSidebarResourceContainer() { return R.id.activity_main_super; }

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int arg0) { pagerAdapter.getActionBar(arg0); }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i("OMG!", "Got fling!");
		return false;
	}

}
