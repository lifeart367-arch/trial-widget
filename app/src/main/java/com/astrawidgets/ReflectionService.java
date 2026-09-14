package com.astrawidgets;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

public class ReflectionService extends Service
        implements SensorEventListener {

    private static final String CHANNEL_ID =
            "astra_reflection";

    private SensorManager sensorManager;
    private Sensor gyroscope;

    private long lastReflectionUpdate = 0;
    private int currentReflection = 1;

    @Override
    public void onCreate() {

        super.onCreate();

        sensorManager =
                (SensorManager) getSystemService(
                        SENSOR_SERVICE);

        if (sensorManager != null) {

            gyroscope =
                    sensorManager.getDefaultSensor(
                            Sensor.TYPE_GYROSCOPE);
        }

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(
                        this,
                        CHANNEL_ID)
                        .setContentTitle(
                                "Astra Widgets")
                        .setContentText(
                                "Reflection effect is active")
                        .setSmallIcon(
                                R.drawable.astra_logo)
                        .setOngoing(true)
                        .build();

        startForeground(
                1001,
                notification);

        if (sensorManager != null &&
                gyroscope != null) {

            sensorManager.registerListener(
                    this,
                    gyroscope,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Astra Reflection",
                            NotificationManager
                                    .IMPORTANCE_LOW);

            channel.setDescription(
                    "Controls the Astra weather widget reflection");

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class);

            if (manager != null) {

                manager.createNotificationChannel(
                        channel);
            }
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        return START_STICKY;
    }

    @Override
public void onSensorChanged(
        SensorEvent event) {

    if (event.sensor.getType() !=
            Sensor.TYPE_GYROSCOPE) {

        return;
    }

    long now =
            System.currentTimeMillis();

    if (now - lastReflectionUpdate < 50) {
        return;
    }

    float rotation =
            event.values[0];

    /*
     * Convert gyroscope movement into
     * a subtle reflection intensity.
     *
     * Small movements produce a gentle shine.
     * Strong movements produce a slightly
     * brighter rim.
     */
    float movement =
            Math.abs(rotation);

    float targetAlpha =
            0.18f +
            Math.min(
                    movement * 0.45f,
                    0.45f);

    /*
     * Smooth the transition instead of
     * jumping directly to the new value.
     */
    float currentAlpha =
            getSharedPreferences(
                    "astra_weather",
                    MODE_PRIVATE)
                    .getFloat(
                            "reflection_alpha",
                            0.22f);

    float smoothedAlpha =
            currentAlpha +
            (targetAlpha - currentAlpha)
                    * 0.18f;

    /*
     * Ignore extremely tiny changes.
     * This prevents flickering.
     */
    if (Math.abs(
            smoothedAlpha -
            currentAlpha) < 0.008f) {

        return;
    }

    lastReflectionUpdate =
            now;

    sendReflectionUpdate(
            smoothedAlpha);
}
    private void sendReflectionUpdate(
        float reflectionAlpha) {

    Intent intent =
            new Intent(
                    this,
                    AstraWidgetProvider.class);

    intent.setAction(
            "com.astrawidgets.ACTION_REFLECTION");

    intent.putExtra(
            "reflection_alpha",
            reflectionAlpha);

    sendBroadcast(intent);
}
    @Override
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy) {
    }

    @Override
    public void onDestroy() {

        if (sensorManager != null) {

            sensorManager.unregisterListener(
                    this);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(
            Intent intent) {

        return null;
    }
}