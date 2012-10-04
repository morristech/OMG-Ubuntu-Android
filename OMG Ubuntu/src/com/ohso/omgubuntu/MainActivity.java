package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends BaseActivity implements ActionBar.OnNavigationListener {	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        MainPager pagerAdapter = new MainPager(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        
        //TODO This stuff should move to individual fragments
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.actionbar_categories, R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(list, this);
    }
    
	@Override
	protected boolean onActionBarItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Log.i("OMG!", "Something pressed in home view!");
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		Log.i("OMG!", "Got navigation callback for "+itemId+" at "+itemPosition);
		
		return true;
	}

	@Override
	protected int getLayoutResourceId() {
		// TODO Auto-generated method stub
		return R.layout.activity_main;
	}

	@Override
	protected int getMenuId() {
		// TODO Auto-generated method stub
		return R.menu.activity_main;
	}

	@Override
	protected int getLayoutResourceContainer() { return R.id.activity_main_container; }
}
