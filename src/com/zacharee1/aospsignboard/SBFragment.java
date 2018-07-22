package com.zacharee1.aospsignboard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SBFragment extends Fragment {
    private View subView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.sb_page, container, false);

        if (subView != null) rootView.addView(subView);

        return rootView;
    }

    public void setView(View view) {
        if (getView() != null) {
            LinearLayout v = (LinearLayout) getView();
            v.removeAllViews();
            v.addView(subView);
        } else {
            subView = view;
        }
    }
}
