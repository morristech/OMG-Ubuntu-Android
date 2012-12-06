package com.ohso.omgubuntu;

import android.app.Activity;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class FragmentGestureListener extends SimpleOnGestureListener {
    //http://stackoverflow.com/a/5174757
    private int minDistance;
    private int maxOffPath;
    private int thresholdVelocity;
    private OnFragmentGestureListener mFragmentGestureListener;
    public FragmentGestureListener(Activity a, int minDistance, int maxOffPath, int thresholdVelocity) {
        this.minDistance = minDistance;
        this.maxOffPath = maxOffPath;
        this.thresholdVelocity = thresholdVelocity;
        try {
            mFragmentGestureListener = (OnFragmentGestureListener) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement OnFragmentGestureListener");
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        if (Math.abs(e1.getY() - e2.getY()) > maxOffPath) return false;
        if (e1.getX() - e2.getX() > minDistance && Math.abs(velocityX) > thresholdVelocity) onSwipeToLeft();
        else if (e2.getX() - e1.getX() > minDistance && Math.abs(velocityX) > thresholdVelocity) onSwipeToRight();
        return false;
    }

    /*
     * Finger swipe starting on the left, terminating on the right
     *
     */
    public void onSwipeToRight() {
        mFragmentGestureListener.onSwipeToRight();
    }

    /*
     * Finger swipe starting on right, terminating on the left
     *
     */
    public void onSwipeToLeft() {
        mFragmentGestureListener.onSwipeToLeft();
    }

    public interface OnFragmentGestureListener {
        void onSwipeToRight();
        void onSwipeToLeft();
    }

}
