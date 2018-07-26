package com.zacharee1.aospsignboard;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class ItemView extends LinearLayout {
    public ImageView handle;
    public TextView name;
    public Switch toggle;

    public ItemView(Context context) {
        super(context);
        init();
    }

    public ItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.page_item, this);
        handle = findViewById(R.id.handle);
        name = findViewById(R.id.name);
        toggle = findViewById(R.id.switcher);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public static class Info {
        public ComponentName component;
        public ComponentName configure;
        public boolean enabled;
        public String title;
    }
}
