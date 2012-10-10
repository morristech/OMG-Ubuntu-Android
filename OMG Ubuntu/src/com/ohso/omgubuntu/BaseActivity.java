package com.ohso.omgubuntu;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseActivity.
 *
 * @author Sam Tran <samvtran@gmail.com>
 */
public abstract class BaseActivity extends SherlockFragmentActivity implements SidebarFragment.OnSidebarClickListener {
    private boolean         sidebarFragmentActive = false;
    private FragmentManager fragmentManager       = getSupportFragmentManager();
    private SidebarFragment sidebarFragment       = new SidebarFragment();
    public static ActionBar actionBar;

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        actionBar = getSupportActionBar();
        //actionBar.setCustomView(R.layout.test);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Gets the default ActionBar
     *
     */
    public void getDefaultActionBar() {
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_HOME|
                ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    /**
     * Action bar item selected.
     *
     * @param item MenuItem corresponding to the actionBar item selected.
     * @return true (consumed selection)
     */
    public boolean actionBarItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        Log.i("OMG!", "Something pressed in home view!");
        return true;
    }

    /**
     * Gets the layout resource id.
     *
     * @return the layout resource id
     */
    protected abstract int getLayoutResourceId();

    /*
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(getMenuId(), menu);
        return true;
    }

    /**
     * Gets the id for the ActionBar's menu.
     *
     * @return the menu id
     */
    protected abstract int getMenuId();

    /*
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleSidebarFragment();
                return true;
            default:
                return actionBarItemSelected(item);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sidebarFragmentActive) {
            sidebarFragmentActive = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // TODO Auto-generated method stub
        Log.i("OMG!", "Orientation changed");
    }

    /**
     * Toggle sidebar fragment.
     */
    private void toggleSidebarFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_left, 0, 0, R.anim.slide_out_to_left);
        if (sidebarFragmentActive) {
            sidebarFragmentActive = false;
            fragmentManager.popBackStack();
            fragmentTransaction.commit();
        } else {
            sidebarFragmentActive = true;
            fragmentTransaction.add(getSidebarResourceContainer(), sidebarFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    /**
     * Gets the resource container to add the sidebar to.
     *
     * @return the sidebar resource container
     */
    protected abstract int getSidebarResourceContainer();

    /*
     * (non-Javadoc)
     *
     * @see com.ohso.omgubuntu.SidebarFragment.OnSidebarClickListener#onSidebarItemClicked(java.lang.String, boolean)
     */
    public void onSidebarItemClicked(String name, boolean onActiveActivity) {
        Log.i("OMG!", "Got sidebar click.");
        toggleSidebarFragment();
        if (onActiveActivity) {
            Log.i("OMG!", "On active activity. Not leaving.");
            return;
        }

        // TODO: Switch active fragment instead
        if (name.equals("Home")) {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            Log.i("OMG!", "Going home.");
            startActivity(homeIntent);
        } else if (name.equals("Authors")) {}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ohso.omgubuntu.SidebarFragment.OnSidebarClickListener#onSidebarLostFocus()
     */
    public void onSidebarLostFocus() {
        Log.i("OMG!", "Sidebar lost focus.");
        toggleSidebarFragment();
    }
}
