package com.zacharee1.aospsignboard.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SignBoardManager;
import android.widget.RemoteViews;
import com.zacharee1.aospsignboard.App;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.receivers.ActionReceiver;
import com.zacharee1.aospsignboard.widgets.helpers.MusicButton;

public class Music extends AppWidgetProvider {
    public static void update(Context context) {
        update(context, null);
    }

    public static void update(Context context, String key) {
        Intent update = new Intent(SignBoardManager.ACTION_UPDATE_MUSIC);
        if (key != null) update.putExtra(SignBoardManager.EXTRA_MUSIC_BUTTON, key);
        update.setComponent(new ComponentName(context, Music.class));
        context.sendBroadcast(update);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SignBoardManager.ACTION_UPDATE_MUSIC)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, Music.class));
            onUpdate(context, manager, ids);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        App.get(context).setMusicControllerEnabled(true);
    }

    @Override
    public void onDisabled(Context context) {
        App.get(context).setMusicControllerEnabled(false);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.music_root);
        MusicButton button = MusicButton.getInstance(context);

        root.setTextViewText(R.id.title, button.getTitle());
        root.setTextViewText(R.id.artist, button.getArtist());

        root.setTextColor(R.id.title, button.getColor(SignBoardManager.MUSIC_TITLE));
        root.setTextColor(R.id.artist, button.getColor(SignBoardManager.MUSIC_ARTIST));
        root.setInt(R.id.prev, "setColorFilter", button.getColor(SignBoardManager.MUSIC_PREV));
        root.setInt(R.id.play_pause, "setColorFilter", button.getColor(SignBoardManager.MUSIC_PLAY_PAUSE));
        root.setInt(R.id.next, "setColorFilter", button.getColor(SignBoardManager.MUSIC_NEXT));

        root.setImageViewBitmap(R.id.art, button.getArt());

        Intent prev = new Intent(SignBoardManager.ACTION_MUSIC_CONTROL);
        Intent pp = new Intent(SignBoardManager.ACTION_MUSIC_CONTROL);
        Intent next = new Intent(SignBoardManager.ACTION_MUSIC_CONTROL);

        ComponentName target = new ComponentName(context, ActionReceiver.class);

        prev.putExtra(SignBoardManager.EXTRA_MUSIC_BUTTON, SignBoardManager.MUSIC_PREV);
        pp.putExtra(SignBoardManager.EXTRA_MUSIC_BUTTON, SignBoardManager.MUSIC_PLAY_PAUSE);
        next.putExtra(SignBoardManager.EXTRA_MUSIC_BUTTON, SignBoardManager.MUSIC_NEXT);

        prev.setComponent(target);
        pp.setComponent(target);
        next.setComponent(target);

        root.setOnClickPendingIntent(R.id.prev, PendingIntent.getBroadcast(context, 100, prev, 0));
        root.setOnClickPendingIntent(R.id.play_pause, PendingIntent.getBroadcast(context, 1001, pp, 0));
        root.setOnClickPendingIntent(R.id.next, PendingIntent.getBroadcast(context, 1002, next, 0));

        root.setImageViewResource(R.id.play_pause, button.getPlayPauseDrawable());

        appWidgetManager.updateAppWidget(appWidgetIds, root.clone());
    }
}
