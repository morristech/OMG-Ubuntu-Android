package com.ohso.omgubuntu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.GridView;

public class HeterogeneousGridView extends GridView {

    public HeterogeneousGridView(Context context) {
        super(context);
    }

    public HeterogeneousGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeterogeneousGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i("OMG!", "onLayout called");
        if(changed) {

            ArticleAdapter adapter = (ArticleAdapter) getAdapter();
            Log.i("OMG!", "Changed for # " + adapter.getCount());
            int numColumns = BaseFragment.getColumnByScreenSize();
            GridViewItemLayout.initItemLayout(numColumns, adapter.getCount());

            if (numColumns > 1) {
                int columnWidth = getMeasuredWidth() / numColumns;
                adapter.measureItems(columnWidth);
            }
        }
        super.onLayout(changed, l, t, r, b);
    }
}
