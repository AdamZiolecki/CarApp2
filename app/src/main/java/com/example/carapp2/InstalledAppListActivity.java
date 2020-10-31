package com.example.carapp2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

public class InstalledAppListActivity extends AppCompatActivity {

    private List<AppList> installedApps;
    private AppAdapter installedAppAdapter;
    ListView userInstalledApps;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.installed_app_list);

        this.getSupportActionBar().hide();
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);

        userInstalledApps = (ListView) findViewById(R.id.installed_app_list_view);

        installedApps = getInstalledApps();
        installedAppAdapter = new AppAdapter(this, installedApps);
        userInstalledApps.setAdapter(installedAppAdapter);
        userInstalledApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Drawable appIcon = installedApps.get(i).getIcon();
                String appName = installedApps.get(i).getPackages();
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), );
                Bitmap bitmap = ((BitmapDrawable)appIcon).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] convertedIcon = baos.toByteArray();

                Intent output = new Intent();
                output.putExtra("appName", appName);
                output.putExtra("appIcon", convertedIcon);
                setResult(RESULT_OK, output);
                finish();
            }
        });
    }

    private List<AppList> getInstalledApps () {
        List<AppList> apps = new ArrayList<AppList>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
        for (int i = 0; i < pkgAppsList.size(); ++i) {
            ApplicationInfo p = pkgAppsList.get(i).activityInfo.applicationInfo;
            String appName = p.loadLabel(getPackageManager()).toString();
            Drawable icon = p.loadIcon(getPackageManager());
            String packages = p.packageName;
            apps.add(new AppList(appName, icon, packages));
        }
        return apps;
    }

    private boolean isSystemPackage (PackageInfo pkgInfo){
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
