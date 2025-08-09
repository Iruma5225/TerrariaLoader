package com.terrarialoader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.terrarialoader.loader.ModManager;

public class SandboxActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView view = new TextView(this);
        view.setPadding(40, 40, 40, 40);
        view.setText("🔬 Sandbox Mode:\nRunning mods without Terraria...\n\nCheck logcat for mod output.");
        setContentView(view);

        try {
            Log.i("Sandbox", "Loading mods in sandbox...");
            ModManager.loadMods(getApplicationContext());
            Log.i("Sandbox", "Mods loaded.");
        } catch (Exception e) {
            Log.e("Sandbox", "Error during sandbox test", e);
        }
    }
}