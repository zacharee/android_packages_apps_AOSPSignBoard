package com.zacharee1.aospsignboard;

import android.appwidget.*;
import android.content.Context;
import android.os.*;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ISignBoardService service = ISignBoardService.Stub.asInterface(ServiceManager.getService("signboardservice"));

        findViewById(R.id.add_window).setOnClickListener(v -> {
            try {
                service.removeAllViews();
                service.initViews();
            } catch (RemoteException e) {
                Log.w("AOSPSignBoard", e.getLocalizedMessage());
            }
        });

        findViewById(R.id.remove_all).setOnClickListener(v -> {
            try {
                service.removeAllViews();
            } catch (RemoteException e) {
                Log.w("AOSPSignBoard", e.getLocalizedMessage());
            }
        });
    }
}
