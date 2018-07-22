package com.zacharee1.aospsignboard;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Rect;

public class CustomHost extends AppWidgetHost {
    public CustomHost(Context context) {
        super(context, 1001);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new FixedHostView(context);
    }

    public static class FixedHostView extends AppWidgetHostView {
        public FixedHostView(Context context) {
            super(context);
        }

        @Override
        public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
            super.setAppWidget(appWidgetId, info);

            if (info != null) {
                setPadding(0, 0, 0, 0);
            }
        }
    }
}
