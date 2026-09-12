package com.astrawidgets;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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

    private static final int BG = Color.rgb(7, 13, 31);
    private static final int CARD = Color.rgb(18, 28, 54);
    private static final int TEXT = Color.rgb(244, 247, 255);
    private static final int MUTED = Color.rgb(166, 178, 211);
    private static final int BLUE = Color.rgb(76, 145, 255);
    private static final int PURPLE = Color.rgb(108, 82, 255);

    private EditText cityInput;
    private TextView resultText;
    private LinearLayout resultsLayout;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(
            int color,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private GradientDrawable gradientBackground(
            int start,
            int end,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{start, end});

        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private TextView label(
            String text,
            float size,
            int color) {

        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(
                Typeface.create(
                        "sans-serif",
                        Typeface.NORMAL));

        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BG);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL);

        root.setPadding(
                dp(22),
                dp(30),
                dp(22),
                dp(24));

        root.setBackgroundColor(BG);

        scrollView.addView(root);

        // HEADER

        TextView title =
                label(
                        "Astra Widgets",
                        32,
                        TEXT);

        title.setTypeface(
                Typeface.create(
                        "sans-serif",
                        Typeface.BOLD));

        root.addView(title);

        TextView subtitle =
                label(
                        "Beautiful weather, always with you",
                        16,
                        MUTED);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        subtitleParams.topMargin = dp(5);

        root.addView(
                subtitle,
                subtitleParams);

        // HERO CARD

        LinearLayout hero =
                new LinearLayout(this);

        hero.setOrientation(
                LinearLayout.VERTICAL);

        hero.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(18));

        hero.setBackground(
                gradientBackground(
                        Color.rgb(22, 51, 100),
                        Color.rgb(47, 55, 112),
                        24));

        TextView heroTitle =
                label(
                        "Choose your place",
                        21,
                        TEXT);

        heroTitle.setTypeface(
                Typeface.create(
                        "sans-serif",
                        Typeface.BOLD));

        hero.addView(heroTitle);

        TextView heroText =
                label(
                        "Search for a city and make your home-screen widget yours.",
                        14,
                        Color.rgb(205, 216, 242));

        LinearLayout.LayoutParams heroTextParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        heroTextParams.topMargin = dp(6);

        hero.addView(
                heroText,
                heroTextParams);

        LinearLayout.LayoutParams heroParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        heroParams.topMargin = dp(24);

        root.addView(
                hero,
                heroParams);

        // SEARCH FIELD

        cityInput =
                new EditText(this);

        cityInput.setHint(
                "  Search city...");

        cityInput.setHintTextColor(
                Color.rgb(
                        130,
                        145,
                        180));

        cityInput.setTextColor(TEXT);
        cityInput.setTextSize(17);
        cityInput.setSingleLine(true);

        cityInput.setPadding(
                dp(16),
                0,
                dp(16),
                0);

        cityInput.setBackground(
                roundedBackground(
                        CARD,
                        18));

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58));

        inputParams.topMargin = dp(18);

        root.addView(
                cityInput,
                inputParams);

        // SEARCH BUTTON

        Button searchButton =
                new Button(this);

        searchButton.setText(
                "Search locations");

        searchButton.setTextSize(16);

        searchButton.setTextColor(
                Color.WHITE);

        searchButton.setTypeface(
                Typeface.create(
                        "sans-serif",
                        Typeface.BOLD));

        searchButton.setAllCaps(false);

        searchButton.setGravity(
                Gravity.CENTER);

        searchButton.setPadding(
                0,
                0,
                0,
                0);

        searchButton.setBackground(
                gradientBackground(
                        BLUE,
                        PURPLE,
                        18));

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56));

        buttonParams.topMargin = dp(12);

        root.addView(
                searchButton,
                buttonParams);

        // RESULT STATUS

        resultText =
                label(
                        "",
                        15,
                        MUTED);

        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        resultParams.topMargin = dp(22);

        root.addView(
                resultText,
                resultParams);

        // RESULTS

        resultsLayout =
                new LinearLayout(this);

        resultsLayout.setOrientation(
                LinearLayout.VERTICAL);

        LinearLayout.LayoutParams resultsParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        resultsParams.topMargin = dp(8);

        root.addView(
                resultsLayout,
                resultsParams);

        // FOOTER

        TextView footer =
                label(
                        "Astra Widgets  •  Weather looks better here.",
                        13,
                        Color.rgb(
                                105,
                                120,
                                155));

        footer.setGravity(
                Gravity.CENTER);

        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        footerParams.topMargin = dp(28);

        root.addView(
                footer,
                footerParams);

        searchButton.setOnClickListener(
                v -> searchCity());

        setContentView(scrollView);
    }

    private void searchCity() {

        String city =
                cityInput
                        .getText()
                        .toString()
                        .trim();

        if (city.isEmpty()) {

            resultText.setText(
                    "Please enter a city name.");

            return;
        }

        resultText.setText(
                "Searching for places...");

        resultsLayout.removeAllViews();

        executor.execute(() -> {

            try {

                String encodedCity =
                        URLEncoder.encode(
                                city,
                                "UTF-8");

                URL url =
                        new URL(
                                "https://geocoding-api.open-meteo.com/v1/search"
                                + "?name=" + encodedCity
                                + "&count=5"
                                + "&language=en"
                                + "&format=json");

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

                    throw new Exception(
                            "Search failed: HTTP "
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

                JSONArray results =
                        json.optJSONArray(
                                "results");

                runOnUiThread(() -> {

                    resultsLayout
                            .removeAllViews();

                    if (results == null ||
                            results.length() == 0) {

                        resultText.setText(
                                "No locations found. Try another city.");

                        return;
                    }

                    resultText.setText(
                            "Search results");

                    for (int i = 0;
                         i < results.length();
                         i++) {

                        try {

                            JSONObject place =
                                    results.getJSONObject(i);

                            String name =
                                    place.getString(
                                            "name");

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

                            LinearLayout card =
                                    new LinearLayout(
                                            this);

                            card.setOrientation(
                                    LinearLayout.HORIZONTAL);

                            card.setGravity(
                                    Gravity.CENTER_VERTICAL);

                            card.setPadding(
                                    dp(18),
                                    dp(14),
                                    dp(14),
                                    dp(14));

                            card.setBackground(
                                    roundedBackground(
                                            CARD,
                                            18));

                            card.setClickable(true);
                            card.setFocusable(true);

                            LinearLayout textBox =
                                    new LinearLayout(
                                            this);

                            textBox.setOrientation(
                                    LinearLayout.VERTICAL);

                            TextView nameView =
                                    label(
                                            name,
                                            18,
                                            TEXT);

                            nameView.setTypeface(
                                    Typeface.create(
                                            "sans-serif",
                                            Typeface.BOLD));

                            textBox.addView(
                                    nameView);

                            String location =
                                    admin;

                            if (!admin.isEmpty()
                                    && !country.isEmpty()) {

                                location += ", ";
                            }

                            location += country;

                            TextView detailView =
                                    label(
                                            location,
                                            14,
                                            MUTED);

                            LinearLayout.LayoutParams
                                    detailParams =
                                    new LinearLayout.LayoutParams(
                                            -1,
                                            -2);

                            detailParams.topMargin =
                                    dp(3);

                            textBox.addView(
                                    detailView,
                                    detailParams);

                            card.addView(
                                    textBox,
                                    new LinearLayout.LayoutParams(
                                            0,
                                            -2,
                                            1));

                            TextView arrow =
                                    label(
                                            "›",
                                            30,
                                            Color.rgb(
                                                    130,
                                                    153,
                                                    210));

                            arrow.setGravity(
                                    Gravity.CENTER);

                            card.addView(
                                    arrow,
                                    new LinearLayout.LayoutParams(
                                            dp(35),
                                            -1));

                            LinearLayout.LayoutParams
                                    cardParams =
                                    new LinearLayout.LayoutParams(
                                            -1,
                                            -2);

                            cardParams.topMargin =
                                    dp(8);

                            resultsLayout.addView(
                                    card,
                                    cardParams);

                            card.setOnClickListener(
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
                .putString(
                        "city",
                        name)
                .putFloat(
                        "latitude",
                        (float) latitude)
                .putFloat(
                        "longitude",
                        (float) longitude)
                .apply();

        resultText.setText(
                "✓ Location saved: " + name);

        resultsLayout.removeAllViews();

        // Immediately refresh every existing Astra Widget.

        AppWidgetManager widgetManager =
                AppWidgetManager.getInstance(
                        this);

        ComponentName provider =
                new ComponentName(
                        this,
                        AstraWidgetProvider.class);

        int[] widgetIds =
                widgetManager.getAppWidgetIds(
                        provider);

        if (widgetIds.length > 0) {

            Intent intent =
                    new Intent(
                            AppWidgetManager
                                    .ACTION_APPWIDGET_UPDATE);

            intent.setComponent(provider);

            intent.putExtra(
                    AppWidgetManager
                            .EXTRA_APPWIDGET_IDS,
                    widgetIds);

            sendBroadcast(intent);
        }
    }
}