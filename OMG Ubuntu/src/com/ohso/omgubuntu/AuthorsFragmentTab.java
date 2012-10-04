package com.ohso.omgubuntu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class AuthorsFragmentTab extends Fragment {

	public AuthorsFragmentTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (container == null) {
			//If the fragment frame doesn't exist, don't waste time inflating the view
			return null;
		}
		//return super.onCreateView(inflater, container, savedInstanceState);
		return (PullToRefreshListView)inflater.inflate(R.layout.tab_fragment_authors, container, false);
	}
	public static AuthorsFragmentTab newInstance (String title) {
		AuthorsFragmentTab fragmentPage = new AuthorsFragmentTab();
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		fragmentPage.setArguments(bundle);
		return fragmentPage;
	}

}
