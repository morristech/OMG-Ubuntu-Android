package com.ohso.omgubuntu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.LinearLayout;

public class GridViewItemLayout extends LinearLayout {
    private static SparseIntArray mMaxRowHeight;
    private static int mNumColumns;
    private int mPosition;

    public GridViewItemLayout(Context context) {
        super(context);
    }

    public GridViewItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setPosition(int position) { mPosition = position; }

    public static void initItemLayout(int numColumns, int itemCount) {
        mNumColumns = numColumns;
        mMaxRowHeight = new SparseIntArray((int) Math.ceil(itemCount / numColumns));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mNumColumns <= 1 || mMaxRowHeight == null) return;

        final int rowIndex = mPosition / mNumColumns;
        final int measuredHeight = getMeasuredHeight();

        // TODO any time this happens, we need to recalculate everything, especially for the footer.
        if (mMaxRowHeight.get(rowIndex, -1) == -1) {
            Log.i("OMG!", rowIndex + " has no value");
        }
        if (mMaxRowHeight.get(rowIndex, -1) == -1 || measuredHeight > mMaxRowHeight.get(rowIndex)) {
            mMaxRowHeight.put(rowIndex, measuredHeight);
        }

        setMeasuredDimension(getMeasuredWidth(), mMaxRowHeight.get(rowIndex));
    }
}
