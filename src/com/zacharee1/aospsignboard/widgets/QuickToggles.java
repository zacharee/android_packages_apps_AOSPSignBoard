package com.zacharee1.aospsignboard.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SignBoardManager;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.widgets.qticons.QTIcon;

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
                QTIcon icon = QTIcon.getInstance(context, key);
                RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.quicktoggles_root);
                root.setImageViewResource(icon.getViewId(), icon.getDrawableId());
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
            QTIcon icon = QTIcon.getInstance(context, key);
            RemoteViews button = new RemoteViews(context.getPackageName(), icon.getLayoutId());
            button.setImageViewResource(icon.getViewId(), icon.getDrawableId());
            button.setOnClickPendingIntent(icon.getViewId(), icon.getIntent());
            root.addView(R.id.root, button);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, root.clone());
    }
}
