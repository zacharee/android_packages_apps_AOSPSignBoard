package com.zacharee1.aospsignboard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private LinearLayout layout;
    private WindowManager windowManager;
    private RotationWatcher rotationWatcher;
    private WindowManager.LayoutParams params = new WindowManager.LayoutParams();

    private float pivotX = 1040f / 2f;
    private float pivotY = 160f / 2f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        rotationWatcher = new RotationWatcher(this);

        layout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(v -> Log.e("AOSPSignBoard", "Layout Click"));

        findViewById(R.id.add_window).setOnClickListener(v -> {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            params.type = WindowManager.LayoutParams.TYPE_SIGNBOARD_NORMAL;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.privateFlags = WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            params.setTitle("SignBoard");

            try {
                windowManager.removeView(layout);
            } catch (Exception ignored) {}

            windowManager.addView(layout, params);
            windowManager.updateViewLayout(layout, params);

            rotationWatcher.enable();
            rotationWatcher.onOrientationChanged(0);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        windowManager.removeView(layout);
        rotationWatcher.disable();
    }

    private class RotationWatcher extends OrientationEventListener {
        private int oldRot = -1;

        public RotationWatcher(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            int rot = windowManager.getDefaultDisplay().getRotation();
            if (rot != oldRot) {
                oldRot = rot;

                int toDeg = 0;
                float pX = pivotX;
                float pY = pivotY;

                switch (rot) {
                    case Surface.ROTATION_0:
                        toDeg = 0;
                        break;
                    case Surface.ROTATION_90:
                        toDeg = -90;
                        pX = pivotX;
                        pY = pivotX;
                        break;
                    case Surface.ROTATION_180:
                        toDeg = 180;
                        pX = pivotX;
                        pY = pivotY;
                        break;
                    case Surface.ROTATION_270:
                        toDeg = 90;
                        pX = pivotY;
                        pY = pivotY;
                        break;
                }

                layout.setPivotX(pX);
                layout.setPivotY(pY);
                layout.setRotation(toDeg);
                layout.invalidate();

                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.width = 1040;
                layoutParams.height = 160;
            }
        }
    }
}
