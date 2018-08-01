package com.zacharee1.aospsignboard.configure;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.widgets.Music;

public class MusicConfigure extends AppCompatActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (prefs, key) -> {
        if (key.contains("_color")) {
            Music.update(MusicConfigure.this, key.replace("_color", ""));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_activity);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.root, new Prefs())
                .commit();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public static class Prefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_music);
        }
    }
}
