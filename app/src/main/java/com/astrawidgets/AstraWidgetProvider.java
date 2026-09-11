package com.astrawidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AstraWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views =
                new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        String time = new SimpleDateFormat(
                "HH:mm", Locale.getDefault()).format(new Date());

        String date = new SimpleDateFormat(
                "EEE, dd MMM", Locale.getDefault()).format(new Date());

        views.setTextViewText(R.id.widget_time, time);
        views.setTextViewText(R.id.widget_date, date);

        manager.updateAppWidget(widgetId, views);
    }
}
