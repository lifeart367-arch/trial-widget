package com.astrawidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

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

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(
            Context context,
            AppWidgetManager manager,
            int widgetId) {

        RemoteViews views =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.widget_layout);

        

        String date = new SimpleDateFormat(
                "EEE, dd MMM",
                Locale.getDefault()).format(new Date());

        
        views.setTextViewText(R.id.widget_date, date);
        views.setTextViewText(R.id.widget_location, "Bhubaneswar");
        views.setTextViewText(R.id.widget_temperature, "Loading...");

        manager.updateAppWidget(widgetId, views);

        EXECUTOR.execute(() -> {
            try {

                URL url = new URL(
                        "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=20.2961"
                        + "&longitude=85.8245"
                        + "&current=temperature_2m,apparent_temperature,weather_code"
                        + "&timezone=auto");

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "AstraWidgets/1.0");

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                 throw new IOException(
                "Weather API HTTP " + responseCode);
             }

BufferedReader reader =
        new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject json =
                        new JSONObject(response.toString());

                JSONObject current =
                        json.getJSONObject("current");

                double temperature =
                        current.getDouble("temperature_2m");

                int weatherCode =
                        current.getInt("weather_code");

                String condition =
                        getWeatherCondition(weatherCode);

                RemoteViews updatedViews =
                        new RemoteViews(
                                context.getPackageName(),
                                R.layout.widget_layout);

            

                updatedViews.setTextViewText(
                        R.id.widget_date,
                        new SimpleDateFormat(
                                "EEE, dd MMM",
                                Locale.getDefault()).format(new Date()));

                updatedViews.setTextViewText(
                        R.id.widget_location,
                        "Bhubaneswar");
                updatedViews.setInt(
                        R.id.widget_root,
                        "setBackgroundResource",
                        getWeatherBackground(weatherCode));

                updatedViews.setTextViewText(
                        R.id.widget_temperature,
                        String.format(
                                Locale.getDefault(),
                                "%.0f°C • %s",
                                temperature,
                                condition));

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
                         "Weather error: " + e.getClass().getSimpleName());
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

    private String getWeatherCondition(int code) {

        if (code == 0) {
            return "Clear";
        } else if (code <= 3) {
            return "Cloudy";
        } else if (code == 45 || code == 48) {
            return "Fog";
        } else if (code >= 51 && code <= 67) {
            return "Rain";
        } else if (code >= 71 && code <= 77) {
            return "Snow";
        } else if (code >= 80 && code <= 82) {
            return "Showers";
        } else if (code >= 95) {
            return "Storm";
        }

        return "Weather";
            
    }

    private int getWeatherBackground(int code) {

        if (code == 0) {
            return R.drawable.weather_clear;

        } else if (code <= 3) {
            return R.drawable.weather_cloudy;

        } else if (code == 45 || code == 48) {
            return R.drawable.weather_fog;

        } else if (code >= 51 && code <= 67) {
            return R.drawable.weather_rain;

        } else if (code >= 71 && code <= 77) {
            return R.drawable.weather_cloudy;

        } else if (code >= 80 && code <= 82) {
            return R.drawable.weather_rain;

        } else if (code >= 95) {
            return R.drawable.weather_storm;
        }

        return R.drawable.weather_cloudy;
    }
}
