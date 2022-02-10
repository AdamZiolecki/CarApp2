package com.example.carapp2;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class BackgroundService extends Service {
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "BackgroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Add broadcast receiver for plugging in and out here
        ChargeDetection chargeDetector = new ChargeDetection(); //This is the name of the class at the bottom of this code.

        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(chargeDetector, filter);

        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Background service is running")
                .setContentIntent(pendingIntent)
                .build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}

class ChargeDetection extends BroadcastReceiver {
    static int lastActionCode = 0; // 0 - none, 1 - ActionPowerConnected, 2 - ActionPowerDisconnected
    SharedPreferences pref;

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        //Now check if user is charging here. This will only run when device is either plugged in or unplugged.
        DevicePolicyManager deviceManger;
        deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED") && lastActionCode != 1) {
            lastActionCode = 1;

            // Start location updates
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  5000, 10, locationListener);

            Uri uri = new Uri.Builder().scheme("rating").authority("call").build();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(uri);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "AppName:tag");
            wakeLock.acquire();
            wakeLock.release();

        } else if (intent.getAction().equals("android.intent.action.ACTION_POWER_DISCONNECTED") && lastActionCode != 2) {
            lastActionCode = 2;

            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            pref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            Boolean sendLocationEmail = pref.getBoolean("sendLocationEmail", false);
            String userEmail = pref.getString("userEmail", "");
            if (sendLocationEmail) {
                sendLocationEmail(context, userEmail);
            }

            try {
                Thread.sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deviceManger.lockNow();
        }
    }

    void sendLocationEmail(Context context, String userEmail) {
        String longitudeNet = "",
                latitudeNet = "",
                longitudeGps = "",
                latitudeGps = "";

        // getting Network and GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isNetworkEnabled) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.requestSingleUpdate(criteria, locationListener, null);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitudeNet = String.valueOf(location.getLatitude());
                longitudeNet = String.valueOf(location.getLongitude());
            }
        }
        if (isGPSEnabled) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestSingleUpdate(criteria, locationListener, null);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitudeGps = String.valueOf(location.getLatitude());
                longitudeGps = String.valueOf(location.getLongitude());
            }
        }

        String text = "NETWORK:\n" +
                      "https://www.google.com/maps/search/?api=1&query=" + latitudeNet + "," + longitudeNet + "\n" +
                      "GPS: \n" +
                      "https://www.google.com/maps/search/?api=1&query=" + latitudeGps + "," + longitudeGps;

        EmailSender mailSender = new EmailSender();
        mailSender.init(userEmail, "Lokalizacja samochodu", text);
        mailSender.execute();
    }
}
