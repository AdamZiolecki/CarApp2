package com.example.carapp2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    final String PRIMARY_BTN_NAME = "btnImage";
    final int NUMBER_OF_BUTTONS = 7;

    SharedPreferences pref;

    ConstraintLayout mainActivityLayout;

    List<ImageButton> imgBtnList = new ArrayList<ImageButton>();
    int currentBtnClicked = -1;

    Bitmap addImage, settingsImage;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivityLayout = findViewById(R.id.mainActivityLayout);

        this.getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        Bitmap basicSquareImg = BitmapFactory.decodeResource(getResources(), R.drawable.square_two);
        Bitmap basicAddImg = BitmapFactory.decodeResource(getResources(), R.drawable.add_image);
        Bitmap basicSettingsImg = BitmapFactory.decodeResource(getResources(), R.drawable.settings_image);

        addImage = createSingleImageFromMultipleImages(basicSquareImg, basicAddImg);
        settingsImage = createSingleImageFromMultipleImages(basicSquareImg, basicSettingsImg);

        TableLayout tableLayout = findViewById(R.id.tableLayout);
        TableRow tableRowTop = new TableRow(this);
        tableRowTop.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f
        ));
        TableRow tableRowBot = new TableRow(this);
        tableRowBot.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f
        ));

        for (int i = 0; i < NUMBER_OF_BUTTONS + 1; ++i) {   // All functional buttons + setting button
            ImageButton btn = new ImageButton(this);
            btn.setId(i);
            btn.setPadding(20, 20, 20, 20);
            btn.setScaleType(ImageView.ScaleType.CENTER);
            btn.setAdjustViewBounds(true);
            btn.setBackgroundResource(R.drawable.roundcorner);
            btn.setImageResource(R.drawable.square_two);
            btn.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            btn.setOnClickListener(this);
            btn.setOnLongClickListener(this);

            imgBtnList.add(btn);
            if (i < ((NUMBER_OF_BUTTONS + 1) / 2)) {
                tableRowTop.addView(btn);
            } else {
                tableRowBot.addView(btn);
            }
        }

        tableLayout.addView(tableRowTop);
        tableLayout.addView(tableRowBot);

        loadPreferences();

        startService(new Intent(this, BackgroundService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(pref.getBoolean("fullscreenMode", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(pref.getBoolean("screenAlwaysOn", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        String backgroundName = pref.getString("background", "bg1");
        int resId = getResources().getIdentifier(backgroundName, "drawable", getPackageName());
        mainActivityLayout.setBackgroundResource(resId);

        loadPreferences();
    }

    private void loadPreferences() {
        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        for (int i = 0; i < NUMBER_OF_BUTTONS; ++i) {
            String path = pref.getString("path" + i, "");
            String name = PRIMARY_BTN_NAME + i;
            if (path != "") {
                loadImageFromStorage(path, name, i);
            }
            else {
                imgBtnList.get(i).setImageBitmap(addImage);
            }
        }
        imgBtnList.get(NUMBER_OF_BUTTONS).setImageBitmap(settingsImage);  // Settings button
    }

    public void onClick(final View v) {
        currentBtnClicked = v.getId();

        if (currentBtnClicked == NUMBER_OF_BUTTONS) {   // Settings button
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 1);
        }
        else {
            String packageName = pref.getString("packageName" + currentBtnClicked, "");
            if (packageName != "") {
                System.out.println("PACKAGE: " + packageName);
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    System.out.println("AFTER PACKAGE - START ACITVITY");
                    startActivity(launchIntent); // null pointer check in case package name was not found
                }
            }
            else {
                Intent intent = new Intent(MainActivity.this, InstalledAppListActivity.class);
                startActivityForResult(intent, 1);
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        currentBtnClicked = view.getId();
        if (currentBtnClicked == NUMBER_OF_BUTTONS) {   // Settings button
            return false;
        }

        String packageName = pref.getString("packageName" + currentBtnClicked, "");
        if (packageName != "") {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Czy jesteś pewien, że chcesz usunąć skrót do aplikacji?");

            builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int k) {
                    final SharedPreferences.Editor editor = pref.edit();
                    editor.putString("packageName" + currentBtnClicked, "");
                    editor.putString("path" + currentBtnClicked, "");
                    editor.apply();
                    onResume();
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
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (1): {
                if (resultCode == Activity.RESULT_OK) {
                    String appName = data.getStringExtra("appName");
                    Bundle extras = data.getExtras();
                    byte[] convertedIcon = extras.getByteArray("appIcon");
                    Bitmap bmp = BitmapFactory.decodeByteArray(convertedIcon, 0, convertedIcon.length);

                    Bitmap basicSquareImg = BitmapFactory.decodeResource(getResources(), R.drawable.square_two);
                    Bitmap resultImage = createSingleImageFromMultipleImages(basicSquareImg, bmp);

                    imgBtnList.get(currentBtnClicked).setImageBitmap(resultImage);
                    String name = PRIMARY_BTN_NAME + currentBtnClicked;
                    String absolutePath = saveToInternalStorage(resultImage, name);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("packageName" + currentBtnClicked, appName);
                    editor.putString("path" + currentBtnClicked, absolutePath);
                    editor.apply();
                }
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path, String name, int btnId)
    {
        try {
            File f = new File(path, name);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            imgBtnList.get(btnId).setImageBitmap(b);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage) {
        int canvasWidth = firstImage.getWidth();
        int canvasHeight = firstImage.getHeight();
        Bitmap result = Bitmap.createBitmap(canvasWidth, canvasHeight, firstImage.getConfig());
        Canvas canvas = new Canvas(result);

        int secondImageWidth = (int)(canvasWidth * 0.85);
        int secondImageHeight = (int)(canvasHeight * 0.85);
        Bitmap secondImageScaled = Bitmap.createScaledBitmap(secondImage,
                (int)(firstImage.getWidth()*0.85),
                (int)(firstImage.getHeight()*0.85), false);
        int centreX = (canvasWidth  - secondImageWidth) /2;
        int centreY = (canvasHeight - secondImageHeight) /2;

        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImageScaled, centreX, centreY, null);
        return result;
    }
}