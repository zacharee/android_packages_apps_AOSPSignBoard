package com.zacharee1.aospsignboard;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class FixedPager extends ViewPager {
    private PagerAdapter adapter;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (adapter != null) {
            super.setAdapter(adapter);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
    }

    public FixedPager(Context context) {
        super(context);
    }

    public FixedPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
