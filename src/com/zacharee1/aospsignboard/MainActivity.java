package com.zacharee1.aospsignboard;

import android.appwidget.*;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager layout;
    private WindowManager windowManager;
    private AppWidgetManager manager;
    private AppWidgetHost host;
    private WindowManager.LayoutParams params = new WindowManager.LayoutParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        manager = AppWidgetManager.getInstance(getApplicationContext());
        host = new CustomHost(getApplicationContext());

        setContentView(R.layout.activity_main);

        layout = (ViewPager) LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(v -> Log.e("AOSPSignBoard", "Layout Click"));
        setUpHosts();

        findViewById(R.id.add_window).setOnClickListener(v -> {

            params.type = WindowManager.LayoutParams.TYPE_SIGNBOARD_NORMAL;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.privateFlags = WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            params.setTitle("SignBoard");

            try {
                windowManager.removeView(layout);
            } catch (Exception ignored) {}


            windowManager.addView(layout, params);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        host.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        host.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        windowManager.removeView(layout);
    }

    private void setUpHosts() {
        int category = getResources().getInteger(R.integer.category);
        List<AppWidgetProviderInfo> infos = manager.getInstalledProviders(category);

        layout.setAdapter(new InfinitePagerAdapter(new Adapter(new ArrayList<>(infos), host, getApplicationContext())));
    }

    private static class Adapter extends PagerAdapter {
        private ArrayList<AppWidgetProviderInfo> infos = new ArrayList<>();
        private AppWidgetHost host;
        private Context context;

        public Adapter(ArrayList<AppWidgetProviderInfo> infos, AppWidgetHost host, Context context) {
            this.infos.addAll(infos);
            this.host = host;
            this.context = context;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof AppWidgetProviderInfo) return infos.indexOf(object);
            else return -1;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int id = host.allocateAppWidgetId();
            AppWidgetHostView view = host.createView(context, id, infos.get(position));
            view.setAppWidget(id, infos.get(position));
            container.addView(view);

            AppWidgetManager.getInstance(context).bindAppWidgetId(id, infos.get(position).provider);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }
    }

    /*
     * Copyright (C) 2016 The CyanogenMod Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    /**
     * A PagerAdapter that wraps around another PagerAdapter to handle paging wrap-around.
     */
    public class InfinitePagerAdapter extends PagerAdapter {

        private static final String TAG = "InfinitePagerAdapter";
        private static final boolean DEBUG = false;

        private PagerAdapter adapter;

        public InfinitePagerAdapter(PagerAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getCount() {
            // warning: scrolling to very high values (1,000,000+) results in
            // strange drawing behaviour
            return Integer.MAX_VALUE;
        }

        /**
         * @return the {@link #getCount()} result of the wrapped adapter
         */
        public int getRealCount() {
            return adapter.getCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int virtualPosition = position % getRealCount();
            debug("instantiateItem: real position: " + position);
            debug("instantiateItem: virtual position: " + virtualPosition);

            // only expose virtual position to the inner adapter
            return adapter.instantiateItem(container, virtualPosition);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            int virtualPosition = position % getRealCount();
            debug("destroyItem: real position: " + position);
            debug("destroyItem: virtual position: " + virtualPosition);

            // only expose virtual position to the inner adapter
            adapter.destroyItem(container, virtualPosition, object);
        }

        /*
         * Delegate rest of methods directly to the inner adapter.
         */

        @Override
        public void finishUpdate(ViewGroup container) {
            adapter.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return adapter.isViewFromObject(view, object);
        }

        @Override
        public void restoreState(Parcelable bundle, ClassLoader classLoader) {
            adapter.restoreState(bundle, classLoader);
        }

        @Override
        public Parcelable saveState() {
            return adapter.saveState();
        }

        @Override
        public void startUpdate(ViewGroup container) {
            adapter.startUpdate(container);
        }

        /*
         * End delegation
         */

        private void debug(String message) {
            if (DEBUG) {
                Log.d(TAG, message);
            }
        }
    }
}
