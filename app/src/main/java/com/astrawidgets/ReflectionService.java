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

        if (now - lastReflectionUpdate < 300) {
            return;
        }

        float rotation =
                event.values[0];

        int reflection;

        if (rotation > 0.15f) {

            reflection = 2;

        } else if (rotation < -0.15f) {

            reflection = 0;

        } else {

            reflection = 1;
        }

        if (reflection != currentReflection) {

            currentReflection = reflection;
            lastReflectionUpdate = now;

            sendReflectionUpdate(
                    reflection);
        }
    }

    private void sendReflectionUpdate(
            int reflection) {

        Intent intent =
                new Intent(
                        this,
                        AstraWidgetProvider.class);

        intent.setAction(
                "com.astrawidgets.ACTION_REFLECTION");

        intent.putExtra(
                "reflection",
                reflection);

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