package com.ohso.omgubuntu;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {
	private boolean sidebarFragmentActive = false;
	private FragmentManager fragmentManager = getSupportFragmentManager();
	private SidebarFragment sidebarFragment = new SidebarFragment();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.actionbar_categories, R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(list, this);
    }

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
	    getSupportMenuInflater().inflate(R.menu.activity_main, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Log.i("OMG!", "Got an option clicked! ");
		switch(item.getItemId()) {
			case android.R.id.home:
				//Log.i("OMG!", "Requesting home!");
				toggleSidebarFragment();
				return true;
			default:
				//Log.i("OMG!", "Regular option caught.");
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		Log.i("OMG!", "Got navigation callback for "+itemId+" at "+itemPosition);
		
		return true;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//Surely there's a better way to toggle this?
		if(sidebarFragmentActive) {
			sidebarFragmentActive = false;
		}
	}
	
	private void toggleSidebarFragment() {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(sidebarFragmentActive) {
			Log.i("OMG!", "Hiding sidebar!");
			sidebarFragmentActive = false;
			//fragmentTransaction.detach(sidebarFragment);
			fragmentManager.popBackStack();
			fragmentTransaction.commit();
		}
		else {
			Log.i("OMG!", "Showing sidebar!");
			sidebarFragmentActive = true;
			fragmentTransaction.add(R.id.activity_main_container, sidebarFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
    
}
