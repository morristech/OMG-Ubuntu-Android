/*
 * Copyright (C) 2012 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.omgubuntu;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;


public class AboutFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle(getResources().getString(R.string.title_about));

        TextView omgCopy = (TextView) view.findViewById(R.id.activity_about_omg_copy);
        omgCopy.setText(Html.fromHtml(getString(R.string.activity_about_omg_ubuntu)));
        omgCopy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView ohsoCopy = (TextView) view.findViewById(R.id.activity_about_ohso_copy);
        ohsoCopy.setText(Html.fromHtml(getString(R.string.activity_about_ohso)));
        ohsoCopy.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }
}
