package com.example.carapp2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class SelectBackgroundActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_background_activity);

        final int BACKGROUND_THUMBNAILS_IN_ROW = 4;

        this.getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;

        final String pathToBackgroundThumbnails = "backgroundThumbnails";

        String[] backgroundImages = null;
        try {
            backgroundImages = getAssets().list(pathToBackgroundThumbnails);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < backgroundImages.length; i += BACKGROUND_THUMBNAILS_IN_ROW) {
            TableRow tableRow = new TableRow(this);

            for (int j = 0; j < BACKGROUND_THUMBNAILS_IN_ROW; ++j) {
                if (backgroundImages.length <= (j + i)) {
                    break;
                }
                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setPadding(5, 5, 5, 5);
                frameLayout.setLayoutParams(new TableRow.LayoutParams(
                        screenWidth / 4,
                        screenHeight / 4
                ));

                AssetManager assetManager = getAssets();
                InputStream ims = null;
                try {
                    ims = assetManager.open(pathToBackgroundThumbnails + "/" + backgroundImages[i + j]);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("ADAM Out of bound");
                }
                ImageView backgroundThumbnail = new ImageView(this);
                backgroundThumbnail.setTransitionName(backgroundImages[i + j]);
                backgroundThumbnail.setBackground(Drawable.createFromStream(ims, null));
                backgroundThumbnail.setClickable(true);
                backgroundThumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent output = new Intent();
                        output.putExtra("backgroundName", view.getTransitionName());
                        setResult(RESULT_OK, output);
                        finish();
                    }
                });

                frameLayout.addView(backgroundThumbnail);
                tableRow.addView(frameLayout);
            }
            mainLayout.addView(tableRow);
        }
    }
}
