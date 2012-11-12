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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.ohso.omgubuntu.sqlite.OMGUbuntuDatabaseHelper;

public class MainActivity extends BaseActivity implements OnNavigationListener {
    private final boolean DEVELOPER_MODE = true;
    private HashMap<String, Object> fragments = new HashMap<String, Object>();
    private FrameLayout sidebarFragmentLayout;
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
        Fragment searchFragment = new SearchFragment();
        Fragment aboutFragment = new AboutFragment();
        fragments.put("Home", articlesFragment);
        fragments.put("Categories", categoriesFragment);
        fragments.put("Starred", starredFragment);
        fragments.put("Search", searchFragment);
        fragments.put("About", aboutFragment);

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
        fragmentTransaction.add(R.id.fragment_articles_container, articlesFragment);
        fragmentTransaction.commit();
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
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO actually implement this at some point
        return false;
    }

    @Override
    public void toggleSidebarFragment() {
        sidebarFragmentTransitionComplete = false;
        super.toggleSidebarFragment();
        if (sidebarFragmentLayout == null) {
            sidebarFragmentLayout = (FrameLayout) findViewById(R.id.sidebar_fragment_overlay);
        }

        // TODO add overlay to capture clicks

        sidebarFragmentLayoutOffset = sidebarFragmentLayout.getChildAt(0).getWidth();
        ObjectAnimator sidebarAnimation = ObjectAnimator.ofFloat(articleFragmentContainer, "translationX",
                sidebarFragmentActive ? sidebarFragmentLayoutOffset : 0);
        sidebarAnimation.setDuration(250);
        sidebarAnimation.setStartDelay(100);
        sidebarAnimation.start();
        Handler handler = new Handler();
        handler.postDelayed(transitionComplete, 350);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    private Runnable transitionComplete = new Runnable() {
        @Override
        public void run() {
            sidebarFragmentTransitionComplete = true;
        }
    };

    @Override
    public void onSidebarItemClicked(String name, boolean onActiveActivity) {
        super.onSidebarItemClicked(name, onActiveActivity);
        Log.i("OMG!", "Got sidebar click.");
        toggleSidebarFragment();
        if (onActiveActivity) {
            Log.i("OMG!", "On active activity. Not leaving.");
            return;
        }
        sidebar.sActiveFragment = name;

        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right)
            .replace(R.id.fragment_articles_container, (Fragment) fragments.get(name))
            .commit();
    }

    /*
     * Passes the articleId onto ArticleActivity for displaying
     *
     */
    public void openArticle(String path) {
        Intent intent = new Intent(this, ArticleActivity.class).putExtra("article_path", path);
        startActivity(intent);
    }


}
