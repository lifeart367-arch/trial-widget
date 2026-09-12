package com.astrawidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import android.content.SharedPreferences;
import android.app.PendingIntent;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;

public class AstraWidgetProvider extends AppWidgetProvider {
private static final String ACTION_REFRESH =
        "com.astrawidgets.ACTION_REFRESH";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();
@Override
public void onReceive(
        Context context,
        Intent intent) {

    super.onReceive(context, intent);

    if (ACTION_REFRESH.equals(intent.getAction())) {

        AppWidgetManager manager =
                AppWidgetManager.getInstance(context);

        android.content.ComponentName component =
                new android.content.ComponentName(
                        context,
                        AstraWidgetProvider.class);

        int[] widgetIds =
                manager.getAppWidgetIds(component);

        onUpdate(
                context,
                manager,
                widgetIds);
    }
}
    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateWidget(
                    context,
                    appWidgetManager,
                    appWidgetId);
        }
    }

    private void updateWidget(
            Context context,
            AppWidgetManager manager,
            int widgetId) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        "astra_weather",
                        Context.MODE_PRIVATE);

        String city =
                preferences.getString(
                        "city",
                        "Bhubaneswar");

        float latitude =
                preferences.getFloat(
                        "latitude",
                        20.2961f);

        float longitude =
                preferences.getFloat(
                        "longitude",
                        85.8245f);

        RemoteViews views =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.widget_layout);

Intent refreshIntent =
        new Intent(context, AstraWidgetProvider.class);

refreshIntent.setAction(ACTION_REFRESH);

