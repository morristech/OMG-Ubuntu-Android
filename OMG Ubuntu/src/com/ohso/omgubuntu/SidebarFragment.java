package com.ohso.omgubuntu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

public class SidebarFragment extends ListFragment implements OnClickListener {
	private OnSidebarClickListener mCallback;
	public static String activeActivity = "Home";
	public SidebarFragment() {
		// TODO Auto-generated constructor stub
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = new ContextThemeWrapper(getActivity(), R.style.SidebarTheme_Styled);
		LayoutInflater localInflator = inflater.cloneInContext(context);

		View v = localInflator.inflate(R.layout.fragment_sidebar, container, false);
		FrameLayout frame = (FrameLayout) v.findViewById(R.id.sidebar_fragment_overlay);
		frame.setOnClickListener(this);
		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String name = (String) l.getItemAtPosition(position);
		boolean onActiveActivity = false;
		Log.i("OMG!", "On activity "+name+". Active is "+activeActivity+" at pos"+position+"and id "+id);
		if(activeActivity.equals(name)) {
			onActiveActivity = true;
		} else {
			//activeActivity = name;
		}
		Log.i("OMG!", "Got menu item: "+name);
		mCallback.onSidebarItemClicked(name, onActiveActivity);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnSidebarClickListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement OnSidebarClickListener");
		}
	}

	public interface OnSidebarClickListener {
		public void onSidebarItemClicked(String name, boolean active);
		public void onSidebarLostFocus();
	}

	@Override
	public void onClick(View v) {
		Log.i("OMG!", "Got sidebar clicking for"+v.toString());
		mCallback.onSidebarLostFocus();

	}
}
