// File: MainActivity.java (Activity Class) - Fixed Navigation Structure
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/MainActivity.java

package com.terrarialoader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.util.LogUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            stopService(new Intent().setClassName(getPackageName(), "com.itsaky.androidide.logsender.LogSenderService"));
        } catch (Exception ignored) {}

        LogUtils.initialize(getApplicationContext());
        LogUtils.logDebug("MainActivity.onCreate() started");

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LogUtils.logDebug("FATAL CRASH: " + throwable);
            for (StackTraceElement e : throwable.getStackTrace()) {
                LogUtils.logDebug("    at " + e.toString());
            }
        });

        LogUtils.logDebug("Setting up main navigation");

        setContentView(R.layout.activity_main);
        setTitle("Terraria Loader - Main Menu");

        Button universalButton = findViewById(R.id.universal_button);
        Button specificButton = findViewById(R.id.specific_button);

        universalButton.setOnClickListener(v -> {
            LogUtils.logUser("Universal Mode selected");
            startActivity(new Intent(MainActivity.this, UniversalActivity.class));
        });

        specificButton.setOnClickListener(v -> {
            LogUtils.logUser("Specific Mode selected - showing app selection");
            startActivity(new Intent(MainActivity.this, SpecificSelectionActivity.class));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.logDebug("Permission result callback triggered");
    }
}