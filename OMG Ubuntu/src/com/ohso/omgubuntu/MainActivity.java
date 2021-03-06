/*
 * Copyright (C) 2012 - 2013 Ohso Ltd
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

import java.util.HashMap;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.ohso.omgubuntu.data.OMGUbuntuDatabaseHelper;

public class MainActivity extends BaseActivity {
    public static final boolean DEVELOPER_MODE = false;
    private HashMap<String, Object> fragments = new HashMap<String, Object>();
    private LinearLayout sidebarFragmentLayout;
    private RelativeLayout articleFragmentContainer;
    private int sidebarFragmentLayoutOffset;

    public MainActivity() {
        setLayoutResourceId(R.layout.activity_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(Build.VERSION.SDK_INT > 11) enableStrictMode();
        super.onCreate(savedInstanceState);
        Fragment articlesFragment = new ArticlesFragment();
        Fragment categoriesFragment = new CategoriesFragment();
        Fragment starredFragment = new StarredFragment();
        Fragment settingsFragment = new SettingsFragment();
        Fragment feedbackFragment = new FeedbackFragment();
        Fragment aboutFragment = new AboutFragment();
        fragments.put("sidebar_home", articlesFragment);
        fragments.put("sidebar_categories", categoriesFragment);
        fragments.put("sidebar_starred", starredFragment);
        fragments.put("sidebar_settings", settingsFragment);
        fragments.put("sidebar_feedback", feedbackFragment);
        fragments.put("sidebar_about", aboutFragment);

        articleFragmentContainer = (RelativeLayout) findViewById(R.id.fragment_articles_container);
        setTitle(R.string.app_name);
        OMGUbuntuDatabaseHelper db = new OMGUbuntuDatabaseHelper(this);
        try {
            db.getWritableDatabase();
            db.close();
        } catch (SQLException e) {
            throw new SQLException("Couldn't get db " + e.toString());
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        sidebar.setActiveFragment("sidebar_home");
        fragmentTransaction.replace(R.id.fragment_articles_container, articlesFragment);
        fragmentTransaction.commit();


        if (getSharedPreferences(OMGUbuntuApplication.PREFS_FILE, 0)
            .getBoolean(SettingsFragment.NOTIFICATIONS_ENABLED, true)) {
            NotificationAlarmGenerator.generateAlarm(this);
        }

        // Debug lines to force notifications for testing
        // if (!NotificationService.isNotificationAlarmActive()) {
        //     NotificationAlarmGenerator.generateAlarm(getApplicationContext());
        // }
        // Intent testIntent = new Intent(this, NotificationService.class);
        // startService(testIntent);
    }

    @TargetApi(11)
    private void enableStrictMode() {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }

    @Override
    public void toggleSidebarFragment() {
        sidebarFragmentTransitionComplete = false;
        super.toggleSidebarFragment();
        if (sidebarFragmentLayout == null) {
            sidebarFragmentLayout = (LinearLayout) findViewById(R.id.sidebar_fragment_overlay);
        }

        sidebarFragmentLayoutOffset = sidebarFragmentLayout.getChildAt(0).getWidth();
        if (sidebarFragmentActive) sidebarFragmentLayout.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= 11) {
            ObjectAnimator sidebarAnimation = ObjectAnimator.ofFloat(articleFragmentContainer, "translationX",
                    sidebarFragmentActive ? sidebarFragmentLayoutOffset : 0);
            sidebarAnimation.setDuration(250);
            sidebarAnimation.setStartDelay(100);
            sidebarAnimation.start();
            Handler handler = new Handler();
            // Need to timeout transitionComplete so that the transition...completes.
            handler.postDelayed(transitionComplete, 350);
        } else {
            LayoutParams relParams = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            relParams.leftMargin = sidebarFragmentActive ? sidebarFragmentLayoutOffset : 0;
            relParams.rightMargin = sidebarFragmentActive ? - sidebarFragmentLayoutOffset : 0;
            articleFragmentContainer.setLayoutParams(relParams);
            sidebarFragmentTransitionComplete = true;
            if (!sidebarFragmentActive) sidebarFragmentLayout.setVisibility(View.INVISIBLE);
        }
    }

    private Runnable transitionComplete = new Runnable() {
        @Override
        public void run() {
            sidebarFragmentTransitionComplete = true;
            if (!sidebarFragmentActive) sidebarFragmentLayout.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onSidebarItemClicked(String name, boolean onActiveActivity) {
        super.onSidebarItemClicked(name, onActiveActivity);
        toggleSidebarFragment();
        if (onActiveActivity) {
            if (MainActivity.DEVELOPER_MODE) Log.e("OMG!", "On active activity. Not leaving.");
            return;
        }
        changeActiveSidebar(name);
    }

    public void changeActiveSidebar(String name) {
        sidebar.setActiveFragment(name);

        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, 0)
            .replace(R.id.fragment_articles_container, (Fragment) fragments.get(name))
            .commit();
    }

    /*
     * Passes the articleId onto ArticleActivity for displaying
     *
     */
    public void openArticle(String path) {
        Intent intent = new Intent(this, ArticleActivity.class)
            .putExtra(ArticleActivity.INTERNAL_ARTICLE_PATH_INTENT, path);
        startActivity(intent);
    }

    /*
     * Overriding onNewIntent() so we can refresh the contents of any instances of BaseFragment. This lets the ListView
     * immediately reflect (un)read and (un)starred statuses. This gets called from ArticleActivity because of
     * MainActivity's singleTop flag, so onCreate() isn't called if an instance exists; rather, onNewIntent() is called.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (fragments.get(sidebar.getActiveFragment()) instanceof BaseFragment) {
            ((BaseFragment) fragments.get(sidebar.getActiveFragment())).refreshView();
        }
    }
}