PendingIntent refreshPendingIntent =
        PendingIntent.getBroadcast(
                context,
                widgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

views.setOnClickPendingIntent(
        R.id.widget_refresh,
        refreshPendingIntent);

        String date =
                new SimpleDateFormat(
                        "EEE, dd MMM",
                        Locale.getDefault())
                        .format(new Date());

        views.setTextViewText(
                R.id.widget_date,
                date);

        views.setTextViewText(
                R.id.widget_location,
                city);

        views.setTextViewText(
                R.id.widget_temperature,
                "—°");

        views.setTextViewText(
                R.id.widget_condition,
                "Loading");

        views.setTextViewText(
                R.id.widget_feels,
                "Updating weather...");

        views.setTextViewText(
                R.id.widget_weather_icon,
                "☁");

        manager.updateAppWidget(
                widgetId,
                views);

        EXECUTOR.execute(() -> {

            try {

                URL url =
                        new URL(
                                "https://api.open-meteo.com/v1/forecast"
                                + "?latitude=" + latitude
                                + "&longitude=" + longitude
                                + "&current="
                                + "temperature_2m,"
                                + "apparent_temperature,"
                                + "weather_code"
                                + "&daily="
                                + "weather_code,"
                                + "temperature_2m_max,"
                                + "temperature_2m_min"
                                + "&forecast_days=3"
                                + "&timezone=auto");

                HttpURLConnection connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");

                connection.setConnectTimeout(
                        10000);

                connection.setReadTimeout(
                        10000);

                connection.setRequestProperty(
                        "User-Agent",
                        "AstraWidgets/1.0");

                int responseCode =
                        connection.getResponseCode();

                if (responseCode !=
                        HttpURLConnection.HTTP_OK) {

                    throw new IOException(
                            "Weather API HTTP "
                                    + responseCode);
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection
                                                .getInputStream()));

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line =
                        reader.readLine()) != null) {

                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject json =
                        new JSONObject(
                                response.toString());

                // CURRENT WEATHER

                JSONObject current =
                        json.getJSONObject(
                                "current");

                double temperature =
                        current.getDouble(
                                "temperature_2m");

                double apparentTemperature =
                        current.getDouble(
                                "apparent_temperature");

                int weatherCode =
                        current.getInt(
                                "weather_code");

                String condition =
                        getWeatherCondition(
                                weatherCode);

                String icon =
                        getWeatherIcon(
                                weatherCode);

                // DAILY FORECAST

                JSONObject daily =
                        json.getJSONObject(
                                "daily");

                JSONArray maxTemperatures =
                        daily.getJSONArray(
                                "temperature_2m_max");

                JSONArray minTemperatures =
                        daily.getJSONArray(
                                "temperature_2m_min");

                RemoteViews updatedViews =
                        new RemoteViews(
                                context.getPackageName(),
                                R.layout.widget_layout);

                // DATE

                updatedViews.setTextViewText(
                        R.id.widget_date,
                        new SimpleDateFormat(
                                "EEE, dd MMM",
                                Locale.getDefault())
                                .format(new Date()));

                // LOCATION

                updatedViews.setTextViewText(
                        R.id.widget_location,
                        city);

                // CURRENT TEMPERATURE

                updatedViews.setTextViewText(
                        R.id.widget_temperature,
                        String.format(
                                Locale.getDefault(),
                                "%.0f°",
                                temperature));

                // CONDITION

                updatedViews.setTextViewText(
                        R.id.widget_condition,
                        condition);

                // FEELS LIKE

                updatedViews.setTextViewText(
                        R.id.widget_feels,
                        String.format(
                                Locale.getDefault(),
                                "Feels like %.0f°",
                                apparentTemperature));

                // WEATHER ICON

                updatedViews.setTextViewText(
                        R.id.widget_weather_icon,
                        icon);

                // TODAY

                updatedViews.setTextViewText(
                        R.id.widget_forecast_1,
                        formatForecast(
                                minTemperatures
                                        .getDouble(0),
                                maxTemperatures
                                        .getDouble(0)));

                // TOMORROW

                updatedViews.setTextViewText(
                        R.id.widget_forecast_2,
                        formatForecast(
                                minTemperatures
                                        .getDouble(1),
                                maxTemperatures
                                        .getDouble(1)));

                // NEXT DAY

                updatedViews.setTextViewText(
                        R.id.widget_forecast_3,
                        formatForecast(
                                minTemperatures
                                        .getDouble(2),
                                maxTemperatures
                                        .getDouble(2)));

                // DYNAMIC WEATHER BACKGROUND

                updatedViews.setInt(
                        R.id.widget_root,
                        "setBackgroundResource",
                        getWeatherBackground(
                                weatherCode));

                manager.updateAppWidget(
                        widgetId,
                        updatedViews);

            } catch (Exception e) {

                RemoteViews errorViews =
                        new RemoteViews(
                                context.getPackageName(),
                                R.layout.widget_layout);

                errorViews.setTextViewText(
                        R.id.widget_temperature,
                        "—°");

                errorViews.setTextViewText(
                        R.id.widget_condition,
                        "Weather unavailable");

                errorViews.setTextViewText(
                        R.id.widget_feels,
                        "Check your connection");

                errorViews.setTextViewText(
                        R.id.widget_weather_icon,
                        "☁");

                errorViews.setInt(
                        R.id.widget_root,
                        "setBackgroundResource",
                        R.drawable.weather_cloudy);

                manager.updateAppWidget(
                        widgetId,
                        errorViews);
            }
        });
    }

    private String formatForecast(
            double minimum,
            double maximum) {

        return String.format(
                Locale.getDefault(),
                "%.0f° / %.0f°",
                minimum,
                maximum);
    }

    private String getWeatherCondition(
            int code) {

        if (code == 0) {
            return "Clear";

        } else if (code <= 3) {
            return "Cloudy";

        } else if (code == 45 ||
                code == 48) {
            return "Fog";

        } else if (code >= 51 &&
                code <= 67) {
            return "Rain";

        } else if (code >= 71 &&
                code <= 77) {
            return "Snow";

        } else if (code >= 80 &&
                code <= 82) {
            return "Showers";

        } else if (code >= 95) {
            return "Storm";
        }

        return "Weather";
    }

    private String getWeatherIcon(
            int code) {

        if (code == 0) {
            return "☀";

        } else if (code == 1 ||
                code == 2) {
            return "⛅";

        } else if (code == 3) {
            return "☁";

        } else if (code == 45 ||
                code == 48) {
            return "〰";

        } else if (code >= 51 &&
                code <= 67) {
            return "🌧";

        } else if (code >= 71 &&
                code <= 77) {
            return "❄";

        } else if (code >= 80 &&
                code <= 82) {
            return "🌦";

        } else if (code >= 95) {
            return "⛈";

        }

        return "☁️";
    }

    private int getWeatherBackground(
            int code) {

        if (code == 0) {

            return R.drawable.weather_clear;

        } else if (code <= 3) {

            return R.drawable.weather_cloudy;

        } else if (code == 45 ||
                code == 48) {

            return R.drawable.weather_fog;

        } else if (code >= 51 &&
                code <= 67) {

            return R.drawable.weather_rain;

        } else if (code >= 71 &&
                code <= 77) {

            return R.drawable.weather_cloudy;

        } else if (code >= 80 &&
                code <= 82) {

            return R.drawable.weather_rain;

        } else if (code >= 95) {

            return R.drawable.weather_storm;
        }

        return R.drawable.weather_cloudy;
    }
}