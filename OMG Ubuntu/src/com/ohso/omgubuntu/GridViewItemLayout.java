package com.ohso.omgubuntu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class GridViewItemLayout extends LinearLayout {
    private static int[] mMaxRowHeight;
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
        mMaxRowHeight = new int[(int) Math.ceil(itemCount / numColumns)];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mNumColumns <= 1 || mMaxRowHeight == null) return;

        final int rowIndex = mPosition / mNumColumns;
        final int measuredHeight = getMeasuredHeight();
        if (measuredHeight > mMaxRowHeight[rowIndex]) {
            mMaxRowHeight[rowIndex] = measuredHeight;
        }

        setMeasuredDimension(getMeasuredWidth(), mMaxRowHeight[rowIndex]);
    }
}
