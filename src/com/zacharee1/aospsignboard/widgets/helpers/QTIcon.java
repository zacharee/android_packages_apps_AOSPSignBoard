package com.zacharee1.aospsignboard.widgets.helpers;

import android.annotation.DrawableRes;
import android.annotation.IdRes;
import android.annotation.LayoutRes;
import android.annotation.StringRes;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.SignBoardManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.telephony.TelephonyManager;
import android.util.SparseIntArray;
import com.zacharee1.aospsignboard.App;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.receivers.ActionReceiver;

import java.util.HashMap;

import static android.os.SignBoardManager.*;

public class QTIcon {
    public static final int STATE_NONE = -2;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_SPECIAL = 2;

    private static HashMap<String, QTIcon> INSTANCES = new HashMap<>();

    public String key;
    public String colorKey;
    public Context context;

    private @LayoutRes int layoutId;
    private @IdRes int viewId;
    private @StringRes int titleId;
    private SparseIntArray drawableIds = new SparseIntArray();

    private Object manager;
    private SharedPreferences preferences;
    private StateRunner stateRunner;

    public static QTIcon getInstance(Context context, String key) {
        if (INSTANCES.get(key) == null) {
            INSTANCES.put(key, new QTIcon(context, key));
        }

        return INSTANCES.get(key);
    }

    private QTIcon(Context context, String key) {
        this.key = key;
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        colorKey = key + "_color";

        init();
    }

    private void init() {
        switch (key) {
            case QT_WIFI:
                manager = context.getSystemService(Context.WIFI_SERVICE);
                layoutId = R.layout.qt_wifi;
                viewId = R.id.wifi;
                titleId = R.string.wifi;
                drawableIds.put(WifiManager.WIFI_STATE_DISABLED, R.drawable.wifi_off);
                drawableIds.put(WifiManager.WIFI_STATE_DISABLING, R.drawable.wifi_off);
                drawableIds.put(WifiManager.WIFI_STATE_UNKNOWN, R.drawable.wifi_off);
                drawableIds.put(WifiManager.WIFI_STATE_ENABLED, R.drawable.wifi_on);
                drawableIds.put(WifiManager.WIFI_STATE_ENABLING, R.drawable.wifi_on);
                stateRunner = () -> ((WifiManager) manager).getWifiState();
                break;
            case QT_BT:
                manager = context.getSystemService(Context.BLUETOOTH_SERVICE);
                layoutId = R.layout.qt_bt;
                viewId = R.id.bt;
                titleId = R.string.bluetooth;
                drawableIds.put(BluetoothAdapter.STATE_OFF, R.drawable.bt_off);
                drawableIds.put(BluetoothAdapter.STATE_TURNING_OFF, R.drawable.bt_off);
                drawableIds.put(BluetoothAdapter.STATE_ON, R.drawable.bt_on);
                drawableIds.put(BluetoothAdapter.STATE_TURNING_ON, R.drawable.bt_on);
                stateRunner = () -> ((BluetoothManager) manager).getAdapter().getState();
                break;
            case QT_AIRPLANE:
                manager = null;
                layoutId = R.layout.qt_airplane;
                viewId = R.id.airplane;
                titleId = R.string.airplane;
                drawableIds.put(STATE_DISABLED, R.drawable.airplane_off);
                drawableIds.put(STATE_ENABLED, R.drawable.airplane_on);
                stateRunner = () -> Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
                break;
            case QT_LOCATION:
                manager = null;
                layoutId = R.layout.qt_location;
                viewId = R.id.location;
                titleId = R.string.location;
                drawableIds.put(Settings.Secure.LOCATION_MODE_OFF, R.drawable.location_off);
                drawableIds.put(Settings.Secure.LOCATION_MODE_BATTERY_SAVING, R.drawable.location_on);
                drawableIds.put(Settings.Secure.LOCATION_MODE_SENSORS_ONLY, R.drawable.location_on);
                drawableIds.put(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY, R.drawable.location_on);
                stateRunner = () -> Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, 0) != 0
                        ? STATE_ENABLED : STATE_DISABLED;
                break;
            case QT_DATA:
                manager = context.getSystemService(Context.TELEPHONY_SERVICE);
                layoutId = R.layout.qt_data;
                viewId = R.id.data;
                titleId = R.string.data;
                drawableIds.put(STATE_DISABLED, R.drawable.data_off);
                drawableIds.put(STATE_ENABLED, R.drawable.data_on);
                stateRunner = () -> ((TelephonyManager) manager).isDataEnabled() ? STATE_ENABLED : STATE_DISABLED;
                break;
            case QT_VOLUME:
                manager = context.getSystemService(Context.AUDIO_SERVICE);
                layoutId = R.layout.qt_volume;
                viewId = R.id.volume;
                titleId = R.string.volume;
                drawableIds.put(AudioManager.RINGER_MODE_SILENT, R.drawable.volume_mute);
                drawableIds.put(AudioManager.RINGER_MODE_VIBRATE, R.drawable.volume_vibrate);
                drawableIds.put(AudioManager.RINGER_MODE_NORMAL, R.drawable.volume_sound);
                stateRunner = () -> ((AudioManager) manager).getRingerMode();
                break;
            case QT_FLASHLIGHT:
                manager = null;
                layoutId = R.layout.qt_flashlight;
                viewId = R.id.flashlight;
                titleId = R.string.flashlight;
                drawableIds.put(STATE_DISABLED, R.drawable.flashlight_off);
                drawableIds.put(STATE_ENABLED, R.drawable.flashlight_on);
                stateRunner = () -> App.get(context).isFlashlightEnabled() ? STATE_ENABLED : STATE_DISABLED;
                break;
            case QT_ROTATION:
                manager = null;
                layoutId = R.layout.qt_rotation;
                viewId = R.id.rotation;
                titleId = R.string.rotation;
                drawableIds.put(STATE_DISABLED, R.drawable.rotation_off);
                drawableIds.put(STATE_ENABLED, R.drawable.rotation_on);
                stateRunner = () -> Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                break;
            case QT_SETTINGS:
                manager = null;
                layoutId = R.layout.qt_settings;
                viewId = R.id.settings;
                titleId = R.string.settings;
                drawableIds.put(STATE_ENABLED, R.drawable.settings);
                stateRunner = () -> STATE_ENABLED;
                break;
            case QT_CAMERA:
                manager = null;
                layoutId = R.layout.qt_camera;
                viewId = R.id.camera;
                titleId = R.string.camera;
                drawableIds.put(STATE_ENABLED, R.drawable.camera);
                stateRunner = () -> STATE_ENABLED;
                break;
        }
    }

    @LayoutRes
    public int getLayoutId() {
        return layoutId;
    }

    @IdRes
    public int getViewId() {
        return viewId;
    }

    @DrawableRes
    public int getDrawableId() {
        return drawableIds.get(getState());
    }

    public int getState() {
        return stateRunner.getState();
    }

    public PendingIntent getIntent() {
        PendingIntent pendingIntent;
        Intent intent;
        switch (key) {
            default:
                intent = new Intent(SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE);
                intent.putExtra(SignBoardManager.EXTRA_QT_TOGGLE, key);
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

    @ColorInt
    public int getColor() {
        return preferences.getInt(colorKey, Color.WHITE);
    }

    @StringRes
    public int getTitle() {
        return titleId;
    }

    @Override
    public String toString() {
        return key;
    }

    @FunctionalInterface
    private interface StateRunner {
        int getState();
    }
}
