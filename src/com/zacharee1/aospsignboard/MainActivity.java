package com.zacharee1.aospsignboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.OrientationListener;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private SizeInfo[] sizeInfos = new SizeInfo[4];

    private LinearLayout layout;
    private WindowManager windowManager;
    private RotationWatcher rotationWatcher = new RotationWatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizeInfos[Surface.ROTATION_0] = new SizeInfo(0, Gravity.RIGHT, 1040, 160);
        sizeInfos[Surface.ROTATION_90] = new SizeInfo(-90, Gravity.RIGHT, 1040, 160);
        sizeInfos[Surface.ROTATION_180] = new SizeInfo(180, Gravity.LEFT, 1040, 160);
        sizeInfos[Surface.ROTATION_270] = new SizeInfo(-270, Gravity.LEFT, 1040, 160);

        setContentView(R.layout.activity_main);

        layout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(v -> Log.e("AOSPSignBoard", "Layout Click"));


        layout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_sb_layout, null);
        layout.setOnClickListener(v -> Log.e("AOSPSignBoard", "Layout Click"));

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
                } catch (Exception ignored) {}
                
                windowManager.addView(layout, params);
                windowManager.updateViewLayout(layout, params);

                IWindowManager iwm = WindowManagerGlobal.getWindowManagerService();

                try {
                    iwm.removeRotationWatcher(rotationWatcher);
                } catch (Exception ignored) {}
                try {
                    rotationWatcher.onRotationChanged(iwm.watchRotation(rotationWatcher, getDisplay().getDisplayId()));
                } catch (RemoteException ignored) {}
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        windowManager.removeView(layout);
    }

    private class RotationWatcher extends IRotationWatcher.Stub {
        @Override
        public void onRotationChanged(int rotation) throws RemoteException {
            SizeInfo info = sizeInfos[rotation];
            ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();

            layout.setRotation(info.rotation);
            layout.setGravity(info.gravity);

            layoutParams.height = info.height;
            layoutParams.width = info.width;

            layout.setLayoutParams(layoutParams);

            windowManager.updateViewLayout(layout, layout.getLayoutParams());
        }
    }

    static class SizeInfo {
        float rotation;
        int gravity;
        int width;
        int height;

        public SizeInfo(float rotation, int gravity, int width, int height) {
            this.rotation = rotation;
            this.gravity = gravity;
            this.width = width;
            this.height = height;
        }
    }
}
