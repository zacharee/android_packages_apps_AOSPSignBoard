package com.zacharee1.aospsignboard.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.SignBoardManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.receivers.ActionReceiver;

import java.util.ArrayList;
import java.util.Arrays;

import static android.os.SignBoardManager.*;

public class QuickToggles extends AppWidgetProvider {
    public static final String DEFAULT = 
            QT_VOLUME + SEPARATOR
            + QT_WIFI + SEPARATOR
            + QT_BT + SEPARATOR
//            + DATA + SEPARATOR
            + QT_AIRPLANE + SEPARATOR
            + QT_LOCATION;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SignBoardManager.ACTION_UPDATE_QUICKTOGGLES)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, QuickToggles.class));

            if (intent.hasExtra(SignBoardManager.QT_TOGGLE)) {
                String key = intent.getStringExtra(SignBoardManager.QT_TOGGLE);
                QTInfo info = getInfo(key);
                RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.quicktoggles_root);
                root.setImageViewResource(info.viewId, getDrawableFromKey(context, key));
                manager.partiallyUpdateAppWidget(ids, root);
            } else {
                onUpdate(context, manager, ids);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        SignBoardManager.getInstance(context).setQuickToolsEnabled(true);
    }

    @Override
    public void onDisabled(Context context) {
        SignBoardManager.getInstance(context).setQuickToolsEnabled(false);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String saved = PreferenceManager.getDefaultSharedPreferences(context).getString(QT_KEY, DEFAULT);
        ArrayList<String> split = new ArrayList<>(Arrays.asList(saved.split(SEPARATOR)));
        RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.quicktoggles_root);
        root.removeAllViews(R.id.root);

        for (String key : split) {
            int icon = getDrawableFromKey(context, key);
            QTInfo info = getInfo(key);
            RemoteViews button = new RemoteViews(context.getPackageName(), info.layoutId);
            if (icon > 0) button.setImageViewResource(info.viewId, icon);
            button.setOnClickPendingIntent(info.viewId, makeOnClickIntent(context, key));
            root.addView(R.id.root, button);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, root.clone());
    }
    
    private int getDrawableFromKey(Context context, String key) {
        boolean enabled = isKeyEnabled(context, key);
        switch (key) {
            case QT_WIFI:
                return enabled ? R.drawable.wifi_on : R.drawable.wifi_off;
            case QT_BT:
                return enabled ? R.drawable.bt_on : R.drawable.bt_off;
            case QT_AIRPLANE:
                return enabled ? R.drawable.airplane_on : R.drawable.airplane_off;
            case QT_VOLUME:
                return parseVolumeDrawable(context);
            case QT_LOCATION:
                return enabled ? R.drawable.location_on : R.drawable.location_off;
            case QT_SETTINGS:
                return R.drawable.settings;
            case QT_CAMERA:
                return R.drawable.camera;
            case QT_DATA:
                return 0;
            default:
                return 0;
        }
    }

    private PendingIntent makeOnClickIntent(Context context, String key) {
        PendingIntent pendingIntent = null;
        Intent intent;
        switch (key) {
            case QT_WIFI:
            case QT_BT:
            case QT_AIRPLANE:
            case QT_LOCATION:
            case QT_DATA:
            case QT_VOLUME:
                intent = new Intent(SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE);
                intent.putExtra(SignBoardManager.QT_TOGGLE, key);
                intent.setComponent(new ComponentName(context, ActionReceiver.class));
                pendingIntent = PendingIntent.getBroadcast(context, key.hashCode(), intent, 0);
                break;
            case QT_SETTINGS:
                intent = new Intent(Settings.ACTION_SETTINGS);
                pendingIntent = PendingIntent.getActivity(context, 2, intent, 0);
                break;
            case QT_CAMERA:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                pendingIntent = PendingIntent.getActivity(context, 3, intent, 0);
                break;
        }

        return pendingIntent;
    }

    private int parseVolumeDrawable(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (manager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                return R.drawable.volume_mute;
            case AudioManager.RINGER_MODE_VIBRATE:
                return R.drawable.volume_vibrate;
            case AudioManager.RINGER_MODE_NORMAL:
                return R.drawable.volume_sound;
            default:
                return 0;
        }
    }

    private boolean isKeyEnabled(Context context, String key) {
        switch (key) {
            case QT_WIFI:
                WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                return manager.isWifiEnabled();
            case QT_BT:
                BluetoothManager bt = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                return bt.getAdapter().isEnabled();
            case QT_AIRPLANE:
                return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
            case QT_LOCATION:
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, 0) != 0;
            case QT_DATA:
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return telephonyManager.isDataEnabled();
            default:
                return false;
        }
    }

    private QTInfo getInfo(String key) {
        QTInfo info = new QTInfo();
        switch (key) {
            case QT_WIFI:
                info.layoutId = R.layout.qt_wifi;
                info.viewId = R.id.wifi;
                break;
            case QT_BT:
                info.layoutId = R.layout.qt_bt;
                info.viewId = R.id.bt;
                break;
            case QT_AIRPLANE:
                info.layoutId = R.layout.qt_airplane;
                info.viewId = R.id.airplane;
                break;
            case QT_LOCATION:
                info.layoutId = R.layout.qt_location;
                info.viewId = R.id.location;
                break;
            case QT_DATA:
                info.layoutId = R.layout.qt_data;
                info.viewId = R.id.data;
                break;
            case QT_VOLUME:
                info.layoutId = R.layout.qt_volume;
                info.viewId = R.id.volume;
                break;
            case QT_SETTINGS:
                info.layoutId = R.layout.qt_settings;
                info.viewId = R.id.settings;
                break;
            case QT_CAMERA:
                info.layoutId = R.layout.qt_camera;
                info.viewId = R.id.camera;
                break;
        }

        return info;
    }

    private static class QTInfo {
        public int layoutId = 0;
        public int viewId = 0;
    }
}
