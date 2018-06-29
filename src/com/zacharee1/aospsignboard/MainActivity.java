package com.zacharee1.aospsignboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private LinearLayout layout;
    private WindowManager windowManager;

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

        findViewById(R.id.add_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.type = WindowManager.LayoutParams.TYPE_SIGNBOARD_NORMAL;
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.setTitle("SignBoard");

                try {
                    windowManager.removeView(layout);
                } catch (Exception e) {}
                
                windowManager.addView(layout, params);
                windowManager.updateViewLayout(layout, params);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        windowManager.removeView(layout);
    }
}
