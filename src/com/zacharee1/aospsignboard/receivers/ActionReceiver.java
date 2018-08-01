package com.zacharee1.aospsignboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SignBoardManager;
import com.zacharee1.aospsignboard.App;
import com.zacharee1.aospsignboard.widgets.Music;
import com.zacharee1.aospsignboard.widgets.QuickToggles;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult result = goAsync();
        switch (intent.getAction()) {
            case SignBoardManager.ACTION_TOGGLE_QUICKTOGGLE:
                if (intent.hasExtra(SignBoardManager.EXTRA_QT_TOGGLE)) {
                    ((App) context.getApplicationContext()).sendQuickToolsAction(intent.getStringExtra(SignBoardManager.EXTRA_QT_TOGGLE));

                    QuickToggles.update(context);
                }
                break;
            case SignBoardManager.ACTION_MUSIC_CONTROL:
                if (intent.hasExtra(SignBoardManager.EXTRA_MUSIC_BUTTON)) {
                    ((App) context.getApplicationContext()).sendMediaEvent(intent.getStringExtra(SignBoardManager.EXTRA_MUSIC_BUTTON));

                    Music.update(context);
                }
                break;
        }
        result.finish();
    }
}
