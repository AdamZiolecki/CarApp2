package com.example.carapp2;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import androidx.core.app.ActivityCompat;

public class OnPowerReceiver extends BroadcastReceiver {
    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            System.out.println("Action!");
            String toastMessage = "TEST: empty";
            switch (intentAction) {
                case Intent.ACTION_POWER_CONNECTED:
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    toastMessage = "TEST: Connected";
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    toastMessage = "TEST: Disconnected";
                    pref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    Boolean sendLocationEmail = pref.getBoolean("sendLocationEmail", false);
                    String userEmail = pref.getString("userEmail", "");
                    if (sendLocationEmail && userEmail != "") {
                        try {
                            sendLocationEmail(context, userEmail);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    System.exit(0);

                    break;
            }
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void sendLocationEmail(Context context, String userEmail) throws ExecutionException, InterruptedException {
        String longitudeNet = "",
                latitudeNet = "",
                longitudeGps = "",
                latitudeGps = "";

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            // First get location from Network Provider
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(context, "LIPA", Toast.LENGTH_LONG).show();
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                System.out.println("NETWORK");
                if (locationManager != null) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitudeNet = String.valueOf(location.getLatitude());
                        longitudeNet = String.valueOf(location.getLongitude());
                    }
                }
            }
            //get the location by gps
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  5000, 10, locationListener);
                System.out.println("GPS");
                if (locationManager == null) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitudeGps = String.valueOf(location.getLatitude());
                        longitudeGps = String.valueOf(location.getLongitude());
                    }
                }
            }
        }

        String text = "NETWORK:\n" +
                      "https://www.google.com/maps/search/?api=1&query=" + latitudeNet + "," + longitudeNet + "\n" +
                      "GPS: \n" +
                      "https://www.google.com/maps/search/?api=1&query=" + latitudeGps + "," + longitudeGps;
                EmailSender mailSender = new EmailSender();
        mailSender.init(userEmail, "Lokalizacja samochodu", text);
        mailSender.execute().get();
    }


}

