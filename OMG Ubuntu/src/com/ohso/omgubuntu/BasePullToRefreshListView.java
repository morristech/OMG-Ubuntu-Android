package com.ohso.omgubuntu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class BasePullToRefreshListView extends PullToRefreshListView {

	public BasePullToRefreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ListView createListView(Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.createListView(context, attrs);
	}

}
