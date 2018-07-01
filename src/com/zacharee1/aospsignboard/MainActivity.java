package com.zacharee1.aospsignboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationListener;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private LinearLayout layout;
    private WindowManager windowManager;
    private OrientationListener orientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        layout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AOSPSignBoard", "Layout Click");
            }
        });

        orientationListener = new OrientationListener(this);

        layout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AOSPSignBoard", "Layout Click");
            }
        });

        findViewById(R.id.add_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.type = WindowManager.LayoutParams.TYPE_SIGNBOARD_NORMAL;
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.privateFlags = WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
                params.setTitle("SignBoard");

                try {
                    windowManager.removeView(layout);
                } catch (Exception e) {}
                
                windowManager.addView(layout, params);
                windowManager.updateViewLayout(layout, params);
            }
        });
    }

    @Override protected void onStart() {
        orientationListener.enable();
        super.onStart();
    }

    @Override protected void onStop() {
        orientationListener.disable();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        windowManager.removeView(layout);
    }

    private class OrientationListener extends OrientationEventListener{
        final int ROTATION_O = 1;
        final int ROTATION_90 = 2;
        final int ROTATION_180 = 3;
        final int ROTATION_270 = 4;

        private int rotation = 0;
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {
            if((orientation < 35 || orientation > 325) && rotation!= ROTATION_O){
                rotation = ROTATION_O;
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.RIGHT);
            }
            else if(orientation > 145 && orientation < 215 && rotation!=ROTATION_180){
                rotation = ROTATION_180;
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.LEFT);
            }
            else if(orientation > 55 && orientation < 125 && rotation!=ROTATION_270){
                rotation = ROTATION_270;
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.BOTTOM);
            }
            else if(orientation > 235 && orientation < 305 && rotation!=ROTATION_90){
                rotation = ROTATION_90;
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.TOP);
            }
        }
    }
}
