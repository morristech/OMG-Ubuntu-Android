package com.ohso.omgubuntu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SidebarFragment extends Fragment {

	public SidebarFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = new ContextThemeWrapper(getActivity(), R.style.SidebarTheme_Styled);
		LayoutInflater localInflator = inflater.cloneInContext(context);
		return localInflator.inflate(R.layout.fragment_sidebar, container, false);
	}

	
}
