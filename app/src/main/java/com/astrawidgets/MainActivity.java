package com.astrawidgets;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.appwidget.AppWidgetManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private EditText cityInput;
    private TextView resultText;
    private LinearLayout resultsLayout;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 50, 40, 40);
        root.setBackgroundColor(Color.rgb(20, 20, 25));

        TextView title = new TextView(this);
        title.setText("Astra Widgets");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose your weather location");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        subtitleParams.setMargins(0, 15, 0, 30);
        root.addView(subtitle, subtitleParams);

        cityInput = new EditText(this);
        cityInput.setHint("Enter city name");
        cityInput.setTextColor(Color.WHITE);
        cityInput.setHintTextColor(Color.GRAY);
        cityInput.setSingleLine(true);

        root.addView(cityInput);

        Button searchButton = new Button(this);
        searchButton.setText("Search");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        buttonParams.setMargins(0, 15, 0, 20);
        root.addView(searchButton, buttonParams);

        resultText = new TextView(this);
        resultText.setText("");
        resultText.setTextSize(16);
        resultText.setTextColor(Color.LTGRAY);

        root.addView(resultText);

        resultsLayout = new LinearLayout(this);
        resultsLayout.setOrientation(LinearLayout.VERTICAL);

        root.addView(resultsLayout);

        searchButton.setOnClickListener(v -> searchCity());

        setContentView(root);
    }

    private void searchCity() {

        String city = cityInput.getText().toString().trim();

        if (city.isEmpty()) {
            resultText.setText("Please enter a city.");
            return;
        }

        resultText.setText("Searching...");
        resultsLayout.removeAllViews();

        executor.execute(() -> {

            try {

                String encodedCity =
                        URLEncoder.encode(city, "UTF-8");

                URL url = new URL(
                        "https://geocoding-api.open-meteo.com/v1/search"
                        + "?name=" + encodedCity
                        + "&count=5"
                        + "&language=en"
                        + "&format=json");

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty(
                        "User-Agent",
                        "AstraWidgets/1.0");

                int responseCode =
                        connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception(
                            "Search failed: HTTP "
                                    + responseCode);
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()));

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject json =
                        new JSONObject(response.toString());

                JSONArray results =
                        json.optJSONArray("results");

                runOnUiThread(() -> {

                    resultsLayout.removeAllViews();

                    if (results == null ||
                            results.length() == 0) {

                        resultText.setText(
                                "No cities found.");

                        return;
                    }

                    resultText.setText(
                            "Select a location:");

                    for (int i = 0;
                         i < results.length();
                         i++) {

                        try {

                            JSONObject place =
                                    results.getJSONObject(i);

                            String name =
                                    place.getString("name");

                            String country =
                                    place.optString(
                                            "country",
                                            "");

                            String admin =
                                    place.optString(
                                            "admin1",
                                            "");

                            double latitude =
                                    place.getDouble(
                                            "latitude");

                            double longitude =
                                    place.getDouble(
                                            "longitude");

                            TextView cityView =
                                    new TextView(this);

                            cityView.setText(
                                    name
                                    + "\n"
                                    + admin
                                    + ", "
                                    + country);

                            cityView.setTextSize(18);
                            cityView.setTextColor(
                                    Color.WHITE);
                            cityView.setPadding(
                                    25, 25, 25, 25);

                            cityView.setBackgroundColor(
                                    Color.rgb(
                                            45, 45, 55));

                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(
                                            -1,
                                            -2);

                            params.setMargins(
                                    0, 8, 0, 8);

                            resultsLayout.addView(
                                    cityView,
                                    params);

                            cityView.setOnClickListener(
                                    v -> saveLocation(
                                            name,
                                            latitude,
                                            longitude));

                        } catch (Exception ignored) {
                        }
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        resultText.setText(
                                "Search error: "
                                        + e.getClass()
                                        .getSimpleName()));
            }
        });
    }

    private void saveLocation(
            String name,
            double latitude,
            double longitude) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "astra_weather",
                        MODE_PRIVATE);

        preferences.edit()
                .putString("city", name)
                .putFloat(
                        "latitude",
                        (float) latitude)
                .putFloat(
                        "longitude",
                        (float) longitude)
                .apply();

        resultText.setText(
                "Location saved: " + name);

        resultsLayout.removeAllViews();

        Intent intent =
                new Intent(
                        "android.appwidget.action.APPWIDGET_UPDATE");

        intent.setPackage(getPackageName());

        sendBroadcast(intent);
    }
}