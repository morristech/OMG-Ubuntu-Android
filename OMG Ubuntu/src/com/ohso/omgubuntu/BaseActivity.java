package com.ohso.omgubuntu;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * The Class BaseActivity.
 *
 * @author Sam Tran <samvtran@gmail.com>
 */
public abstract class BaseActivity extends SherlockFragmentActivity implements SidebarFragment.OnSidebarClickListener {
    protected boolean         sidebarFragmentActive = false;
    protected FragmentManager fragmentManager       = getSupportFragmentManager();
    public static ActionBar actionBar;

    private int layoutResourceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(getLayoutResourceId());
        } catch (NotFoundException e) {
            throw new NotFoundException(getClass().toString() + " must provide a layout resource id.");
        }
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Gets the layout resource id.
     *
     * @return the layout resource id
     */
    protected int getLayoutResourceId() { return layoutResourceId; }
    protected void setLayoutResourceId(int layoutResourceId) { this.layoutResourceId = layoutResourceId; }

    //protected void setLayoutResourceId(int layoutResourceId) { layoutResourceId = this.layoutResourceId; }

    /**
     * Gets the default ActionBar
     *
     */
    public void getDefaultActionBar() {
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_HOME|
                ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sidebarFragmentActive) sidebarFragmentActive = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // TODO Should this do anything or just be caught to keep the sidebar active?
        Log.i("OMG!", "Orientation changed");
    }

    /**
     * Toggle sidebar fragment.
     *
     */
    protected void toggleSidebarFragment() {
        // TODO This might do something else...one day
        sidebarFragmentActive = !sidebarFragmentActive;
    }

    /**
     * Gets the resource container to add the sidebar to.
     *
     * @return the sidebar resource container
     */
    //protected abstract int getSidebarResourceContainer();

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

    public void onSidebarLostFocus() {
        Log.i("OMG!", "Sidebar lost focus.");
        toggleSidebarFragment();
    }
}
