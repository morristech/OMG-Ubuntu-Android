package com.ohso.omgubuntu;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.audiofx.AudioEffect.OnControlStatusChangeListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public abstract class BaseActivity extends SherlockFragmentActivity implements SidebarFragment.OnSidebarItemClickListener {
	private boolean sidebarFragmentActive = false;
	private FragmentManager fragmentManager = getSupportFragmentManager();
	private SidebarFragment sidebarFragment = new SidebarFragment();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
    }
    protected abstract int getLayoutResourceId();

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
	    getSupportMenuInflater().inflate(getMenuId(), menu);
	    return true;
	}
	protected abstract int getMenuId();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				toggleSidebarFragment();
				return true;
			default:
				return onActionBarItemSelected(item);
		}
	}
	protected abstract boolean onActionBarItemSelected(MenuItem item);
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//Surely there's a better way to toggle this?
		if(sidebarFragmentActive) {
			sidebarFragmentActive = false;
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		Log.i("OMG!", "Orientation changed");
		super.onConfigurationChanged(newConfig);
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
			fragmentTransaction.add(getLayoutResourceContainer(), sidebarFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
	protected abstract int getLayoutResourceContainer();
	
	public void onSidebarItemClicked(String name, boolean onActiveActivity) {
		toggleSidebarFragment();
		if(onActiveActivity) {
			Log.i("OMG!", "On active activity. Not leaving.");
			return;
		}
		// TODO: Switch active fragment instead
		if(name.equals("Home")) {
			Intent homeIntent = new Intent(this, MainActivity.class);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			Log.i("OMG!", "Going home.");
			startActivity(homeIntent);
		}
		else if (name.equals("Authors")) {
		}
	}
	
	public interface ActionBarListener {
		public boolean onActionBarItemSelected(MenuItem item);
	}
}
