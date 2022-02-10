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
        if (intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED")){
            //Toast.makeText(context, "CONNECTED", Toast.LENGTH_LONG).show();
            //Intent mainActivityIntent = new Intent(context, MainActivity.class);
            //mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(mainActivityIntent);
        }else if(intent.getAction().equals("android.intent.action.ACTION_POWER_DISCONNECTED")){
            //Toast.makeText(context, "DISCONNECTED", Toast.LENGTH_LONG).show();
        }
    }
}

