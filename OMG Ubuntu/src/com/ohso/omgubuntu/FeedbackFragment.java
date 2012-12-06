package com.ohso.omgubuntu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

public class FeedbackFragment extends SherlockFragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, null);
        Button feedback = (Button) view.findViewById(R.id.fragment_feedback_button);
        Button tips = (Button) view.findViewById(R.id.fragment_feedback_tip_button);
        feedback.setOnClickListener(this);
        tips.setOnClickListener(this);
        ((BaseActivity) getSherlockActivity()).getDefaultActionBar();
        getSherlockActivity().getSupportActionBar().setTitle("Feedback");
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
                startActivity(Intent.createChooser(emailIntent, "Send mail via"));
                break;
            case R.id.fragment_feedback_tip_button:
                Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse("http://omgubuntu.co.uk/tip"));
                external.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(external);
                break;
        }
    }

}
