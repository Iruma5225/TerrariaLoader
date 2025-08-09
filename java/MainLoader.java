package com.loader;

import android.app.Application;
import android.util.Log;
import com.terrarialoader.loader.ModManager;
import java.io.*;

public class MainLoader extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("LoaderDex", "MainLoader has started in Terraria");

        if (shouldEnableMods()) {
            ModManager.loadMods(getApplicationContext());
        }
    }

    private boolean shouldEnableMods() {
        File config = new File(getExternalFilesDir(null), "config.txt");
        if (!config.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(config))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("enable_mods=true")) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e("LoaderDex", "Error reading config.txt", e);
        }
        return false;
    }
}