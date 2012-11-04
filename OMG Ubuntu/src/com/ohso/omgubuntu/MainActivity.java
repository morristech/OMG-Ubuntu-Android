package com.ohso.omgubuntu;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;

public class MainActivity extends BaseActivity implements OnNavigationListener {
    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    public MainActivity() {
        setLayoutResourceId(R.layout.activity_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO actually implement this at some point
        return false;
    }

    @Override
    protected void toggleSidebarFragment() {
        super.toggleSidebarFragment();
        FrameLayout flayout = (FrameLayout) findViewById(R.id.sidebar_fragment_overlay);
        final int offset = flayout.getChildAt(0).getWidth();
        int width = 0;
        RelativeLayout slide = (RelativeLayout) findViewById(R.id.fragment_articles_container);
        if(sidebarFragmentActive) width = offset;
        ObjectAnimator.ofFloat(slide, "translationX", width).start();
    }

}
