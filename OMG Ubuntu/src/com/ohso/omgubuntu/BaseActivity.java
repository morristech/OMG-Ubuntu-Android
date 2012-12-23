/*
 * Copyright (C) 2012 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.omgubuntu;

import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ohso.util.ImageHandler;

public abstract class BaseActivity extends SherlockFragmentActivity implements SidebarFragment.OnSidebarClickListener {
    protected static boolean         sidebarFragmentActive = false;
    // Useful for disabling certain actions whilst the sidebar is still transitioning
    public boolean sidebarFragmentTransitionComplete = true;
    protected SidebarFragment sidebar;
    private ImageHandler imageHandler;
    protected FragmentManager fragmentManager = getSupportFragmentManager();
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

    protected int getLayoutResourceId() { return layoutResourceId; }
    protected void setLayoutResourceId(int layoutResourceId) { this.layoutResourceId = layoutResourceId; }


    public void getDefaultActionBar() {
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_HOME|
                ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleSidebarFragment();
                return true;
            default:
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
    }

    public void toggleSidebarFragment() {
        sidebarFragmentActive = !sidebarFragmentActive;
    }

    public static boolean isSidebarActive() {
        return sidebarFragmentActive;
    }

    public void onSidebarItemClicked(String name, boolean onActiveActivity) {}
}
