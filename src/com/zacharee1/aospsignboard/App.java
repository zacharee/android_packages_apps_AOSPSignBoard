package com.zacharee1.aospsignboard;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SignBoardManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.zacharee1.aospsignboard.widgets.Music;

import java.util.ArrayList;
import java.util.Arrays;

import static android.os.SignBoardManager.*;

public class App extends Application {
    public QuickToolsListener quickToolsListener = new QuickToolsListener();
    public FlashlightController flashlightController = new FlashlightController();
    public MusicController musicController = new MusicController();

    public static App get(Context context) {
        Context appContext = context.getApplicationContext();
        return (App) appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SignBoardManager.getInstance(this).initViews();

        quickToolsListener.onCreate();
        flashlightController.onCreate();
        musicController.onCreate();
    }

    public ArrayList<String> getToggleList() {
        return new ArrayList<>(Arrays.asList(getToggleString().split(SEPARATOR)));
    }

    public String getToggleString() {
        String ret = Settings.Secure.getString(getContentResolver(), Settings.Secure.CURRENT_SIGNBOARD_QUICK_TOGGLES);
        if (ret == null || ret.isEmpty()) ret = QT_DEFAULT;
        return ret;
    }

    public void saveToggleList(ArrayList<String> list) {
        if (list == null) saveToggleString(null);
        else {
            String joined = TextUtils.join(SEPARATOR, list);
            saveToggleString(joined);
        }
    }

    public void saveToggleString(String list) {
        Settings.Secure.putString(getContentResolver(), Settings.Secure.CURRENT_SIGNBOARD_QUICK_TOGGLES, list);
    }

    public void setQuickToolsEnabled(boolean enabled) {
        if (enabled) {
            flashlightController.onCreate();
            quickToolsListener.onCreate();
        } else {
            flashlightController.onDestroy();
            quickToolsListener.onDestroy();
        }
    }

    public void setMusicControllerEnabled(boolean enabled) {
        if (enabled) {
            musicController.onCreate();
        } else {
            musicController.onDestroy();
        }
    }

    public boolean isFlashlightEnabled() {
        return flashlightController.flashlightEnabled;
    }

    public void sendQuickToolsAction(String action) {
        quickToolsListener.sendQuickToolsAction(action);
    }

    public void sendMediaEvent(String key) {
        musicController.sendMediaEvent(key);
    }

    private class QuickToolsListener extends ContentObserver {
        private BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE)) {
                    if (intent.hasExtra(SignBoardManager.EXTRA_QT_TOGGLE)) {
                        sendQuickToolsAction(intent.getStringExtra(SignBoardManager.EXTRA_QT_TOGGLE));
                    }
                } else {
                    update();
                }
            }
        };

        public QuickToolsListener() {
            super(Handler.getMain());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update();
        }

        public void onCreate() {
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON), true, this);
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true, this);

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            filter.addAction(SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE);
            filter.addAction(LocationManager.MODE_CHANGED_ACTION);
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

            registerReceiver(receiver, filter);
        }

        public void onDestroy() {
            getContentResolver().unregisterContentObserver(this);
            unregisterReceiver(receiver);
        }

        public void update() {
            Music.update(App.this);
        }

        public void sendQuickToolsAction(String action) {
            switch (action) {
                case SignBoardManager.QT_WIFI:
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
                    break;
                case SignBoardManager.QT_BT:
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter.isEnabled()) adapter.disable();
                    else adapter.enable();
                    break;
                case SignBoardManager.QT_AIRPLANE:
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    connectivityManager.setAirplaneMode(Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 1);
                    break;
                case SignBoardManager.QT_LOCATION:
                    boolean enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
                            != Settings.Secure.LOCATION_MODE_OFF;
                    int prev = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_PREVIOUS_MODE, Settings.Secure.LOCATION_MODE_OFF);
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, enabled ? 0 : prev);
                    break;
                case SignBoardManager.QT_DATA:
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    telephonyManager.setDataEnabled(!telephonyManager.isDataEnabled());
                    break;
                case SignBoardManager.QT_VOLUME:
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    switch (audioManager.getRingerMode()) {
                        case AudioManager.RINGER_MODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            break;
                        case AudioManager.RINGER_MODE_NORMAL:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
                case SignBoardManager.QT_FLASHLIGHT:
                    flashlightController.setFlashlightEnabled(!flashlightController.flashlightEnabled);
                    break;
                case SignBoardManager.QT_ROTATION:
                    boolean rotEnabled = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
                    Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, rotEnabled ? 0 : 1);
                    break;
            }
        }
    }

    private class FlashlightController {
        public boolean flashlightEnabled = false;

        private CameraManager manager;
        private CameraManager.TorchCallback callback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(String cameraId) {
            }

            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                boolean changed = flashlightEnabled != enabled;
                flashlightEnabled = enabled;

                if (changed) Music.update(App.this);
            }
        };

        public void onCreate() {
            manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            manager.registerTorchCallback(callback, new Handler());
        }

        public void onDestroy() {
            manager.unregisterTorchCallback(callback);
        }

        public void setFlashlightEnabled(boolean enabled) {
            flashlightEnabled = enabled;
            try {
                manager.setTorchMode(getCameraId(), enabled);
            } catch (CameraAccessException e) {}
        }

        private String getCameraId() throws CameraAccessException {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics c = manager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == 1) {
                    return id;
                }
            }
            return null;
        }
    }

    private class MusicController {
        private MediaSessionManager mediaManager;
        private MediaController latestActive;
        private MediaController.Callback callback = new MediaController.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackState state) {
                update();
            }

            @Override
            public void onMetadataChanged(MediaMetadata metadata) {
                update();
            }
        };
        private MediaSessionManager.OnActiveSessionsChangedListener listener = controllers -> {
            if (controllers.isEmpty()) {
                if (latestActive != null) latestActive.unregisterCallback(callback);
            } else {
                latestActive = controllers.get(0);
                latestActive.registerCallback(callback);
            }
            update();
        };

        public void onCreate() {
            mediaManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaManager.addOnActiveSessionsChangedListener(listener, null, new Handler());
            if (latestActive != null) latestActive.registerCallback(callback);
        }

        public void onDestroy() {
            mediaManager.removeOnActiveSessionsChangedListener(listener);
            if (latestActive != null) latestActive.unregisterCallback(callback);
        }

        public void sendMediaEvent(String key) {
            int code = -1;

            switch (key) {
                case SignBoardManager.MUSIC_PREV:
                    code = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                    break;
                case SignBoardManager.MUSIC_PLAY_PAUSE:
                    code = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                    break;
                case SignBoardManager.MUSIC_NEXT:
                    code = KeyEvent.KEYCODE_MEDIA_NEXT;
                    break;
            }

            if (code != -1) {
                mediaManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
                mediaManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, code));
            }
        }

        private void update() {
            Music.update(App.this);
        }
    }
}
