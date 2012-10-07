package com.ohso.omgubuntu;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.ohso.omgubuntu.BaseActivity.ActionBarListener;

public class BaseFragment extends Fragment {
	private String title;
	public BaseFragment() {
		Log.i("OMG!", "Geting BaseFragment() for "+ this.toString());
		setTitle(null);
	}

	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }

	public void getActionBar() {
		try {
			((ActionBarListener)getActivity()).onGetDefaultActionBar();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() +
					" must implement ActionBarListener");
		}
	}

//	public BaseFragment newInstance() {
//		return new BaseFragment();
//	}
}
