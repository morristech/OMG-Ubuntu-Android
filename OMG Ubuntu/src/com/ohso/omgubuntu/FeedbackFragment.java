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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class FeedbackFragment extends SherlockFragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        view.findViewById(R.id.fragment_feedback_button).setOnClickListener(this);
        view.findViewById(R.id.fragment_feedback_tip_button).setOnClickListener(this);
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle(getResources().getString(R.string.title_feedback));
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fragment_feedback_button:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                StringBuilder uriText = new StringBuilder();
                uriText.append("mailto:contact@omgubuntu.co.uk");
                uriText.append("?subject=[APP-UBUNTU] Feedback");
                emailIntent.setData(Uri.parse(uriText.toString()));
                startActivity(Intent.createChooser(emailIntent,
                        getResources().getString(R.string.fragment_feedback_email_text)));
                break;
            case R.id.fragment_feedback_tip_button:
                Intent external = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(R.string.base_url) + "/tip"));
                external.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(external);
                break;
        }
    }
}
