// File: SettingsActivity.java (Activity Class) - Phase 1 Complete (Error-Free)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/SettingsActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.terrarialoader.R;
import com.terrarialoader.util.LogUtils;

import java.io.File;

public class SettingsActivity extends Activity {

    private static final String PREFS_NAME = "terraria_loader_prefs";
    
    private CheckBox enableModsCheck;
    private CheckBox autoSaveLogsCheck;
    private CheckBox debugModeCheck;
    private CheckBox sandboxModeCheck;
    private Button clearLogsBtn;
    private Button resetModsBtn;
    private Button clearCacheBtn;
    private Button resetSettingsBtn;
    private TextView storageInfo;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        initializeViews();
        loadSettings();
        setupListeners();
        updateStorageInfo();
    }

    private void initializeViews() {
        enableModsCheck = findViewById(R.id.enableModsCheck);
        autoSaveLogsCheck = findViewById(R.id.autoSaveLogsCheck);
        debugModeCheck = findViewById(R.id.debugModeCheck);
        sandboxModeCheck = findViewById(R.id.sandboxModeCheck);
        clearLogsBtn = findViewById(R.id.clearLogsBtn);
        resetModsBtn = findViewById(R.id.resetModsBtn);
        clearCacheBtn = findViewById(R.id.clearCacheBtn);
        resetSettingsBtn = findViewById(R.id.resetSettingsBtn);
        storageInfo = findViewById(R.id.storageInfo);
    }

    private void loadSettings() {
        // Load saved preferences with defaults
        enableModsCheck.setChecked(prefs.getBoolean("enable_mods", true));
        autoSaveLogsCheck.setChecked(prefs.getBoolean("auto_save_logs", false));
        debugModeCheck.setChecked(prefs.getBoolean("debug_mode", false));
        sandboxModeCheck.setChecked(prefs.getBoolean("sandbox_mode", false));
    }

    private void setupListeners() {
        // Enable Mods Toggle
        enableModsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("enable_mods", isChecked).apply();
            LogUtils.logUser("Mods " + (isChecked ? "enabled" : "disabled") + " in settings");
            Toast.makeText(this, "Mods " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Auto-save Logs Toggle
        autoSaveLogsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_save_logs", isChecked).apply();
            LogUtils.setAutoSaveEnabled(isChecked);
            Toast.makeText(this, "Auto-save logs " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Debug Mode Toggle
        debugModeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("debug_mode", isChecked).apply();
            LogUtils.logUser("Debug mode " + (isChecked ? "enabled" : "disabled"));
            Toast.makeText(this, "Debug mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Sandbox Mode Toggle
        sandboxModeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sandbox_mode", isChecked).apply();
            LogUtils.logUser("Sandbox mode " + (isChecked ? "enabled" : "disabled"));
            Toast.makeText(this, "Sandbox mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Clear Logs Button
        clearLogsBtn.setOnClickListener(v -> {
            LogUtils.clearLogs();
            updateStorageInfo();
            Toast.makeText(this, "All logs cleared", Toast.LENGTH_SHORT).show();
        });

        // Reset Mods Button  
        resetModsBtn.setOnClickListener(v -> resetAllMods());

        // Clear Cache Button
        clearCacheBtn.setOnClickListener(v -> clearApplicationCache());

        // Reset Settings Button
        resetSettingsBtn.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            loadSettings(); // Reload default settings
            Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
            LogUtils.logUser("Settings reset to defaults");
        });
    }

    private void resetAllMods() {
        try {
            File modDir = new File(getExternalFilesDir(null), "mods");
            if (modDir.exists()) {
                File[] modFiles = modDir.listFiles();
                if (modFiles != null) {
                    int deletedCount = 0;
                    for (File file : modFiles) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                    Toast.makeText(this, deletedCount + " mods removed", Toast.LENGTH_SHORT).show();
                    LogUtils.logUser(deletedCount + " mods reset via settings");
                }
            } else {
                Toast.makeText(this, "No mods directory found", Toast.LENGTH_SHORT).show();
            }
            updateStorageInfo();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to reset mods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            LogUtils.logDebug("Mod reset failed: " + e.getMessage());
        }
    }

    private void clearApplicationCache() {
        try {
            File cacheDir = getCacheDir();
            long freedSpace = 0;
            
            if (cacheDir.exists()) {
                freedSpace = calculateDirectorySize(cacheDir);
                deleteDirectoryContents(cacheDir);
            }
            
            String freedText = formatFileSize(freedSpace);
            Toast.makeText(this, "Cache cleared (" + freedText + " freed)", Toast.LENGTH_SHORT).show();
            LogUtils.logUser("Cache cleared via settings - " + freedText + " freed");
            updateStorageInfo();
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to clear cache: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            LogUtils.logDebug("Cache clear failed: " + e.getMessage());
        }
    }

    private void updateStorageInfo() {
        try {
            File appDir = getExternalFilesDir(null);
            long totalSize = 0;
            
            if (appDir != null && appDir.exists()) {
                totalSize = calculateDirectorySize(appDir);
            }
            
            String sizeText = formatFileSize(totalSize);
            int logCount = LogUtils.getLogCount();
            int modCount = getModCount();
            
            String info = String.format("Storage: %s | Logs: %d | Mods: %d", 
                                      sizeText, logCount, modCount);
            storageInfo.setText(info);
            
        } catch (Exception e) {
            storageInfo.setText("Storage info unavailable");
            LogUtils.logDebug("Storage info update failed: " + e.getMessage());
        }
    }

    private long calculateDirectorySize(File dir) {
        long size = 0;
        try {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            size += calculateDirectorySize(file);
                        } else {
                            size += file.length();
                        }
                    }
                }
            } else {
                size = dir.length();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Size calculation failed for: " + dir.getAbsolutePath());
        }
        return size;
    }

    private int getModCount() {
        try {
            File modDir = new File(getExternalFilesDir(null), "mods");
            if (modDir.exists()) {
                File[] modFiles = modDir.listFiles((dir, name) -> 
                    name.endsWith(".dex") || name.endsWith(".dex.disabled") ||
                    name.endsWith(".jar") || name.endsWith(".jar.disabled"));
                return modFiles != null ? modFiles.length : 0;
            }
        } catch (Exception e) {
            LogUtils.logDebug("Mod count failed: " + e.getMessage());
        }
        return 0;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void deleteDirectoryContents(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryContents(file);
                    }
                    file.delete();
                }
            }
        }
    }

    // Static utility methods for other classes to check settings
    public static boolean isModsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("enable_mods", true);
    }

    public static boolean isDebugMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("debug_mode", false);
    }

    public static boolean isSandboxMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("sandbox_mode", false);
    }

    public static boolean isAutoSaveEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("auto_save_logs", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStorageInfo(); // Refresh storage info when returning to activity
    }
}