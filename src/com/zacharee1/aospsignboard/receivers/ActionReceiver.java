package com.zacharee1.aospsignboard.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.SignBoardManager;
import com.zacharee1.aospsignboard.widgets.Music;
import com.zacharee1.aospsignboard.widgets.QuickToggles;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult result = goAsync();
        switch (intent.getAction()) {
            case SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE:
                if (intent.hasExtra(SignBoardManager.EXTRA_QT_TOGGLE)) {
                    SignBoardManager.getInstance(context).sendQuickToolsAction(intent.getStringExtra(SignBoardManager.EXTRA_QT_TOGGLE));

                    Intent update = new Intent(SignBoardManager.ACTION_UPDATE_QUICKTOGGLES);
                    update.putExtra(SignBoardManager.EXTRA_QT_TOGGLE, intent.getStringExtra(SignBoardManager.EXTRA_QT_TOGGLE));
                    update.setComponent(new ComponentName(context, QuickToggles.class));
                    context.sendBroadcastAsUser(update, Process.myUserHandle(), Manifest.permission.MANAGE_SIGNBOARD);
                }
                break;
            case SignBoardManager.ACTION_MUSIC_CONTROL:
                if (intent.hasExtra(SignBoardManager.EXTRA_MUSIC_BUTTON)) {
                    SignBoardManager.getInstance(context).sendMusicControllerAction(intent.getStringExtra(SignBoardManager.EXTRA_MUSIC_BUTTON));

                    Intent update = new Intent(SignBoardManager.ACTION_UPDATE_MUSIC);
                    update.putExtra(SignBoardManager.EXTRA_MUSIC_BUTTON, intent.getStringExtra(SignBoardManager.EXTRA_MUSIC_BUTTON));
                    update.setComponent(new ComponentName(context, Music.class));
                    context.sendBroadcastAsUser(update, Process.myUserHandle(), Manifest.permission.MANAGE_SIGNBOARD);
                }
                break;
        }
        result.finish();
    }
}
