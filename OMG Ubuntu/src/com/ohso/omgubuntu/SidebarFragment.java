package com.ohso.omgubuntu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SidebarFragment extends ListFragment {
	public static String activeActivity = "Home";
	OnSidebarItemClickListener mCallback;
	public SidebarFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = new ContextThemeWrapper(getActivity(), R.style.SidebarTheme_Styled);
		LayoutInflater localInflator = inflater.cloneInContext(context);
		return localInflator.inflate(R.layout.fragment_sidebar, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String name = (String) l.getItemAtPosition(position);
		boolean onActiveActivity = false;
		if(activeActivity.equals(name)) {
			onActiveActivity = true;
		} else {
			activeActivity = name;
		}
		Log.i("OMG!", "Got menu item: "+name);
		mCallback.onSidebarItemClicked(name, onActiveActivity);

	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mCallback = (OnSidebarItemClickListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement OnSidebarItemClickListener");
		}
	}
	
	public interface OnSidebarItemClickListener {
		public void onSidebarItemClicked(String name, boolean active);
	}
}
