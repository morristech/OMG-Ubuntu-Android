package com.ohso.omgubuntu;

import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.util.ImageHandler;

/**
 * The Class BaseActivity that contains the default sidebar for the application
 *
 * @author Sam Tran <samvtran@gmail.com>
 */
public abstract class BaseActivity extends SherlockFragmentActivity implements SidebarFragment.OnSidebarClickListener {
    protected static boolean         sidebarFragmentActive = false;

 // Useful for disabling certain actions whilst the sidebar is still transitioning
    public boolean         sidebarFragmentTransitionComplete = true;
    protected SidebarFragment sidebar;
    private ImageHandler imageHandler;
    protected FragmentManager fragmentManager       = getSupportFragmentManager();
    protected ActionBar actionBar;
    private MenuItem refreshMenuItem;

    private int layoutResourceId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(getLayoutResourceId());
        } catch (NotFoundException e) {
            throw new NotFoundException(getClass().toString() + " must provide a layout resource id.");
        }
        sidebar = new SidebarFragment();
        imageHandler = new ImageHandler(this);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_sidebar_container, sidebar);
        fragmentTransaction.commit();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }




    public ImageHandler getImageHandler() { return imageHandler; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageHandler.closeCache();
    }

    public MenuItem getRefreshMenuItem() { return refreshMenuItem; }

    /**
     * Gets the layout resource id.
     *
     * @return the layout resource id
     */
    protected int getLayoutResourceId() { return layoutResourceId; }
    protected void setLayoutResourceId(int layoutResourceId) { this.layoutResourceId = layoutResourceId; }


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
//    public boolean actionBarItemSelected(MenuItem item) {
//        // TODO Auto-generated method stub
//        Log.i("OMG!", "Something pressed in home view!");
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleSidebarFragment();
                return true;
            default:
                //return actionBarItemSelected(item);
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (sidebarFragmentActive) toggleSidebarFragment();
        else super.onBackPressed();
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
    public void toggleSidebarFragment() {
        // TODO This might do something else...one day
        sidebarFragmentActive = !sidebarFragmentActive;
    }

    public static boolean isSidebarActive() {
        return sidebarFragmentActive;
    }

    /**
     * Gets the resource container to add the sidebar to.
     *
     * @return the sidebar resource container
     */
    //protected abstract int getSidebarResourceContainer();

    public void onSidebarItemClicked(String name, boolean onActiveActivity) {}

}
