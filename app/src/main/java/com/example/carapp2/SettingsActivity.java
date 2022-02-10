package com.example.carapp2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

public class SettingsActivity extends AppCompatActivity {
    final int NUMBER_OF_BUTTONS = 7;

    Switch fullscreenSwitch, screenAlwaysOnSwitch, emailSwitch;
    EditText emailEditText;
    Button resetButton, setBackgroundButton;
    SharedPreferences pref;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        this.getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        fullscreenSwitch = (Switch)  findViewById(R.id.fullscreenSwitch);
        screenAlwaysOnSwitch = (Switch) findViewById(R.id.screenAlwaysOnSwitch);
        emailSwitch = (Switch) findViewById(R.id.emailSwitch);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        resetButton = (Button) findViewById(R.id.resetButton);
        setBackgroundButton = (Button) findViewById(R.id.setBackgroundButton);

        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        loadPreferences();

        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                editor.putBoolean("fullscreenMode", isChecked);
                editor.apply();
            }
        });

        screenAlwaysOnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                editor.putBoolean("screenAlwaysOn", isChecked);
                editor.apply();
            }
        });

        emailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    emailEditText.setEnabled(isChecked);
                    editor.putBoolean("sendLocationEmail", isChecked);
                    editor.apply();
            }
        });

        emailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                editor.putString("userEmail", emailEditText.getText().toString());
                editor.apply();
                return false;
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage("Jesteś pewien?");

                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int k) {
                        for (int i = 0; i < NUMBER_OF_BUTTONS; ++i) {
                            editor.putString("packageName" + i, "");
                            editor.putString("path" + i, "");
                        }
                        editor.apply();
                        Toast.makeText(getBaseContext(), "Zresetowano", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        setBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, SelectBackgroundActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (1): {
                if (resultCode == Activity.RESULT_OK) {
                    String newBackgroundName = data.getStringExtra("backgroundName");
                    newBackgroundName = newBackgroundName.replace("_t.jpg", "");
                    newBackgroundName = newBackgroundName.replace("_t.png", "");

                    final SharedPreferences.Editor editor = pref.edit();
                    editor.putString("background", newBackgroundName);
                    editor.apply();
                }
            }
            break;
        }
    }

    private void loadPreferences() {
        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        fullscreenSwitch.setChecked(pref.getBoolean("fullscreenMode", false));
        screenAlwaysOnSwitch.setChecked(pref.getBoolean("screenAlwaysOn", false));
        emailSwitch.setChecked(pref.getBoolean("sendLocationEmail", false));
        emailEditText.setEnabled(pref.getBoolean("sendLocationEmail", false));
        emailEditText.setText(pref.getString("userEmail", ""));

        if(fullscreenSwitch.isChecked()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(screenAlwaysOnSwitch.isChecked()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}
