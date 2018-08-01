package com.zacharee1.aospsignboard.widgets.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.preference.PreferenceManager;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.utils.Utils;

import java.util.List;

public class MusicButton {
    private static MusicButton INSTANCE;

    public static MusicButton getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MusicButton(context);
        }

        return INSTANCE;
    }

    private AudioManager audioManager;
    private MediaSessionManager mediaSessionManager;
    private SharedPreferences preferences;

    private MusicButton(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getPlayPauseDrawable() {
        return audioManager.isMusicActive() ? R.drawable.music_pause : R.drawable.music_play;
    }

    public String getTitle() {
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(null);
        if (controllers.isEmpty()) return null;
        else {
            MediaMetadata metadata = controllers.get(0).getMetadata();
            if (metadata == null) return null;
            else return metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
        }
    }

    public String getArtist() {
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(null);
        if (controllers.isEmpty()) return null;
        else {
            MediaMetadata metadata = controllers.get(0).getMetadata();
            if (metadata == null) return null;
            else return metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        }
    }

    public Bitmap getArt() {
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(null);
        if (controllers.isEmpty()) return null;
        else {
            MediaMetadata metadata = controllers.get(0).getMetadata();
            if (metadata == null) return null;
            else {
                Bitmap art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                if (art == null) art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
                return Utils.createTrimmedBitmap(art);
            }
        }
    }

    public int getColor(String which) {
        return preferences.getInt(which + "_color", Color.WHITE);
    }
}
