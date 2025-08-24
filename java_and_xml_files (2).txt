/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
        tools:ignore="ScopedStorage" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    <uses-permission android:name="android.permission.INSTALL_PACKAGES"
        tools:ignore="ProtectedPermissions" />

    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:fullBackupContent="@xml/backup_rules"
        android:label="Terraria Loader"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MaterialComponents.DayNight.DarkActionBar"
        android:requestLegacyExternalStorage="true">

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/paths" />
        </provider>

        <!-- Main launcher activity -->
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Core activities -->
        <activity android:name=".TerrariaSpecificActivity" android:exported="false"/>
        <activity android:name=".AboutActivity" android:exported="false"/>
        <activity android:name=".UniversalActivity" android:exported="false"/>
        <activity android:name=".SpecificSelectionActivity" android:exported="false"/>
        <activity android:name=".ui.OfflineDiagnosticActivity" android:exported="false"/>

        <!-- NEW: Unified Loader Activity (replaces TerrariaActivity and DllModActivity) -->
        <activity android:name=".ui.UnifiedLoaderActivity" android:exported="false"/>
        
        <!-- Updated: Pure mod management only -->
        <activity android:name=".ui.ModManagementActivity" android:exported="false"/>
        <activity android:name=".ui.ModListActivity" android:exported="false"/>
        
        <!-- Updated: Setup guide with offline ZIP import -->
        <activity android:name=".ui.SetupGuideActivity" android:exported="false"/>
        
        <!-- Utility activities -->
        <activity android:name=".ui.SettingsActivity" android:exported="false"/>
        <activity android:name=".ui.LogViewerActivity" android:exported="false"/>
        <activity android:name=".ui.InstructionsActivity" android:exported="false"/>
        <activity android:name=".SandboxActivity" android:exported="false"/>
        
        
    </application>
</manifest>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/AboutActivity.java
package com.terrarialoader;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("About");
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/LogActivity.java
package com.terrarialoader;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogActivity extends AppCompatActivity {

    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Logs");
        setContentView(R.layout.activity_log);

        logText = findViewById(R.id.log_text);
        ScrollView scrollView = findViewById(R.id.log_scroll);

        logText.setText(LogUtils.getLogs());
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

        findViewById(R.id.export_logs_button).setOnClickListener(v -> exportLogs());
    }

    private void exportLogs() {
        try {
            File logDir = new File(getExternalFilesDir(null), "exports");
            if (!logDir.exists()) logDir.mkdirs();

            File logFile = new File(logDir, "logs.txt");
            FileWriter writer = new FileWriter(logFile);
            writer.write(LogUtils.getLogs());
            writer.close();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, getPackageName() + ".provider", logFile));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Logs"));

            Toast.makeText(this, "Logs exported", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export logs", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/MainActivity.java
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

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/MyApplication.java
// File: MyApplication.java (FIXED) - Initialize Logs and Handle Migration
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/MyApplication.java

package com.terrarialoader;

import android.app.Application;
import android.os.Environment;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.installer.ModInstaller;
import java.io.File;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // FIXED: Initialize LogUtils first
        LogUtils.initialize(getApplicationContext());
        
        // FIXED: Initialize app startup logging
        LogUtils.initializeAppStartup();
        
        // Auto-create TerrariaLoader directory structure on first run
        initializeTerrariaLoaderStructure();
        
        // FIXED: Handle migration from legacy structure
        handleMigration();
    }
    
    private void initializeTerrariaLoaderStructure() {
        try {
            LogUtils.logUser("🚀 Initializing TerrariaLoader directory structure...");
            
            // The primary and most reliable location for app files.
            File baseDir = getExternalFilesDir(null);
            if (baseDir == null) {
                LogUtils.logDebug("❌ External storage is not available or app-specific directory is null.");
                return;
            }

            // FIXED: Use new directory structure
            File terrariaLoaderDir = new File(baseDir, "TerrariaLoader");
            File gameDir = new File(terrariaLoaderDir, "com.and.games505.TerrariaPaid");

            if (!gameDir.exists()) {
                if (gameDir.mkdirs()) {
                    LogUtils.logUser("✅ Created base TerrariaLoader directory at: " + gameDir.getAbsolutePath());
                } else {
                    LogUtils.logDebug("❌ Failed to create base directory: " + gameDir.getAbsolutePath());
                    return; // Stop if we can't create the main folder
                }
            }
            
            // Create Terraria-specific directories manually inside the new base directory
            createTerrariaDirectories(gameDir);
            
        } catch (Exception e) {
            LogUtils.logDebug("Directory initialization error: " + e.getMessage());
        }
    }
    
    private void createTerrariaDirectories(File gameBaseDir) {
        try {
            String basePath = gameBaseDir.getAbsolutePath();
            
            // FIXED: Updated directory structure with correct paths
            String[] directories = {
                basePath,
                basePath + "/Mods",
                basePath + "/Mods/DEX",      // FIXED: DEX mods directory
                basePath + "/Mods/DLL",      // FIXED: DLL mods directory
                basePath + "/Config",
                basePath + "/Logs",          // FIXED: Game logs (MelonLoader logs)
                basePath + "/AppLogs",       // FIXED: App logs (TerrariaLoader logs)
                basePath + "/Backups",
                basePath + "/Loaders",
                basePath + "/Loaders/MelonLoader",
                basePath + "/Loaders/MelonLoader/net8",
                basePath + "/Loaders/MelonLoader/net35",
                basePath + "/Loaders/MelonLoader/Dependencies",
                basePath + "/Loaders/MelonLoader/Dependencies/SupportModules",
                basePath + "/Loaders/MelonLoader/Dependencies/CompatibilityLayers",
                basePath + "/Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator",
                basePath + "/Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Cpp2IL/cpp2il_out",
                basePath + "/Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/UnityDependencies",
                basePath + "/Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Il2CppInterop/Il2CppAssemblies",
                // FIXED: Plugins and UserLibs at game root level, not inside MelonLoader
                basePath + "/Plugins",       // FIXED: Plugins at root level
                basePath + "/UserLibs"       // FIXED: UserLibs at root level
            };
            
            int createdCount = 0;
            for (String dirPath : directories) {
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        createdCount++;
                        LogUtils.logDebug("✅ Created: " + dirPath);
                    } else {
                        LogUtils.logDebug("❌ Failed: " + dirPath);
                    }
                } else {
                    LogUtils.logDebug("📁 Exists: " + dirPath);
                }
            }
            
            if (createdCount > 0) {
                LogUtils.logUser("✅ TerrariaLoader directory structure created successfully");
                LogUtils.logUser("📁 Path: " + basePath);
                LogUtils.logUser("📊 Created " + createdCount + " directories");
                createInfoFiles(basePath);
            } else {
                LogUtils.logUser("📁 TerrariaLoader directories already exist");
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Manual directory creation error: " + e.getMessage());
        }
    }
    
    private void createInfoFiles(String basePath) {
        try {
            // FIXED: Create README files in correct directories
            
            // DEX mods README
            File dexReadme = new File(basePath + "/Mods/DEX", "README.txt");
            if (!dexReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(dexReadme)) {
                    writer.write("=== TerrariaLoader - DEX/JAR Mods ===\n\n");
                    writer.write("Place your .dex and .jar mod files here.\n");
                    writer.write("Supported formats:\n");
                    writer.write("• .dex files (enabled)\n");
                    writer.write("• .jar files (enabled)\n");
                    writer.write("• .dex.disabled files (disabled)\n");
                    writer.write("• .jar.disabled files (disabled)\n\n");
                    writer.write("Path: " + dexReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created DEX mods README");
            }
            
            // DLL mods README
            File dllReadme = new File(basePath + "/Mods/DLL", "README.txt");
            if (!dllReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(dllReadme)) {
                    writer.write("=== TerrariaLoader - DLL Mods ===\n\n");
                    writer.write("Place your .dll mod files here.\n");
                    writer.write("Requires MelonLoader to be installed.\n");
                    writer.write("Supported formats:\n");
                    writer.write("• .dll files (enabled)\n");
                    writer.write("• .dll.disabled files (disabled)\n\n");
                    writer.write("Path: " + dllReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created DLL mods README");
            }
            
            // FIXED: Plugins README
            File pluginsReadme = new File(basePath + "/Plugins", "README.txt");
            if (!pluginsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(pluginsReadme)) {
                    writer.write("=== MelonLoader Plugins Directory ===\n\n");
                    writer.write("This directory contains MelonLoader plugins.\n");
                    writer.write("Plugins extend MelonLoader functionality.\n\n");
                    writer.write("Files you might see here:\n");
                    writer.write("• .dll plugin files\n");
                    writer.write("• Plugin configuration files\n\n");
                    writer.write("Path: " + pluginsReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created Plugins README");
            }
            
            // FIXED: UserLibs README
            File userlibsReadme = new File(basePath + "/UserLibs", "README.txt");
            if (!userlibsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(userlibsReadme)) {
                    writer.write("=== MelonLoader UserLibs Directory ===\n\n");
                    writer.write("This directory contains user libraries.\n");
                    writer.write("Libraries that mods depend on go here.\n\n");
                    writer.write("Files you might see here:\n");
                    writer.write("• .dll library files\n");
                    writer.write("• Shared mod dependencies\n\n");
                    writer.write("Path: " + userlibsReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created UserLibs README");
            }
            
            // FIXED: Game logs README
            File gameLogsReadme = new File(basePath + "/Logs", "README.txt");
            if (!gameLogsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(gameLogsReadme)) {
                    writer.write("=== MelonLoader Game Logs ===\n\n");
                    writer.write("This directory contains logs from MelonLoader and mods.\n");
                    writer.write("These are generated when running the patched game.\n\n");
                    writer.write("Log files follow rotation:\n");
                    writer.write("• Log.txt (current log)\n");
                    writer.write("• Log1.txt to Log5.txt (previous logs)\n");
                    writer.write("• Oldest logs are automatically deleted\n\n");
                    writer.write("Path: " + gameLogsReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created Game logs README");
            }
            
            // FIXED: App logs README
            File appLogsReadme = new File(basePath + "/AppLogs", "README.txt");
            if (!appLogsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(appLogsReadme)) {
                    writer.write("=== TerrariaLoader App Logs ===\n\n");
                    writer.write("This directory contains logs from TerrariaLoader app.\n");
                    writer.write("These are generated when using this app.\n\n");
                    writer.write("Log files follow rotation:\n");
                    writer.write("• AppLog.txt (current log)\n");
                    writer.write("• AppLog1.txt to AppLog5.txt (previous logs)\n");
                    writer.write("• Oldest logs are automatically deleted\n\n");
                    writer.write("Path: " + appLogsReadme.getAbsolutePath() + "\n");
                }
                LogUtils.logDebug("✅ Created App logs README");
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Info file creation error: " + e.getMessage());
        }
    }
    
    // FIXED: Handle migration from legacy structure
    private void handleMigration() {
        try {
            LogUtils.logUser("🔍 Checking for legacy structure migration...");
            
            // Check if migration is needed for mods
            if (ModInstaller.needsMigration(this)) {
                LogUtils.logUser("📦 Legacy mods found - starting migration...");
                if (ModInstaller.migrateLegacyMods(this)) {
                    LogUtils.logUser("✅ Successfully migrated legacy mods");
                } else {
                    LogUtils.logUser("⚠️ Migration completed with some issues");
                }
            }
            
            // Check if migration is needed for app logs
            migrateLegacyAppLogs();
            
            LogUtils.logUser("🔍 Migration check completed");
            
        } catch (Exception e) {
            LogUtils.logDebug("Migration error: " + e.getMessage());
        }
    }
    
    // FIXED: Migrate legacy app logs
    private void migrateLegacyAppLogs() {
        try {
            File legacyLogsDir = new File(getExternalFilesDir(null), "logs");
            if (!legacyLogsDir.exists()) {
                return; // No legacy logs to migrate
            }
            
            File[] legacyLogFiles = legacyLogsDir.listFiles((dir, name) -> 
                name.startsWith("auto_save_") || name.endsWith(".txt"));
            
            if (legacyLogFiles == null || legacyLogFiles.length == 0) {
                return; // No log files to migrate
            }
            
            LogUtils.logUser("📋 Migrating " + legacyLogFiles.length + " legacy log files...");
            
            // Create new app logs directory
            File gameBaseDir = new File(getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File newAppLogsDir = new File(gameBaseDir, "AppLogs");
            if (!newAppLogsDir.exists()) {
                newAppLogsDir.mkdirs();
            }
            
            int migratedCount = 0;
            for (File legacyLogFile : legacyLogFiles) {
                // Rename to new format
                String newName = "AppLog" + (migratedCount + 1) + ".txt";
                File newLocation = new File(newAppLogsDir, newName);
                
                if (legacyLogFile.renameTo(newLocation)) {
                    migratedCount++;
                    LogUtils.logDebug("Migrated log: " + legacyLogFile.getName() + " -> " + newName);
                }
            }
            
            LogUtils.logUser("✅ Migrated " + migratedCount + " log files to new structure");
            
            // Remove legacy logs directory if empty
            if (legacyLogsDir.listFiles() == null || legacyLogsDir.listFiles().length == 0) {
                legacyLogsDir.delete();
                LogUtils.logDebug("Removed empty legacy logs directory");
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Legacy logs migration error: " + e.getMessage());
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/SandboxActivity.java
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

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/SpecificSelectionActivity.java
// File: SpecificSelectionActivity.java (Fixed Activity Class) - Fixed App Selection
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/SpecificSelectionActivity.java

package com.terrarialoader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.util.LogUtils;

public class SpecificSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_selection);
        setTitle("Select Game/App");

        LogUtils.logDebug("SpecificSelectionActivity started");

        // Add header text
        TextView headerText = findViewById(R.id.headerText);
        if (headerText != null) {
            headerText.setText("Choose which game/app to mod:");
        }

        // Setup Terraria button - FIXED: Changed to TerrariaSpecificActivity
        Button terrariaBtn = findViewById(R.id.terraria_button);
        terrariaBtn.setOnClickListener(v -> {
            LogUtils.logUser("Terraria selected for specific modding");
            Intent intent = new Intent(this, TerrariaSpecificActivity.class);
            startActivity(intent);
        });

        // Add future games section
        LinearLayout futureGamesSection = findViewById(R.id.futureGamesSection);
        if (futureGamesSection != null) {
            TextView futureText = new TextView(this);
            futureText.setText("More games coming soon:\n• Minecraft PE\n• Among Us\n• Other Unity Games");
            futureText.setTextSize(14);
            futureText.setPadding(16, 16, 16, 16);
            futureText.setTextColor(getColor(android.R.color.darker_gray));
            futureGamesSection.addView(futureText);
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/TerrariaSpecificActivity.java
// File: TerrariaSpecificActivity.java (Updated) - Elegant UI with Dynamic Theming
// Path: /main/java/com/terrarialoader/TerrariaSpecificActivity.java

package com.terrarialoader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.terrarialoader.R;
import com.terrarialoader.ui.UnifiedLoaderActivity;
import com.terrarialoader.ui.ModListActivity;
import com.terrarialoader.ui.SettingsActivity;
import com.terrarialoader.ui.LogViewerActivity;
import com.terrarialoader.ui.InstructionsActivity;
import com.terrarialoader.SandboxActivity;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.ui.OfflineDiagnosticActivity;


public class TerrariaSpecificActivity extends AppCompatActivity {

    private LinearLayout rootLayout;
    private TextView loaderStatusText;
    private CardView setupCard;
    private CardView modManagementCard;
    private CardView toolsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terraria_specific_updated);
        
        setTitle("🌍 Terraria Mod Loader");
        
        LogUtils.logUser("Terraria Menu Activity opened");
        
        initializeViews();
        applyTerrariaTheme();
        setupUI();
        updateLoaderStatus();
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.rootLayout);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        setupCard = findViewById(R.id.setupCard);
        modManagementCard = findViewById(R.id.modManagementCard);
        toolsCard = findViewById(R.id.toolsCard);
    }

    private void applyTerrariaTheme() {
        // Dynamic Terraria-inspired green/blue theme
        int primaryGreen = Color.parseColor("#4CAF50");
        int secondaryBlue = Color.parseColor("#2196F3");
        int accentGreen = Color.parseColor("#81C784");
        
        // Apply theme to root layout
        rootLayout.setBackgroundColor(Color.parseColor("#E8F5E8"));
        
        // Apply theme to cards
        setupCard.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
        modManagementCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        toolsCard.setCardBackgroundColor(Color.parseColor("#F3E5F5"));
        
        // Set status bar color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryGreen);
        }
    }

    private void setupUI() {
        // === SETUP SECTION ===
        
        // Unified Loader Setup (NEW - replaces multiple buttons)
        Button unifiedSetupBtn = findViewById(R.id.unifiedSetupButton);
        unifiedSetupBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Unified MelonLoader Setup");
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
        });
        
        // Quick Setup Guide
        Button setupGuideBtn = findViewById(R.id.setupGuideButton);
        setupGuideBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Setup Guide");
            Intent intent = new Intent(this, com.terrarialoader.ui.SetupGuideActivity.class);
            startActivity(intent);
        });
        
        // Manual Instructions (fallback)
        Button manualInstructionsBtn = findViewById(R.id.manualInstructionsButton);
        manualInstructionsBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Manual Installation Instructions");
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        // === MOD MANAGEMENT SECTION ===
        
        // DEX/JAR Mod Manager
        Button dexModManagerBtn = findViewById(R.id.dexModManagerButton);
        dexModManagerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening DEX/JAR Mod Manager");
            Intent intent = new Intent(this, ModListActivity.class);
            startActivity(intent);
        });
        
        // DLL Mod Manager (redirects to unified system)
        Button dllModManagerBtn = findViewById(R.id.dllModManagerButton);
        dllModManagerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening DLL Mod Management via Unified Loader");
            
            // Check if loader is installed
            if (MelonLoaderManager.isMelonLoaderInstalled(this) || MelonLoaderManager.isLemonLoaderInstalled(this)) {
                // If loader is installed, go directly to mod management
                Intent intent = new Intent(this, com.terrarialoader.ui.ModManagementActivity.class);
                startActivity(intent);
            } else {
                // If no loader, start unified setup wizard
                Intent intent = new Intent(this, UnifiedLoaderActivity.class);
                startActivity(intent);
            }
        });
        
        // === TOOLS SECTION ===
        
        // Log Viewer
        Button logViewerBtn = findViewById(R.id.logViewerButton);
        logViewerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Log Viewer");
            Intent intent = new Intent(this, LogViewerActivity.class);
            startActivity(intent);
        });
        
        // Settings
        Button settingsBtn = findViewById(R.id.settingsButton);
        settingsBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Settings");
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // Sandbox Mode
        Button sandboxBtn = findViewById(R.id.sandboxButton);
        sandboxBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Sandbox Mode");
            Intent intent = new Intent(this, SandboxActivity.class);
            startActivity(intent);
            
        });    
            
        // Diagnostic Tool
        Button diagnosticBtn = findViewById(R.id.diagnosticButton);
        diagnosticBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Offline Diagnostic Tool");
            Intent intent = new Intent(this, OfflineDiagnosticActivity.class);
            startActivity(intent);
   
        });
        
        // === NAVIGATION ===
        
        // Back to App Selection
        Button backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            LogUtils.logUser("Returning to app selection");
            finish();
        });
    }

    private void updateLoaderStatus() {
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(this);
        
        String statusText;
        int statusColor;
        
        if (melonInstalled) {
            statusText = "✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " ready for DLL mods";
            statusColor = Color.parseColor("#4CAF50"); // Green
        } else if (lemonInstalled) {
            statusText = "✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " ready for DLL mods";
            statusColor = Color.parseColor("#4CAF50"); // Green
        } else {
            statusText = "⚠️ No loader installed - Use 'Complete Setup Wizard' to install MelonLoader";
            statusColor = Color.parseColor("#FF9800"); // Orange
        }
        
        loaderStatusText.setText(statusText);
        loaderStatusText.setTextColor(statusColor);
        
        // Update button states based on loader status
        updateButtonStates(melonInstalled || lemonInstalled);
    }

    private void updateButtonStates(boolean loaderInstalled) {
        Button dllModManagerBtn = findViewById(R.id.dllModManagerButton);
        
        if (loaderInstalled) {
            dllModManagerBtn.setText("🔧 Manage DLL Mods");
            dllModManagerBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            dllModManagerBtn.setText("🔧 Setup DLL Support");
            dllModManagerBtn.setBackgroundColor(Color.parseColor("#FF9800"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loader status when returning to this activity
        updateLoaderStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.logUser("Terraria Menu Activity closed");
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/UniversalActivity.java
package com.terrarialoader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.util.LogUtils;

public class UniversalActivity extends AppCompatActivity {

    private Uri apkUri;
    private Uri zipUri;

    private TextView statusText;

    private final ActivityResultLauncher<Intent> apkPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    apkUri = result.getData().getData();
                    statusText.setText("Universal APK selected:\n" + apkUri.getPath());
                    LogUtils.logUser("Universal - Selected APK: " + apkUri);
                }
            });

    private final ActivityResultLauncher<Intent> zipPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    zipUri = result.getData().getData();
                    statusText.setText("Loader ZIP selected:\n" + zipUri.getPath());
                    LogUtils.logUser("Universal - Selected ZIP: " + zipUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universal);

        LogUtils.logDebug("UniversalActivity launched");

        Button selectApkBtn = findViewById(R.id.select_apk_button);
        Button selectZipBtn = findViewById(R.id.select_zip_button);
        Button injectBtn = findViewById(R.id.inject_button);
        statusText = findViewById(R.id.status_text);

        selectApkBtn.setOnClickListener(v -> openApkPicker());
        selectZipBtn.setOnClickListener(v -> openZipPicker());

        injectBtn.setOnClickListener(v -> {
            if (apkUri == null || zipUri == null) {
                Toast.makeText(this, "Please select both APK and ZIP", Toast.LENGTH_SHORT).show();
                return;
            }
            injectLoader();
        });
    }

    private void openApkPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        apkPickerLauncher.launch(Intent.createChooser(intent, "Select APK"));
    }

    private void openZipPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        zipPickerLauncher.launch(Intent.createChooser(intent, "Select Loader ZIP"));
    }

    private void injectLoader() {
        LogUtils.logUser("Universal - Injecting loader...");
        statusText.setText("Injecting loader into APK...\n(This is a placeholder operation)");

        Toast.makeText(this, "Injection (mocked)", Toast.LENGTH_SHORT).show();
        LogUtils.logUser("Universal - Injection completed (simulated)");
    }

    private void exportModifiedApk() {
        Toast.makeText(this, "Exporting modified APK (not implemented)", Toast.LENGTH_SHORT).show();
    }

    private void exportLogs() {
        Toast.makeText(this, "Exporting logs (not implemented)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_log) {
            startActivity(new Intent(this, LogActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_dark_mode) {
            Toast.makeText(this, "Toggle dark mode (not implemented)", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_export_apk) {
            exportModifiedApk();
            return true;
        } else if (id == R.id.action_export_logs) {
            exportLogs();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/Diagnostic/DiagnosticManager.java
package com.terrarialoader.diagnostic;

import android.content.Context;
import android.os.Environment;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiagnosticManager {
    private final Context context;
    private final File gameBaseDir;
    private final List<String> reportLines = new ArrayList<>();
    private final Map<String, Boolean> checks = new LinkedHashMap<>();
    private final String gamePackage = "com.and.games505.TerrariaPaid";

    public DiagnosticManager(Context context) {
        this.context = context;
        this.gameBaseDir = new File(context.getExternalFilesDir(null), "TerrariaLoader/" + gamePackage);
    }

    public String runFullDiagnostics() {
        log("🔧 Starting Offline Diagnostics...");
        reportLines.clear();
        checks.clear();

        checkDirectoryStructure();
        checkLoaderPresence();
        checkFileIntegrity();
        checkPermissions();
        checkLogRotation();
        checkReadmes();

        log("📊 Diagnostics completed.");
        generateReport();
        return getFormattedReport();
    }

    public boolean attemptSelfRepair() {
        log("🛠️ Starting repair process...");
        boolean success = true;

        for (String path : getExpectedDirectories()) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    log("✅ Created: " + dir.getAbsolutePath());
                } else {
                    log("❌ Failed to create: " + dir.getAbsolutePath());
                    success = false;
                }
            }
        }

        createReadmes();
        rotateLogsIfNeeded();

        log(success ? "✅ Repair completed successfully." : "⚠️ Repair completed with issues.");
        return success;
    }

    private void checkDirectoryStructure() {
        log("📂 Checking directory structure...");
        for (String path : getExpectedDirectories()) {
            File f = new File(path);
            boolean exists = f.exists() && f.isDirectory();
            checks.put("Directory: " + f.getName(), exists);
        }
    }

    private void checkLoaderPresence() {
        log("🛠️ Checking loader status...");
        File melonDir = new File(gameBaseDir, "Loaders/MelonLoader");
        checks.put("MelonLoader Installed", melonDir.exists());

        File net8 = new File(melonDir, "net8");
        File net35 = new File(melonDir, "net35");
        File deps = new File(melonDir, "Dependencies");
        checks.put("ML/net8 Present", net8.exists());
        checks.put("ML/net35 Present", net35.exists());
        checks.put("ML/Dependencies Present", deps.exists());
    }

    private void checkFileIntegrity() {
        log("🔍 Checking file integrity...");
        File logsDir = new File(gameBaseDir, "Logs");
        File log = new File(logsDir, "Log.txt");
        checks.put("Log.txt Exists", log.exists());

        File[] oldLogs = logsDir.listFiles((dir, name) -> name.matches("Log[1-5]\\.txt"));
        checks.put("Old Logs Present (1-5)", oldLogs != null && oldLogs.length > 0);
    }

    private void checkPermissions() {
        log("🔐 Checking permissions...");
        File test = new File(context.getExternalFilesDir(null), "perm_test.txt");
        try {
            FileWriter writer = new FileWriter(test);
            writer.write("test");
            writer.close();
            checks.put("Write Permission", true);
            test.delete();
        } catch (IOException e) {
            checks.put("Write Permission", false);
        }
    }

    private void checkLogRotation() {
        log("🕒 Checking log rotation...");
        File logsDir = new File(gameBaseDir, "AppLogs");
        File[] logFiles = logsDir.listFiles((dir, name) -> name.startsWith("AppLog"));
        checks.put("AppLogs Present", logFiles != null && logFiles.length > 0);
    }

    private void checkReadmes() {
        log("📘 Checking README files...");
        File[] dirs = new File[]{
            new File(gameBaseDir, "Mods/DEX"),
            new File(gameBaseDir, "Mods/DLL"),
            new File(gameBaseDir, "Plugins"),
            new File(gameBaseDir, "UserLibs"),
            new File(gameBaseDir, "Logs"),
            new File(gameBaseDir, "AppLogs")
        };

        for (File dir : dirs) {
            File readme = new File(dir, "README.txt");
            checks.put(dir.getName() + "/README Present", readme.exists());
        }
    }

    private void rotateLogsIfNeeded() {
        File logsDir = new File(gameBaseDir, "AppLogs");
        File currentLog = new File(logsDir, "AppLog.txt");
        if (currentLog.exists()) {
            for (int i = 5; i >= 1; i--) {
                File prev = new File(logsDir, "AppLog" + i + ".txt");
                if (prev.exists() && i == 5) prev.delete();
                else if (prev.exists())
                    prev.renameTo(new File(logsDir, "AppLog" + (i + 1) + ".txt"));
            }
            currentLog.renameTo(new File(logsDir, "AppLog1.txt"));
        }
    }

    private void createReadmes() {
        String[] dirs = {
            "Mods/DEX", "Mods/DLL", "Plugins", "UserLibs", "Logs", "AppLogs"
        };

        for (String relPath : dirs) {
            File dir = new File(gameBaseDir, relPath);
            File readme = new File(dir, "README.txt");
            if (!readme.exists()) {
                try {
                    FileWriter writer = new FileWriter(readme);
                    writer.write("README for " + relPath + "\nThis directory is used by TerrariaLoader.");
                    writer.close();
                    log("📘 Created README in: " + relPath);
                } catch (IOException e) {
                    log("❌ Failed to write README in: " + relPath);
                }
            }
        }
    }

    private void generateReport() {
        reportLines.add("=== TerrariaLoader Offline Diagnostic Report ===");
        reportLines.add("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        reportLines.add("Base Directory: " + gameBaseDir.getAbsolutePath());
        reportLines.add("\nChecks:");

        for (Map.Entry<String, Boolean> entry : checks.entrySet()) {
            String icon = entry.getValue() ? "✅" : "❌";
            reportLines.add(icon + " " + entry.getKey());
        }

        reportLines.add("\nSuggestions:");
        for (Map.Entry<String, Boolean> entry : checks.entrySet()) {
            if (!entry.getValue()) {
                reportLines.add("• Fix: " + entry.getKey());
            }
        }
    }

    public boolean exportReportToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : reportLines) {
                writer.write(line + "\n");
            }
            log("✅ Report exported: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log("❌ Failed to export report: " + e.getMessage());
            return false;
        }
    }

    public String getFormattedReport() {
        StringBuilder sb = new StringBuilder();
        for (String line : reportLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private void log(String message) {
        LogUtils.logDebug("[DiagnosticManager] " + message);
    }

    private String[] getExpectedDirectories() {
        String base = gameBaseDir.getAbsolutePath();
        return new String[]{
            base,
            base + "/Mods", base + "/Mods/DEX", base + "/Mods/DLL",
            base + "/Config", base + "/Logs", base + "/AppLogs", base + "/Backups",
            base + "/Loaders/MelonLoader", base + "/Loaders/MelonLoader/net8",
            base + "/Loaders/MelonLoader/net35", base + "/Loaders/MelonLoader/Dependencies",
            base + "/Plugins", base + "/UserLibs"
        };
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/installer/ModInstaller.java
// File: ModInstaller.java (FIXED) - Updated to use new directory structure
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/installer/ModInstaller.java

package com.terrarialoader.installer;

import android.content.Context;
import android.net.Uri;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.PathManager;
import com.terrarialoader.loader.ModManager;
import java.io.File;

public class ModInstaller {
    // Enhanced file extension whitelist
    private static final String[] ALLOWED_EXTENSIONS = {
        ".dex", ".jar", ".dll", ".dex.disabled", ".jar.disabled", ".dll.disabled"
    };
    
    // Maximum file size (50MB)
    private static final long MAX_MOD_SIZE = 50 * 1024 * 1024;

    public static boolean installMod(Context context, Uri sourceUri, String filename) {
        LogUtils.logUser("Starting mod installation: " + filename);
        
        // Enhanced input validation
        if (context == null) {
            LogUtils.logUser("❌ Installation failed: context is null");
            return false;
        }
        
        if (sourceUri == null) {
            LogUtils.logUser("❌ Installation failed: source URI is null");
            return false;
        }
        
        if (filename == null || filename.trim().isEmpty()) {
            LogUtils.logUser("❌ Installation failed: filename is invalid");
            return false;
        }

        // Sanitize filename
        filename = sanitizeFilename(filename);
        
        // Check file extension
        if (!isValidModFile(filename)) {
            LogUtils.logUser("❌ Invalid mod file type: " + filename);
            LogUtils.logDebug("Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
            return false;
        }

        // FIXED: Determine mod directory based on file type using new structure
        File modDir = getModDirectoryByType(context, filename);
        if (modDir == null) {
            LogUtils.logUser("❌ Cannot determine mod directory for file type");
            return false;
        }

        // Ensure mod directory exists
        if (!PathManager.ensureDirectoryExists(modDir)) {
            LogUtils.logUser("❌ Cannot create mod directory: " + modDir.getAbsolutePath());
            return false;
        }

        // Check for existing mod with same name
        File outputFile = new File(modDir, filename);
        if (outputFile.exists()) {
            LogUtils.logUser("⚠️ Mod already exists: " + filename);
            // Create backup of existing mod
            File backupFile = new File(modDir, filename + ".backup." + System.currentTimeMillis());
            if (outputFile.renameTo(backupFile)) {
                LogUtils.logUser("📦 Created backup: " + backupFile.getName());
            }
        }

        // Validate file size before copying
        try {
            long fileSize = context.getContentResolver().openInputStream(sourceUri).available();
            if (fileSize > MAX_MOD_SIZE) {
                LogUtils.logUser("❌ Mod file too large: " + formatFileSize(fileSize) + " (max: " + formatFileSize(MAX_MOD_SIZE) + ")");
                return false;
            }
            LogUtils.logDebug("Mod file size: " + formatFileSize(fileSize));
        } catch (Exception e) {
            LogUtils.logDebug("Could not determine file size: " + e.getMessage());
        }

        // Install the mod
        boolean success = FileUtils.copyUriToFile(context, sourceUri, outputFile);
        
        if (success) {
            // Validate the copied file
            if (validateInstalledMod(outputFile)) {
                LogUtils.logUser("✅ Successfully installed: " + filename);
                LogUtils.logUser("📁 Location: " + outputFile.getAbsolutePath());
                
                // Auto-enable mod if it was installed with .disabled extension and user wants it enabled
                if (filename.endsWith(".disabled")) {
                    LogUtils.logUser("ℹ️ Mod installed as disabled. Enable it in mod manager to use.");
                } else {
                    LogUtils.logUser("ℹ️ Mod installed and ready to use.");
                }
                
                // Refresh mod list
                ModManager.loadMods(context);
                
                return true;
            } else {
                LogUtils.logUser("❌ Installed mod file validation failed");
                outputFile.delete(); // Clean up invalid file
                return false;
            }
        } else {
            LogUtils.logUser("❌ Failed to install: " + filename);
            return false;
        }
    }

    /**
     * FIXED: Get appropriate mod directory based on file type using new structure
     */
    private static File getModDirectoryByType(Context context, String filename) {
        String gamePackage = "com.and.games505.TerrariaPaid"; // Default to Terraria
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
            // DLL mods go to DLL directory
            return PathManager.getDllModsDir(context, gamePackage);
        } else if (lowerFilename.endsWith(".dex") || lowerFilename.endsWith(".dex.disabled") ||
                   lowerFilename.endsWith(".jar") || lowerFilename.endsWith(".jar.disabled")) {
            // DEX/JAR mods go to DEX directory
            return PathManager.getDexModsDir(context, gamePackage);
        }
        
        return null; // Unknown file type
    }

    // Enhanced mod installation with auto-detection
    public static boolean installModAuto(Context context, Uri sourceUri) {
        try {
            // Try to get filename from URI
            String filename = FileUtils.getFilenameFromUri(context, sourceUri);
            if (filename == null) {
                filename = "mod_" + System.currentTimeMillis() + ".dex";
                LogUtils.logDebug("Auto-generated filename: " + filename);
            }
            
            return installMod(context, sourceUri, filename);
            
        } catch (Exception e) {
            LogUtils.logUser("❌ Auto-installation failed: " + e.getMessage());
            LogUtils.logDebug("Auto-install error: " + e.toString());
            return false;
        }
    }

    // Batch mod installation
    public static int installMultipleMods(Context context, Uri[] sourceUris, String[] filenames) {
        if (sourceUris == null || filenames == null || sourceUris.length != filenames.length) {
            LogUtils.logUser("❌ Batch install failed: invalid parameters");
            return 0;
        }
        
        int successCount = 0;
        LogUtils.logUser("Starting batch installation of " + sourceUris.length + " mods");
        
        for (int i = 0; i < sourceUris.length; i++) {
            if (installMod(context, sourceUris[i], filenames[i])) {
                successCount++;
            }
        }
        
        LogUtils.logUser("Batch installation complete: " + successCount + "/" + sourceUris.length + " mods installed");
        return successCount;
    }

    private static boolean isValidModFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static String sanitizeFilename(String filename) {
        // Remove dangerous characters and limit length
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (filename.length() > 100) {
            filename = filename.substring(0, 100);
        }
        return filename;
    }

    private static boolean validateInstalledMod(File modFile) {
        if (!modFile.exists()) {
            LogUtils.logDebug("Validation failed: file does not exist");
            return false;
        }
        
        if (modFile.length() == 0) {
            LogUtils.logDebug("Validation failed: file is empty");
            return false;
        }
        
        if (modFile.length() > MAX_MOD_SIZE) {
            LogUtils.logDebug("Validation failed: file too large");
            return false;
        }
        
        // Basic file header validation for .dex files
        if (modFile.getName().toLowerCase().endsWith(".dex")) {
            try {
                byte[] header = new byte[8];
                java.io.FileInputStream fis = new java.io.FileInputStream(modFile);
                int bytesRead = fis.read(header);
                fis.close();
                
                if (bytesRead >= 8) {
                    // Check DEX magic number
                    String magic = new String(header, 0, 4);
                    if (!"dex\n".equals(magic)) {
                        LogUtils.logDebug("Validation failed: invalid DEX magic number");
                        return false;
                    }
                }
            } catch (Exception e) {
                LogUtils.logDebug("Validation warning: could not read file header: " + e.getMessage());
                // Don't fail validation just because we can't read header
            }
        }
        
        LogUtils.logDebug("Mod validation passed: " + modFile.getName());
        return true;
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // FIXED: Uninstall mod from correct directory
    public static boolean uninstallMod(Context context, String filename) {
        String gamePackage = "com.and.games505.TerrariaPaid";
        
        // Try to find the mod in appropriate directory
        File modFile = findModFile(context, gamePackage, filename);
        
        if (modFile == null || !modFile.exists()) {
            LogUtils.logUser("❌ Mod not found: " + filename);
            return false;
        }
        
        if (modFile.delete()) {
            LogUtils.logUser("🗑️ Uninstalled mod: " + filename);
            LogUtils.logUser("📁 From: " + modFile.getParent());
            ModManager.loadMods(context); // Refresh mod list
            return true;
        } else {
            LogUtils.logUser("❌ Failed to uninstall: " + filename);
            return false;
        }
    }

    /**
     * FIXED: Find mod file in appropriate directory based on file type
     */
    private static File findModFile(Context context, String gamePackage, String filename) {
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
            File dllDir = PathManager.getDllModsDir(context, gamePackage);
            File modFile = new File(dllDir, filename);
            if (modFile.exists()) {
                return modFile;
            }
        }
        
        if (lowerFilename.endsWith(".dex") || lowerFilename.endsWith(".dex.disabled") ||
            lowerFilename.endsWith(".jar") || lowerFilename.endsWith(".jar.disabled")) {
            File dexDir = PathManager.getDexModsDir(context, gamePackage);
            File modFile = new File(dexDir, filename);
            if (modFile.exists()) {
                return modFile;
            }
        }
        
        // Fallback: check legacy directory for backward compatibility
        File legacyDir = PathManager.getLegacyModsDir(context);
        File modFile = new File(legacyDir, filename);
        if (modFile.exists()) {
            LogUtils.logDebug("Found mod in legacy directory: " + filename);
            return modFile;
        }
        
        return null;
    }

    // Get installation status
    public static String[] getSupportedExtensions() {
        return ALLOWED_EXTENSIONS.clone();
    }

    public static long getMaxModSize() {
        return MAX_MOD_SIZE;
    }

    /**
     * FIXED: Check if migration is needed from legacy structure
     */
    public static boolean needsMigration(Context context) {
        File legacyModsDir = PathManager.getLegacyModsDir(context);
        if (!legacyModsDir.exists()) {
            return false;
        }
        
        File[] legacyMods = legacyModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : ALLOWED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        return legacyMods != null && legacyMods.length > 0;
    }

    /**
     * FIXED: Migrate legacy mods to new directory structure
     */
    public static boolean migrateLegacyMods(Context context) {
        if (!needsMigration(context)) {
            LogUtils.logDebug("No legacy mods to migrate");
            return true;
        }
        
        LogUtils.logUser("🔄 Migrating legacy mods to new structure...");
        
        String gamePackage = "com.and.games505.TerrariaPaid";
        File legacyModsDir = PathManager.getLegacyModsDir(context);
        
        // Ensure new directories exist
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (!PathManager.ensureDirectoryExists(dexModsDir) || 
            !PathManager.ensureDirectoryExists(dllModsDir)) {
            LogUtils.logUser("❌ Failed to create new mod directories");
            return false;
        }
        
        File[] legacyMods = legacyModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : ALLOWED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        if (legacyMods == null || legacyMods.length == 0) {
            LogUtils.logDebug("No legacy mod files found");
            return true;
        }
        
        int migratedCount = 0;
        int dexCount = 0;
        int dllCount = 0;
        
        for (File legacyMod : legacyMods) {
            String filename = legacyMod.getName();
            String lowerFilename = filename.toLowerCase();
            
            File targetDir;
            if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
                targetDir = dllModsDir;
                dllCount++;
            } else {
                targetDir = dexModsDir;
                dexCount++;
            }
            
            File newLocation = new File(targetDir, filename);
            if (legacyMod.renameTo(newLocation)) {
                migratedCount++;
                LogUtils.logDebug("Migrated: " + filename + " -> " + targetDir.getName());
            } else {
                LogUtils.logDebug("Failed to migrate: " + filename);
            }
        }
        
        LogUtils.logUser("✅ Migration completed:");
        LogUtils.logUser("📊 Total migrated: " + migratedCount + "/" + legacyMods.length);
        LogUtils.logUser("📊 DEX/JAR mods: " + dexCount);
        LogUtils.logUser("📊 DLL mods: " + dllCount);
        
        // Remove legacy directory if it's empty
        File[] remainingFiles = legacyModsDir.listFiles();
        if (remainingFiles == null || remainingFiles.length == 0) {
            if (legacyModsDir.delete()) {
                LogUtils.logDebug("Removed empty legacy mods directory");
            }
        }
        
        return migratedCount > 0;
    }

    /**
     * Get mod installation statistics
     */
    public static String getInstallationStats(Context context) {
        String gamePackage = "com.and.games505.TerrariaPaid";
        
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        int dexCount = 0, dllCount = 0;
        int dexEnabled = 0, dllEnabled = 0;
        
        // Count DEX mods
        if (dexModsDir.exists()) {
            File[] dexMods = dexModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dex") || lowerName.endsWith(".jar") ||
                       lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
            });
            
            if (dexMods != null) {
                dexCount = dexMods.length;
                for (File mod : dexMods) {
                    if (!mod.getName().endsWith(".disabled")) {
                        dexEnabled++;
                    }
                }
            }
        }
        
        // Count DLL mods
        if (dllModsDir.exists()) {
            File[] dllMods = dllModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dll") || lowerName.endsWith(".dll.disabled");
            });
            
            if (dllMods != null) {
                dllCount = dllMods.length;
                for (File mod : dllMods) {
                    if (!mod.getName().endsWith(".disabled")) {
                        dllEnabled++;
                    }
                }
            }
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== Mod Installation Statistics ===\n");
        stats.append("DEX/JAR Mods: ").append(dexEnabled).append("/").append(dexCount).append(" enabled\n");
        stats.append("DLL Mods: ").append(dllEnabled).append("/").append(dllCount).append(" enabled\n");
        stats.append("Total Mods: ").append(dexEnabled + dllEnabled).append("/").append(dexCount + dllCount).append(" enabled\n");
        stats.append("\nDirectories:\n");
        stats.append("DEX: ").append(dexModsDir.getAbsolutePath()).append("\n");
        stats.append("DLL: ").append(dllModsDir.getAbsolutePath()).append("\n");
        
        return stats.toString();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/LoaderFileManager.java
// File: LoaderFileManager.java (Fixed Component) - Complete Path Management Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/LoaderFileManager.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LoaderFileManager {
    
    // Install a DLL mod into standardized structure
    public boolean installDllMod(Context context, File dllFile, String gamePackage) {
        if (!isMelonLoaderInstalled(context, gamePackage)) {
            LogUtils.logUser("❌ No MelonLoader installed for: " + gamePackage);
            return false;
        }
        
        try {
            File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
            PathManager.ensureDirectoryExists(dllModsDir);
            
            File targetFile = new File(dllModsDir, dllFile.getName());
            
            if (copyFile(dllFile, targetFile)) {
                LogUtils.logUser("✅ DLL mod installed: " + dllFile.getName());
                LogUtils.logUser("📁 Location: " + targetFile.getAbsolutePath());
                return true;
            } else {
                LogUtils.logUser("❌ Failed to install DLL mod: " + dllFile.getName());
                return false;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("DLL mod installation failed: " + e.getMessage());
            return false;
        }
    }

    // Get list of installed DLL mods for a game
    public File[] getInstalledDllMods(Context context, String gamePackage) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (!dllModsDir.exists()) {
            return new File[0];
        }
        
        File[] dllFiles = dllModsDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".dll") && !name.toLowerCase().endsWith(".disabled"));
        
        return dllFiles != null ? dllFiles : new File[0];
    }
    
    // Get list of installed DEX mods for a game
    public File[] getInstalledDexMods(Context context, String gamePackage) {
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        
        if (!dexModsDir.exists()) {
            // Check legacy location for backward compatibility
            File legacyModsDir = PathManager.getLegacyModsDir(context);
            if (legacyModsDir.exists()) {
                LogUtils.logDebug("Using legacy mods directory for DEX mods");
                File[] legacyFiles = legacyModsDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return (lowerName.endsWith(".dex") || lowerName.endsWith(".jar")) && 
                           !lowerName.endsWith(".disabled");
                });
                return legacyFiles != null ? legacyFiles : new File[0];
            }
            return new File[0];
        }
        
        File[] dexFiles = dexModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return (lowerName.endsWith(".dex") || lowerName.endsWith(".jar")) && 
                   !lowerName.endsWith(".disabled");
        });
        
        return dexFiles != null ? dexFiles : new File[0];
    }

    // Enable/disable DLL mod
    public boolean toggleDllMod(Context context, String gamePackage, String modName, boolean enable) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (enable) {
            // Enable: Remove .disabled extension
            File disabledFile = new File(dllModsDir, modName + ".disabled");
            File enabledFile = new File(dllModsDir, modName);
            
            if (disabledFile.exists()) {
                return disabledFile.renameTo(enabledFile);
            }
        } else {
            // Disable: Add .disabled extension
            File enabledFile = new File(dllModsDir, modName);
            File disabledFile = new File(dllModsDir, modName + ".disabled");
            
            if (enabledFile.exists()) {
                return enabledFile.renameTo(disabledFile);
            }
        }
        
        return false;
    }

    // Delete DLL mod
    public boolean deleteDllMod(Context context, String gamePackage, String modName) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        File modFile = new File(dllModsDir, modName);
        File disabledFile = new File(dllModsDir, modName + ".disabled");
        
        boolean deleted = false;
        
        if (modFile.exists()) {
            deleted = modFile.delete();
        }
        
        if (disabledFile.exists()) {
            deleted = disabledFile.delete() || deleted;
        }
        
        if (deleted) {
            LogUtils.logUser("🗑️ Deleted DLL mod: " + modName);
        }
        
        return deleted;
    }

    // Get all available mods (both DLL and DEX) with legacy support
    public List<File> getAllMods(Context context, String gamePackage) {
        List<File> allMods = new ArrayList<>();
        
        // Add DLL mods
        File[] dllMods = getInstalledDllMods(context, gamePackage);
        for (File mod : dllMods) {
            allMods.add(mod);
        }
        
        // Add disabled DLL mods
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        if (dllModsDir.exists()) {
            File[] disabledDllMods = dllModsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".dll.disabled"));
            if (disabledDllMods != null) {
                for (File mod : disabledDllMods) {
                    allMods.add(mod);
                }
            }
        }
        
        // Add DEX mods (with legacy support)
        File[] dexMods = getInstalledDexMods(context, gamePackage);
        for (File mod : dexMods) {
            allMods.add(mod);
        }
        
        // Add disabled DEX mods
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        if (dexModsDir.exists()) {
            File[] disabledDexMods = dexModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
            });
            if (disabledDexMods != null) {
                for (File mod : disabledDexMods) {
                    allMods.add(mod);
                }
            }
        } else {
            // Check legacy location for disabled DEX mods
            File legacyModsDir = PathManager.getLegacyModsDir(context);
            if (legacyModsDir.exists()) {
                File[] disabledDexMods = legacyModsDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
                });
                if (disabledDexMods != null) {
                    for (File mod : disabledDexMods) {
                        allMods.add(mod);
                    }
                }
            }
        }
        
        return allMods;
    }

    // Check if mod is enabled
    public boolean isModEnabled(File modFile) {
        return !modFile.getName().toLowerCase().endsWith(".disabled");
    }

    // Get mod type from file
    public String getModType(File modFile) {
        String fileName = modFile.getName().toLowerCase();
        if (fileName.endsWith(".dll") || fileName.endsWith(".dll.disabled")) {
            return "DLL";
        } else if (fileName.endsWith(".dex") || fileName.endsWith(".dex.disabled")) {
            return "DEX";
        } else if (fileName.endsWith(".jar") || fileName.endsWith(".jar.disabled")) {
            return "JAR";
        }
        return "Unknown";
    }

    // Toggle mod enabled/disabled state
    public boolean toggleMod(Context context, String gamePackage, File modFile) {
        boolean isEnabled = isModEnabled(modFile);
        String modType = getModType(modFile);
        
        if ("DLL".equals(modType)) {
            return toggleDllMod(context, gamePackage, modFile.getName(), !isEnabled);
        } else if ("DEX".equals(modType) || "JAR".equals(modType)) {
            // Handle DEX/JAR toggling through FileUtils
            return FileUtils.toggleModFile(modFile);
        }
        
        return false;
    }

    // Get formatted mod info
    public String getModInfo(File modFile) {
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(modFile.getName()).append("\n");
        info.append("Type: ").append(getModType(modFile)).append("\n");
        info.append("Size: ").append(FileUtils.formatFileSize(modFile.length())).append("\n");
        info.append("Status: ").append(isModEnabled(modFile) ? "Enabled" : "Disabled").append("\n");
        info.append("Path: ").append(modFile.getAbsolutePath()).append("\n");
        
        if (modFile.exists()) {
            info.append("Last Modified: ").append(new java.util.Date(modFile.lastModified()).toString()).append("\n");
        }
        
        return info.toString();
    }

    // Backup existing mods before operations
    public boolean backupMods(Context context, String gamePackage) {
        try {
            File modsDir = PathManager.getGameBaseDir(context, gamePackage);
            if (modsDir == null || !modsDir.exists()) {
                LogUtils.logDebug("No game directory to backup for: " + gamePackage);
                return true;
            }
            
            File backupDir = new File(PathManager.getBackupsDir(context, gamePackage), "mods_" + System.currentTimeMillis());
            PathManager.ensureDirectoryExists(backupDir.getParentFile());
            
            return copyDirectory(modsDir, backupDir);
            
        } catch (Exception e) {
            LogUtils.logDebug("Mod backup failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to copy directory
    private boolean copyDirectory(File source, File target) {
        try {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    target.mkdirs();
                }
                
                File[] files = source.listFiles();
                if (files != null) {
                    for (File file : files) {
                        File targetFile = new File(target, file.getName());
                        if (!copyDirectory(file, targetFile)) {
                            return false;
                        }
                    }
                }
            } else {
                return copyFile(source, target);
            }
            return true;
        } catch (Exception e) {
            LogUtils.logDebug("Directory copy failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to copy files
    private boolean copyFile(File source, File target) {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(target)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("File copy failed: " + e.getMessage());
            return false;
        }
    }

    // Calculate directory size
    private long calculateDirectorySize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
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
        } else if (dir != null && dir.isFile()) {
            size = dir.length();
        }
        return size;
    }

    // Count files recursively
    private int countFiles(File dir) {
        int count = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countFiles(file);
                    } else {
                        count++;
                    }
                }
            }
        } else if (dir != null && dir.isFile()) {
            count = 1;
        }
        return count;
    }

    // Get file statistics for a game package
    public FileStatistics getFileStatistics(Context context, String gamePackage) {
        FileStatistics stats = new FileStatistics();
        stats.gamePackage = gamePackage;
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        if (baseDir != null && baseDir.exists()) {
            stats.totalFiles = countFiles(baseDir);
            stats.totalSize = calculateDirectorySize(baseDir);
            
            // Count mods specifically
            File[] dllMods = getInstalledDllMods(context, gamePackage);
            File[] dexMods = getInstalledDexMods(context, gamePackage);
            List<File> allMods = getAllMods(context, gamePackage);
            
            stats.dllModCount = dllMods != null ? dllMods.length : 0;
            stats.dexModCount = dexMods != null ? dexMods.length : 0;
            stats.totalModCount = allMods != null ? allMods.size() : 0;
            
            // Count enabled vs disabled
            if (allMods != null) {
                for (File mod : allMods) {
                    if (isModEnabled(mod)) {
                        stats.enabledModCount++;
                    } else {
                        stats.disabledModCount++;
                    }
                }
            }
        }
        
        return stats;
    }

    // Clean up old backup files
    public void cleanupOldBackups(Context context, String gamePackage, int maxBackups) {
        try {
            File backupDir = PathManager.getBackupsDir(context, gamePackage);
            if (!backupDir.exists()) {
                return;
            }
            
            File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("mods_"));
            if (backupFiles == null || backupFiles.length <= maxBackups) {
                return;
            }
            
            // Sort by timestamp (oldest first)
            java.util.Arrays.sort(backupFiles, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            
            // Delete oldest backups
            int toDelete = backupFiles.length - maxBackups;
            for (int i = 0; i < toDelete; i++) {
                deleteDirectory(backupFiles[i]);
                LogUtils.logDebug("Cleaned up old backup: " + backupFiles[i].getName());
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Backup cleanup failed: " + e.getMessage());
        }
    }

    // Helper method to delete directory recursively
    private boolean deleteDirectory(File dir) {
        if (dir == null) return false;
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return dir.delete();
    }

    // Check if MelonLoader is installed (uses PathManager)
    private boolean isMelonLoaderInstalled(Context context, String gamePackage) {
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        return loaderDir != null && loaderDir.exists() && dllModsDir != null && dllModsDir.exists();
    }

    // File Statistics helper class
    public static class FileStatistics {
        public String gamePackage;
        public int totalFiles = 0;
        public long totalSize = 0;
        public int dllModCount = 0;
        public int dexModCount = 0;
        public int totalModCount = 0;
        public int enabledModCount = 0;
        public int disabledModCount = 0;
        
        @Override
        public String toString() {
            return String.format("FileStats[%s]: %d files, %s, %d mods (%d enabled, %d disabled)", 
                               gamePackage != null ? gamePackage : "null", 
                               totalFiles,
                               FileUtils.formatFileSize(totalSize),
                               totalModCount, 
                               enabledModCount, 
                               disabledModCount);
        }
        
        public String getDetailedInfo() {
            StringBuilder info = new StringBuilder();
            info.append("=== File Statistics for ").append(gamePackage != null ? gamePackage : "Unknown").append(" ===\n");
            info.append("Total Files: ").append(totalFiles).append("\n");
            info.append("Total Size: ").append(FileUtils.formatFileSize(totalSize)).append("\n");
            info.append("DLL Mods: ").append(dllModCount).append("\n");
            info.append("DEX Mods: ").append(dexModCount).append("\n");
            info.append("Total Mods: ").append(totalModCount).append("\n");
            info.append("Enabled Mods: ").append(enabledModCount).append("\n");
            info.append("Disabled Mods: ").append(disabledModCount).append("\n");
            return info.toString();
        }
    }

    // Export mod list to text file
    public boolean exportModList(Context context, String gamePackage, File outputFile) {
        try {
            List<File> allMods = getAllMods(context, gamePackage);
            
            try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
                writer.write("=== TerrariaLoader Mod List ===\n");
                writer.write("Game Package: " + gamePackage + "\n");
                writer.write("Export Date: " + new java.util.Date().toString() + "\n");
                writer.write("Total Mods: " + (allMods != null ? allMods.size() : 0) + "\n\n");
                
                if (allMods != null) {
                    for (File mod : allMods) {
                        writer.write("Mod: " + mod.getName() + "\n");
                        writer.write("  Type: " + getModType(mod) + "\n");
                        writer.write("  Status: " + (isModEnabled(mod) ? "Enabled" : "Disabled") + "\n");
                        writer.write("  Size: " + FileUtils.formatFileSize(mod.length()) + "\n");
                        writer.write("  Path: " + mod.getAbsolutePath() + "\n\n");
                    }
                }
            }
            
            LogUtils.logUser("✅ Mod list exported to: " + outputFile.getName());
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("Mod list export failed: " + e.getMessage());
            return false;
        }
    }

    // Validate mod directory structure
    public boolean validateModDirectories(Context context, String gamePackage) {
        try {
            File baseDir = PathManager.getGameBaseDir(context, gamePackage);
            File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
            File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
            
            boolean valid = true;
            
            if (baseDir == null) {
                LogUtils.logDebug("Base directory is null for: " + gamePackage);
                return false;
            }
            
            if (!baseDir.exists()) {
                LogUtils.logDebug("Creating base directory: " + baseDir.getAbsolutePath());
                if (!PathManager.ensureDirectoryExists(baseDir)) {
                    valid = false;
                }
            }
            
            if (dllModsDir == null || !dllModsDir.exists()) {
                LogUtils.logDebug("Creating DLL mods directory");
                if (dllModsDir != null && !PathManager.ensureDirectoryExists(dllModsDir)) {
                    valid = false;
                }
            }
            
            if (dexModsDir == null || !dexModsDir.exists()) {
                LogUtils.logDebug("Creating DEX mods directory");
                if (dexModsDir != null && !PathManager.ensureDirectoryExists(dexModsDir)) {
                    valid = false;
                }
            }
            
            return valid;
            
        } catch (Exception e) {
            LogUtils.logDebug("Directory validation failed: " + e.getMessage());
            return false;
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/LoaderInstaller.java
// File: LoaderInstaller.java (Fixed Component) - Uses Correct App-Specific Paths
// Path: /storage/emulated/0/AndroidIDEProjects/main/java/com/terrarialoader/loader/LoaderInstaller.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import com.terrarialoader.util.ApkPatcher;
import java.util.zip.ZipInputStream;

public class LoaderInstaller {
    public static final String TERRARIA_PACKAGE = "com.and.games505.TerrariaPaid";
    
    // FIXED: Complete directory structure using PathManager (app-specific paths)
    private static final String[] LOADER_DIRECTORIES = {
        // Core Runtime Directories  
        "Loaders/MelonLoader/net8",                              // .NET 8 runtime files
        "Loaders/MelonLoader/net35",                             // .NET 3.5 runtime files (compatibility)
        
        // Dependencies Structure (from MelonLoader_File_List.txt)
        "Loaders/MelonLoader/Dependencies",
        "Loaders/MelonLoader/Dependencies/SupportModules",       // Core support DLLs
        "Loaders/MelonLoader/Dependencies/CompatibilityLayers",  // Compatibility DLLs
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator",      // Assembly generation tools
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Cpp2IL",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Cpp2IL/cpp2il_out",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Il2CppInterop",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/Il2CppInterop/Il2CppAssemblies",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/UnityDependencies",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/linux-arm64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/linux-arm/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/linux-x64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/linux-x86/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/osx-arm64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/osx-x64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/win-arm64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/win-x64/native",
        "Loaders/MelonLoader/Dependencies/Il2CppAssemblyGenerator/runtimes/win-x86/native",
        
        // User Directories
        "Mods/DLL",                                              // DLL mods go here
        "Mods/DEX",                                              // DEX/JAR mods go here
        "Loaders/MelonLoader/UserLibs",                          // User libraries
        "Loaders/MelonLoader/Plugins",                           // Plugin files
        "Logs",                                                  // All logs (both MelonLoader and TerrariaLoader)
        
        // Backup and Config
        "Backups",                                               // APK backups
        "Config"                                                 // Configuration files
    };

    // FIXED: Create complete directory structure using app-specific paths
    public boolean createLoaderStructure(Context context, String gamePackage, MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("Creating " + loaderType.getDisplayName() + " structure for: " + gamePackage);
        
        try {
            // FIXED: Use app-specific directory via PathManager
            File baseDir = PathManager.getGameBaseDir(context, gamePackage);
            LogUtils.logDebug("Using base directory: " + baseDir.getAbsolutePath());
            
            // Create all required directories
            for (String dirPath : LOADER_DIRECTORIES) {
                File dir = new File(baseDir, dirPath);
                if (!dir.exists() && !dir.mkdirs()) {
                    LogUtils.logDebug("Failed to create directory: " + dir.getAbsolutePath());
                    return false;
                }
            }
            
            // Create informational files
            createInfoFiles(baseDir, loaderType);
            
            LogUtils.logUser("✅ " + loaderType.getDisplayName() + " structure created successfully");
            LogUtils.logUser("📁 Base path: " + baseDir.getAbsolutePath());
            
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("Failed to create loader structure: " + e.getMessage());
            return false;
        }
    }
    
    // Create helpful info files
    private void createInfoFiles(File baseDir, MelonLoaderManager.LoaderType loaderType) throws IOException {
        // DLL Mods directory README
        File dllModsReadme = new File(baseDir, "Mods/DLL/README.txt");
        try (java.io.FileWriter writer = new java.io.FileWriter(dllModsReadme)) {
            writer.write("=== TerrariaLoader - DLL Mods Directory ===\n\n");
            writer.write("Place your .dll mod files here.\n");
            writer.write("Mods will be loaded automatically when Terraria starts.\n\n");
            writer.write("Supported formats:\n");
            writer.write("• .dll files implementing MelonMod (enabled)\n");
            writer.write("• .dll.disabled files (disabled mods)\n\n");
            writer.write("Loader Type: " + loaderType.getDisplayName() + "\n");
            writer.write("Unity Version: " + MelonLoaderManager.TERRARIA_UNITY_VERSION + "\n");
            writer.write("Game Version: " + MelonLoaderManager.TERRARIA_GAME_VERSION + "\n\n");
            writer.write("Examples of DLL mods:\n");
            writer.write("• MyTerrariaMod.dll (enabled)\n");
            writer.write("• SomeOtherMod.dll.disabled (disabled)\n");
        }
        
        // DEX Mods directory README
        File dexModsReadme = new File(baseDir, "Mods/DEX/README.txt");
        try (java.io.FileWriter writer = new java.io.FileWriter(dexModsReadme)) {
            writer.write("=== TerrariaLoader - DEX/JAR Mods Directory ===\n\n");
            writer.write("Place your .dex and .jar mod files here.\n");
            writer.write("These are Java-based mods for Android.\n\n");
            writer.write("Supported formats:\n");
            writer.write("• .dex files (enabled)\n");
            writer.write("• .jar files (enabled)\n");
            writer.write("• .dex.disabled files (disabled)\n");
            writer.write("• .jar.disabled files (disabled)\n\n");
            writer.write("Examples of DEX mods:\n");
            writer.write("• MyJavaMod.dex (enabled)\n");
            writer.write("• SomeJarMod.jar (enabled)\n");
            writer.write("• DisabledMod.dex.disabled (disabled)\n");
        }
        
        // Config directory info
        File configReadme = new File(baseDir, "Config/README.txt");
        try (java.io.FileWriter writer = new java.io.FileWriter(configReadme)) {
            writer.write("=== TerrariaLoader Configuration Directory ===\n\n");
            writer.write("This directory contains configuration files for TerrariaLoader and MelonLoader.\n");
            writer.write("Mod-specific config files will appear here automatically.\n\n");
            writer.write("File types you might see:\n");
            writer.write("• .cfg files (MelonLoader configs)\n");
            writer.write("• .json files (TerrariaLoader configs)\n");
            writer.write("• .txt files (Various settings)\n");
        }
    }

    // FIXED: Install from LemonLoader installer APK using correct paths
    public boolean installFromLemonLoaderApk(Context context, File installerApk, MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("Installing " + loaderType.getDisplayName() + " from LemonLoader installer APK...");
        
        try {
            // First create the directory structure using PathManager
            if (!createLoaderStructure(context, TERRARIA_PACKAGE, loaderType)) {
                return false;
            }
            
            // Extract files from LemonLoader installer APK
            return extractAndInstallFiles(context, installerApk, loaderType);
            
        } catch (Exception e) {
            LogUtils.logDebug(loaderType.getDisplayName() + " installation from APK failed: " + e.getMessage());
            return false;
        }
    }
    
    // FIXED: Extract files from LemonLoader installer APK and place them correctly using PathManager
    private boolean extractAndInstallFiles(Context context, File installerApk, MelonLoaderManager.LoaderType loaderType) {
        try {
            // FIXED: Use PathManager for correct app-specific paths
            File baseDir = PathManager.getGameBaseDir(context, TERRARIA_PACKAGE);
            File loaderDir = PathManager.getMelonLoaderDir(context, TERRARIA_PACKAGE);
            int installedCount = 0;
            
            LogUtils.logDebug("Extracting to: " + loaderDir.getAbsolutePath());
            
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(installerApk))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    
                    String entryName = entry.getName();
                    String targetPath = getTargetPath(entryName, loaderType);
                    
                    if (targetPath != null) {
                        File outputFile = new File(loaderDir, targetPath);
                        outputFile.getParentFile().mkdirs();
                        
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        
                        installedCount++;
                        LogUtils.logDebug("Installed: " + targetPath);
                    }
                }
            }
            
            LogUtils.logUser("✅ Installed " + installedCount + " " + loaderType.getDisplayName() + " files");
            return installedCount > 0;
            
        } catch (Exception e) {
            LogUtils.logDebug("File extraction failed: " + e.getMessage());
            return false;
        }
    }
    
    // Enhanced file path mapping based on MelonLoader_File_List.txt
    private String getTargetPath(String entryName, MelonLoaderManager.LoaderType loaderType) {
        // Remove common prefixes
        entryName = entryName.replaceFirst("^(assets/|lib/|)", "");
        
        // Handle DLL files
        if (entryName.endsWith(".dll")) {
            String fileName = new File(entryName).getName();
            
            // Core loader files - map to appropriate runtime
            if (isCoreDll(fileName)) {
                if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                    return "net8/" + fileName;
                } else {
                    return "net35/" + fileName;
                }
            }
            
            // Support modules
            if (isSupportModule(fileName)) {
                return "Dependencies/SupportModules/" + fileName;
            }
            
            // Assembly generator files
            if (isAssemblyGeneratorFile(fileName)) {
                return "Dependencies/Il2CppAssemblyGenerator/" + fileName;
            }
            
            // Unity dependencies
            if (isUnityDependency(fileName)) {
                return "Dependencies/Il2CppAssemblyGenerator/UnityDependencies/" + fileName;
            }
            
            // Compatibility layers
            if (isCompatibilityLayer(fileName)) {
                return "Dependencies/CompatibilityLayers/" + fileName;
            }
        }
        
        // Handle other file types
        if (entryName.endsWith(".json") || entryName.endsWith(".runtimeconfig.json")) {
            String fileName = new File(entryName).getName();
            if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                return "net8/" + fileName;
            } else {
                return "net35/" + fileName;
            }
        }
        
        // Handle native libraries
        if (entryName.contains("libcapstone.so") || entryName.contains("capstone.dll")) {
            if (entryName.contains("arm64")) {
                return "Dependencies/Il2CppAssemblyGenerator/runtimes/linux-arm64/native/" + new File(entryName).getName();
            } else if (entryName.contains("arm")) {
                return "Dependencies/Il2CppAssemblyGenerator/runtimes/linux-arm/native/" + new File(entryName).getName();
            }
        }
        
        // Configuration files
        if (entryName.endsWith(".cfg")) {
            return "../../Config/" + new File(entryName).getName();
        }
        
        return null; // Skip this file
    }
    
    // Helper methods for file classification based on MelonLoader_File_List.txt
    private boolean isCoreDll(String fileName) {
        return fileName.equals("MelonLoader.dll") ||
               fileName.equals("0Harmony.dll") ||
               fileName.startsWith("MonoMod.") ||
               fileName.equals("Il2CppInterop.Runtime.dll");
    }
    
    private boolean isSupportModule(String fileName) {
        return fileName.startsWith("Il2CppInterop.") ||
               fileName.equals("Il2Cpp.dll") ||
               fileName.equals("Preload.dll") ||
               fileName.equals("Mono.dll");
    }
    
    private boolean isAssemblyGeneratorFile(String fileName) {
        return fileName.contains("Il2CppAssemblyGenerator") ||
               fileName.contains("Cpp2IL") ||
               fileName.contains("LibCpp2IL") ||
               fileName.equals("AsmResolver.dll") ||
               fileName.equals("Disarm.dll") ||
               fileName.equals("Iced.dll");
    }
    
    private boolean isUnityDependency(String fileName) {
        return fileName.startsWith("UnityEngine.");
    }
    
    private boolean isCompatibilityLayer(String fileName) {
        return fileName.equals("Demeo.dll") ||
               fileName.equals("EOS.dll") ||
               fileName.equals("IPA.dll") ||
               fileName.equals("Muse_Dash_Mono.dll") ||
               fileName.equals("Stress_Level_Zero_Il2Cpp.dll");
    }

    // FIXED: Install MelonLoader into APK using REAL injection
    public boolean installMelonLoader(Context context, File inputApk, File outputApk) {
        LogUtils.logUser("Installing MelonLoader into APK...");
        
        try {
            // First ensure MelonLoader files exist
            if (!createLoaderStructure(context, TERRARIA_PACKAGE, MelonLoaderManager.LoaderType.MELONLOADER_NET8)) {
                LogUtils.logUser("❌ Failed to create loader structure");
                return false;
            }
            
            // Check if MelonLoader files are actually installed
            if (!MelonLoaderManager.isMelonLoaderInstalled(context, TERRARIA_PACKAGE)) {
                LogUtils.logUser("❌ MelonLoader files not found!");
                LogUtils.logUser("💡 Use 'Automated Installation' first to download MelonLoader files");
                return false;
            }
            
            // FIXED: Use real APK injection instead of just copying
            boolean success = ApkPatcher.injectMelonLoaderIntoApk(context, inputApk, outputApk, 
                MelonLoaderManager.LoaderType.MELONLOADER_NET8);
            
            if (success) {
                LogUtils.logUser("✅ MelonLoader successfully injected into APK");
                return true;
            } else {
                LogUtils.logUser("❌ MelonLoader injection failed");
                return false;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("MelonLoader installation failed: " + e.getMessage());
            LogUtils.logUser("❌ MelonLoader installation failed: " + e.getMessage());
            return false;
        }
    }

    // FIXED: Install LemonLoader into APK using REAL injection
    public boolean installLemonLoader(Context context, File inputApk, File outputApk) {
        LogUtils.logUser("Installing LemonLoader into APK...");
        
        try {
            // First ensure LemonLoader files exist
            if (!createLoaderStructure(context, TERRARIA_PACKAGE, MelonLoaderManager.LoaderType.MELONLOADER_NET35)) {
                LogUtils.logUser("❌ Failed to create loader structure");
                return false;
            }
            
            // Check if LemonLoader files are actually installed
            if (!MelonLoaderManager.isLemonLoaderInstalled(context, TERRARIA_PACKAGE)) {
                LogUtils.logUser("❌ LemonLoader files not found!");
                LogUtils.logUser("💡 Use 'Automated Installation' first to download LemonLoader files");
                return false;
            }
            
            // FIXED: Use real APK injection instead of just copying
            boolean success = ApkPatcher.injectMelonLoaderIntoApk(context, inputApk, outputApk, 
                MelonLoaderManager.LoaderType.MELONLOADER_NET35);
            
            if (success) {
                LogUtils.logUser("✅ LemonLoader successfully injected into APK");
                return true;
            } else {
                LogUtils.logUser("❌ LemonLoader injection failed");
                return false;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("LemonLoader installation failed: " + e.getMessage());
            LogUtils.logUser("❌ LemonLoader installation failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to copy files
    private boolean copyFile(File source, File target) {
        try (java.io.FileInputStream in = new java.io.FileInputStream(source);
             java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("File copy failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to delete directory recursively
    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return dir.delete();
    }
    
    // FIXED: Clean up method for removing loader installation using correct paths
    public boolean uninstallLoader(String gamePackage) {
        // FIXED: This should use Context to get proper paths, but kept for compatibility
        LogUtils.logDebug("Uninstall called without context - this method is deprecated");
        return false;
    }
    
    // NEW: Proper uninstall method with context
    public boolean uninstallLoader(Context context, String gamePackage) {
        File loaderDir = PathManager.getGameBaseDir(context, gamePackage);
        
        if (!loaderDir.exists()) {
            LogUtils.logUser("No loader installation found for: " + gamePackage);
            return true;
        }
        
        try {
            boolean success = deleteDirectory(loaderDir);
            if (success) {
                LogUtils.logUser("✅ Loader uninstalled successfully");
            } else {
                LogUtils.logUser("❌ Failed to uninstall loader");
            }
            return success;
        } catch (Exception e) {
            LogUtils.logDebug("Loader uninstallation error: " + e.getMessage());
            return false;
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/LoaderValidator.java
// File: LoaderValidator.java (Fixed Component) - Complete Path Management Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/LoaderValidator.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.io.FileInputStream;

public class LoaderValidator {
    private static final String MELONLOADER_VERSION = "0.6.5";
    
    // Essential files mapping based on MelonLoader_File_List.txt
    private static final String[] MELONLOADER_NET8_FILES = {
        "net8/MelonLoader.dll",
        "net8/MelonLoader.deps.json",
        "net8/MelonLoader.runtimeconfig.json",
        "net8/MelonLoader.xml",
        "net8/MelonLoader.NativeHost.deps.json",
        "net8/MelonLoader.NativeHost.dll",
        "net8/0Harmony.dll",
        "net8/0Harmony.pdb",
        "net8/0Harmony.xml",
        "net8/Il2CppInterop.Runtime.dll",
        "net8/Il2CppInterop.Runtime.xml",
        "net8/MonoMod.RuntimeDetour.dll",
        "net8/MonoMod.RuntimeDetour.pdb",
        "net8/MonoMod.RuntimeDetour.xml",
        "net8/MonoMod.Utils.dll",
        "net8/MonoMod.Utils.pdb",
        "net8/MonoMod.Utils.xml"
    };
    
    private static final String[] MELONLOADER_NET35_FILES = {
        "net35/MelonLoader.dll",
        "net35/MelonLoader.xml",
        "net35/0Harmony.dll",
        "net35/0Harmony.pdb",
        "net35/0Harmony.xml",
        "net35/MonoMod.RuntimeDetour.dll",
        "net35/MonoMod.RuntimeDetour.pdb",
        "net35/MonoMod.RuntimeDetour.xml",
        "net35/MonoMod.Utils.dll",
        "net35/MonoMod.Utils.pdb",
        "net35/MonoMod.Utils.xml"
    };
    
    private static final String[] MELONLOADER_SUPPORT_FILES = {
        "Dependencies/SupportModules/Il2Cpp.dll",
        "Dependencies/SupportModules/Il2Cpp.deps.json",
        "Dependencies/SupportModules/Il2CppInterop.Runtime.dll",
        "Dependencies/SupportModules/Il2CppInterop.Runtime.xml",
        "Dependencies/SupportModules/Il2CppInterop.HarmonySupport.dll",
        "Dependencies/SupportModules/Il2CppInterop.HarmonySupport.xml",
        "Dependencies/Il2CppAssemblyGenerator/Il2CppAssemblyGenerator.dll",
        "Dependencies/Il2CppAssemblyGenerator/Il2CppAssemblyGenerator.deps.json"
    };

    // Enhanced detection with PathManager
    public boolean isMelonLoaderInstalled(Context context, String gamePackage) {
        if (context == null || gamePackage == null) {
            LogUtils.logDebug("Context or gamePackage is null");
            return false;
        }
        
        // Check if directories need to be initialized
        if (PathManager.needsMigration(context)) {
            LogUtils.logUser("Migrating from legacy directory structure...");
            PathManager.migrateLegacyStructure(context);
        }
        
        // Auto-initialize directories if they don't exist
        if (!PathManager.initializeGameDirectories(context, gamePackage)) {
            LogUtils.logDebug("Failed to initialize game directories for: " + gamePackage);
        }
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
        File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        // Check for core files (either net8 or net35)
        boolean hasNet8 = checkCoreFiles(loaderDir, MELONLOADER_NET8_FILES);
        boolean hasNet35 = checkCoreFiles(loaderDir, MELONLOADER_NET35_FILES);
        boolean hasSupportFiles = checkCoreFiles(loaderDir, MELONLOADER_SUPPORT_FILES);
        
        boolean installed = loaderDir != null && loaderDir.exists() && 
                           depsDir != null && depsDir.exists() && 
                           dllModsDir != null && dllModsDir.exists() && 
                           (hasNet8 || hasNet35) && hasSupportFiles;
        
        if (installed) {
            LogUtils.logDebug("MelonLoader detected for: " + gamePackage + 
                             " (NET8: " + hasNet8 + ", NET35: " + hasNet35 + ")");
        } else {
            LogUtils.logDebug("MelonLoader not detected for: " + gamePackage);
            LogUtils.logDebug("  Loader dir exists: " + (loaderDir != null && loaderDir.exists()));
            LogUtils.logDebug("  Dependencies exist: " + (depsDir != null && depsDir.exists()));
            LogUtils.logDebug("  DLL mods dir exists: " + (dllModsDir != null && dllModsDir.exists()));
            LogUtils.logDebug("  Has NET8: " + hasNet8);
            LogUtils.logDebug("  Has NET35: " + hasNet35);
            LogUtils.logDebug("  Has support files: " + hasSupportFiles);
        }
        
        return installed;
    }
    
    // Helper method to check if core files exist
    private boolean checkCoreFiles(File baseDir, String[] filePaths) {
        if (baseDir == null || !baseDir.exists() || filePaths == null) {
            return false;
        }
        
        int foundFiles = 0;
        int requiredFiles = Math.max(1, filePaths.length / 2); // At least half the files should exist
        
        for (String filePath : filePaths) {
            File file = new File(baseDir, filePath);
            if (file.exists() && file.length() > 0) {
                foundFiles++;
            }
        }
        
        return foundFiles >= requiredFiles;
    }

    // Validate DLL mod file
    public boolean validateDllMod(File dllFile) {
        if (dllFile == null || !dllFile.exists() || !dllFile.getName().toLowerCase().endsWith(".dll")) {
            return false;
        }
        
        if (dllFile.length() == 0) {
            LogUtils.logDebug("DLL file is empty: " + dllFile.getName());
            return false;
        }
        
        // Basic PE header validation
        try (FileInputStream fis = new FileInputStream(dllFile)) {
            byte[] header = new byte[2];
            if (fis.read(header) == 2) {
                // Check for PE signature (MZ)
                return header[0] == 0x4D && header[1] == 0x5A;
            }
        } catch (Exception e) {
            LogUtils.logDebug("DLL validation error: " + e.getMessage());
        }
        
        return false;
    }

    // Check loader type availability
    public boolean isNet8Available(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return false;
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        return checkCoreFiles(loaderDir, MELONLOADER_NET8_FILES);
    }

    public boolean isNet35Available(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return false;
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        return checkCoreFiles(loaderDir, MELONLOADER_NET35_FILES);
    }

    // Get active loader type
    public MelonLoaderManager.LoaderType getActiveLoaderType(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return null;
        
        if (isNet8Available(context, gamePackage)) {
            return MelonLoaderManager.LoaderType.MELONLOADER_NET8;
        } else if (isNet35Available(context, gamePackage)) {
            return MelonLoaderManager.LoaderType.MELONLOADER_NET35;
        }
        return null;
    }

    // Comprehensive validation of loader installation
    public ValidationResult validateLoaderInstallation(Context context, String gamePackage) {
        ValidationResult result = new ValidationResult();
        result.gamePackage = gamePackage;
        
        if (context == null || gamePackage == null) {
            result.isValid = false;
            result.issues.add("Context or game package is null");
            return result;
        }
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        result.basePathExists = baseDir != null && baseDir.exists();
        
        if (!result.basePathExists) {
            result.isValid = false;
            result.issues.add("Base directory does not exist: " + (baseDir != null ? baseDir.getAbsolutePath() : "null"));
            
            // Try to create the directory structure
            LogUtils.logUser("Attempting to create missing directory structure...");
            if (PathManager.initializeGameDirectories(context, gamePackage)) {
                LogUtils.logUser("✅ Directory structure created successfully");
                result.basePathExists = true;
                // Re-check after creation
                baseDir = PathManager.getGameBaseDir(context, gamePackage);
            } else {
                LogUtils.logUser("❌ Failed to create directory structure");
                return result;
            }
        }
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        result.loaderDirExists = loaderDir != null && loaderDir.exists();
        
        if (!result.loaderDirExists && loaderDir != null) {
            result.issues.add("Loader directory does not exist: " + loaderDir.getAbsolutePath());
        }
        
        // Check runtime availability
        result.hasNet8 = isNet8Available(context, gamePackage);
        result.hasNet35 = isNet35Available(context, gamePackage);
        
        if (!result.hasNet8 && !result.hasNet35) {
            result.issues.add("No valid runtime found (neither NET8 nor NET35)");
        }
        
        // Check support files
        result.hasSupportFiles = checkCoreFiles(loaderDir, MELONLOADER_SUPPORT_FILES);
        if (!result.hasSupportFiles) {
            result.issues.add("Missing support files in Dependencies directory");
        }
        
        // Check mod directories
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        result.dllModsDirExists = dllModsDir != null && dllModsDir.exists();
        result.dexModsDirExists = dexModsDir != null && dexModsDir.exists();
        
        if (!result.dllModsDirExists) {
            result.issues.add("DLL mods directory does not exist");
        }
        if (!result.dexModsDirExists) {
            result.issues.add("DEX mods directory does not exist");
        }
        
        // Overall validation
        result.isValid = result.basePathExists && result.loaderDirExists && 
                        (result.hasNet8 || result.hasNet35) && result.hasSupportFiles &&
                        result.dllModsDirExists && result.dexModsDirExists;
        
        if (result.isValid) {
            result.activeLoaderType = getActiveLoaderType(context, gamePackage);
        }
        
        return result;
    }

    // Get detailed information about loader installation
    public String getValidationReport(Context context, String gamePackage) {
        ValidationResult result = validateLoaderInstallation(context, gamePackage);
        StringBuilder report = new StringBuilder();
        
        report.append("=== Loader Validation Report ===\n");
        report.append("Game Package: ").append(gamePackage != null ? gamePackage : "null").append("\n");
        report.append("Overall Status: ").append(result.isValid ? "✅ VALID" : "❌ INVALID").append("\n\n");
        
        report.append("Directory Structure:\n");
        report.append("- Base Path: ").append(result.basePathExists ? "✅" : "❌").append("\n");
        report.append("- Loader Dir: ").append(result.loaderDirExists ? "✅" : "❌").append("\n");
        report.append("- DLL Mods Dir: ").append(result.dllModsDirExists ? "✅" : "❌").append("\n");
        report.append("- DEX Mods Dir: ").append(result.dexModsDirExists ? "✅" : "❌").append("\n\n");
        
        report.append("Runtime Support:\n");
        report.append("- NET8 Runtime: ").append(result.hasNet8 ? "✅" : "❌").append("\n");
        report.append("- NET35 Runtime: ").append(result.hasNet35 ? "✅" : "❌").append("\n");
        report.append("- Support Files: ").append(result.hasSupportFiles ? "✅" : "❌").append("\n\n");
        
        if (result.activeLoaderType != null) {
            report.append("Active Loader: ").append(result.activeLoaderType.getDisplayName()).append("\n\n");
        }
        
        if (!result.issues.isEmpty()) {
            report.append("Issues Found:\n");
            for (String issue : result.issues) {
                report.append("- ").append(issue).append("\n");
            }
        }
        
        // Add path information
        if (context != null && gamePackage != null) {
            report.append("\n").append(PathManager.getPathInfo(context, gamePackage));
        }
        
        return report.toString();
    }

    // Get installed loader version
    public String getInstalledLoaderVersion() {
        return MELONLOADER_VERSION;
    }

    // Helper class for validation results
    public static class ValidationResult {
        public String gamePackage;
        public boolean isValid = false;
        public boolean basePathExists = false;
        public boolean loaderDirExists = false;
        public boolean dllModsDirExists = false;
        public boolean dexModsDirExists = false;
        public boolean hasNet8 = false;
        public boolean hasNet35 = false;
        public boolean hasSupportFiles = false;
        public MelonLoaderManager.LoaderType activeLoaderType = null;
        public java.util.List<String> issues = new java.util.ArrayList<>();
        
        @Override
        public String toString() {
            return String.format("ValidationResult[%s]: %s (%d issues)", 
                               gamePackage != null ? gamePackage : "null", 
                               isValid ? "VALID" : "INVALID",
                               issues.size());
        }
    }

    // Quick validation methods for backward compatibility
    public boolean isLemonLoaderInstalled(Context context, String gamePackage) {
        return isMelonLoaderInstalled(context, gamePackage);
    }

    public boolean isLemonLoaderInstalled(Context context) {
        return isMelonLoaderInstalled(context, MelonLoaderManager.TERRARIA_PACKAGE);
    }

    // File integrity check
    public boolean checkFileIntegrity(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        if (file.length() == 0) {
            LogUtils.logDebug("File is empty: " + file.getName());
            return false;
        }
        
        // Check if file is readable
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead = fis.read(buffer);
            return bytesRead > 0;
        } catch (Exception e) {
            LogUtils.logDebug("File integrity check failed: " + e.getMessage());
            return false;
        }
    }

    // Repair corrupted installation
    public boolean attemptRepair(Context context, String gamePackage) {
        LogUtils.logUser("Attempting to repair loader installation for: " + gamePackage);
        
        if (context == null || gamePackage == null) {
            LogUtils.logUser("❌ Cannot repair: context or gamePackage is null");
            return false;
        }
        
        ValidationResult result = validateLoaderInstallation(context, gamePackage);
        
        if (result.isValid) {
            LogUtils.logUser("Installation is already valid, no repair needed");
            return true;
        }
        
        boolean repaired = false;
        
        // Try to create missing directories
        if (!result.basePathExists || !result.dllModsDirExists || !result.dexModsDirExists) {
            LogUtils.logDebug("Creating missing directories");
            if (PathManager.initializeGameDirectories(context, gamePackage)) {
                LogUtils.logDebug("Successfully created missing directories");
                repaired = true;
            }
        }
        
        // Re-validate after repair attempts
        ValidationResult newResult = validateLoaderInstallation(context, gamePackage);
        
        if (newResult.isValid) {
            LogUtils.logUser("✅ Loader installation repaired successfully");
            return true;
        } else {
            LogUtils.logUser("❌ Could not fully repair installation");
            LogUtils.logDebug("Remaining issues: " + newResult.issues.size());
            for (String issue : newResult.issues) {
                LogUtils.logDebug("  - " + issue);
            }
            return repaired; // Return true if we made some progress
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/MainLoader.java
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

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/MelonLoaderManager.java
// File: MelonLoaderManager.java (Fixed Facade) - Updated with PathManager
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/MelonLoaderManager.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.util.List;

/**
 * MelonLoaderManager serves as a facade that delegates to specialized components:
 * - LoaderInstaller: Handles installation of MelonLoader/LemonLoader
 * - LoaderValidator: Handles detection and validation of loader installations
 * - LoaderFileManager: Handles file operations for mods and loaders
 * 
 * This maintains backward compatibility while organizing code into focused components.
 * Updated to use PathManager for consistent directory structure.
 */
public class MelonLoaderManager {
    // Static components
    private static final LoaderInstaller installer = new LoaderInstaller();
    private static final LoaderValidator validator = new LoaderValidator();
    private static final LoaderFileManager fileManager = new LoaderFileManager();
    
    // Constants for Terraria specifically (from your logs)
    public static final String TERRARIA_PACKAGE = "com.and.games505.TerrariaPaid";
    public static final String TERRARIA_UNITY_VERSION = "2021.3.35f1";
    public static final String TERRARIA_GAME_VERSION = "1.4.4.9.6";

    // Enum for loader types
    public enum LoaderType {
        MELONLOADER_NET8("MelonLoader .NET 8", ".dll"),
        MELONLOADER_NET35("MelonLoader .NET 3.5", ".dll");
        
        private final String displayName;
        private final String extension;
        
        LoaderType(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }
        
        public String getDisplayName() { return displayName; }
        public String getExtension() { return extension; }
    }

    // Enhanced status class
    public static class LoaderStatus {
        public String gamePackage;
        public boolean melonLoaderInstalled;
        public LoaderType activeLoader;
        public String version;
        public String basePath;
        public File[] installedDllMods;
        public File[] installedDexMods;
        public File modsDirectory;
        public int totalFiles;
        public long totalSize;
        public boolean isNet8Available;
        public boolean isNet35Available;
        
        @Override
        public String toString() {
            return String.format("Loader[%s]: %s (%d DLL mods, %d DEX mods, %d files)", 
                               gamePackage != null ? gamePackage : "null", 
                               activeLoader != null ? "Installed " + activeLoader.getDisplayName() + " v" + version : "Not Installed",
                               installedDllMods != null ? installedDllMods.length : 0,
                               installedDexMods != null ? installedDexMods.length : 0,
                               totalFiles);
        }
    }

    // === DETECTION AND VALIDATION ===
    public static boolean isMelonLoaderInstalled(Context context, String gamePackage) {
        return validator.isMelonLoaderInstalled(context, gamePackage);
    }
    
    public static boolean isMelonLoaderInstalled(Context context) {
        return validator.isMelonLoaderInstalled(context, TERRARIA_PACKAGE);
    }
    
    public static boolean isLemonLoaderInstalled(Context context, String gamePackage) {
        return validator.isLemonLoaderInstalled(context, gamePackage);
    }
    
    public static boolean isLemonLoaderInstalled(Context context) {
        return validator.isLemonLoaderInstalled(context);
    }
    
    public static String getInstalledLoaderVersion() {
        return validator.getInstalledLoaderVersion();
    }

    // === INSTALLATION ===
    public static boolean createLoaderStructure(Context context, String gamePackage, LoaderType loaderType) {
        return installer.createLoaderStructure(context, gamePackage, loaderType);
    }
    
    public static boolean installFromLemonLoaderApk(Context context, File installerApk, LoaderType loaderType) {
        return installer.installFromLemonLoaderApk(context, installerApk, loaderType);
    }
    
    public static boolean installMelonLoader(Context context, File inputApk, File outputApk) {
        return installer.installMelonLoader(context, inputApk, outputApk);
    }

    public static boolean installLemonLoader(Context context, File inputApk, File outputApk) {
        return installer.installLemonLoader(context, inputApk, outputApk);
    }

    public static boolean uninstallLoader(Context context, String gamePackage) {
        return installer.uninstallLoader(gamePackage);
    }

    // === FILE MANAGEMENT ===
    public static boolean installDllMod(Context context, File dllFile, String gamePackage) {
        return fileManager.installDllMod(context, dllFile, gamePackage);
    }

    public static File[] getInstalledDllMods(Context context, String gamePackage) {
        return fileManager.getInstalledDllMods(context, gamePackage);
    }
    
    public static File[] getInstalledDexMods(Context context, String gamePackage) {
        return fileManager.getInstalledDexMods(context, gamePackage);
    }

    public static boolean toggleDllMod(Context context, String gamePackage, String modName, boolean enable) {
        return fileManager.toggleDllMod(context, gamePackage, modName, enable);
    }

    public static boolean deleteDllMod(Context context, String gamePackage, String modName) {
        return fileManager.deleteDllMod(context, gamePackage, modName);
    }

    public static List<File> getAllMods(Context context, String gamePackage) {
        return fileManager.getAllMods(context, gamePackage);
    }

    public static boolean isModEnabled(File modFile) {
        return fileManager.isModEnabled(modFile);
    }

    public static String getModType(File modFile) {
        return fileManager.getModType(modFile);
    }

    public static boolean toggleMod(Context context, String gamePackage, File modFile) {
        return fileManager.toggleMod(context, gamePackage, modFile);
    }

    public static String getModInfo(File modFile) {
        return fileManager.getModInfo(modFile);
    }

    public static boolean backupMods(Context context, String gamePackage) {
        return fileManager.backupMods(context, gamePackage);
    }

    // === VALIDATION ===
    public static boolean validateDllMod(File dllFile) {
        return validator.validateDllMod(dllFile);
    }

    public static LoaderValidator.ValidationResult validateLoaderInstallation(Context context, String gamePackage) {
        return validator.validateLoaderInstallation(context, gamePackage);
    }

    public static String getValidationReport(Context context, String gamePackage) {
        return validator.getValidationReport(context, gamePackage);
    }

    public static boolean attemptRepair(Context context, String gamePackage) {
        return validator.attemptRepair(context, gamePackage);
    }

    // === STATUS INFORMATION ===
    public static LoaderStatus getStatus(Context context, String gamePackage) {
        LoaderStatus status = new LoaderStatus();
        status.gamePackage = gamePackage;
        
        if (context == null || gamePackage == null) {
            status.melonLoaderInstalled = false;
            return status;
        }
        
        status.melonLoaderInstalled = validator.isMelonLoaderInstalled(context, gamePackage);
        
        if (status.melonLoaderInstalled) {
            // Check which runtime versions are available
            status.isNet8Available = validator.isNet8Available(context, gamePackage);
            status.isNet35Available = validator.isNet35Available(context, gamePackage);
            status.activeLoader = validator.getActiveLoaderType(context, gamePackage);
            status.version = validator.getInstalledLoaderVersion();
            
            File baseDir = PathManager.getGameBaseDir(context, gamePackage);
            status.basePath = baseDir != null ? baseDir.getAbsolutePath() : "null";
            status.installedDllMods = fileManager.getInstalledDllMods(context, gamePackage);
            status.installedDexMods = fileManager.getInstalledDexMods(context, gamePackage);
            
            File modsDir = PathManager.getDllModsDir(context, gamePackage);
            status.modsDirectory = modsDir;
            
            // Get file statistics
            LoaderFileManager.FileStatistics stats = fileManager.getFileStatistics(context, gamePackage);
            status.totalFiles = stats.totalFiles;
            status.totalSize = stats.totalSize;
        }
        
        return status;
    }

    // === DEBUG INFORMATION ===
    public static String getDebugInfo(Context context, String gamePackage) {
        StringBuilder info = new StringBuilder();
        info.append("=== MelonLoaderManager Debug Info (Facade Pattern) ===\n");
        info.append("Components: LoaderInstaller, LoaderValidator, LoaderFileManager\n");
        info.append("PathManager: Centralized path management\n\n");
        
        // Validation info
        info.append(validator.getValidationReport(context, gamePackage)).append("\n");
        
        // File statistics
        LoaderFileManager.FileStatistics stats = fileManager.getFileStatistics(context, gamePackage);
        info.append(stats.getDetailedInfo()).append("\n");
        
        // Status summary
        LoaderStatus status = getStatus(context, gamePackage);
        info.append("Status Summary: ").append(status.toString()).append("\n");
        
        return info.toString();
    }

    public static String getSummary(Context context, String gamePackage) {
        LoaderStatus status = getStatus(context, gamePackage);
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== TerrariaLoader Summary ===\n");
        summary.append("Game: ").append(gamePackage != null ? gamePackage : "null").append("\n");
        summary.append("Loader Status: ").append(status.melonLoaderInstalled ? "Installed" : "Not Installed").append("\n");
        
        if (status.melonLoaderInstalled) {
            summary.append("Loader Type: ").append(status.activeLoader != null ? status.activeLoader.getDisplayName() : "Unknown").append("\n");
            summary.append("Version: ").append(status.version != null ? status.version : "Unknown").append("\n");
            summary.append("DLL Mods: ").append(status.installedDllMods != null ? status.installedDllMods.length : 0).append("\n");
            summary.append("DEX Mods: ").append(status.installedDexMods != null ? status.installedDexMods.length : 0).append("\n");
            summary.append("Total Files: ").append(status.totalFiles).append("\n");
            summary.append("Total Size: ").append(FileUtils.formatFileSize(status.totalSize)).append("\n");
            summary.append("Base Path: ").append(status.basePath != null ? status.basePath : "null").append("\n");
        }
        
        return summary.toString();
    }

    // === COMPONENT ACCESS (for advanced usage) ===
    public static LoaderInstaller getInstaller() {
        return installer;
    }

    public static LoaderValidator getValidator() {
        return validator;
    }

    public static LoaderFileManager getFileManager() {
        return fileManager;
    }

    // === BACKWARD COMPATIBILITY ===
    // These methods maintain compatibility with existing code that expects the old interface

    // Backward compatibility enum values
    public enum LoaderTypeCompat {
        MELONLOADER("MelonLoader", ".dll"),
        LEMONLOADER("LemonLoader", ".dll");

        private final String displayName;
        private final String extension;

        LoaderTypeCompat(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }

        public String getDisplayName() { return displayName; }
        public String getExtension() { return extension; }

        public LoaderType toNewLoaderType() {
            switch (this) {
                case MELONLOADER:
                    return LoaderType.MELONLOADER_NET8;
                case LEMONLOADER:
                    return LoaderType.MELONLOADER_NET35;
                default:
                    return LoaderType.MELONLOADER_NET8;
            }
        }
    }

    // === UTILITY METHODS ===
    public static boolean validateModDirectories(Context context, String gamePackage) {
        return fileManager.validateModDirectories(context, gamePackage);
    }

    public static boolean exportModList(Context context, String gamePackage, File outputFile) {
        return fileManager.exportModList(context, gamePackage, outputFile);
    }

    public static LoaderFileManager.FileStatistics getFileStatistics(Context context, String gamePackage) {
        return fileManager.getFileStatistics(context, gamePackage);
    }

    public static void cleanupOldBackups(Context context, String gamePackage, int maxBackups) {
        fileManager.cleanupOldBackups(context, gamePackage, maxBackups);
    }

    // === INITIALIZATION AND CLEANUP ===
    public static void initialize(Context context) {
        LogUtils.logDebug("MelonLoaderManager initialized with facade pattern and PathManager");
        
        // Check for migration needs
        if (PathManager.needsMigration(context)) {
            LogUtils.logUser("Legacy directory structure detected, migrating...");
            PathManager.migrateLegacyStructure(context);
        }
        
        // Validate directory structure on startup
        validateModDirectories(context, TERRARIA_PACKAGE);
    }

    public static void cleanup(Context context, String gamePackage) {
        // Clean up old backups (keep last 5)
        cleanupOldBackups(context, gamePackage, 5);
        LogUtils.logDebug("MelonLoaderManager cleanup completed for: " + gamePackage);
    }

    // === HEALTH CHECK ===
    public static boolean performHealthCheck(Context context, String gamePackage) {
        LogUtils.logDebug("Performing health check for: " + gamePackage);
        
        if (context == null || gamePackage == null) {
            LogUtils.logDebug("Health check failed: null parameters");
            return false;
        }
        
        boolean healthy = true;
        
        // Check if directories exist and are writable
        if (!validateModDirectories(context, gamePackage)) {
            LogUtils.logDebug("Health check failed: mod directories invalid");
            healthy = false;
        }
        
        // Check if loader is properly installed (if supposed to be)
        if (isMelonLoaderInstalled(context, gamePackage)) {
            LoaderValidator.ValidationResult result = validateLoaderInstallation(context, gamePackage);
            if (!result.isValid) {
                LogUtils.logDebug("Health check failed: loader installation invalid");
                healthy = false;
            }
        }
        
        LogUtils.logDebug("Health check result: " + (healthy ? "PASS" : "FAIL"));
        return healthy;
    }

    // === MIGRATION SUPPORT ===
    public static boolean migrateFromOldStructure(Context context, String gamePackage) {
        LogUtils.logUser("Checking for old structure to migrate...");
        
        if (context == null || gamePackage == null) {
            LogUtils.logDebug("Migration failed: null parameters");
            return false;
        }
        
        // Check if migration is needed
        if (PathManager.needsMigration(context)) {
            return PathManager.migrateLegacyStructure(context);
        } else {
            // Just ensure new structure exists
            return validateModDirectories(context, gamePackage);
        }
    }

    // === PATH INFORMATION ===
    public static String getPathInfo(Context context, String gamePackage) {
        if (context == null || gamePackage == null) {
            return "Path info unavailable: null parameters";
        }
        return PathManager.getPathInfo(context, gamePackage);
    }

    // === AUTO-REPAIR FUNCTIONALITY ===
    public static boolean autoRepairInstallation(Context context, String gamePackage) {
        LogUtils.logUser("🔧 Auto-repairing installation for: " + gamePackage);
        
        if (context == null || gamePackage == null) {
            LogUtils.logUser("❌ Auto-repair failed: invalid parameters");
            return false;
        }
        
        boolean repaired = false;
        
        // Step 1: Initialize directories if missing
        if (!PathManager.initializeGameDirectories(context, gamePackage)) {
            LogUtils.logDebug("Failed to initialize directories during auto-repair");
        } else {
            repaired = true;
        }
        
        // Step 2: Attempt validation repair
        if (attemptRepair(context, gamePackage)) {
            repaired = true;
        }
        
        // Step 3: Migrate legacy structure if needed
        if (migrateFromOldStructure(context, gamePackage)) {
            repaired = true;
        }
        
        // Step 4: Final health check
        boolean healthy = performHealthCheck(context, gamePackage);
        
        if (healthy) {
            LogUtils.logUser("✅ Auto-repair completed successfully");
        } else if (repaired) {
            LogUtils.logUser("⚠️ Auto-repair made improvements but issues remain");
        } else {
            LogUtils.logUser("❌ Auto-repair could not fix the installation");
        }
        
        return healthy || repaired;
    }

    // === LEGACY COMPATIBILITY METHODS ===
    // These maintain compatibility with old method signatures
    
    @Deprecated
    public static boolean isMelonLoaderInstalled(String gamePackage) {
        LogUtils.logDebug("Using deprecated method - context required for proper functionality");
        return false; // Cannot function without context
    }
    
    @Deprecated
    public static boolean isLemonLoaderInstalled() {
        LogUtils.logDebug("Using deprecated method - context required for proper functionality");  
        return false; // Cannot function without context
    }
    
    @Deprecated
    public static LoaderStatus getStatus(String gamePackage) {
        LogUtils.logDebug("Using deprecated method - context required for proper functionality");
        LoaderStatus status = new LoaderStatus();
        status.gamePackage = gamePackage;
        status.melonLoaderInstalled = false;
        return status;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModBase.java
// File: ModBase.java (Interface Class) - Phase 4 Enhanced with DLL Support
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/ModBase.java

package com.terrarialoader.loader;

import android.content.Context;

public interface ModBase {
    void onLoad(Context context);
    
    // Optional metadata methods (Phase 1 features preserved)
    default String getModName() {
        return "Unknown Mod";
    }
    
    default String getModVersion() {
        return "1.0.0";
    }
    
    default String getModDescription() {
        return "No description provided";
    }
    
    default String getModAuthor() {
        return "Unknown Author";
    }
    
    default String[] getDependencies() {
        return new String[0]; // No dependencies by default
    }
    
    default String getMinGameVersion() {
        return "1.0.0";
    }
    
    default String getMaxGameVersion() {
        return "999.0.0";
    }
    
    default boolean isCompatibleWith(String gameVersion) {
        return true; // Compatible by default
    }
    
    // Configuration support (Phase 1 features preserved)
    default void onConfigurationChanged(String key, Object value) {
        // Override to handle configuration changes
    }
    
    default void onUnload() {
        // Override to handle cleanup
    }
    
    // NEW PHASE 4: DLL Support Methods
    default ModType getModType() {
        return ModType.DEX; // Default to DEX for existing mods
    }
    
    default String[] getSupportedPlatforms() {
        return new String[]{"Android"}; // Default platform support
    }
    
    default boolean requiresMelonLoader() {
        return getModType() == ModType.DLL;
    }
    
    default String getMelonLoaderVersion() {
        return "0.6.1"; // Default MelonLoader version
    }
    
    default String[] getRequiredAssemblies() {
        return new String[0]; // No additional assemblies by default
    }
    
    // Hybrid mod support
    default boolean isHybridMod() {
        return false; // Most mods are single-type
    }
    
    default String getCompanionModPath() {
        return null; // Path to companion .dex/.dll file for hybrid mods
    }
    
    // Unity-specific methods for DLL mods
    default String getUnityVersion() {
        return "2021.3.21f1"; // Default Unity version for Terraria
    }
    
    default boolean requiresIl2Cpp() {
        return true; // Most Unity games use IL2CPP
    }
    
    default String[] getHarmonyPatches() {
        return new String[0]; // Harmony patch classes
    }
    
    // Mod type enumeration
    enum ModType {
        DEX("Java/Android DEX", ".dex"),
        JAR("Java Archive", ".jar"), 
        DLL("C# Dynamic Library", ".dll"),
        HYBRID("Hybrid Java+C#", ".hybrid");
        
        private final String displayName;
        private final String extension;
        
        ModType(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }
        
        public String getDisplayName() { return displayName; }
        public String getExtension() { return extension; }
        
        public static ModType fromFileName(String fileName) {
            if (fileName.endsWith(".dex") || fileName.endsWith(".dex.disabled")) {
                return DEX;
            } else if (fileName.endsWith(".jar") || fileName.endsWith(".jar.disabled")) {
                return JAR;
            } else if (fileName.endsWith(".dll") || fileName.endsWith(".dll.disabled")) {
                return DLL;
            } else if (fileName.endsWith(".hybrid") || fileName.endsWith(".hybrid.disabled")) {
                return HYBRID;
            }
            return DEX; // Default
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModConfiguration.java
package com.terrarialoader.loader;

import android.content.Context;
import android.content.SharedPreferences;
import com.terrarialoader.util.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModConfiguration {
    private static final String PREFS_PREFIX = "mod_config_";
    private final String modName;
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, Object> defaultValues;

    public ModConfiguration(Context context, String modName) {
        this.context = context;
        this.modName = modName;
        this.prefs = context.getSharedPreferences(PREFS_PREFIX + modName, Context.MODE_PRIVATE);
        this.defaultValues = new HashMap<>();
        
        LogUtils.logDebug("Created configuration for mod: " + modName);
    }

    // Set default values
    public void setDefault(String key, Object defaultValue) {
        defaultValues.put(key, defaultValue);
    }

    public void setDefaults(Map<String, Object> defaults) {
        defaultValues.putAll(defaults);
    }

    // Get values with type safety
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, (String) defaultValues.getOrDefault(key, defaultValue));
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, (Integer) defaultValues.getOrDefault(key, defaultValue));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, (Boolean) defaultValues.getOrDefault(key, defaultValue));
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, (Float) defaultValues.getOrDefault(key, defaultValue));
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, (Long) defaultValues.getOrDefault(key, defaultValue));
    }

    // Set values
    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    // Check if key exists
    public boolean contains(String key) {
        return prefs.contains(key) || defaultValues.containsKey(key);
    }

    // Remove key
    public void remove(String key) {
        prefs.edit().remove(key).apply();
        LogUtils.logDebug("Removed " + modName + " config: " + key);
    }

    // Clear all configuration
    public void clear() {
        prefs.edit().clear().apply();
        LogUtils.logUser("Cleared all configuration for mod: " + modName);
    }

    // Reset to defaults
    public void resetToDefaults() {
        prefs.edit().clear().apply();
        
        // Apply default values
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(entry.getKey(), (Float) value);
            } else if (value instanceof Long) {
                editor.putLong(entry.getKey(), (Long) value);
            }
        }
        editor.apply();
        
        LogUtils.logUser("Reset " + modName + " configuration to defaults");
    }

    // Export configuration as JSON
    public String exportToJson() {
        JSONObject json = new JSONObject();
        try {
            Map<String, ?> allPrefs = prefs.getAll();
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            LogUtils.logDebug("Failed to export config to JSON: " + e.getMessage());
        }
        return json.toString();
    }

    // Import configuration from JSON
    public boolean importFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            SharedPreferences.Editor editor = prefs.edit();
            
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);
                
                if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Double) {
                    editor.putFloat(key, ((Double) value).floatValue());
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                }
            }
            
            editor.apply();
            LogUtils.logUser("Imported configuration for mod: " + modName);
            return true;
            
        } catch (JSONException e) {
            LogUtils.logDebug("Failed to import config from JSON: " + e.getMessage());
            return false;
        }
    }

    // Get all configuration keys
    public String[] getAllKeys() {
        Map<String, ?> allPrefs = prefs.getAll();
        return allPrefs.keySet().toArray(new String[0]);
    }

    // Get configuration summary
    public String getConfigSummary() {
        Map<String, ?> allPrefs = prefs.getAll();
        StringBuilder summary = new StringBuilder();
        summary.append("Configuration for ").append(modName).append(":\n");
        
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            summary.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        if (allPrefs.isEmpty()) {
            summary.append("No configuration set (using defaults)\n");
        }
        
        return summary.toString();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModController.java
// File: ModController.java (Extracted Component) - Handles mod state management
// Path: /storage/emulated/0/AndroidIDEProjects/main/java/com/terrarialoader/loader/ModController.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import java.io.File;

public class ModController {
    private ModLoader modLoader;
    private ModRepository modRepository;

    public ModController() {
        this.modLoader = new ModLoader();
        this.modRepository = new ModRepository();
    }

    // Allow injection of components for testing/customization
    public ModController(ModLoader modLoader, ModRepository modRepository) {
        this.modLoader = modLoader;
        this.modRepository = modRepository;
    }

    public boolean enableMod(Context context, File modFile) {
        if (!modFile.exists()) {
            LogUtils.logDebug("Cannot enable non-existent mod: " + modFile.getName());
            return false;
        }

        if (isModEnabled(modFile)) {
            LogUtils.logDebug("Mod already enabled: " + modFile.getName());
            return true;
        }

        String currentName = modFile.getName();
        String newName = currentName.replace(".disabled", "");
        File enabledFile = new File(modFile.getParentFile(), newName);

        if (modFile.renameTo(enabledFile)) {
            LogUtils.logUser("✅ Enabled mod: " + newName);
            refreshMods(context);
            return true;
        } else {
            LogUtils.logDebug("Failed to enable mod: " + currentName);
            return false;
        }
    }

    public boolean disableMod(Context context, File modFile) {
        if (!modFile.exists()) {
            LogUtils.logDebug("Cannot disable non-existent mod: " + modFile.getName());
            return false;
        }

        if (!isModEnabled(modFile)) {
            LogUtils.logDebug("Mod already disabled: " + modFile.getName());
            return true;
        }

        String currentName = modFile.getName();
        String newName = currentName + ".disabled";
        File disabledFile = new File(modFile.getParentFile(), newName);

        if (modFile.renameTo(disabledFile)) {
            LogUtils.logUser("⏸️ Disabled mod: " + currentName);
            refreshMods(context);
            return true;
        } else {
            LogUtils.logDebug("Failed to disable mod: " + currentName);
            return false;
        }
    }

    public boolean deleteMod(Context context, File modFile) {
        if (!modFile.exists()) {
            LogUtils.logDebug("Cannot delete non-existent mod: " + modFile.getName());
            return false;
        }

        String modName = modFile.getName();
        
        // Clean up configuration
        String configModName = modName.replace(".dex", "").replace(".jar", "")
                                     .replace(".dll", "").replace(".disabled", "");
        
        // For DLL mods, also clean up MelonLoader directory
        if (modName.toLowerCase().endsWith(".dll") || modName.toLowerCase().endsWith(".dll.disabled")) {
            modLoader.cleanupDllMod(context, modFile);
        }

        if (modFile.delete()) {
            LogUtils.logUser("🗑️ Deleted mod: " + modName);
            
            // Remove from repository
            modRepository.removeMod(configModName);
            
            refreshMods(context);
            return true;
        } else {
            LogUtils.logDebug("Failed to delete mod: " + modName);
            return false;
        }
    }

    // Batch operations
    public void enableAllMods(Context context) {
        java.util.List<ModMetadata> sortedMods = modRepository.resolveDependencies();
        int enabledCount = 0;
        
        for (ModMetadata metadata : sortedMods) {
            if (!isModEnabled(metadata.getModFile())) {
                if (enableMod(context, metadata.getModFile())) {
                    enabledCount++;
                }
            }
        }
        
        LogUtils.logUser("Batch enabled " + enabledCount + " mods (dependency order)");
    }

    public void disableAllMods(Context context) {
        int disabledCount = 0;
        java.util.List<File> modsToDisable = new java.util.ArrayList<>(modRepository.getAvailableMods());
        
        for (File mod : modsToDisable) {
            if (isModEnabled(mod)) {
                if (disableMod(context, mod)) {
                    disabledCount++;
                }
            }
        }
        
        LogUtils.logUser("Batch disabled " + disabledCount + " mods");
    }

    // Toggle mod state
    public boolean toggleMod(Context context, File modFile) {
        if (isModEnabled(modFile)) {
            return disableMod(context, modFile);
        } else {
            return enableMod(context, modFile);
        }
    }

    // Validate mod before operations
    public boolean validateMod(File modFile) {
        if (!modFile.exists() || !modFile.isFile()) {
            LogUtils.logDebug("Mod file does not exist or is not a file: " + modFile.getName());
            return false;
        }

        if (modFile.length() == 0) {
            LogUtils.logDebug("Mod file is empty: " + modFile.getName());
            return false;
        }

        String fileName = modFile.getName().toLowerCase();
        String[] supportedExtensions = ModRepository.getSupportedExtensions();
        
        for (String ext : supportedExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }

        LogUtils.logDebug("Unsupported mod file type: " + modFile.getName());
        return false;
    }

    // Create backup before mod operations
    public File createModBackup(File modFile) {
        if (!modFile.exists()) {
            return null;
        }

        try {
            String backupName = modFile.getName() + ".backup." + System.currentTimeMillis();
            File backupFile = new File(modFile.getParentFile(), backupName);

            java.io.FileInputStream in = new java.io.FileInputStream(modFile);
            java.io.FileOutputStream out = new java.io.FileOutputStream(backupFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            in.close();
            out.close();

            LogUtils.logDebug("Created backup: " + backupName);
            return backupFile;

        } catch (Exception e) {
            LogUtils.logDebug("Failed to create backup: " + e.getMessage());
            return null;
        }
    }

    // Refresh mods after changes
    public void refreshMods(Context context) {
        modRepository.scanForMods(context);
        modLoader.loadMods(context, modRepository.getAvailableMods(), modRepository);
    }

    // Status checks
    private boolean isModEnabled(File file) {
        String fileName = file.getName().toLowerCase();
        return !fileName.endsWith(".disabled");
    }

    public String getModStatus(File modFile) {
        if (!modFile.exists()) {
            return "Not Found";
        }
        
        if (isModEnabled(modFile)) {
            return "Enabled";
        } else {
            return "Disabled";
        }
    }

    public ModBase.ModType getModType(File modFile) {
        return ModBase.ModType.fromFileName(modFile.getName());
    }

    // Cleanup operations
    public void cleanupTemporaryFiles(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] tempFiles = cacheDir.listFiles((dir, name) -> 
                    name.startsWith("mod_temp_") || name.endsWith(".tmp"));
                
                if (tempFiles != null) {
                    int deletedCount = 0;
                    for (File tempFile : tempFiles) {
                        if (tempFile.delete()) {
                            deletedCount++;
                        }
                    }
                    
                    if (deletedCount > 0) {
                        LogUtils.logDebug("Cleaned up " + deletedCount + " temporary mod files");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error cleaning up temporary files: " + e.getMessage());
        }
    }

    // Repair operations
    public boolean repairModFile(Context context, File modFile) {
        LogUtils.logDebug("Attempting to repair mod file: " + modFile.getName());
        
        // Create backup first
        File backup = createModBackup(modFile);
        if (backup == null) {
            LogUtils.logDebug("Cannot repair - backup creation failed");
            return false;
        }

        try {
            // Basic repair operations
            if (!validateMod(modFile)) {
                LogUtils.logDebug("Mod validation failed during repair");
                return false;
            }

            // Additional repair logic can be added here
            LogUtils.logUser("✅ Mod repair completed: " + modFile.getName());
            return true;

        } catch (Exception e) {
            LogUtils.logDebug("Mod repair failed: " + e.getMessage());
            
            // Restore from backup if repair failed
            try {
                java.nio.file.Files.copy(backup.toPath(), modFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                LogUtils.logDebug("Restored from backup after failed repair");
            } catch (Exception restoreError) {
                LogUtils.logDebug("Failed to restore from backup: " + restoreError.getMessage());
            }
            
            return false;
        } finally {
            // Clean up backup
            if (backup.exists()) {
                backup.delete();
            }
        }
    }

    // Getters for components (for advanced usage)
    public ModLoader getModLoader() {
        return modLoader;
    }

    public ModRepository getModRepository() {
        return modRepository;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModLoader.java
// File: ModLoader.java (Complete Fixed Component) - Updated Method Calls with Context
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/ModLoader.java

package com.terrarialoader.loader;

import android.content.Context;
import android.util.Log;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.ui.SettingsActivity;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ModLoader {
    private static final String TAG = "ModLoader";
    private final List<ModBase> loadedDexMods = new ArrayList<>();
    private final List<File> loadedDllMods = new ArrayList<>();

    public void loadMods(Context context, List<File> availableMods, ModRepository repository) {
        if (context == null) {
            LogUtils.logDebug("Context is null, cannot load mods");
            return;
        }

        loadedDexMods.clear();
        loadedDllMods.clear();

        if (!SettingsActivity.isModsEnabled(context)) {
            LogUtils.logUser("Mod loading disabled in settings");
            return;
        }

        if (availableMods == null || availableMods.isEmpty()) {
            LogUtils.logUser("No mods found to load");
            return;
        }

        // Check loader requirements
        checkLoaderRequirements(context, availableMods);
        
        // Load mods by type and dependency order
        List<ModMetadata> sortedMods = repository != null ? repository.resolveDependencies() : new ArrayList<>();
        int dexLoaded = 0, dllLoaded = 0;

        for (ModMetadata metadata : sortedMods) {
            if (metadata != null && metadata.getModFile() != null && isModEnabled(metadata.getModFile())) {
                ModBase.ModType type = ModBase.ModType.fromFileName(metadata.getModFile().getName());
                switch (type) {
                    case DEX:
                    case JAR:
                        if (loadDexMod(context, metadata)) {
                            dexLoaded++;
                        }
                        break;
                    case DLL:
                        if (loadDllMod(context, metadata)) {
                            dllLoaded++;
                        }
                        break;
                    case HYBRID:
                        if (loadHybridMod(context, metadata)) {
                            dexLoaded++;
                            dllLoaded++;
                        }
                        break;
                }
            }
        }

        LogUtils.logUser("Loaded " + dexLoaded + " DEX/JAR mods and " + dllLoaded + " DLL mods");
        LogUtils.logUser("Total: " + (dexLoaded + dllLoaded) + " out of " + availableMods.size() + " mods");
    }

    private void checkLoaderRequirements(Context context, List<File> availableMods) {
        if (context == null || availableMods == null) {
            return;
        }

        boolean needsMelonLoader = false;
        for (File mod : availableMods) {
            if (mod != null && isModEnabled(mod)) {
                ModBase.ModType type = ModBase.ModType.fromFileName(mod.getName());
                if (type == ModBase.ModType.DLL || type == ModBase.ModType.HYBRID) {
                    needsMelonLoader = true;
                    break;
                }
            }
        }

        // FIXED: Pass context parameter to MelonLoaderManager method
        if (needsMelonLoader && !MelonLoaderManager.isMelonLoaderInstalled(context)) {
            LogUtils.logUser("⚠️ DLL mods found but MelonLoader not installed");
            LogUtils.logUser("💡 Use 'Inject Loader' to install MelonLoader for DLL mod support");
        }
    }

    private boolean loadDexMod(Context context, ModMetadata metadata) {
        if (context == null || metadata == null || metadata.getModFile() == null) {
            LogUtils.logDebug("Invalid parameters for DEX mod loading");
            return false;
        }

        File file = metadata.getModFile();
        try {
            String optimizedDir = context.getCodeCacheDir().getAbsolutePath();
            DexClassLoader loader = new DexClassLoader(
                file.getAbsolutePath(),
                optimizedDir,
                null,
                context.getClassLoader()
            );
            
            String[] possibleClassNames = {
                "com.mod.MyMod",
                "com.mod.MainMod",
                "com.terrariamod.Main",
                "mod.Main",
                "Main"
            };
            
            Class<?> modClass = null;
            String foundClassName = null;

            for (String className : possibleClassNames) {
                try {
                    modClass = loader.loadClass(className);
                    foundClassName = className;
                    break;
                } catch (ClassNotFoundException e) {
                    // Try next class name
                }
            }

            if (modClass == null) {
                LogUtils.logDebug("No valid mod class found in: " + file.getName());
                return false;
            }

            if (!ModBase.class.isAssignableFrom(modClass)) {
                LogUtils.logDebug("Class " + foundClassName + " does not implement ModBase interface");
                return false;
            }

            ModBase mod = (ModBase) modClass.newInstance();
            metadata.updateFromModBase(mod);

            if (SettingsActivity.isSandboxMode(context)) {
                LogUtils.logDebug("Loading DEX mod in sandbox mode: " + file.getName());
            }

            mod.onLoad(context);
            loadedDexMods.add(mod);

            LogUtils.logUser("✅ Loaded DEX mod: " + metadata.getName() + " v" + metadata.getVersion() +
                           " (class: " + foundClassName + ")");
            return true;

        } catch (Exception e) {
            String errorMsg = "Failed to load DEX mod: " + file.getName() + " - " + e.getMessage();
            LogUtils.logDebug(errorMsg);
            Log.e(TAG, errorMsg, e);
            return false;
        }
    }

    private boolean loadDllMod(Context context, ModMetadata metadata) {
        if (context == null || metadata == null || metadata.getModFile() == null) {
            LogUtils.logDebug("Invalid parameters for DLL mod loading");
            return false;
        }

        File file = metadata.getModFile();
        try {
            // FIXED: Pass context parameter to MelonLoaderManager method
            if (!MelonLoaderManager.isMelonLoaderInstalled(context)) {
                LogUtils.logDebug("Cannot load DLL mod - no loader installed: " + file.getName());
                return false;
            }

            // Create mods directory for MelonLoader using proper path management
            File melonModsDir = new File(context.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid/Mods/DLL");
            if (!melonModsDir.exists()) {
                melonModsDir.mkdirs();
            }

            // Copy DLL to MelonLoader mods directory
            File targetFile = new File(melonModsDir, file.getName().replace(".disabled", ""));
            if (!targetFile.exists()) {
                if (!copyFile(file, targetFile)) {
                    LogUtils.logDebug("Failed to copy DLL mod: " + file.getName());
                    return false;
                }
            }

            // Validate DLL
            if (!validateDllMod(targetFile)) {
                LogUtils.logDebug("DLL validation failed: " + file.getName());
                return false;
            }

            loadedDllMods.add(file);
            LogUtils.logUser("✅ Registered DLL mod: " + metadata.getName() + " v" + metadata.getVersion() +
                           " (will load via MelonLoader on game startup)");
            return true;

        } catch (Exception e) {
            String errorMsg = "Failed to register DLL mod: " + file.getName() + " - " + e.getMessage();
            LogUtils.logDebug(errorMsg);
            Log.e(TAG, errorMsg, e);
            return false;
        }
    }

    private boolean loadHybridMod(Context context, ModMetadata metadata) {
        if (context == null || metadata == null) {
            LogUtils.logDebug("Invalid parameters for hybrid mod loading");
            return false;
        }

        LogUtils.logDebug("Loading hybrid mod: " + metadata.getName());
        // For hybrid mods, we need to load both components
        boolean dexLoaded = loadDexMod(context, metadata);
        boolean dllLoaded = loadDllMod(context, metadata);

        if (dexLoaded || dllLoaded) {
            LogUtils.logUser("✅ Loaded hybrid mod: " + metadata.getName() +
                           " (DEX: " + dexLoaded + ", DLL: " + dllLoaded + ")");
            return true;
        }

        return false;
    }

    private boolean isModEnabled(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        return !fileName.endsWith(".disabled");
    }

    private boolean copyFile(File source, File target) {
        if (source == null || target == null || !source.exists()) {
            return false;
        }

        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(target)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            LogUtils.logDebug("Successfully copied: " + source.getName() + " -> " + target.getName());
            return true;
        } catch (Exception e) {
            LogUtils.logDebug("File copy failed: " + e.getMessage());
            return false;
        }
    }

    private boolean validateDllMod(File dllFile) {
        if (dllFile == null || !dllFile.exists()) {
            return false;
        }

        try {
            // Basic DLL validation - check file size and header
            if (dllFile.length() == 0) {
                LogUtils.logDebug("DLL file is empty: " + dllFile.getName());
                return false;
            }
            
            // Check for PE header (basic DLL validation)
            try (FileInputStream fis = new FileInputStream(dllFile)) {
                byte[] header = new byte[2];
                if (fis.read(header) == 2) {
                    // Check for MZ signature (PE executable)
                    if (header[0] == 0x4D && header[1] == 0x5A) {
                        LogUtils.logDebug("DLL validation passed: " + dllFile.getName());
                        return true;
                    } else {
                        LogUtils.logDebug("Invalid PE header in DLL: " + dllFile.getName());
                        return false;
                    }
                }
            }
            LogUtils.logDebug("Could not read DLL header: " + dllFile.getName());
            return false;
        } catch (Exception e) {
            LogUtils.logDebug("DLL validation error: " + e.getMessage());
            return false;
        }
    }

    // Cleanup method for DLL mods
    public void cleanupDllMod(Context context, File modFile) {
        if (context == null || modFile == null) {
            return;
        }

        try {
            File melonModsDir = new File(context.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid/Mods/DLL");
            File targetFile = new File(melonModsDir, modFile.getName().replace(".disabled", ""));
            if (targetFile.exists()) {
                if (targetFile.delete()) {
                    LogUtils.logDebug("Cleaned up DLL from MelonLoader directory: " + targetFile.getName());
                } else {
                    LogUtils.logDebug("Failed to cleanup DLL: " + targetFile.getName());
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("DLL cleanup error: " + e.getMessage());
        }
    }

    // Enhanced cleanup for all temporary files
    public void cleanupAllTemporaryFiles(Context context) {
        if (context == null) {
            return;
        }

        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] tempFiles = cacheDir.listFiles((dir, name) -> 
                    name.startsWith("mod_temp_") || 
                    name.startsWith("input_") || 
                    name.endsWith(".tmp"));
                
                if (tempFiles != null) {
                    int deletedCount = 0;
                    for (File tempFile : tempFiles) {
                        if (tempFile.delete()) {
                            deletedCount++;
                        }
                    }
                    
                    if (deletedCount > 0) {
                        LogUtils.logDebug("Cleaned up " + deletedCount + " temporary mod files");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error cleaning up temporary files: " + e.getMessage());
        }
    }

    // Getters for loaded mods with null safety
    public List<ModBase> getLoadedDexMods() {
        return new ArrayList<>(loadedDexMods);
    }

    public List<File> getLoadedDllMods() {
        return new ArrayList<>(loadedDllMods);
    }

    // Get loading statistics
    public int getLoadedDexModCount() {
        return loadedDexMods.size();
    }

    public int getLoadedDllModCount() {
        return loadedDllMods.size();
    }

    public int getTotalLoadedModCount() {
        return loadedDexMods.size() + loadedDllMods.size();
    }

    // Check if any mods are loaded
    public boolean hasLoadedMods() {
        return !loadedDexMods.isEmpty() || !loadedDllMods.isEmpty();
    }

    // Get mod loading status
    public String getLoadingStatus() {
        return "Loaded " + getLoadedDexModCount() + " DEX/JAR mods and " + 
               getLoadedDllModCount() + " DLL mods (" + getTotalLoadedModCount() + " total)";
    }

    // Clear all loaded mods
    public void clearLoadedMods() {
        loadedDexMods.clear();
        loadedDllMods.clear();
        LogUtils.logDebug("Cleared all loaded mods from memory");
    }

    // Unload specific mod (for DEX mods)
    public boolean unloadDexMod(ModBase mod) {
        if (mod == null) {
            return false;
        }

        try {
            mod.onUnload();
            boolean removed = loadedDexMods.remove(mod);
            if (removed) {
                LogUtils.logDebug("Unloaded DEX mod: " + mod.getModName());
            }
            return removed;
        } catch (Exception e) {
            LogUtils.logDebug("Error unloading DEX mod: " + e.getMessage());
            return false;
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModManager.java
// File: ModManager.java (Fixed Facade) - Updated with PathManager
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/ModManager.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.util.List;

/**
 * ModManager serves as a facade that delegates to specialized components:
 * - ModLoader: Handles the actual loading of mods
 * - ModRepository: Manages mod storage and metadata
 * - ModController: Handles mod state changes (enable/disable/delete)
 * 
 * This maintains backward compatibility while organizing code into focused components.
 * Updated to use PathManager for consistent directory structure.
 */
public class ModManager {
    private static final ModLoader modLoader = new ModLoader();
    private static final ModRepository modRepository = new ModRepository();
    private static final ModController modController = new ModController(modLoader, modRepository);

    // === LOADING OPERATIONS ===
    public static void loadMods(Context context) {
        if (context == null) {
            return;
        }
        
        // Check for migration needs first
        if (PathManager.needsMigration(context)) {
            PathManager.migrateLegacyStructure(context);
        }
        
        // Initialize directory structure
        PathManager.initializeGameDirectories(context, MelonLoaderManager.TERRARIA_PACKAGE);
        
        modRepository.scanForMods(context);
        modLoader.loadMods(context, modRepository.getAvailableMods(), modRepository);
    }

    // === RETRIEVAL OPERATIONS ===
    public static List<ModBase> getLoadedMods() {
        return modLoader.getLoadedDexMods();
    }

    public static List<File> getLoadedDllMods() {
        return modLoader.getLoadedDllMods();
    }

    public static List<File> getAvailableMods() {
        return modRepository.getAvailableMods();
    }

    public static List<File> getModsByType(ModBase.ModType type) {
        return modRepository.getModsByType(type);
    }

    public static List<ModMetadata> getModMetadata() {
        return modRepository.getModMetadata();
    }

    public static ModMetadata getMetadata(String modName) {
        return modRepository.getMetadata(modName);
    }

    public static ModConfiguration getConfiguration(Context context, String modName) {
        return modRepository.getConfiguration(context, modName);
    }

    // === MOD CONTROL OPERATIONS ===
    public static boolean enableMod(Context context, File modFile) {
        return modController.enableMod(context, modFile);
    }

    public static boolean disableMod(Context context, File modFile) {
        return modController.disableMod(context, modFile);
    }

    public static boolean deleteMod(Context context, File modFile) {
        return modController.deleteMod(context, modFile);
    }

    public static boolean toggleMod(Context context, File modFile) {
        return modController.toggleMod(context, modFile);
    }

    // === BATCH OPERATIONS ===
    public static void enableAllMods(Context context) {
        modController.enableAllMods(context);
    }

    public static void disableAllMods(Context context) {
        modController.disableAllMods(context);
    }

    // === STATISTICS ===
    public static int getEnabledModCount() {
        return modRepository.getEnabledModCount();
    }

    public static int getDisabledModCount() {
        return modRepository.getDisabledModCount();
    }

    public static int getTotalModCount() {
        return modRepository.getTotalModCount();
    }

    public static int getDexModCount() {
        return modRepository.getDexModCount();
    }

    public static int getDllModCount() {
        return modRepository.getDllModCount();
    }

    public static int getHybridModCount() {
        return modRepository.getHybridModCount();
    }

    // === UTILITY OPERATIONS ===
    public static boolean validateMod(File modFile) {
        return modController.validateMod(modFile);
    }

    public static String getModStatus(File modFile) {
        return modController.getModStatus(modFile);
    }

    public static ModBase.ModType getModType(File modFile) {
        return modController.getModType(modFile);
    }

    // === MAINTENANCE OPERATIONS ===
    public static void cleanupTemporaryFiles(Context context) {
        modController.cleanupTemporaryFiles(context);
    }

    public static boolean repairModFile(Context context, File modFile) {
        return modController.repairModFile(context, modFile);
    }

    public static File createModBackup(File modFile) {
        return modController.createModBackup(modFile);
    }

    // === DEBUG OPERATIONS ===
    public static String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== ModManager Debug Info (Facade Pattern) ===\n");
        info.append("Components: ModLoader, ModRepository, ModController\n");
        info.append("PathManager: Centralized path management\n\n");
        info.append(modRepository.getDebugInfo());
        
        info.append("\nLoaded Components:\n");
        info.append("- DEX Mods Loaded: ").append(modLoader.getLoadedDexMods().size()).append("\n");
        info.append("- DLL Mods Loaded: ").append(modLoader.getLoadedDllMods().size()).append("\n");
        
        return info.toString();
    }

    // === COMPONENT ACCESS (for advanced usage) ===
    public static ModLoader getModLoader() {
        return modLoader;
    }

    public static ModRepository getModRepository() {
        return modRepository;
    }

    public static ModController getModController() {
        return modController;
    }

    // === REFRESH OPERATION ===
    public static void refreshMods(Context context) {
        modController.refreshMods(context);
    }

    // === DEPENDENCY RESOLUTION ===
    public static List<ModMetadata> resolveDependencies() {
        return modRepository.resolveDependencies();
    }

    // === PATH-AWARE OPERATIONS ===
    public static String getModsDirectoryPath(Context context) {
        if (context == null) return "Context is null";
        
        File dexModsDir = PathManager.getDexModsDir(context, MelonLoaderManager.TERRARIA_PACKAGE);
        return dexModsDir != null ? dexModsDir.getAbsolutePath() : "Path unavailable";
    }

    public static String getDllModsDirectoryPath(Context context) {
        if (context == null) return "Context is null";
        
        File dllModsDir = PathManager.getDllModsDir(context, MelonLoaderManager.TERRARIA_PACKAGE);
        return dllModsDir != null ? dllModsDir.getAbsolutePath() : "Path unavailable";
    }

    public static boolean initializeModDirectories(Context context) {
        if (context == null) return false;
        
        return PathManager.initializeGameDirectories(context, MelonLoaderManager.TERRARIA_PACKAGE);
    }

    // === MIGRATION SUPPORT ===
    public static boolean migrateModsToNewStructure(Context context) {
        if (context == null) return false;
        
        if (PathManager.needsMigration(context)) {
            return PathManager.migrateLegacyStructure(context);
        }
        return true; // No migration needed
    }

    // === DIRECTORY VALIDATION ===
    public static boolean validateModDirectories(Context context) {
        if (context == null) return false;
        
        return MelonLoaderManager.validateModDirectories(context, MelonLoaderManager.TERRARIA_PACKAGE);
    }

    // === HEALTH CHECK ===
    public static boolean performHealthCheck(Context context) {
        if (context == null) return false;
        
        boolean healthy = true;
        
        // Check directory structure
        if (!validateModDirectories(context)) {
            healthy = false;
        }
        
        // Check for migration needs
        if (PathManager.needsMigration(context)) {
            migrateModsToNewStructure(context);
        }
        
        // Validate mod files
        List<File> availableMods = getAvailableMods();
        if (availableMods != null) {
            for (File mod : availableMods) {
                if (!validateMod(mod)) {
                    healthy = false;
                    break;
                }
            }
        }
        
        return healthy;
    }

    // === INITIALIZATION ===
    public static void initialize(Context context) {
        if (context == null) return;
        
        // Initialize directory structure
        initializeModDirectories(context);
        
        // Perform migration if needed
        migrateModsToNewStructure(context);
        
        // Load initial mods
        loadMods(context);
    }

    // === CLEANUP ===
    public static void cleanup(Context context) {
        if (context == null) return;
        
        cleanupTemporaryFiles(context);
    }

    // === LEGACY COMPATIBILITY ===
    // These methods are deprecated but maintained for backward compatibility
    
    @Deprecated
    public static void loadMods() {
        // Cannot function without context
        throw new UnsupportedOperationException("Context is required. Use loadMods(Context context) instead.");
    }
    
    @Deprecated
    public static boolean enableMod(File modFile) {
        // Cannot function without context
        throw new UnsupportedOperationException("Context is required. Use enableMod(Context context, File modFile) instead.");
    }
    
    @Deprecated
    public static boolean disableMod(File modFile) {
        // Cannot function without context
        throw new UnsupportedOperationException("Context is required. Use disableMod(Context context, File modFile) instead.");
    }
    
    @Deprecated
    public static boolean deleteMod(File modFile) {
        // Cannot function without context
        throw new UnsupportedOperationException("Context is required. Use deleteMod(Context context, File modFile) instead.");
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModMetadata.java
// File: ModMetadata.java (Fixed Class) - Enhanced Null Safety
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/ModMetadata.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModMetadata {
    private String name;
    private String version;
    private String description;
    private String author;
    private List<String> dependencies;
    private String minGameVersion;
    private String maxGameVersion;
    private File modFile;
    private boolean isValid;
    private ModBase.ModType modType;

    // FIXED: Constructor with enhanced null safety
    public ModMetadata(File modFile) {
        this.modFile = modFile;
        this.dependencies = new ArrayList<>();
        this.isValid = false;
        
        // FIXED: Safe mod type detection
        try {
            this.modType = ModBase.ModType.fromFileName(modFile.getName());
        } catch (Exception e) {
            LogUtils.logDebug("Error detecting mod type: " + e.getMessage());
            this.modType = ModBase.ModType.DEX; // Default fallback
        }
        
        // Set safe defaults
        this.name = modFile.getName();
        this.version = "1.0.0";
        this.description = "No description available";
        this.author = "Unknown";
        this.minGameVersion = "1.0.0";
        this.maxGameVersion = "999.0.0";
        
        loadMetadata();
    }

    // Constructor with explicit ModType (for advanced usage)
    public ModMetadata(File modFile, ModBase.ModType modType) {
        this.modFile = modFile;
        this.dependencies = new ArrayList<>();
        this.isValid = false;
        this.modType = modType != null ? modType : ModBase.ModType.DEX; // Null safety
        
        // Set safe defaults
        this.name = modFile.getName();
        this.version = "1.0.0";
        this.description = "No description available";
        this.author = "Unknown";
        this.minGameVersion = "1.0.0";
        this.maxGameVersion = "999.0.0";
        
        loadMetadata();
    }

    private void loadMetadata() {
        try {
            // Try to load from mod.json file in same directory
            File metadataFile = new File(modFile.getParentFile(), 
                modFile.getName().replace(".dex", ".json")
                                  .replace(".jar", ".json")
                                  .replace(".dll", ".json")
                                  .replace(".hybrid", ".json")
                                  .replace(".disabled", "")); // Remove .disabled if present
            
            if (metadataFile.exists()) {
                loadFromJsonFile(metadataFile);
            } else {
                // Use defaults and mark as valid
                this.isValid = true;
                LogUtils.logDebug("Using default metadata for: " + name);
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error loading metadata: " + e.getMessage());
            this.isValid = true; // Still mark as valid with defaults
        }
    }

    private void loadFromJsonFile(File jsonFile) {
        try {
            byte[] jsonData = new byte[(int) jsonFile.length()];
            FileInputStream fis = new FileInputStream(jsonFile);
            fis.read(jsonData);
            fis.close();
            
            String jsonString = new String(jsonData);
            JSONObject json = new JSONObject(jsonString);
            
            this.name = json.optString("name", this.name);
            this.version = json.optString("version", this.version);
            this.description = json.optString("description", this.description);
            this.author = json.optString("author", this.author);
            this.minGameVersion = json.optString("minGameVersion", this.minGameVersion);
            this.maxGameVersion = json.optString("maxGameVersion", this.maxGameVersion);
            
            // Load dependencies
            JSONArray depsArray = json.optJSONArray("dependencies");
            if (depsArray != null) {
                for (int i = 0; i < depsArray.length(); i++) {
                    dependencies.add(depsArray.getString(i));
                }
            }
            
            this.isValid = true;
            LogUtils.logDebug("Loaded metadata from JSON: " + name);
            
        } catch (Exception e) {
            LogUtils.logDebug("Failed to load metadata from JSON: " + e.getMessage());
            this.isValid = true; // Still mark as valid with defaults
        }
    }

    // FIXED: Update metadata from loaded ModBase instance with null safety
    public void updateFromModBase(ModBase modInstance) {
        if (modInstance != null) {
            try {
                this.name = modInstance.getModName() != null ? modInstance.getModName() : this.name;
                this.version = modInstance.getModVersion() != null ? modInstance.getModVersion() : this.version;
                this.description = modInstance.getModDescription() != null ? modInstance.getModDescription() : this.description;
                this.author = modInstance.getModAuthor() != null ? modInstance.getModAuthor() : this.author;
                this.minGameVersion = modInstance.getMinGameVersion() != null ? modInstance.getMinGameVersion() : this.minGameVersion;
                this.maxGameVersion = modInstance.getMaxGameVersion() != null ? modInstance.getMaxGameVersion() : this.maxGameVersion;
                
                String[] deps = modInstance.getDependencies();
                if (deps != null) {
                    this.dependencies = Arrays.asList(deps);
                }
                
                LogUtils.logDebug("Updated metadata from ModBase: " + name);
            } catch (Exception e) {
                LogUtils.logDebug("Error updating from ModBase: " + e.getMessage());
            }
        }
    }

    // Getters with null safety
    public String getName() { return name != null ? name : "Unknown"; }
    public String getVersion() { return version != null ? version : "1.0.0"; }
    public String getDescription() { return description != null ? description : "No description"; }
    public String getAuthor() { return author != null ? author : "Unknown"; }
    public List<String> getDependencies() { return dependencies != null ? dependencies : new ArrayList<>(); }
    public String getMinGameVersion() { return minGameVersion != null ? minGameVersion : "1.0.0"; }
    public String getMaxGameVersion() { return maxGameVersion != null ? maxGameVersion : "999.0.0"; }
    public File getModFile() { return modFile; }
    public boolean isValid() { return isValid; }
    public ModBase.ModType getModType() { return modType != null ? modType : ModBase.ModType.DEX; }

    // Dependency checking
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }

    public boolean isDependencySatisfied(List<ModMetadata> availableMods) {
        if (!hasDependencies()) return true;
        
        for (String dependency : dependencies) {
            boolean found = false;
            for (ModMetadata mod : availableMods) {
                if (mod != null && dependency.equals(mod.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // Version compatibility
    public boolean isCompatibleWithGameVersion(String gameVersion) {
        try {
            return compareVersions(gameVersion, minGameVersion) >= 0 &&
                   compareVersions(gameVersion, maxGameVersion) <= 0;
        } catch (Exception e) {
            LogUtils.logDebug("Version comparison failed: " + e.getMessage());
            return true; // Default to compatible
        }
    }

    private int compareVersions(String version1, String version2) {
        try {
            String[] v1Parts = version1.split("\\.");
            String[] v2Parts = version2.split("\\.");
            
            int maxLength = Math.max(v1Parts.length, v2Parts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
                int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
                
                if (v1Part != v2Part) {
                    return Integer.compare(v1Part, v2Part);
                }
            }
            
            return 0;
        } catch (Exception e) {
            LogUtils.logDebug("Version parsing error: " + e.getMessage());
            return 0; // Assume equal if parsing fails
        }
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion() + " by " + getAuthor() + 
               " (Type: " + (getModType() != null ? getModType().getDisplayName() : "Unknown") + ")";
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/loader/ModRepository.java
// File: ModRepository.java (Fixed Component) - Updated Method Calls
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/ModRepository.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModRepository {
    private final List<File> availableMods = new ArrayList<>();
    private final Map<String, ModMetadata> modMetadataMap = new HashMap<>();
    private final Map<String, ModConfiguration> modConfigMap = new HashMap<>();
    
    // Enhanced file extensions for DLL support
    private static final String[] SUPPORTED_EXTENSIONS = {
        ".dex", ".jar", ".dll", ".dex.disabled", ".jar.disabled", ".dll.disabled", ".hybrid", ".hybrid.disabled"
    };

    public void scanForMods(Context context) {
        availableMods.clear();
        modMetadataMap.clear();

        File modDir = new File(context.getExternalFilesDir(null), "mods");
        if (!modDir.exists() && !modDir.mkdirs()) {
            LogUtils.logDebug("Failed to create mods directory");
            return;
        }

        // Enhanced file filtering for DLL support
        File[] modFiles = modDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });

        if (modFiles != null) {
            LogUtils.logUser("Found " + modFiles.length + " mod files");
            
            // Load metadata for all mods
            for (File file : modFiles) {
                availableMods.add(file);
                ModMetadata metadata = new ModMetadata(file);
                modMetadataMap.put(metadata.getName(), metadata);
            }
        }
    }

    public List<File> getAvailableMods() {
        return new ArrayList<>(availableMods);
    }

    public List<File> getModsByType(ModBase.ModType type) {
        List<File> typedMods = new ArrayList<>();
        for (File mod : availableMods) {
            if (ModBase.ModType.fromFileName(mod.getName()) == type) {
                typedMods.add(mod);
            }
        }
        return typedMods;
    }

    public List<ModMetadata> getModMetadata() {
        return new ArrayList<>(modMetadataMap.values());
    }

    public ModMetadata getMetadata(String modName) {
        return modMetadataMap.get(modName);
    }

    public ModConfiguration getConfiguration(Context context, String modName) {
        if (!modConfigMap.containsKey(modName)) {
            modConfigMap.put(modName, new ModConfiguration(context, modName));
        }
        return modConfigMap.get(modName);
    }

    // Dependency resolution
    public List<ModMetadata> resolveDependencies() {
        List<ModMetadata> allMods = new ArrayList<>(modMetadataMap.values());
        List<ModMetadata> sortedMods = new ArrayList<>();
        List<ModMetadata> remaining = new ArrayList<>(allMods);
        
        while (!remaining.isEmpty()) {
            boolean progress = false;
            for (int i = remaining.size() - 1; i >= 0; i--) {
                ModMetadata mod = remaining.get(i);
                if (mod.isDependencySatisfied(sortedMods)) {
                    sortedMods.add(mod);
                    remaining.remove(i);
                    progress = true;
                    LogUtils.logDebug("Dependencies satisfied for: " + mod.getName());
                }
            }

            if (!progress && !remaining.isEmpty()) {
                for (ModMetadata mod : remaining) {
                    LogUtils.logDebug("Dependency issue with mod: " + mod.getName());
                    sortedMods.add(mod);
                }
                remaining.clear();
            }
        }

        return sortedMods;
    }

    // Statistics methods
    public int getEnabledModCount() {
        int count = 0;
        for (File mod : availableMods) {
            if (isModEnabled(mod)) {
                count++;
            }
        }
        return count;
    }

    public int getDisabledModCount() {
        return availableMods.size() - getEnabledModCount();
    }

    public int getTotalModCount() {
        return availableMods.size();
    }

    public int getDexModCount() {
        return getModsByType(ModBase.ModType.DEX).size() + getModsByType(ModBase.ModType.JAR).size();
    }

    public int getDllModCount() {
        return getModsByType(ModBase.ModType.DLL).size();
    }

    public int getHybridModCount() {
        return getModsByType(ModBase.ModType.HYBRID).size();
    }

    private boolean isModEnabled(File file) {
        String fileName = file.getName().toLowerCase();
        return !fileName.endsWith(".disabled");
    }

    // Clean up method to remove mod from repository
    public void removeMod(String modName) {
        // Remove from metadata map
        modMetadataMap.remove(modName);
        
        // Remove from available mods list
        availableMods.removeIf(file -> {
            String fileName = file.getName();
            String cleanName = fileName.replace(".dex", "").replace(".jar", "")
                                      .replace(".dll", "").replace(".disabled", "");
            return cleanName.equals(modName);
        });
        
        // Clean up configuration
        if (modConfigMap.containsKey(modName)) {
            modConfigMap.get(modName).clear();
            modConfigMap.remove(modName);
        }
    }

    // Debug information
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== ModRepository Debug Info ===\n");
        info.append("Total mods: ").append(availableMods.size()).append("\n");
        info.append("DEX/JAR mods: ").append(getDexModCount()).append("\n");
        info.append("DLL mods: ").append(getDllModCount()).append("\n");
        info.append("Hybrid mods: ").append(getHybridModCount()).append("\n");
        info.append("Metadata entries: ").append(modMetadataMap.size()).append("\n");
        info.append("Configuration entries: ").append(modConfigMap.size()).append("\n");
        info.append("NOTE: MelonLoader status requires Context parameter\n");
        info.append("\nMod details:\n");
        
        for (ModMetadata metadata : modMetadataMap.values()) {
            ModBase.ModType type = ModBase.ModType.fromFileName(metadata.getModFile().getName());
            info.append("- ").append(metadata.toString()).append(" [").append(type.getDisplayName()).append("]\n");
            if (metadata.hasDependencies()) {
                info.append("  Dependencies: ").append(metadata.getDependencies()).append("\n");
            }
        }
        
        return info.toString();
    }

    // Supported extensions getter
    public static String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS.clone();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/DllModActivity.java
// File: DllModActivity.java (Updated Activity) - Uses Controller Pattern
// Path: /storage/emulated/0/AndroidIDEProjects/main/java/com/terrarialoader/ui/DllModActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

/**
 * DllModActivity now delegates business logic to DllModController
 * This keeps the activity focused on UI management while the controller handles the logic
 */
public class DllModActivity extends Activity implements DllModController.DllModCallback {

    private static final int REQUEST_SELECT_APK = 1001;
    private static final int REQUEST_SELECT_DLL = 1002;
    
    // UI Components
    private TextView statusText;
    private TextView loaderStatusText;
    private RecyclerView dllModRecyclerView;
    private Button installLoaderBtn;
    private Button selectApkBtn;
    private Button installDllBtn;
    private Button viewLogsBtn;
    private Button refreshBtn;
    private LinearLayout loaderInfoSection;
    
    // Controller and Adapter
    private DllModController controller;
    private DllModAdapter adapter;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dll_mod);
        
        setTitle("DLL Mod Manager");
        
        // Initialize controller
        controller = new DllModController(this);
        controller.setCallback(this);
        
        initializeViews();
        setupUI();
        
        // Load initial data
        controller.loadDllMods();
        controller.updateLoaderStatus();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        dllModRecyclerView = findViewById(R.id.dllModRecyclerView);
        installLoaderBtn = findViewById(R.id.installLoaderBtn);
        selectApkBtn = findViewById(R.id.selectApkBtn);
        installDllBtn = findViewById(R.id.installDllBtn);
        viewLogsBtn = findViewById(R.id.viewLogsBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        loaderInfoSection = findViewById(R.id.loaderInfoSection);
    }

    private void setupUI() {
        // Setup RecyclerView
        dllModRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DllModAdapter(this, controller);
        dllModRecyclerView.setAdapter(adapter);

        // Button listeners - delegate to controller
        installLoaderBtn.setOnClickListener(v -> controller.showLoaderInstallDialog());
        
        selectApkBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_APK);
        });
        
        installDllBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all files, we'll filter by extension
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DLL);
        });
        
        viewLogsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LogViewerActivity.class));
        });
        
        refreshBtn.setOnClickListener(v -> {
            controller.refresh();
            Toast.makeText(this, "Refreshed DLL mod list", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        
        switch (requestCode) {
            case REQUEST_SELECT_APK:
                controller.handleApkSelection(uri);
                break;
                
            case REQUEST_SELECT_DLL:
                String filename = getFilenameFromUri(uri);
                controller.handleDllSelection(uri, filename);
                break;
        }
    }

    private String getFilenameFromUri(Uri uri) {
        String filename = null;
        
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename from URI: " + e.getMessage());
        }
        
        return filename;
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.refresh();
    }

    // === DllModController.DllModCallback Implementation ===
    
    @Override
    public void onModsLoaded(List<File> mods) {
        runOnUiThread(() -> {
            adapter.updateMods(mods);
            statusText.setText(controller.getStatusText());
        });
    }

    @Override
    public void onLoaderStatusChanged(boolean installed, String statusText, int textColor) {
        runOnUiThread(() -> {
            loaderStatusText.setText(statusText);
            loaderStatusText.setTextColor(textColor);
            
            if (installed) {
                installLoaderBtn.setText("Reinstall Loader");
                loaderInfoSection.setVisibility(View.VISIBLE);
                installDllBtn.setEnabled(true);
                installDllBtn.setText("Install DLL Mod");
            } else {
                installLoaderBtn.setText("Install Loader");
                loaderInfoSection.setVisibility(View.GONE);
                installDllBtn.setEnabled(false);
                installDllBtn.setText("Install Loader First");
            }
        });
    }

    @Override
    public void onInstallationProgress(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            
            if (message.contains("Please select")) {
                // This is a request for user action, not a progress message
                return;
            }
            
            progressDialog = new AlertDialog.Builder(this)
                .setTitle("Installing Loader")
                .setMessage(message)
                .setCancelable(false)
                .show();
        });
    }

    @Override
    public void onInstallationComplete(boolean success, String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            
            if (success) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    // === Simple adapter class for DLL mods ===
    private static class DllModAdapter extends RecyclerView.Adapter<DllModAdapter.ViewHolder> {
        private final Activity activity;
        private final DllModController controller;
        private List<File> mods;

        public DllModAdapter(Activity activity, DllModController controller) {
            this.activity = activity;
            this.controller = controller;
            this.mods = controller.getDllMods();
        }

        public void updateMods(List<File> newMods) {
            this.mods = newMods;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mod, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File mod = mods.get(position);
            String name = mod.getName();
            boolean isEnabled = !name.endsWith(".disabled");
            
            holder.modDescription.setText(name);
            holder.modSwitch.setChecked(isEnabled);
            
            holder.modSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                controller.toggleMod(mod, isChecked);
            });
            
            holder.modDeleteButton.setOnClickListener(v -> {
                controller.deleteMod(mod, () -> {
                    mods.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mods.size());
                });
            });
        }

        @Override
        public int getItemCount() {
            return mods.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView modDescription;
            android.widget.Switch modSwitch;
            android.widget.ImageButton modDeleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                modDescription = itemView.findViewById(R.id.modDescription);
                modSwitch = itemView.findViewById(R.id.modSwitch);
                modDeleteButton = itemView.findViewById(R.id.modDeleteButton);
            }
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/DllModController.java
// File: DllModController.java (Fixed) - Corrected method calls with Context parameter
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/DllModController.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.loader.ModBase;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.installer.ModInstaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DllModController {
    private final Activity activity;
    private List<File> dllMods = new ArrayList<>();
    
    // Callback interface for UI updates
    public interface DllModCallback {
        void onModsLoaded(List<File> mods);
        void onLoaderStatusChanged(boolean installed, String statusText, int textColor);
        void onInstallationProgress(String message);
        void onInstallationComplete(boolean success, String message);
        void onError(String error);
    }
    
    private DllModCallback callback;

    public DllModController(Activity activity) {
        this.activity = activity;
    }
    
    public void setCallback(DllModCallback callback) {
        this.callback = callback;
    }

    public void loadDllMods() {
        dllMods.clear();
        
        // Get DLL mods from ModManager
        List<File> allMods = ModManager.getAvailableMods();
        for (File mod : allMods) {
            ModBase.ModType type = ModBase.ModType.fromFileName(mod.getName());
            if (type == ModBase.ModType.DLL || type == ModBase.ModType.HYBRID) {
                dllMods.add(mod);
            }
        }
        
        LogUtils.logDebug("Loaded " + dllMods.size() + " DLL mods");
        
        if (callback != null) {
            callback.onModsLoaded(dllMods);
        }
    }

    public void updateLoaderStatus() {
        // FIXED: Pass activity context to MelonLoaderManager methods
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(activity);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(activity);
        
        if (melonInstalled) {
            String statusText = "✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
            if (callback != null) {
                callback.onLoaderStatusChanged(true, statusText, 0xFF4CAF50); // Green
            }
        } else if (lemonInstalled) {
            String statusText = "✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
            if (callback != null) {
                callback.onLoaderStatusChanged(true, statusText, 0xFF4CAF50); // Green
            }
        } else {
            String statusText = "❌ No loader installed - DLL mods will not work";
            if (callback != null) {
                callback.onLoaderStatusChanged(false, statusText, 0xFFF44336); // Red
            }
        }
    }

    public void showLoaderInstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose Loader");
        builder.setMessage("Select which loader to install:\n\n" +
                          "• MelonLoader: Full-featured, works with most Unity games\n" +
                          "• LemonLoader: Lightweight, better for older devices\n\n" +
                          "You will need to select a Terraria APK file after choosing.");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            LogUtils.logUser("User selected MelonLoader installation");
            showApkInstructions("MelonLoader");
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            LogUtils.logUser("User selected LemonLoader installation");
            showApkInstructions("LemonLoader");
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    public void showApkInstructions(String loaderType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("APK Installation Instructions");
        builder.setMessage("To install " + loaderType + ":\n\n" +
                          "1. Select your Terraria APK file\n" +
                          "2. The loader will be injected into the APK\n" +
                          "3. Install the modified APK\n" +
                          "4. Your DLL mods will work in Terraria\n\n" +
                          "Ready to select APK?");
        
        builder.setPositiveButton("Select APK", (dialog, which) -> {
            // Store the selected loader type for later use
            activity.getSharedPreferences("dll_mod_prefs", Activity.MODE_PRIVATE)
                .edit()
                .putString("selected_loader", loaderType)
                .apply();
            
            // Trigger APK selection in the UI
            if (callback != null) {
                callback.onInstallationProgress("Please select Terraria APK file");
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void handleApkSelection(Uri apkUri) {
        LogUtils.logUser("APK selected for loader installation");
        
        // Get the selected loader type
        String selectedLoader = activity.getSharedPreferences("dll_mod_prefs", Activity.MODE_PRIVATE)
            .getString("selected_loader", "MelonLoader");
        
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirm Installation");
        builder.setMessage("Install " + selectedLoader + " into the selected APK?\n\n" +
                          "This will create a new patched APK file that you can install.");
        
        builder.setPositiveButton("Install", (dialog, which) -> {
            if ("MelonLoader".equals(selectedLoader)) {
                installLoaderIntoApk(apkUri, MelonLoaderManager.LoaderType.MELONLOADER_NET8);
            } else {
                installLoaderIntoApk(apkUri, MelonLoaderManager.LoaderType.MELONLOADER_NET35);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void handleDllSelection(Uri dllUri, String filename) {
        LogUtils.logUser("DLL file selected for installation");
        
        if (filename == null) {
            filename = "mod_" + System.currentTimeMillis() + ".dll";
        }
        
        if (!filename.toLowerCase().endsWith(".dll")) {
            if (callback != null) {
                callback.onError("Please select a .dll file");
            }
            return;
        }
        
        // FIXED: Pass activity context to MelonLoaderManager methods
        if (!MelonLoaderManager.isMelonLoaderInstalled(activity) && !MelonLoaderManager.isLemonLoaderInstalled(activity)) {
            showLoaderRequiredDialog();
            return;
        }
        
        // Install DLL mod
        boolean success = ModInstaller.installMod(activity, dllUri, filename);
        
        if (success) {
            if (callback != null) {
                callback.onInstallationComplete(true, "DLL mod installed successfully");
            }
            loadDllMods(); // Refresh list
        } else {
            if (callback != null) {
                callback.onInstallationComplete(false, "Failed to install DLL mod");
            }
        }
    }

    public void showLoaderRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Loader Required");
        builder.setMessage("DLL mods require MelonLoader or LemonLoader to be installed first.\n\n" +
                          "Would you like to install a loader now?");
        
        builder.setPositiveButton("Install Loader", (dialog, which) -> {
            showLoaderInstallDialog();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void installLoaderIntoApk(Uri apkUri, MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("Installing " + loaderType.getDisplayName() + " into APK");
        
        if (callback != null) {
            callback.onInstallationProgress("Installing " + loaderType.getDisplayName() + " into APK...\nThis may take a few minutes.");
        }
        
        // Run installation in background thread
        new Thread(() -> {
            boolean success = false;
            File outputApk = null;
            
            try {
                // Create temporary files
                File tempApk = File.createTempFile("input_", ".apk", activity.getCacheDir());
                outputApk = new File(activity.getExternalFilesDir(null), "patched_terraria_" + 
                                        loaderType.name().toLowerCase() + "_" +
                                        System.currentTimeMillis() + ".apk");
                
                // Copy input APK to temp file
                FileUtils.copyUriToFile(activity, apkUri, tempApk);
                
                // Install loader
                if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                    success = MelonLoaderManager.installMelonLoader(activity, tempApk, outputApk);
                } else if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET35) {
                    success = MelonLoaderManager.installLemonLoader(activity, tempApk, outputApk);
                }
                
                // Cleanup temp file
                tempApk.delete();
                
            } catch (Exception e) {
                LogUtils.logDebug("Loader installation error: " + e.getMessage());
                success = false;
            }
            
            // Update UI on main thread
            final boolean finalSuccess = success;
            final File finalOutputApk = outputApk;
            
            activity.runOnUiThread(() -> {
                if (finalSuccess) {
                    showInstallationSuccessDialog(finalOutputApk, loaderType);
                } else {
                    if (callback != null) {
                        callback.onInstallationComplete(false, "Loader installation failed");
                    }
                    LogUtils.logUser("❌ " + loaderType.getDisplayName() + " installation failed");
                }
                
                updateLoaderStatus();
            });
        }).start();
    }

    public void showInstallationSuccessDialog(File outputApk, MelonLoaderManager.LoaderType loaderType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Installation Complete");
        builder.setMessage("✅ " + loaderType.getDisplayName() + " has been installed into the APK!\n\n" +
                          "Patched APK saved to:\n" + outputApk.getName() + "\n\n" +
                          "Next steps:\n" +
                          "1. Install the patched APK\n" +
                          "2. Install DLL mods\n" +
                          "3. Launch Terraria");
        
        builder.setPositiveButton("Install APK", (dialog, which) -> {
            installApk(outputApk);
        });
        
        builder.setNegativeButton("Later", null);
        builder.show();
        
        LogUtils.logUser("✅ " + loaderType.getDisplayName() + " installation completed successfully");
        
        if (callback != null) {
            callback.onInstallationComplete(true, loaderType.getDisplayName() + " installation completed successfully");
        }
    }

    public void installApk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(
                    activity, 
                    activity.getPackageName() + ".provider", 
                    apkFile
                ),
                "application/vnd.android.package-archive"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Cannot install APK: " + e.getMessage());
            }
            LogUtils.logDebug("APK installation error: " + e.getMessage());
        }
    }

    public void toggleMod(File mod, boolean isChecked) {
        if (isChecked) {
            ModManager.enableMod(activity, mod);
        } else {
            ModManager.disableMod(activity, mod);
        }
    }

    public void deleteMod(File mod, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Delete Mod");
        builder.setMessage("Delete " + mod.getName() + "?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (ModManager.deleteMod(activity, mod)) {
                if (onSuccess != null) {
                    onSuccess.run();
                }
                loadDllMods(); // Refresh the list
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public List<File> getDllMods() {
        return new ArrayList<>(dllMods);
    }

    public String getStatusText() {
        int enabledCount = 0;
        for (File mod : dllMods) {
            if (!mod.getName().endsWith(".disabled")) {
                enabledCount++;
            }
        }
        
        return "DLL Mods: " + enabledCount + " enabled, " + 
               (dllMods.size() - enabledCount) + " disabled, " + 
               dllMods.size() + " total";
    }

    public boolean isLoaderInstalled() {
        // FIXED: Pass activity context to MelonLoaderManager methods
        return MelonLoaderManager.isMelonLoaderInstalled(activity) || MelonLoaderManager.isLemonLoaderInstalled(activity);
    }

    public void refresh() {
        loadDllMods();
        updateLoaderStatus();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/InstructionsActivity.java
// File: InstructionsActivity.java (Fixed) - Corrected method calls with Context parameter
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/InstructionsActivity.java

package com.terrarialoader.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.R;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;

public class InstructionsActivity extends AppCompatActivity {

    private TextView tvInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        setTitle("Manual Installation Guide");

        tvInstructions = findViewById(R.id.tv_instructions);
        
        setupInstructions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Copy GitHub URLs");
        menu.add(0, 2, 0, "Open GitHub");
        menu.add(0, 3, 0, "Check Installation");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                copyGitHubUrls();
                return true;
            case 2:
                openGitHubDialog();
                return true;
            case 3:
                checkInstallationStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupInstructions() {
        // Get the actual path that the app uses
        String actualBasePath = getExternalFilesDir(null) + "/TerrariaLoader/com.and.games505.TerrariaPaid";
        
        String manualInstructions = "📱 Manual MelonLoader/LemonLoader Installation Guide\n\n" +
                
                "🔗 STEP 1: Download Required Files\n" +
                "──────────────────────────────────\n" +
                "Visit GitHub and download ONE of these:\n\n" +
                
                "🔸 For MelonLoader (Full Features):\n" +
                "• Go to: github.com/LavaGang/MelonLoader/releases\n" +
                "• Download 'melon_data.zip' from latest release\n" +
                "• File size: ~40MB\n\n" +
                
                "🔸 For LemonLoader (Lightweight):\n" +
                "• Go to: github.com/LemonLoader/LemonLoader/releases\n" +
                "• Download 'lemon_data.zip' or installer APK\n" +
                "• File size: ~15MB\n\n" +
                
                "📁 STEP 2: Create Directory Structure\n" +
                "────────────────────────────────────\n" +
                "⚠️ IMPORTANT: Use the CORRECT path for your device!\n\n" +
                
                "Using a file manager, create this structure:\n" +
                actualBasePath + "/\n\n" +
                
                "Alternative path (if first doesn't work):\n" +
                "/storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/com.and.games505.TerrariaPaid/\n\n" +
                
                "Create these folders inside the above path:\n" +
                "├── Loaders/MelonLoader/\n" +
                "│   ├── net8/                    (for MelonLoader)\n" +
                "│   ├── net35/                   (for LemonLoader)\n" +
                "│   └── Dependencies/\n" +
                "│       ├── SupportModules/\n" +
                "│       ├── CompatibilityLayers/\n" +
                "│       └── Il2CppAssemblyGenerator/\n" +
                "│           ├── Cpp2IL/cpp2il_out/\n" +
                "│           ├── UnityDependencies/\n" +
                "│           └── Il2CppInterop/Il2CppAssemblies/\n" +
                "├── Mods/\n" +
                "│   ├── DLL/                     (for your DLL mods)\n" +
                "│   └── DEX/                     (for your DEX/JAR mods)\n" +
                "├── Logs/\n" +
                "├── Config/\n" +
                "└── Backups/\n\n" +
                
                "📦 STEP 3: Extract Files\n" +
                "───────────────────────\n" +
                "Extract the downloaded ZIP file:\n\n" +
                
                "🔸 Core Files (place in Loaders/MelonLoader/net8/ or net35/):\n" +
                "• MelonLoader.dll\n" +
                "• 0Harmony.dll\n" +
                "• MonoMod.RuntimeDetour.dll\n" +
                "• MonoMod.Utils.dll\n" +
                "• Il2CppInterop.Runtime.dll (MelonLoader only)\n\n" +
                
                "🔸 Dependencies (place in Loaders/MelonLoader/Dependencies/):\n" +
                "• All remaining DLL files go in appropriate subdirectories\n" +
                "• Unity assemblies go in UnityDependencies/\n" +
                "• Il2Cpp files go in Il2CppAssemblyGenerator/\n\n" +
                
                "⚠️ IMPORTANT FILE PLACEMENT:\n" +
                "────────────────────────────\n" +
                "• MelonLoader files → Loaders/MelonLoader/net8/\n" +
                "• LemonLoader files → Loaders/MelonLoader/net35/\n" +
                "• Support modules → Loaders/MelonLoader/Dependencies/SupportModules/\n" +
                "• Your mod DLLs → Mods/DLL/\n" +
                "• Your DEX/JAR mods → Mods/DEX/\n\n" +
                
                "✅ STEP 4: Verify Installation\n" +
                "─────────────────────────────\n" +
                "Return to TerrariaLoader and:\n" +
                "1. Go to 'DLL Mod Manager'\n" +
                "2. Check loader status\n" +
                "3. Should show '✅ Loader Installed'\n" +
                "4. If not, check file paths carefully\n\n" +
                
                "🎮 STEP 5: Use Your Loader\n" +
                "─────────────────────────\n" +
                "1. Place DLL mods in Mods/DLL/ folder\n" +
                "2. Select Terraria APK in DLL Manager\n" +
                "3. Patch APK with loader\n" +
                "4. Install patched Terraria\n" +
                "5. Launch and enjoy mods!\n\n" +
                
                "🔧 TROUBLESHOOTING:\n" +
                "──────────────────\n" +
                "• Loader not detected → Check file paths exactly\n" +
                "• Can't find directory → Try both paths mentioned in Step 2\n" +
                "• APK patch fails → Verify all DLL files present\n" +
                "• Mods don't load → Check Logs/ folder for errors\n" +
                "• Slow performance → Try LemonLoader instead\n" +
                "• Permission denied → Enable 'All files access' for file manager\n\n" +
                
                "📋 REQUIRED FILES CHECKLIST:\n" +
                "───────────────────────────\n" +
                "☐ MelonLoader.dll (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ 0Harmony.dll (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ MonoMod files (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ Il2CppInterop files (Dependencies/SupportModules/)\n" +
                "☐ Unity dependencies (Dependencies/Il2CppAssemblyGenerator/UnityDependencies/)\n" +
                "☐ Directory structure matches exactly\n" +
                "☐ Using correct base path for your device\n\n" +
                
                "💡 TIPS:\n" +
                "──────\n" +
                "• Use a good file manager (like Solid Explorer)\n" +
                "• Enable 'Show hidden files' in your file manager\n" +
                "• Grant 'All files access' permission to your file manager\n" +
                "• Double-check spelling of folder names\n" +
                "• Keep backup of original Terraria APK\n" +
                "• Start with LemonLoader if you have storage issues\n" +
                "• Copy the exact path from Step 2 to avoid typos\n\n" +
                
                "📍 PATH VERIFICATION:\n" +
                "────────────────────\n" +
                "Your device should use:\n" + actualBasePath + "\n\n" +
                
                "If that doesn't work, try:\n" +
                "/storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/com.and.games505.TerrariaPaid/\n\n" +
                
                "Need help? Use the menu (⋮) for quick actions or check the logs in TerrariaLoader for detailed error messages!";

        tvInstructions.setText(manualInstructions);
    }

    private void copyGitHubUrls() {
        String githubUrls = "MelonLoader: https://github.com/LavaGang/MelonLoader/releases\n" +
                           "LemonLoader: https://github.com/LemonLoader/LemonLoader/releases";
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("GitHub URLs", githubUrls);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "GitHub URLs copied to clipboard!", Toast.LENGTH_SHORT).show();
        LogUtils.logUser("User copied GitHub URLs to clipboard");
    }

    private void openGitHubDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Open GitHub Repository");
        builder.setMessage("Which repository would you like to visit?");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            openUrl("https://github.com/LavaGang/MelonLoader/releases");
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            openUrl("https://github.com/LemonLoader/LemonLoader/releases");
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            LogUtils.logUser("User opened GitHub URL: " + url);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open URL. Please visit manually.", Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Failed to open URL: " + e.getMessage());
        }
    }

    private void checkInstallationStatus() {
        LogUtils.logUser("User checking manual installation status");
        
        // FIXED: Pass context to MelonLoaderManager methods
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        
        if (melonInstalled || lemonInstalled) {
            String loaderType = melonInstalled ? "MelonLoader" : "LemonLoader";
            String version = MelonLoaderManager.getInstalledLoaderVersion();
            
            builder.setTitle("✅ Installation Detected!");
            builder.setMessage("Great! " + loaderType + " v" + version + " is properly installed.\n\n" +
                              "You can now:\n" +
                              "• Go to DLL Mod Manager\n" +
                              "• Install DLL mods\n" +
                              "• Patch Terraria APK\n" +
                              "• Start modding!\n\n" +
                              "Installation path:\n" + 
                              MelonLoaderManager.getStatus(this, MelonLoaderManager.TERRARIA_PACKAGE).basePath);
            
            builder.setPositiveButton("Open DLL Manager", (dialog, which) -> {
                Intent intent = new Intent(this, DllModActivity.class);
                startActivity(intent);
            });
            
            builder.setNegativeButton("Stay Here", null);
            
        } else {
            builder.setTitle("❌ Installation Not Found");
            builder.setMessage("No loader installation detected.\n\n" +
                              "Please check:\n" +
                              "• Files are in correct directories\n" +
                              "• Directory names match exactly\n" +
                              "• Core DLL files are present\n" +
                              "• File permissions are correct\n\n" +
                              "Expected path:\n" +
                              "/storage/emulated/0/TerrariaLoader/com.and.games505.TerrariaPaid/");
            
            builder.setPositiveButton("View Debug Info", (dialog, which) -> {
                showDebugInfo();
            });
            
            builder.setNegativeButton("OK", null);
        }
        
        builder.show();
    }

    private void showDebugInfo() {
        // FIXED: Pass context to MelonLoaderManager.getDebugInfo method
        String debugInfo = MelonLoaderManager.getDebugInfo(this, MelonLoaderManager.TERRARIA_PACKAGE);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Debug Information");
        builder.setMessage(debugInfo);
        builder.setPositiveButton("Copy to Clipboard", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Debug Info", debugInfo);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Debug info copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/LogCategoryAdapter.java
// File: LogCategoryAdapter.java - Advanced Log Display Adapter
// Path: /main/java/com/terrarialoader/ui/LogCategoryAdapter.java

package com.terrarialoader.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.terrarialoader.R;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced RecyclerView adapter for displaying categorized log entries
 * with highlighting, filtering, and rich formatting
 */
public class LogCategoryAdapter extends RecyclerView.Adapter<LogCategoryAdapter.LogViewHolder> {
    
    private final Context context;
    private List<LogEntry> logEntries = new ArrayList<>();
    private String searchQuery = "";
    private boolean highlightMode = true;
    private LogEntry.LogLevel filterLevel = null;
    private LogEntry.LogType filterType = null;
    
    // Interface for item interactions
    public interface LogItemClickListener {
        void onLogItemClick(LogEntry logEntry);
        void onLogItemLongClick(LogEntry logEntry);
    }
    
    private LogItemClickListener clickListener;
    
    public LogCategoryAdapter(Context context) {
        this.context = context;
    }
    
    public void setClickListener(LogItemClickListener listener) {
        this.clickListener = listener;
    }
    
    public void updateEntries(List<LogEntry> entries) {
        this.logEntries = entries != null ? entries : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
        notifyDataSetChanged();
    }
    
    public void setHighlightMode(boolean enabled) {
        this.highlightMode = enabled;
        notifyDataSetChanged();
    }
    
    public void setLevelFilter(LogEntry.LogLevel level) {
        this.filterLevel = level;
        notifyDataSetChanged();
    }
    
    public void setTypeFilter(LogEntry.LogType type) {
        this.filterType = type;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log_entry, parent, false);
        return new LogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return logEntries.size();
    }
    
    public class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView timestampText;
        private final TextView levelText;
        private final TextView tagText;
        private final TextView messageText;
        private final View levelIndicator;
        private final View rootView;
        
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampText = itemView.findViewById(R.id.logTimestamp);
            levelText = itemView.findViewById(R.id.logLevel);
            tagText = itemView.findViewById(R.id.logTag);
            messageText = itemView.findViewById(R.id.logMessage);
            levelIndicator = itemView.findViewById(R.id.levelIndicator);
            rootView = itemView;
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.onLogItemClick(logEntries.get(pos));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.onLogItemLongClick(logEntries.get(pos));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(LogEntry entry) {
            // Set timestamp
            timestampText.setText(entry.getFormattedTimestamp());
            
            // Set level with color
            levelText.setText(entry.getLevel().getDisplayName());
            levelText.setTextColor(Color.parseColor(entry.getLevel().getColor()));
            
            // Set tag
            tagText.setText(entry.getTag());
            
            // Set message with highlighting
            if (highlightMode && !searchQuery.trim().isEmpty()) {
                messageText.setText(highlightText(entry.getMessage(), searchQuery));
            } else {
                messageText.setText(entry.getMessage());
            }
            
            // Set level indicator color
            levelIndicator.setBackgroundColor(Color.parseColor(entry.getLevel().getColor()));
            
            // Set background based on log type and importance
            setItemBackground(entry);
            
            // Set typography based on level
            setTypography(entry);
            
            // Add type badge for system/game logs
            addTypeBadge(entry);
        }
        
        private SpannableString highlightText(String text, String query) {
            SpannableString spannableString = new SpannableString(text);
            
            if (query.trim().isEmpty()) {
                return spannableString;
            }
            
            try {
                // Try regex highlighting first
                if (isValidRegex(query)) {
                    Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(text);
                    
                    while (matcher.find()) {
                        spannableString.setSpan(
                            new BackgroundColorSpan(Color.YELLOW),
                            matcher.start(),
                            matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        spannableString.setSpan(
                            new ForegroundColorSpan(Color.BLACK),
                            matcher.start(),
                            matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                } else {
                    // Fall back to simple text highlighting
                    String lowerText = text.toLowerCase();
                    String lowerQuery = query.toLowerCase();
                    int index = lowerText.indexOf(lowerQuery);
                    
                    while (index >= 0) {
                        spannableString.setSpan(
                            new BackgroundColorSpan(Color.YELLOW),
                            index,
                            index + query.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        spannableString.setSpan(
                            new ForegroundColorSpan(Color.BLACK),
                            index,
                            index + query.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        
                        index = lowerText.indexOf(lowerQuery, index + 1);
                    }
                }
            } catch (Exception e) {
                // If highlighting fails, return original text
                return spannableString;
            }
            
            return spannableString;
        }
        
        private boolean isValidRegex(String regex) {
            try {
                Pattern.compile(regex);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        private void setItemBackground(LogEntry entry) {
            int backgroundColor;
            
            switch (entry.getLevel()) {
                case ERROR:
                    backgroundColor = Color.parseColor("#FFEBEE");
                    break;
                case WARN:
                    backgroundColor = Color.parseColor("#FFF8E1");
                    break;
                case USER:
                    backgroundColor = Color.parseColor("#E8F5E8");
                    break;
                case DEBUG:
                    backgroundColor = Color.parseColor("#F5F5F5");
                    break;
                default:
                    backgroundColor = Color.WHITE;
                    break;
            }
            
            rootView.setBackgroundColor(backgroundColor);
        }
        
        private void setTypography(LogEntry entry) {
            switch (entry.getLevel()) {
                case ERROR:
                    messageText.setTypeface(null, Typeface.BOLD);
                    break;
                case WARN:
                    messageText.setTypeface(null, Typeface.ITALIC);
                    break;
                case USER:
                    messageText.setTypeface(null, Typeface.BOLD);
                    break;
                default:
                    messageText.setTypeface(null, Typeface.NORMAL);
                    break;
            }
        }
        
        private void addTypeBadge(LogEntry entry) {
            if (entry.getType() != LogEntry.LogType.APP) {
                // Add type indicator to tag
                String tagWithType = "[" + entry.getType().getDisplayName() + "] " + entry.getTag();
                tagText.setText(tagWithType);
                
                // Color code the type
                switch (entry.getType()) {
                    case GAME:
                        tagText.setTextColor(Color.parseColor("#FF9800"));
                        break;
                    case SYSTEM:
                        tagText.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    default:
                        tagText.setTextColor(Color.parseColor("#666666"));
                        break;
                }
            } else {
                tagText.setTextColor(Color.parseColor("#666666"));
            }
        }
    }
    
    // Filter methods
    public List<LogEntry> getFilteredEntries() {
        List<LogEntry> filtered = new ArrayList<>();
        
        for (LogEntry entry : logEntries) {
            if (matchesFilters(entry)) {
                filtered.add(entry);
            }
        }
        
        return filtered;
    }
    
    private boolean matchesFilters(LogEntry entry) {
        // Level filter
        if (filterLevel != null && entry.getLevel() != filterLevel) {
            return false;
        }
        
        // Type filter
        if (filterType != null && entry.getType() != filterType) {
            return false;
        }
        
        // Search query filter
        if (!searchQuery.trim().isEmpty()) {
            return entry.matchesFilter(searchQuery);
        }
        
        return true;
    }
    
    // Statistics methods
    public int getErrorCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == LogEntry.LogLevel.ERROR) {
                count++;
            }
        }
        return count;
    }
    
    public int getWarningCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == LogEntry.LogLevel.WARN) {
                count++;
            }
        }
        return count;
    }
    
    public int getImportantCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.isImportant()) {
                count++;
            }
        }
        return count;
    }
    
    // Export filtered entries
    public String exportFilteredEntries() {
        StringBuilder export = new StringBuilder();
        export.append("=== Filtered Log Export ===\n");
        export.append("Total entries: ").append(getItemCount()).append("\n");
        export.append("Errors: ").append(getErrorCount()).append("\n");
        export.append("Warnings: ").append(getWarningCount()).append("\n");
        export.append("Important: ").append(getImportantCount()).append("\n");
        export.append("Export time: ").append(new java.util.Date().toString()).append("\n");
        export.append("=" + "=".repeat(50)).append("\n\n");
        
        for (LogEntry entry : getFilteredEntries()) {
            export.append(entry.toFormattedString()).append("\n");
        }
        
        return export.toString();
    }
    
    // Clear all entries
    public void clear() {
        logEntries.clear();
        notifyDataSetChanged();
    }
    
    // Add single entry
    public void addEntry(LogEntry entry) {
        if (entry != null) {
            logEntries.add(entry);
            notifyItemInserted(logEntries.size() - 1);
        }
    }
    
    // Add multiple entries
    public void addEntries(List<LogEntry> entries) {
        if (entries != null && !entries.isEmpty()) {
            int startPosition = logEntries.size();
            logEntries.addAll(entries);
            notifyItemRangeInserted(startPosition, entries.size());
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/LogEntry.java
// File: LogEntry.java - Enhanced Log Entry Model for Advanced Logging
// Path: /main/java/com/terrarialoader/ui/LogEntry.java

package com.terrarialoader.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Enhanced log entry model for advanced logging system
 * Supports categorization, filtering, and rich metadata
 */
public class LogEntry {
    
    public enum LogLevel {
        DEBUG("DEBUG", "#808080"),
        INFO("INFO", "#2196F3"),
        WARN("WARN", "#FF9800"),
        ERROR("ERROR", "#F44336"),
        USER("USER", "#4CAF50");
        
        private final String displayName;
        private final String color;
        
        LogLevel(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    public enum LogType {
        APP("App", "Application logs"),
        GAME("Game", "Game/MelonLoader logs"),
        SYSTEM("System", "System information");
        
        private final String displayName;
        private final String description;
        
        LogType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final long timestamp;
    private final LogLevel level;
    private final LogType type;
    private final String tag;
    private final String message;
    private final String threadName;
    private final String className;
    
    // Primary constructor
    public LogEntry(long timestamp, LogLevel level, LogType type, String tag, String message) {
        this.timestamp = timestamp;
        this.level = level != null ? level : LogLevel.INFO;
        this.type = type != null ? type : LogType.APP;
        this.tag = tag != null ? tag : "UNKNOWN";
        this.message = message != null ? message : "";
        this.threadName = Thread.currentThread().getName();
        this.className = getCallingClassName();
    }
    
    // Convenience constructor for app logs
    public LogEntry(LogLevel level, String tag, String message) {
        this(System.currentTimeMillis(), level, LogType.APP, tag, message);
    }
    
    // Convenience constructor for simple messages
    public LogEntry(String message) {
        this(System.currentTimeMillis(), LogLevel.INFO, LogType.APP, "APP", message);
    }
    
    // Getters
    public long getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }
    public LogType getType() { return type; }
    public String getTag() { return tag; }
    public String getMessage() { return message; }
    public String getThreadName() { return threadName; }
    public String getClassName() { return className; }
    
    // Formatted timestamp
    public String getFormattedTimestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date(timestamp));
    }
    
    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }
    
    public String getFormattedDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }
    
    // Formatted string representation
    public String toFormattedString() {
        return String.format("[%s] [%s] [%s] %s: %s", 
            getFormattedTimestamp(), 
            level.getDisplayName(), 
            type.getDisplayName(),
            tag, 
            message);
    }
    
    // Detailed string representation
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp: ").append(getFormattedDateTime()).append("\n");
        sb.append("Level: ").append(level.getDisplayName()).append("\n");
        sb.append("Type: ").append(type.getDisplayName()).append("\n");
        sb.append("Tag: ").append(tag).append("\n");
        sb.append("Thread: ").append(threadName).append("\n");
        if (className != null && !className.isEmpty()) {
            sb.append("Class: ").append(className).append("\n");
        }
        sb.append("Message: ").append(message);
        return sb.toString();
    }
    
    // JSON representation
    public String toJson() {
        return String.format(
            "{\"timestamp\":%d,\"level\": %s\",\"type\":\"%s\",\"tag\":\"%s\",\"message\":\"%s\",\"thread\":\"%s\"}",
            timestamp,
            level.getDisplayName(),
            type.getDisplayName(),
            escapeJson(tag),
            escapeJson(message),
            escapeJson(threadName)
        );
    }
    
    // Filtering helpers
    public boolean matchesFilter(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }
        
        String query = searchQuery.toLowerCase();
        return message.toLowerCase().contains(query) ||
               tag.toLowerCase().contains(query) ||
               level.getDisplayName().toLowerCase().contains(query) ||
               type.getDisplayName().toLowerCase().contains(query);
    }
    
    public boolean matchesLevel(LogLevel filterLevel) {
        return filterLevel == null || this.level == filterLevel;
    }
    
    public boolean matchesType(LogType filterType) {
        return filterType == null || this.type == filterType;
    }
    
    public boolean matchesTimeRange(long startTime, long endTime) {
        return timestamp >= startTime && timestamp <= endTime;
    }
    
    // Priority for sorting (higher = more important)
    public int getPriority() {
        switch (level) {
            case ERROR: return 4;
            case WARN: return 3;
            case USER: return 2;
            case INFO: return 1;
            case DEBUG: return 0;
            default: return 0;
        }
    }
    
    // Get severity color for UI display
    public String getSeverityColor() {
        return level.getColor();
    }
    
    // Check if this is an important log entry
    public boolean isImportant() {
        return level == LogLevel.ERROR || level == LogLevel.WARN || 
               message.toLowerCase().contains("fail") ||
               message.toLowerCase().contains("error") ||
               message.toLowerCase().contains("crash");
    }
    
    // Get calling class name for debugging
    private String getCallingClassName() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // Skip the first few elements (Thread.getStackTrace, this constructor, etc.)
            for (int i = 3; i < stackTrace.length && i < 8; i++) {
                String className = stackTrace[i].getClassName();
                if (!className.startsWith("java.") && 
                    !className.startsWith("android.") &&
                    !className.equals(LogEntry.class.getName())) {
                    return className.substring(className.lastIndexOf('.') + 1);
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during class name detection
        }
        return null;
    }
    
    // Helper method to escape JSON strings
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Factory methods for common log types
    public static LogEntry debug(String tag, String message) {
        return new LogEntry(LogLevel.DEBUG, tag, message);
    }
    
    public static LogEntry info(String tag, String message) {
        return new LogEntry(LogLevel.INFO, tag, message);
    }
    
    public static LogEntry warn(String tag, String message) {
        return new LogEntry(LogLevel.WARN, tag, message);
    }
    
    public static LogEntry error(String tag, String message) {
        return new LogEntry(LogLevel.ERROR, tag, message);
    }
    
    public static LogEntry user(String tag, String message) {
        return new LogEntry(LogLevel.USER, tag, message);
    }
    
    public static LogEntry system(String message) {
        return new LogEntry(System.currentTimeMillis(), LogLevel.INFO, LogType.SYSTEM, "SYSTEM", message);
    }
    
    public static LogEntry game(String tag, String message) {
        return new LogEntry(System.currentTimeMillis(), LogLevel.INFO, LogType.GAME, tag, message);
    }
    
    @Override
    public String toString() {
        return toFormattedString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LogEntry logEntry = (LogEntry) obj;
        return timestamp == logEntry.timestamp &&
               level == logEntry.level &&
               type == logEntry.type &&
               tag.equals(logEntry.tag) &&
               message.equals(logEntry.message);
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(timestamp);
        result = 31 * result + level.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/LogViewerActivity.java
// File: LogViewerActivity.java (Enhanced) - Advanced Logging and Export Options
// Path: /main/java/com/terrarialoader/ui/LogViewerActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.DiagnosticBundleExporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Enhanced LogViewerActivity with advanced filtering, categorization, and diagnostic export
 */
public class LogViewerActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView logRecyclerView;
    private LogCategoryAdapter categoryAdapter;
    private EditText searchEditText;
    private Spinner filterTypeSpinner;
    private Spinner timeRangeSpinner;
    private Switch regexModeSwitch;
    private Switch highlightModeSwitch;
    private TextView logCountText;
    private TextView filterStatusText;
    private Button exportDiagnosticBtn;
    private Button clearFiltersBtn;
    private LinearLayout advancedFiltersLayout;
    private View advancedFiltersToggle;

    // State management
    private String currentSearchQuery = "";
    private LogFilterType currentFilterType = LogFilterType.ALL;
    private TimeRange currentTimeRange = TimeRange.ALL;
    private boolean isRegexMode = false;
    private boolean isHighlightMode = true;
    private boolean showAdvancedFilters = false;
    private List<LogEntry> allLogEntries = new ArrayList<>();
    private List<LogEntry> filteredLogEntries = new ArrayList<>();

    // Filter types
    public enum LogFilterType {
        ALL("All Logs", ""),
        INSTALLATION("Installation", "install|setup|download|extract|patch"),
        MOD_LOADING("Mod Loading", "mod|load|enable|disable|dex|dll"),
        ERRORS("Errors & Warnings", "error|warning|fail|exception|crash"),
        SYSTEM("System Info", "system|device|version|permission"),
        USER_ACTIONS("User Actions", "user|button|click|select"),
        DEBUG("Debug Info", "debug|trace|verbose");

        private final String displayName;
        private final String keywords;

        LogFilterType(String displayName, String keywords) {
            this.displayName = displayName;
            this.keywords = keywords;
        }

        public String getDisplayName() { return displayName; }
        public String getKeywords() { return keywords; }
    }

    // Time ranges
    public enum TimeRange {
        ALL("All Time", 0),
        LAST_HOUR("Last Hour", 60 * 60 * 1000L),
        LAST_DAY("Last 24 Hours", 24 * 60 * 60 * 1000L),
        LAST_WEEK("Last Week", 7 * 24 * 60 * 60 * 1000L);

        private final String displayName;
        private final long milliseconds;

        TimeRange(String displayName, long milliseconds) {
            this.displayName = displayName;
            this.milliseconds = milliseconds;
        }

        public String getDisplayName() { return displayName; }
        public long getMilliseconds() { return milliseconds; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer_enhanced);

        setTitle("📋 Advanced Log Viewer");

        initializeViews();
        setupSpinners();
        setupListeners();
        loadLogEntries();
        applyFilters();
    }

    private void initializeViews() {
        logRecyclerView = findViewById(R.id.logRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner);
        timeRangeSpinner = findViewById(R.id.timeRangeSpinner);
        regexModeSwitch = findViewById(R.id.regexModeSwitch);
        highlightModeSwitch = findViewById(R.id.highlightModeSwitch);
        logCountText = findViewById(R.id.logCountText);
        filterStatusText = findViewById(R.id.filterStatusText);
        exportDiagnosticBtn = findViewById(R.id.exportDiagnosticBtn);
        clearFiltersBtn = findViewById(R.id.clearFiltersBtn);
        advancedFiltersLayout = findViewById(R.id.advancedFiltersLayout);
        advancedFiltersToggle = findViewById(R.id.advancedFiltersToggle);

        // Setup RecyclerView
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new LogCategoryAdapter(this);
        logRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupSpinners() {
        // Filter Type Spinner
        String[] filterTypes = new String[LogFilterType.values().length];
        for (int i = 0; i < LogFilterType.values().length; i++) {
            filterTypes[i] = LogFilterType.values()[i].getDisplayName();
        }
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, filterTypes);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(filterAdapter);

        // Time Range Spinner
        String[] timeRanges = new String[TimeRange.values().length];
        for (int i = 0; i < TimeRange.values().length; i++) {
            timeRanges[i] = TimeRange.values()[i].getDisplayName();
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, timeRanges);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeRangeSpinner.setAdapter(timeAdapter);
    }

    private void setupListeners() {
        // Real-time search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter type changes
        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterType = LogFilterType.values()[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time range changes
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTimeRange = TimeRange.values()[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Regex mode toggle
        regexModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRegexMode = isChecked;
            applyFilters();
        });

        // Highlight mode toggle
        highlightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighlightMode = isChecked;
            categoryAdapter.setHighlightMode(isChecked);
            categoryAdapter.setSearchQuery(currentSearchQuery);
            categoryAdapter.notifyDataSetChanged();
        });

        // Export diagnostic bundle
        exportDiagnosticBtn.setOnClickListener(v -> exportDiagnosticBundle());

        // Clear filters
        clearFiltersBtn.setOnClickListener(v -> clearAllFilters());

        // Advanced filters toggle
        advancedFiltersToggle.setOnClickListener(v -> toggleAdvancedFilters());
    }

    private void loadLogEntries() {
        LogUtils.logDebug("Loading log entries for advanced viewer");
        allLogEntries.clear();

        // Load different types of logs
        loadAppLogs();
        loadGameLogs();
        loadSystemLogs();

        LogUtils.logUser("Loaded " + allLogEntries.size() + " log entries for advanced analysis");
    }

    private void loadAppLogs() {
        try {
            // Load current app logs
            String currentLogs = LogUtils.getLogs();
            parseLogString(currentLogs, LogEntry.LogType.APP, "Current Session");

            // Load rotated app logs
            List<File> logFiles = LogUtils.getAvailableLogFiles();
            for (int i = 0; i < logFiles.size(); i++) {
                File logFile = logFiles.get(i);
                String content = LogUtils.readLogFile(i);
                parseLogString(content, LogEntry.LogType.APP, logFile.getName());
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error loading app logs: " + e.getMessage());
        }
    }

    private void loadGameLogs() {
        try {
            // Try to load MelonLoader game logs if available
            File gameLogsDir = new File(getExternalFilesDir(null), 
                "TerrariaLoader/com.and.games505.TerrariaPaid/Logs");
            
            if (gameLogsDir.exists()) {
                File[] gameLogFiles = gameLogsDir.listFiles((dir, name) -> 
                    name.startsWith("Log") && name.endsWith(".txt"));
                
                if (gameLogFiles != null) {
                    for (File logFile : gameLogFiles) {
                        try {
                            String content = readFileContent(logFile);
                            parseLogString(content, LogEntry.LogType.GAME, logFile.getName());
                        } catch (Exception e) {
                            LogUtils.logDebug("Error reading game log: " + logFile.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error loading game logs: " + e.getMessage());
        }
    }

    private void loadSystemLogs() {
        try {
            // Add system information as log entries
            addSystemInfoEntry("Device", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            addSystemInfoEntry("Android Version", android.os.Build.VERSION.RELEASE);
            addSystemInfoEntry("API Level", String.valueOf(android.os.Build.VERSION.SDK_INT));
            addSystemInfoEntry("Architecture", System.getProperty("os.arch", "unknown"));
            
            // Add app information
            try {
                android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                addSystemInfoEntry("App Version", pInfo.versionName);
                addSystemInfoEntry("Version Code", String.valueOf(pInfo.versionCode));
            } catch (Exception e) {
                addSystemInfoEntry("App Version", "Unknown");
            }

            // Add storage information
            File appDir = getExternalFilesDir(null);
            if (appDir != null) {
                long totalSpace = appDir.getTotalSpace();
                long freeSpace = appDir.getFreeSpace();
                addSystemInfoEntry("Storage Total", formatFileSize(totalSpace));
                addSystemInfoEntry("Storage Free", formatFileSize(freeSpace));
            }

        } catch (Exception e) {
            LogUtils.logDebug("Error loading system info: " + e.getMessage());
        }
    }

    private void addSystemInfoEntry(String key, String value) {
        LogEntry entry = new LogEntry(
            System.currentTimeMillis(),
            LogEntry.LogLevel.INFO,
            LogEntry.LogType.SYSTEM,
            "SYSTEM",
            key + ": " + value
        );
        allLogEntries.add(entry);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private void parseLogString(String logString, LogEntry.LogType type, String source) {
        if (logString == null || logString.trim().isEmpty()) {
            return;
        }

        String[] lines = logString.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            LogEntry entry = parseLogLine(line, type, source);
            if (entry != null) {
                allLogEntries.add(entry);
            }
        }
    }

    private LogEntry parseLogLine(String line, LogEntry.LogType type, String source) {
        try {
            // Try to parse timestamp and level from line
            // Format: "HH:mm:ss [LEVEL] message" or similar
            
            long timestamp = System.currentTimeMillis(); // Default to now
            LogEntry.LogLevel level = LogEntry.LogLevel.INFO; // Default level
            String tag = source;
            String message = line;

            // Parse timestamp
            if (line.matches("^\\d{2}:\\d{2}:\\d{2}.*")) {
                // Extract timestamp (rough parsing)
                String timeStr = line.substring(0, 8);
                // For simplicity, use current date with parsed time
                // In a real implementation, you'd want more sophisticated parsing
            }

            // Parse log level
            if (line.contains("[DEBUG]")) {
                level = LogEntry.LogLevel.DEBUG;
                message = line.replaceFirst(".*\\[DEBUG\\]\\s*", "");
            } else if (line.contains("[ERROR]") || line.toLowerCase().contains("error")) {
                level = LogEntry.LogLevel.ERROR;
                message = line.replaceFirst(".*\\[ERROR\\]\\s*", "");
            } else if (line.contains("[WARN]") || line.toLowerCase().contains("warning")) {
                level = LogEntry.LogLevel.WARN;
                message = line.replaceFirst(".*\\[WARN\\]\\s*", "");
            } else if (line.contains("[USER]")) {
                level = LogEntry.LogLevel.INFO;
                message = line.replaceFirst(".*\\[USER\\]\\s*", "");
                tag = "USER";
            }

            return new LogEntry(timestamp, level, type, tag, message);
            
        } catch (Exception e) {
            // Fallback: create basic entry
            return new LogEntry(
                System.currentTimeMillis(),
                LogEntry.LogLevel.INFO,
                type,
                source,
                line
            );
        }
    }

    private void applyFilters() {
        filteredLogEntries.clear();
        
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - currentTimeRange.getMilliseconds();

        for (LogEntry entry : allLogEntries) {
            if (!passesFilters(entry, timeThreshold)) {
                continue;
            }
            filteredLogEntries.add(entry);
        }

        // Update UI
        categoryAdapter.updateEntries(filteredLogEntries);
        updateFilterStatus();
    }

    private boolean passesFilters(LogEntry entry, long timeThreshold) {
        // Time range filter
        if (currentTimeRange != TimeRange.ALL && entry.getTimestamp() < timeThreshold) {
            return false;
        }

        // Category filter
        if (currentFilterType != LogFilterType.ALL) {
            String keywords = currentFilterType.getKeywords();
            if (!keywords.isEmpty()) {
                String message = entry.getMessage().toLowerCase();
                String[] keywordArray = keywords.split("\\|");
                boolean matchesKeyword = false;
                for (String keyword : keywordArray) {
                    if (message.contains(keyword.toLowerCase())) {
                        matchesKeyword = true;
                        break;
                    }
                }
                if (!matchesKeyword) {
                    return false;
                }
            }
        }

        // Search query filter
        if (!currentSearchQuery.trim().isEmpty()) {
            String searchText = currentSearchQuery.toLowerCase();
            String message = entry.getMessage().toLowerCase();
            String tag = entry.getTag().toLowerCase();

            if (isRegexMode) {
                try {
                    Pattern pattern = Pattern.compile(currentSearchQuery, Pattern.CASE_INSENSITIVE);
                    if (!pattern.matcher(message).find() && !pattern.matcher(tag).find()) {
                        return false;
                    }
                } catch (PatternSyntaxException e) {
                    // Fall back to simple text search if regex is invalid
                    if (!message.contains(searchText) && !tag.contains(searchText)) {
                        return false;
                    }
                }
            } else {
                if (!message.contains(searchText) && !tag.contains(searchText)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateFilterStatus() {
        int totalCount = allLogEntries.size();
        int filteredCount = filteredLogEntries.size();
        
        logCountText.setText(String.format("Showing %d of %d entries", filteredCount, totalCount));
        
        StringBuilder status = new StringBuilder();
        if (currentFilterType != LogFilterType.ALL) {
            status.append("Category: ").append(currentFilterType.getDisplayName()).append(" • ");
        }
        if (currentTimeRange != TimeRange.ALL) {
            status.append("Time: ").append(currentTimeRange.getDisplayName()).append(" • ");
        }
        if (!currentSearchQuery.trim().isEmpty()) {
            status.append("Search: '").append(currentSearchQuery).append("'");
            if (isRegexMode) status.append(" (regex)");
            status.append(" • ");
        }
        
        String statusText = status.toString();
        if (statusText.endsWith(" • ")) {
            statusText = statusText.substring(0, statusText.length() - 3);
        }
        
        filterStatusText.setText(statusText.isEmpty() ? "No filters active" : statusText);
    }

    private void clearAllFilters() {
        searchEditText.setText("");
        filterTypeSpinner.setSelection(0);
        timeRangeSpinner.setSelection(0);
        regexModeSwitch.setChecked(false);
        highlightModeSwitch.setChecked(true);
        
        currentSearchQuery = "";
        currentFilterType = LogFilterType.ALL;
        currentTimeRange = TimeRange.ALL;
        isRegexMode = false;
        isHighlightMode = true;
        
        applyFilters();
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void toggleAdvancedFilters() {
        showAdvancedFilters = !showAdvancedFilters;
        advancedFiltersLayout.setVisibility(showAdvancedFilters ? View.VISIBLE : View.GONE);
        
        TextView toggleText = advancedFiltersToggle.findViewById(R.id.advancedFiltersToggleText);
        toggleText.setText(showAdvancedFilters ? "▼ Hide Advanced Filters" : "▶ Show Advanced Filters");
    }

    private void exportDiagnosticBundle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Diagnostic Bundle");
        builder.setMessage("This will create a comprehensive diagnostic bundle including:\n\n" +
                          "• All application logs\n" +
                          "• Game logs (if available)\n" +
                          "• System information\n" +
                          "• Device specifications\n" +
                          "• Installed mod information\n" +
                          "• Directory structure\n\n" +
                          "This bundle can be shared with support for troubleshooting.");
        
        builder.setPositiveButton("Export Bundle", (dialog, which) -> {
            performDiagnosticExport();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performDiagnosticExport() {
        Toast.makeText(this, "Creating diagnostic bundle...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                File bundleFile = DiagnosticBundleExporter.createDiagnosticBundle(this);
                
                runOnUiThread(() -> {
                    if (bundleFile != null && bundleFile.exists()) {
                        shareDiagnosticBundle(bundleFile);
                    } else {
                        Toast.makeText(this, "Failed to create diagnostic bundle", Toast.LENGTH_LONG).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error creating bundle: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                LogUtils.logDebug("Diagnostic export error: " + e.getMessage());
            }
        }).start();
    }

    private void shareDiagnosticBundle(File bundleFile) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, getPackageName() + ".provider", bundleFile));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TerrariaLoader Diagnostic Bundle");
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "TerrariaLoader diagnostic bundle generated on " + 
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Diagnostic Bundle"));
            
            Toast.makeText(this, "Diagnostic bundle ready to share", Toast.LENGTH_SHORT).show();
            LogUtils.logUser("Diagnostic bundle exported: " + bundleFile.getName());
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share bundle: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Bundle sharing error: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_export_filtered) {
            exportFilteredLogs();
            return true;
        } else if (id == R.id.action_copy_selected) {
            copySelectedLogs();
            return true;
        } else if (id == R.id.action_save_search) {
            saveCurrentSearch();
            return true;
        } else if (id == R.id.action_refresh_logs) {
            refreshLogs();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void exportFilteredLogs() {
        try {
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) exportDir.mkdirs();

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", 
                java.util.Locale.getDefault()).format(new java.util.Date());
            File exportFile = new File(exportDir, "filtered_logs_" + timestamp + ".txt");

            try (java.io.FileWriter writer = new java.io.FileWriter(exportFile)) {
                writer.write("=== TerrariaLoader Filtered Logs Export ===\n");
                writer.write("Export Date: " + new java.util.Date().toString() + "\n");
                writer.write("Total Entries: " + filteredLogEntries.size() + "\n");
                writer.write("Filters Applied: " + filterStatusText.getText() + "\n");
                writer.write("=" + "=".repeat(50) + "\n\n");

                for (LogEntry entry : filteredLogEntries) {
                    writer.write(entry.toFormattedString() + "\n");
                }
            }

            Toast.makeText(this, "Filtered logs exported to: " + exportFile.getName(), Toast.LENGTH_LONG).show();
            LogUtils.logUser("Filtered logs exported: " + exportFile.getName());

        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Filtered export error: " + e.getMessage());
        }
    }

    private void copySelectedLogs() {
        // Implementation for copying selected logs to clipboard
        Toast.makeText(this, "Copy selected logs feature - implementation pending", Toast.LENGTH_SHORT).show();
    }

    private void saveCurrentSearch() {
        // Implementation for saving current search/filter configuration
        Toast.makeText(this, "Save current search feature - implementation pending", Toast.LENGTH_SHORT).show();
    }

    private void refreshLogs() {
        loadLogEntries();
        applyFilters();
        Toast.makeText(this, "Logs refreshed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh logs when returning to activity
        refreshLogs();
    }

    // Utility method
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/ModListActivity.java
package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch; // Make sure this is imported if used in your XML

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.installer.ModInstaller;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

public class ModListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ModListAdapter modAdapter; // Changed from ModAdapter
    private TextView modCountTextView;
    private static final int PICK_MOD_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_list);

        modCountTextView = findViewById(R.id.modCountTextView);
        recyclerView = findViewById(R.id.recyclerViewMods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        Button addModButton = findViewById(R.id.addModButton);
        addModButton.setOnClickListener(v -> openFilePicker());

        Button refreshModsButton = findViewById(R.id.refreshModsButton);
        refreshModsButton.setOnClickListener(v -> loadMods());

        loadMods();
    }

    private void loadMods() {
        ModManager.loadMods(this);
        List<File> mods = ModManager.getAvailableMods();
        modAdapter = new ModListAdapter(this, mods); // Changed to ModListAdapter
        recyclerView.setAdapter(modAdapter);
        updateModCount(mods.size());
    }

    private void updateModCount(int count) {
        modCountTextView.setText("Total Mods: " + count + " (Enabled: " + ModManager.getEnabledModCount() + ")");
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types, then filter
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select Mod File"), PICK_MOD_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MOD_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                handlePickedFile(uri); // Added this method
            }
        }
    }

    // New method to handle picked files and install them
    private void handlePickedFile(Uri uri) {
        String filename = getFilenameFromUri(uri);
        if (filename == null || !isValidModExtension(filename)) {
            LogUtils.logUser("❌ Invalid mod file type selected.");
            Toast.makeText(this, "Invalid mod file type. Only .dex, .jar, .dll, .hybrid are supported.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Install Mod");
        builder.setMessage("Do you want to install '" + filename + "'?");
        builder.setPositiveButton("Install", (dialog, which) -> {
            boolean success = ModInstaller.installModAuto(this, uri);
            if (success) {
                Toast.makeText(this, "Mod installed successfully!", Toast.LENGTH_SHORT).show();
                loadMods(); // Reload mods after installation
            } else {
                Toast.makeText(this, "Failed to install mod.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getFilenameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private boolean isValidModExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : ModInstaller.getSupportedExtensions()) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMods(); // Refresh mod list when activity resumes
    }
}


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/ModListAdapter.java
// File: ModListAdapter.java (Fixed Adapter Class) - NullPointerException Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/ModListAdapter.java

package com.terrarialoader.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.installer.ModInstaller;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.loader.ModMetadata;
import com.terrarialoader.loader.ModBase;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.ModViewHolder> {

    private final Context context;
    private List<File> mods; // Changed to non-final to allow updates

    public ModListAdapter(Context context, List<File> mods) {
        this.context = context;
        this.mods = mods;
    }

    @NonNull
    @Override
    public ModViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mod, parent, false);
        return new ModViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModViewHolder holder, int position) {
        File modFile = mods.get(position);
        String modName = modFile.getName();
        boolean isEnabled = !modName.endsWith(".disabled");

        holder.modNameTextView.setText(modName);
        
        // FIXED: Null pointer protection for metadata
        try {
            // Get metadata safely
            String cleanModName = modName.replace(".disabled", "").replace(".dex", "").replace(".jar", "").replace(".dll", "");
            ModMetadata metadata = ModManager.getMetadata(cleanModName);
            
            if (metadata != null) {
                // Use metadata if available
                ModBase.ModType modType = metadata.getModType();
                if (modType != null) {
                    holder.modDescriptionTextView.setText("Type: " + modType.getDisplayName());
                } else {
                    holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
                }
            } else {
                // Fallback to file extension detection
                holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
            }
        } catch (Exception e) {
            // Ultimate fallback
            LogUtils.logDebug("Error getting mod metadata: " + e.getMessage());
            holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
        }

        holder.modSwitch.setChecked(isEnabled);
        holder.modSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                if (isChecked) {
                    ModManager.enableMod(context, modFile);
                } else {
                    ModManager.disableMod(context, modFile);
                }
                // Refresh the adapter after mod state change
                // Note: The list of files is not actually changing here, only their names.
                // A better approach would be to reload the list of files entirely.
                // For now, we will simply notify that the item has changed.
                notifyItemChanged(position);
            } catch (Exception e) {
                LogUtils.logDebug("Error toggling mod: " + e.getMessage());
                Toast.makeText(context, "Error toggling mod: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.modDeleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Mod")
                    .setMessage("Are you sure you want to delete " + modName + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            if (ModInstaller.uninstallMod(context, modName)) {
                                // Remove from list and notify adapter
                                mods.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mods.size());
                                Toast.makeText(context, modName + " deleted.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete " + modName, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            LogUtils.logDebug("Error deleting mod: " + e.getMessage());
                            Toast.makeText(context, "Error deleting mod: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    
    // Helper method to determine mod type from filename
    private String getModTypeFromFileName(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".dex") || lowerName.endsWith(".dex.disabled")) {
            return "DEX (Java)";
        } else if (lowerName.endsWith(".jar") || lowerName.endsWith(".jar.disabled")) {
            return "JAR (Java Library)";
        } else if (lowerName.endsWith(".dll") || lowerName.endsWith(".dll.disabled")) {
            return "DLL (C#/Native)";
        } else {
            return "Unknown";
        }
    }

    @Override
    public int getItemCount() {
        return mods.size();
    }

    /**
     * Updates the adapter's data set with a new list of mods.
     * @param newMods The new list of mods to display.
     */
    public void updateMods(List<File> newMods) {
        this.mods = newMods;
        notifyDataSetChanged();
    }

    public static class ModViewHolder extends RecyclerView.ViewHolder {
        TextView modNameTextView;
        TextView modDescriptionTextView;
        Switch modSwitch;
        ImageButton modDeleteButton;

        public ModViewHolder(@NonNull View itemView) {
            super(itemView);
            modNameTextView = itemView.findViewById(R.id.modNameTextView);
            modDescriptionTextView = itemView.findViewById(R.id.modDescription);
            modSwitch = itemView.findViewById(R.id.modSwitch);
            modDeleteButton = itemView.findViewById(R.id.modDeleteButton);
        }
    }
}


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/ModManagementActivity.java
// File: ModManagementActivity.java - Pure Mod Management (Post-Installation)
// Path: /main/java/com/terrarialoader/ui/ModManagementActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.installer.ModInstaller;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.ui.ModListAdapter;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

/**
 * Pure mod management activity - assumes loader is already installed
 * Focused solely on managing DLL and DEX mods
 */
public class ModManagementActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_DLL = 1001;
    private static final int REQUEST_SELECT_DEX = 1002;
    
    // UI Components
    private TextView statusText;
    private TextView loaderStatusText;
    private RecyclerView modRecyclerView;
    private ModListAdapter modAdapter;
    private Button addDllModBtn;
    private Button addDexModBtn;
    private Button refreshBtn;
    private Button backBtn;
    private LinearLayout loaderInfoSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_management);
        
        setTitle("🎮 Mod Management");
        
        initializeViews();
        setupListeners();
        loadMods();
        updateStatus();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        modRecyclerView = findViewById(R.id.modRecyclerView);
        addDllModBtn = findViewById(R.id.addDllModBtn);
        addDexModBtn = findViewById(R.id.addDexModBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        backBtn = findViewById(R.id.backBtn);
        loaderInfoSection = findViewById(R.id.loaderInfoSection);
        
        // Setup RecyclerView
        modRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        addDllModBtn.setOnClickListener(v -> {
            if (!MelonLoaderManager.isMelonLoaderInstalled(this) && !MelonLoaderManager.isLemonLoaderInstalled(this)) {
                showLoaderRequiredDialog();
                return;
            }
            
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DLL);
        });
        
        addDexModBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DEX);
        });
        
        refreshBtn.setOnClickListener(v -> {
            loadMods();
            updateStatus();
            Toast.makeText(this, "🔄 Mods refreshed", Toast.LENGTH_SHORT).show();
        });
        
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadMods() {
        ModManager.loadMods(this);
        List<File> allMods = ModManager.getAvailableMods();
        
        if (modAdapter == null) {
            modAdapter = new ModListAdapter(this, allMods);
            modRecyclerView.setAdapter(modAdapter);
        } else {
            // Update existing adapter
            modAdapter.updateMods(allMods);
        }
        
        LogUtils.logUser("Loaded " + allMods.size() + " mods for management");
    }

    private void updateStatus() {
        // Check loader status
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(this);
        
        if (melonInstalled) {
            loaderStatusText.setText("✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " - DLL mods supported");
            loaderStatusText.setTextColor(0xFF4CAF50); // Green
            addDllModBtn.setEnabled(true);
            addDllModBtn.setText("📥 Add DLL Mod");
            loaderInfoSection.setVisibility(View.VISIBLE);
        } else if (lemonInstalled) {
            loaderStatusText.setText("✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " - DLL mods supported");
            loaderStatusText.setTextColor(0xFF4CAF50); // Green
            addDllModBtn.setEnabled(true);
            addDllModBtn.setText("📥 Add DLL Mod");
            loaderInfoSection.setVisibility(View.VISIBLE);
        } else {
            loaderStatusText.setText("⚠️ No loader installed - DLL mods unavailable");
            loaderStatusText.setTextColor(0xFFF44336); // Red
            addDllModBtn.setEnabled(false);
            addDllModBtn.setText("❌ Install Loader First");
            loaderInfoSection.setVisibility(View.GONE);
        }
        
        // Update mod counts
        int enabledCount = ModManager.getEnabledModCount();
        int totalCount = ModManager.getTotalModCount();
        int dexCount = ModManager.getDexModCount();
        int dllCount = ModManager.getDllModCount();
        
        statusText.setText(String.format("📊 Total: %d mods (%d enabled) | DEX/JAR: %d | DLL: %d", 
            totalCount, enabledCount, dexCount, dllCount));
    }

    private void showLoaderRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔧 Loader Required");
        builder.setMessage("DLL mods require MelonLoader or LemonLoader to be installed.\n\n" +
                          "Would you like to set up a loader now?");
        
        builder.setPositiveButton("🚀 Setup Loader", (dialog, which) -> {
            // Go to unified loader setup
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
        });
        
        builder.setNegativeButton("📖 Manual Guide", (dialog, which) -> {
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        String filename = getFilenameFromUri(uri);
        
        if (filename == null) {
            Toast.makeText(this, "Could not determine filename", Toast.LENGTH_SHORT).show();
            return;
        }
        
        switch (requestCode) {
            case REQUEST_SELECT_DLL:
                handleDllModInstallation(uri, filename);
                break;
                
            case REQUEST_SELECT_DEX:
                handleDexModInstallation(uri, filename);
                break;
        }
    }

    private void handleDllModInstallation(Uri uri, String filename) {
        if (!filename.toLowerCase().endsWith(".dll")) {
            Toast.makeText(this, "⚠️ Please select a .dll file", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📥 Install DLL Mod");
        builder.setMessage("Install '" + filename + "' as a DLL mod?\n\n" +
                          "This mod will be loaded by MelonLoader when Terraria starts.");
        
        builder.setPositiveButton("Install", (dialog, which) -> {
            boolean success = ModInstaller.installMod(this, uri, filename);
            if (success) {
                Toast.makeText(this, "✅ DLL mod installed: " + filename, Toast.LENGTH_SHORT).show();
                loadMods();
                updateStatus();
            } else {
                Toast.makeText(this, "❌ Failed to install DLL mod", Toast.LENGTH_LONG).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void handleDexModInstallation(Uri uri, String filename) {
        String lowerName = filename.toLowerCase();
        if (!lowerName.endsWith(".dex") && !lowerName.endsWith(".jar")) {
            Toast.makeText(this, "⚠️ Please select a .dex or .jar file", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📥 Install DEX/JAR Mod");
        builder.setMessage("Install '" + filename + "' as a DEX/JAR mod?\n\n" +
                          "This mod will be loaded directly by TerrariaLoader.");
        
        builder.setPositiveButton("Install", (dialog, which) -> {
            boolean success = ModInstaller.installMod(this, uri, filename);
            if (success) {
                Toast.makeText(this, "✅ DEX/JAR mod installed: " + filename, Toast.LENGTH_SHORT).show();
                loadMods();
                updateStatus();
            } else {
                Toast.makeText(this, "❌ Failed to install DEX/JAR mod", Toast.LENGTH_LONG).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getFilenameFromUri(Uri uri) {
        String filename = null;
        
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename from URI: " + e.getMessage());
        }
        
        if (filename == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                    filename = path.substring(lastSlash + 1);
                }
            }
        }
        
        return filename;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMods();
        updateStatus();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/OfflineDiagnosticActivity.java
package com.terrarialoader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.terrarialoader.R;
import com.terrarialoader.diagnostic.DiagnosticManager;
import com.terrarialoader.util.LogUtils;

import java.io.File;

public class OfflineDiagnosticActivity extends Activity {

    private TextView reportText;
    private ScrollView reportScroll;
    private Button runDiagnosticsBtn;
    private Button attemptRepairBtn;
    private Button exportReportBtn;
    private DiagnosticManager diagnosticManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_diagnostic);
        setTitle("🧪 Offline Diagnostic & Repair");

        LogUtils.logUser("OfflineDiagnosticActivity launched");

        diagnosticManager = new DiagnosticManager(this);

        reportText = findViewById(R.id.report_text);
        reportScroll = findViewById(R.id.report_scroll);
        runDiagnosticsBtn = findViewById(R.id.run_diagnostics_button);
        attemptRepairBtn = findViewById(R.id.repair_button);
        exportReportBtn = findViewById(R.id.export_report_button);

        runDiagnosticsBtn.setOnClickListener(v -> runDiagnostics());
        attemptRepairBtn.setOnClickListener(v -> attemptRepair());
        exportReportBtn.setOnClickListener(v -> exportReport());

        runDiagnostics(); // Auto-run on open
    }

    private void runDiagnostics() {
        String report = diagnosticManager.runFullDiagnostics();
        reportText.setText(report);
        reportScroll.post(() -> reportScroll.fullScroll(View.FOCUS_DOWN));
        Toast.makeText(this, "Diagnostics complete", Toast.LENGTH_SHORT).show();
    }

    private void attemptRepair() {
        boolean result = diagnosticManager.attemptSelfRepair();
        Toast.makeText(this, result ? "Repair complete ✅" : "Repair finished with issues ⚠️", Toast.LENGTH_LONG).show();
        runDiagnostics();
    }

    private void exportReport() {
        File exportFile = new File(getExternalFilesDir("exports"), "diagnostic_report.txt");
        boolean success = diagnosticManager.exportReportToFile(exportFile);
        Toast.makeText(this, success ? "Report exported to: " + exportFile.getName() : "Failed to export", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.logUser("OfflineDiagnosticActivity closed");
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/SettingsActivity.java
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

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/SetupGuideActivity.java
// File: SetupGuideActivity.java (Updated) - Added Offline ZIP Import
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/SetupGuideActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.R;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.OnlineInstaller;
import com.terrarialoader.util.OfflineZipImporter;

public class SetupGuideActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_ZIP = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_guide);

        setTitle("🚀 MelonLoader Setup Guide");

        setupButtons();
    }

    private void setupButtons() {
        Button btnOnlineInstall = findViewById(R.id.btn_online_install);
        Button btnOfflineImport = findViewById(R.id.btn_offline_import);
        Button btnManualInstructions = findViewById(R.id.btn_manual_instructions);

        btnOnlineInstall.setOnClickListener(v -> showOnlineInstallDialog());
        btnOfflineImport.setOnClickListener(v -> showOfflineImportDialog());
        btnManualInstructions.setOnClickListener(v -> {
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
    }

    private void showOnlineInstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🌐 Automated Online Installation");
        builder.setMessage("This will automatically download and install MelonLoader/LemonLoader files from GitHub.\n\n" +
                          "Requirements:\n" +
                          "• Active internet connection\n" +
                          "• ~50MB free space\n\n" +
                          "Continue with automated installation?");
        
        builder.setPositiveButton("Continue", (dialog, which) -> {
            showLoaderTypeDialog();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showOfflineImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📦 Offline ZIP Import");
        builder.setMessage("Import a MelonLoader ZIP file that you've already downloaded.\n\n" +
                          "Supported files:\n" +
                          "• melon_data.zip (MelonLoader)\n" +
                          "• lemon_data.zip (LemonLoader)\n" +
                          "• Custom MelonLoader packages\n\n" +
                          "The ZIP will be automatically extracted to the correct directories.");
        
        builder.setPositiveButton("📂 Select ZIP File", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_ZIP);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showLoaderTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Loader Type");
        builder.setMessage("Select which loader to install:\n\n" +
                          "🔸 MelonLoader:\n" +
                          "• Full-featured Unity mod loader\n" +
                          "• Larger file size (~40MB)\n" +
                          "• Best compatibility\n\n" +
                          "🔸 LemonLoader:\n" +
                          "• Lightweight Unity mod loader\n" +
                          "• Smaller file size (~15MB)\n" +
                          "• Faster installation\n\n" +
                          "Which would you like to install?");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            LogUtils.logUser("User selected MelonLoader for automated installation");
            startAutomatedInstallation(MelonLoaderManager.LoaderType.MELONLOADER_NET8);
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            LogUtils.logUser("User selected LemonLoader for automated installation");
            startAutomatedInstallation(MelonLoaderManager.LoaderType.MELONLOADER_NET35);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
    
    private void startAutomatedInstallation(MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("Starting automated " + loaderType.getDisplayName() + " installation...");
        
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("Installing " + loaderType.getDisplayName())
            .setMessage("Downloading and extracting files from GitHub...\nThis may take a few minutes.")
            .setCancelable(false)
            .show();

        new Thread(() -> {
            boolean success = false;
            String errorMessage = "";

            try {
                OnlineInstaller.InstallationResult result = OnlineInstaller.installMelonLoaderOnline(
                    this, MelonLoaderManager.TERRARIA_PACKAGE, loaderType);
                success = result.success;
                errorMessage = result.message;
                
            } catch (Exception e) {
                success = false;
                errorMessage = e.getMessage();
                LogUtils.logDebug("Automated installation error: " + errorMessage);
            }

            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;

            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (finalSuccess) {
                    showInstallationSuccessDialog(loaderType);
                } else {
                    showInstallationErrorDialog(loaderType, finalErrorMessage);
                }
            });
        }).start();
    }

    private void startOfflineImportProcess(Uri zipUri) {
        LogUtils.logUser("Starting offline ZIP import process...");
        
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("Importing MelonLoader ZIP")
            .setMessage("Analyzing and extracting ZIP file...\nThis may take a moment.")
            .setCancelable(false)
            .show();

        new Thread(() -> {
            OfflineZipImporter.ImportResult result = OfflineZipImporter.importMelonLoaderZip(this, zipUri);

            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (result.success) {
                    showImportSuccessDialog(result);
                } else {
                    showImportErrorDialog(result);
                }
            });
        }).start();
    }

    private void showInstallationSuccessDialog(MelonLoaderManager.LoaderType loaderType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Installation Complete!");
        builder.setMessage("Great! " + loaderType.getDisplayName() + " has been successfully installed!\n\n" +
                          "Next steps:\n" +
                          "1. Go to Unified Loader Activity\n" +
                          "2. Select your Terraria APK\n" +
                          "3. Patch APK with loader\n" +
                          "4. Install patched Terraria\n" +
                          "5. Add DLL mods and enjoy!\n\n" +
                          "You can now use DLL mods with Terraria!");
        
        builder.setPositiveButton("🚀 Open Unified Loader", (dialog, which) -> {
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
            finish();
        });
        
        builder.setNegativeButton("Later", (dialog, which) -> {
            finish();
        });
        
        builder.show();
    }

    private void showInstallationErrorDialog(MelonLoaderManager.LoaderType loaderType, String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Installation Failed");
        builder.setMessage("Failed to install " + loaderType.getDisplayName() + "\n\n" +
                          "Error: " + (errorMessage.isEmpty() ? "Unknown error occurred" : errorMessage) + "\n\n" +
                          "Please try:\n" +
                          "• Check your internet connection\n" +
                          "• Use Offline ZIP Import instead\n" +
                          "• Use Manual Installation\n" +
                          "• Try again later");
        
        builder.setPositiveButton("📦 Try Offline Import", (dialog, which) -> {
            showOfflineImportDialog();
        });
        
        builder.setNegativeButton("📖 Manual Guide", (dialog, which) -> {
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showImportSuccessDialog(OfflineZipImporter.ImportResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ ZIP Import Complete!");
        builder.setMessage("Successfully imported " + result.detectedType.getDisplayName() + "!\n\n" +
                          "Files extracted: " + result.filesExtracted + "\n\n" +
                          "The loader files have been automatically placed in the correct directories:\n" +
                          "• NET8/NET35 runtime files\n" +
                          "• Dependencies and support modules\n" +
                          "• All required components\n\n" +
                          "You can now patch APK files!");
        
        builder.setPositiveButton("🚀 Open Unified Loader", (dialog, which) -> {
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
            finish();
        });
        
        builder.setNegativeButton("✅ Done", (dialog, which) -> {
            finish();
        });
        
        builder.show();
    }

    private void showImportErrorDialog(OfflineZipImporter.ImportResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ ZIP Import Failed");
        builder.setMessage("Failed to import ZIP file\n\n" +
                          "Error: " + result.message + "\n\n" +
                          (result.errorDetails != null ? "Details: " + result.errorDetails + "\n\n" : "") +
                          "Please ensure:\n" +
                          "• ZIP file is a valid MelonLoader package\n" +
                          "• File is not corrupted\n" +
                          "• You have sufficient storage space");
        
        builder.setPositiveButton("🌐 Try Online Install", (dialog, which) -> {
            showOnlineInstallDialog();
        });
        
        builder.setNegativeButton("📖 Manual Guide", (dialog, which) -> {
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        builder.setNeutralButton("Try Again", (dialog, which) -> {
            showOfflineImportDialog();
        });
        
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_ZIP && resultCode == Activity.RESULT_OK && data != null) {
            Uri zipUri = data.getData();
            if (zipUri != null) {
                LogUtils.logUser("ZIP file selected for offline import");
                startOfflineImportProcess(zipUri);
            }
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/UnifiedLoaderActivity.java
// File: UnifiedLoaderActivity.java - Wizard-Style Unified Loader Interface
// Path: /main/java/com/terrarialoader/ui/UnifiedLoaderActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.R;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;

/**
 * Unified Loader Activity - Wizard-style interface for complete setup
 * Replaces both TerrariaActivity and DllModActivity with elegant workflow
 */
public class UnifiedLoaderActivity extends AppCompatActivity implements UnifiedLoaderController.UnifiedLoaderCallback {

    private static final int REQUEST_SELECT_APK = 1001;
    private static final int REQUEST_SELECT_ZIP = 1002;
    
    // UI Components
    private ProgressBar stepProgressBar;
    private TextView stepTitleText;
    private TextView stepDescriptionText;
    private TextView stepIndicatorText;
    private LinearLayout stepContentContainer;
    private Button previousButton;
    private Button nextButton;
    private Button actionButton;
    
    // Current step content views
    private LinearLayout welcomeContent;
    private LinearLayout loaderInstallContent;
    private LinearLayout apkSelectionContent;
    private LinearLayout patchingContent;
    private LinearLayout completionContent;
    
    // Status indicators
    private TextView loaderStatusText;
    private TextView apkStatusText;
    private TextView progressText;
    private ProgressBar actionProgressBar;
    
    // Controller
    private UnifiedLoaderController controller;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_loader);
        
        setTitle("🚀 MelonLoader Setup Wizard");
        
        // Initialize controller
        controller = new UnifiedLoaderController(this);
        controller.setCallback(this);
        
        initializeViews();
        setupStepContents();
        setupListeners();
        
        // Start wizard
        controller.setCurrentStep(UnifiedLoaderController.LoaderStep.WELCOME);
    }

    private void initializeViews() {
        stepProgressBar = findViewById(R.id.stepProgressBar);
        stepTitleText = findViewById(R.id.stepTitleText);
        stepDescriptionText = findViewById(R.id.stepDescriptionText);
        stepIndicatorText = findViewById(R.id.stepIndicatorText);
        stepContentContainer = findViewById(R.id.stepContentContainer);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        actionButton = findViewById(R.id.actionButton);
        
        loaderStatusText = findViewById(R.id.loaderStatusText);
        apkStatusText = findViewById(R.id.apkStatusText);
        progressText = findViewById(R.id.progressText);
        actionProgressBar = findViewById(R.id.actionProgressBar);
    }

    private void setupStepContents() {
        // Create step content views dynamically
        welcomeContent = createWelcomeContent();
        loaderInstallContent = createLoaderInstallContent();
        apkSelectionContent = createApkSelectionContent();
        patchingContent = createPatchingContent();
        completionContent = createCompletionContent();
    }

    private LinearLayout createWelcomeContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        
        TextView welcomeText = new TextView(this);
        welcomeText.setText("🎮 Welcome to MelonLoader Setup!\n\nThis wizard will guide you through:\n\n• Installing MelonLoader/LemonLoader\n• Patching your Terraria APK\n• Setting up DLL mod support\n\nClick 'Next' to begin!");
        welcomeText.setTextSize(16);
        welcomeText.setLineSpacing(8, 1.0f);
        content.addView(welcomeText);
        
        return content;
    }

    private LinearLayout createLoaderInstallContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        // Loader status
        TextView statusLabel = new TextView(this);
        statusLabel.setText("Current Status:");
        statusLabel.setTextSize(14);
        statusLabel.setTypeface(null, Typeface.BOLD);
        content.addView(statusLabel);
        
        loaderStatusText = new TextView(this);
        loaderStatusText.setText("Checking...");
        loaderStatusText.setTextSize(14);
        loaderStatusText.setPadding(0, 8, 0, 16);
        content.addView(loaderStatusText);
        
        // Installation options
        TextView optionsLabel = new TextView(this);
        optionsLabel.setText("Installation Options:");
        optionsLabel.setTextSize(14);
        optionsLabel.setTypeface(null, Typeface.BOLD);
        content.addView(optionsLabel);
        
        Button onlineInstallBtn = new Button(this);
        onlineInstallBtn.setText("🌐 Online Installation (Recommended)");
        onlineInstallBtn.setOnClickListener(v -> showOnlineInstallOptions());
        content.addView(onlineInstallBtn);
        
        Button offlineInstallBtn = new Button(this);
        offlineInstallBtn.setText("📦 Offline ZIP Import");
        offlineInstallBtn.setOnClickListener(v -> selectOfflineZip());
        content.addView(offlineInstallBtn);
        
        return content;
    }

    private LinearLayout createApkSelectionContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        TextView instructionText = new TextView(this);
        instructionText.setText("📱 Select your Terraria APK file to patch with MelonLoader:");
        instructionText.setTextSize(16);
        content.addView(instructionText);
        
        Button selectApkBtn = new Button(this);
        selectApkBtn.setText("📱 Select Terraria APK");
        selectApkBtn.setOnClickListener(v -> selectApkFile());
        content.addView(selectApkBtn);
        
        apkStatusText = new TextView(this);
        apkStatusText.setText("No APK selected");
        apkStatusText.setTextSize(14);
        apkStatusText.setPadding(0, 16, 0, 0);
        content.addView(apkStatusText);
        
        return content;
    }

    private LinearLayout createPatchingContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        content.setGravity(android.view.Gravity.CENTER);
        
        TextView patchingText = new TextView(this);
        patchingText.setText("⚡ Patching APK with MelonLoader...");
        patchingText.setTextSize(18);
        patchingText.setGravity(android.view.Gravity.CENTER);
        content.addView(patchingText);
        
        actionProgressBar = new ProgressBar(this);
        actionProgressBar.setIndeterminate(true);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressParams.topMargin = 24;
        progressParams.gravity = android.view.Gravity.CENTER;
        actionProgressBar.setLayoutParams(progressParams);
        content.addView(actionProgressBar);
        
        progressText = new TextView(this);
        progressText.setText("Initializing...");
        progressText.setTextSize(14);
        progressText.setGravity(android.view.Gravity.CENTER);
        progressText.setPadding(0, 16, 0, 0);
        content.addView(progressText);
        
        return content;
    }

    private LinearLayout createCompletionContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        
        TextView completionText = new TextView(this);
        completionText.setText("🎉 Setup Complete!\n\nYour modded Terraria APK is ready!");
        completionText.setTextSize(18);
        completionText.setGravity(android.view.Gravity.CENTER);
        content.addView(completionText);
        
        Button installApkBtn = new Button(this);
        installApkBtn.setText("📲 Install Patched APK");
        installApkBtn.setOnClickListener(v -> controller.installPatchedApk());
        content.addView(installApkBtn);
        
        Button manageModsBtn = new Button(this);
        manageModsBtn.setText("🔧 Manage DLL Mods");
        manageModsBtn.setOnClickListener(v -> openModManagement());
        content.addView(manageModsBtn);
        
        Button viewLogsBtn = new Button(this);
        viewLogsBtn.setText("📋 View Logs");
        viewLogsBtn.setOnClickListener(v -> startActivity(new Intent(this, LogViewerActivity.class)));
        content.addView(viewLogsBtn);
        
        return content;
    }

    private void setupListeners() {
        previousButton.setOnClickListener(v -> {
            if (controller.canProceedToPreviousStep()) {
                controller.previousStep();
            }
        });
        
        nextButton.setOnClickListener(v -> {
            if (controller.canProceedToNextStep()) {
                handleNextStep();
            } else {
                showStepRequirements();
            }
        });
        
        actionButton.setOnClickListener(v -> handleActionButton());
    }

    private void handleNextStep() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        
        switch (currentStep) {
            case WELCOME:
                controller.nextStep();
                break;
            case LOADER_INSTALL:
                if (controller.isLoaderInstalled()) {
                    controller.nextStep();
                } else {
                    Toast.makeText(this, "Please install a loader first", Toast.LENGTH_SHORT).show();
                }
                break;
            case APK_SELECTION:
                if (controller.getSelectedApkUri() != null) {
                    controller.nextStep();
                    controller.patchApk(); // Auto-start patching
                } else {
                    Toast.makeText(this, "Please select an APK first", Toast.LENGTH_SHORT).show();
                }
                break;
            case APK_PATCHING:
                // Patching in progress, disable navigation
                break;
            case COMPLETION:
                finish(); // Exit wizard
                break;
        }
    }

    private void handleActionButton() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        
        switch (currentStep) {
            case WELCOME:
                controller.nextStep();
                break;
            case LOADER_INSTALL:
                showOnlineInstallOptions();
                break;
            case APK_SELECTION:
                selectApkFile();
                break;
            case APK_PATCHING:
                // No action during patching
                break;
            case COMPLETION:
                controller.installPatchedApk();
                break;
        }
    }

    private void showOnlineInstallOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Loader Type");
        builder.setMessage("Select which loader to install:\n\n🔸 MelonLoader: Full-featured, larger size\n🔸 LemonLoader: Lightweight, smaller size");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            controller.installLoaderOnline(MelonLoaderManager.LoaderType.MELONLOADER_NET8);
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            controller.installLoaderOnline(MelonLoaderManager.LoaderType.MELONLOADER_NET35);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void selectOfflineZip() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_SELECT_ZIP);
    }

    private void selectApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_SELECT_APK);
    }

    private void openModManagement() {
        Intent intent = new Intent(this, ModManagementActivity.class);
        startActivity(intent);
    }

    private void showStepRequirements() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        String message = "";
        
        switch (currentStep) {
            case LOADER_INSTALL:
                message = "Please install MelonLoader or LemonLoader first";
                break;
            case APK_SELECTION:
                message = "Please select a Terraria APK file";
                break;
            default:
                message = "Please complete the current step";
                break;
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        
        switch (requestCode) {
            case REQUEST_SELECT_APK:
                controller.selectApk(uri);
                String filename = getFilenameFromUri(uri);
                apkStatusText.setText("✅ Selected: " + filename);
                break;
                
            case REQUEST_SELECT_ZIP:
                controller.installLoaderOffline(uri);
                break;
        }
    }

    private String getFilenameFromUri(Uri uri) {
        String filename = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename: " + e.getMessage());
        }
        return filename != null ? filename : "Unknown file";
    }

    // === UnifiedLoaderController.UnifiedLoaderCallback Implementation ===

    @Override
    public void onStepChanged(UnifiedLoaderController.LoaderStep step, String message) {
        runOnUiThread(() -> {
            stepTitleText.setText(step.getTitle());
            stepDescriptionText.setText(message);
            
            // Clear previous content
            stepContentContainer.removeAllViews();
            
            // Add appropriate content
            switch (step) {
                case WELCOME:
                    stepContentContainer.addView(welcomeContent);
                    actionButton.setText("🚀 Start Setup");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case LOADER_INSTALL:
                    stepContentContainer.addView(loaderInstallContent);
                    actionButton.setText("🌐 Install Online");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case APK_SELECTION:
                    stepContentContainer.addView(apkSelectionContent);
                    actionButton.setText("📱 Select APK");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case APK_PATCHING:
                    stepContentContainer.addView(patchingContent);
                    actionButton.setVisibility(View.GONE);
                    nextButton.setEnabled(false);
                    previousButton.setEnabled(false);
                    break;
                case COMPLETION:
                    stepContentContainer.addView(completionContent);
                    actionButton.setText("📲 Install APK");
                    actionButton.setVisibility(View.VISIBLE);
                    nextButton.setText("🏁 Finish");
                    nextButton.setEnabled(true);
                    previousButton.setEnabled(true);
                    break;
            }
            
            // Update navigation buttons
            previousButton.setEnabled(controller.canProceedToPreviousStep());
            nextButton.setEnabled(controller.canProceedToNextStep());
        });
    }

    @Override
    public void onProgress(String message, int percentage) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage(message + (percentage > 0 ? " (" + percentage + "%)" : ""));
            }
            if (progressText != null) {
                progressText.setText(message);
            }
        });
    }

    @Override
    public void onSuccess(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage(error);
            builder.setPositiveButton("OK", null);
            builder.show();
            
            // Re-enable navigation
            nextButton.setEnabled(controller.canProceedToNextStep());
            previousButton.setEnabled(controller.canProceedToPreviousStep());
        });
    }

    @Override
    public void onLoaderStatusChanged(boolean installed, String statusText) {
        runOnUiThread(() -> {
            if (loaderStatusText != null) {
                loaderStatusText.setText(statusText);
                loaderStatusText.setTextColor(installed ? 0xFF4CAF50 : 0xFFF44336);
            }
        });
    }

    @Override
    public void updateStepIndicator(int currentStep, int totalSteps) {
        runOnUiThread(() -> {
            stepProgressBar.setMax(totalSteps);
            stepProgressBar.setProgress(currentStep);
            stepIndicatorText.setText("Step " + (currentStep + 1) + " of " + (totalSteps + 1));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loader status when returning to activity
        if (controller != null) {
            // Update current step to refresh status
            controller.setCurrentStep(controller.getCurrentStep());
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/ui/UnifiedLoaderController.java
// File: UnifiedLoaderController.java - Business Logic Facade for Unified Loader
// Path: /main/java/com/terrarialoader/ui/UnifiedLoaderController.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.OnlineInstaller;
import com.terrarialoader.util.OfflineZipImporter;
import com.terrarialoader.util.ApkPatcher;
import java.io.File;

/**
 * Unified Loader Controller - Facade pattern to handle all loader operations
 * Keeps the main activity clean and focused on UI
 */
public class UnifiedLoaderController {
    
    public enum LoaderStep {
        WELCOME(0, "Welcome"),
        LOADER_INSTALL(1, "Install Loader"), 
        APK_SELECTION(2, "Select APK"),
        APK_PATCHING(3, "Patch APK"),
        COMPLETION(4, "Complete");
        
        private final int stepNumber;
        private final String title;
        
        LoaderStep(int stepNumber, String title) {
            this.stepNumber = stepNumber;
            this.title = title;
        }
        
        public int getStepNumber() { return stepNumber; }
        public String getTitle() { return title; }
        public static LoaderStep fromStep(int step) {
            for (LoaderStep s : values()) {
                if (s.stepNumber == step) return s;
            }
            return WELCOME;
        }
    }
    
    // State management
    private final Activity activity;
    private LoaderStep currentStep = LoaderStep.WELCOME;
    private MelonLoaderManager.LoaderType selectedLoaderType;
    private Uri selectedApkUri;
    private File patchedApkFile;
    private boolean loaderInstalled = false;
    
    // Callback interface for UI updates
    public interface UnifiedLoaderCallback {
        void onStepChanged(LoaderStep step, String message);
        void onProgress(String message, int percentage);
        void onSuccess(String message);
        void onError(String error);
        void onLoaderStatusChanged(boolean installed, String statusText);
        void updateStepIndicator(int currentStep, int totalSteps);
    }
    
    private UnifiedLoaderCallback callback;
    
    public UnifiedLoaderController(Activity activity) {
        this.activity = activity;
        updateLoaderStatus();
    }
    
    public void setCallback(UnifiedLoaderCallback callback) {
        this.callback = callback;
    }
    
    // === STEP MANAGEMENT ===
    
    public void nextStep() {
        int nextStepNum = Math.min(currentStep.getStepNumber() + 1, LoaderStep.COMPLETION.getStepNumber());
        setCurrentStep(LoaderStep.fromStep(nextStepNum));
    }
    
    public void previousStep() {
        int prevStepNum = Math.max(currentStep.getStepNumber() - 1, LoaderStep.WELCOME.getStepNumber());
        setCurrentStep(LoaderStep.fromStep(prevStepNum));
    }
    
    public void setCurrentStep(LoaderStep step) {
        this.currentStep = step;
        String message = getStepMessage(step);
        
        if (callback != null) {
            callback.onStepChanged(step, message);
            callback.updateStepIndicator(step.getStepNumber(), LoaderStep.values().length - 1);
        }
        
        LogUtils.logUser("Wizard step: " + step.getTitle());
    }
    
    private String getStepMessage(LoaderStep step) {
        switch (step) {
            case WELCOME:
                return "Welcome to the Unified MelonLoader Setup Wizard!\n\nThis wizard will guide you through:\n• Installing MelonLoader/LemonLoader\n• Patching your Terraria APK\n• Setting up DLL mod support";
            case LOADER_INSTALL:
                return loaderInstalled ? 
                    "✅ Loader is installed and ready!\n\nYou can reinstall or proceed to APK selection." :
                    "🔧 Choose how to install MelonLoader:\n\n• Online: Automatic download and setup\n• Offline: Import your own ZIP file";
            case APK_SELECTION:
                return selectedApkUri != null ?
                    "✅ APK selected: " + getFilenameFromUri(selectedApkUri) + "\n\nReady to patch with " + (selectedLoaderType != null ? selectedLoaderType.getDisplayName() : "MelonLoader") :
                    "📱 Select your Terraria APK file to patch with MelonLoader";
            case APK_PATCHING:
                return "⚡ Patching APK with " + (selectedLoaderType != null ? selectedLoaderType.getDisplayName() : "MelonLoader") + "...\n\nThis may take a few minutes.";
            case COMPLETION:
                return patchedApkFile != null ?
                    "🎉 Success! Your modded Terraria APK is ready!\n\n📱 Install the patched APK\n🎮 Add DLL mods\n🚀 Enjoy modded Terraria!" :
                    "❌ Setup incomplete. Please review the previous steps.";
            default:
                return "Processing...";
        }
    }
    
    // === LOADER INSTALLATION ===
    
    public void installLoaderOnline(MelonLoaderManager.LoaderType loaderType) {
        this.selectedLoaderType = loaderType;
        
        if (callback != null) {
            callback.onProgress("Starting online installation...", 0);
        }
        
        OnlineInstaller.installMelonLoaderAsync(activity, MelonLoaderManager.TERRARIA_PACKAGE, loaderType,
            new OnlineInstaller.InstallationProgressCallback() {
                @Override
                public void onProgress(String message, int percentage) {
                    if (callback != null) {
                        callback.onProgress(message, percentage);
                    }
                }
                
                @Override
                public void onComplete(OnlineInstaller.InstallationResult result) {
                    activity.runOnUiThread(() -> {
                        if (result.success) {
                            loaderInstalled = true;
                            updateLoaderStatus();
                            if (callback != null) {
                                callback.onSuccess("✅ " + loaderType.getDisplayName() + " installed successfully!");
                            }
                            nextStep(); // Auto-advance to APK selection
                        } else {
                            if (callback != null) {
                                callback.onError("Installation failed: " + result.message);
                            }
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onError("Installation error: " + error);
                        }
                    });
                }
            });
    }
    
    public void installLoaderOffline(Uri zipUri) {
        if (callback != null) {
            callback.onProgress("Importing ZIP file...", 0);
        }
        
        new Thread(() -> {
            try {
                OfflineZipImporter.ImportResult result = OfflineZipImporter.importMelonLoaderZip(activity, zipUri);
                
                activity.runOnUiThread(() -> {
                    if (result.success) {
                        selectedLoaderType = result.detectedType;
                        loaderInstalled = true;
                        updateLoaderStatus();
                        if (callback != null) {
                            callback.onSuccess("✅ " + result.detectedType.getDisplayName() + " imported successfully!");
                        }
                        nextStep(); // Auto-advance to APK selection
                    } else {
                        if (callback != null) {
                            callback.onError("Import failed: " + result.message);
                        }
                    }
                });
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onError("Import error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    // === APK OPERATIONS ===
    
    public void selectApk(Uri apkUri) {
        this.selectedApkUri = apkUri;
        LogUtils.logUser("APK selected: " + getFilenameFromUri(apkUri));
        setCurrentStep(currentStep); // Refresh step message
    }
    
    public void patchApk() {
        if (selectedApkUri == null || selectedLoaderType == null) {
            if (callback != null) {
                callback.onError("Please select APK and install loader first");
            }
            return;
        }
        
        setCurrentStep(LoaderStep.APK_PATCHING);
        
        if (callback != null) {
            callback.onProgress("Patching APK with " + selectedLoaderType.getDisplayName() + "...", 0);
        }
        
        new Thread(() -> {
            try {
                // Create temp input file
                File tempApk = File.createTempFile("input_", ".apk", activity.getCacheDir());
                FileUtils.copyUriToFile(activity, selectedApkUri, tempApk);
                
                // Create output file
                String originalName = getFilenameFromUri(selectedApkUri);
                String baseName = originalName.replace(".apk", "");
                patchedApkFile = new File(activity.getExternalFilesDir(null), 
                    baseName + "_" + selectedLoaderType.name().toLowerCase() + "_modded_" + 
                    System.currentTimeMillis() + ".apk");
                
                // Patch APK
                boolean success = ApkPatcher.injectMelonLoaderIntoApk(activity, tempApk, patchedApkFile, selectedLoaderType);
                
                // Cleanup
                tempApk.delete();
                
                activity.runOnUiThread(() -> {
                    if (success && patchedApkFile.exists()) {
                        if (callback != null) {
                            callback.onProgress("Patching completed!", 100);
                            callback.onSuccess("✅ APK patched successfully!");
                        }
                        nextStep(); // Move to completion
                    } else {
                        if (callback != null) {
                            callback.onError("APK patching failed");
                        }
                    }
                });
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onError("Patching error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    public void installPatchedApk() {
        if (patchedApkFile == null || !patchedApkFile.exists()) {
            if (callback != null) {
                callback.onError("No patched APK available");
            }
            return;
        }
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(
                    activity, 
                    activity.getPackageName() + ".provider", 
                    patchedApkFile
                ),
                "application/vnd.android.package-archive"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
            
            LogUtils.logUser("📲 Launching APK installer for: " + patchedApkFile.getName());
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Cannot install APK: " + e.getMessage());
            }
        }
    }
    
    // === STATUS CHECKS ===
    
    private void updateLoaderStatus() {
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(activity);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(activity);
        
        loaderInstalled = melonInstalled || lemonInstalled;
        
        String statusText;
        if (melonInstalled) {
            selectedLoaderType = MelonLoaderManager.LoaderType.MELONLOADER_NET8;
            statusText = "✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
        } else if (lemonInstalled) {
            selectedLoaderType = MelonLoaderManager.LoaderType.MELONLOADER_NET35;
            statusText = "✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
        } else {
            statusText = "❌ No loader installed";
        }
        
        if (callback != null) {
            callback.onLoaderStatusChanged(loaderInstalled, statusText);
        }
    }
    
    // === UTILITY METHODS ===
    
    private String getFilenameFromUri(Uri uri) {
        String filename = FileUtils.getFilenameFromUri(activity, uri);
        return filename != null ? filename : "Unknown APK";
    }
    
    public boolean canProceedToNextStep() {
        switch (currentStep) {
            case WELCOME:
                return true;
            case LOADER_INSTALL:
                return loaderInstalled;
            case APK_SELECTION:
                return selectedApkUri != null && loaderInstalled;
            case APK_PATCHING:
                return patchedApkFile != null && patchedApkFile.exists();
            case COMPLETION:
                return false; // Final step
            default:
                return false;
        }
    }
    
    public boolean canProceedToPreviousStep() {
        return currentStep.getStepNumber() > LoaderStep.WELCOME.getStepNumber();
    }
    
    // === GETTERS ===
    
    public LoaderStep getCurrentStep() { return currentStep; }
    public boolean isLoaderInstalled() { return loaderInstalled; }
    public Uri getSelectedApkUri() { return selectedApkUri; }
    public File getPatchedApkFile() { return patchedApkFile; }
    public MelonLoaderManager.LoaderType getSelectedLoaderType() { return selectedLoaderType; }
    
    // === RESET/RESTART ===
    
    public void resetWizard() {
        currentStep = LoaderStep.WELCOME;
        selectedApkUri = null;
        patchedApkFile = null;
        // Don't reset loader installation status
        setCurrentStep(LoaderStep.WELCOME);
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/ApkPatcher.java
// File: ApkPatcher.java (Real APK Patcher Implementation)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/ApkPatcher.java

package com.terrarialoader.util;

import android.content.Context;
import com.terrarialoader.loader.MelonLoaderManager;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

public class ApkPatcher {
    
    /**
     * Actually inject MelonLoader files into an APK
     * This method performs REAL APK modification, not just directory creation
     */
    public static boolean injectMelonLoaderIntoApk(Context context, File inputApk, File outputApk, MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("🔧 Starting REAL APK injection...");
        LogUtils.logUser("Input APK: " + inputApk.getName() + " (" + FileUtils.formatFileSize(inputApk.length()) + ")");
        
        try {
            // Step 1: Get MelonLoader files to inject
            List<FileToInject> filesToInject = getMelonLoaderFiles(context, loaderType);
            if (filesToInject.isEmpty()) {
                LogUtils.logUser("❌ No MelonLoader files found to inject!");
                LogUtils.logUser("💡 Install MelonLoader files first using 'Automated Installation'");
                return false;
            }
            
            LogUtils.logUser("📦 Found " + filesToInject.size() + " MelonLoader files to inject");
            
            // Step 2: Create modified APK
            boolean success = createModifiedApk(inputApk, outputApk, filesToInject);
            
            if (success) {
                long originalSize = inputApk.length();
                long modifiedSize = outputApk.length();
                long addedSize = modifiedSize - originalSize;
                
                LogUtils.logUser("✅ APK injection completed!");
                LogUtils.logUser("📊 Original: " + FileUtils.formatFileSize(originalSize));
                LogUtils.logUser("📊 Modified: " + FileUtils.formatFileSize(modifiedSize));
                LogUtils.logUser("📊 Added: " + FileUtils.formatFileSize(addedSize));
                
                return true;
            } else {
                LogUtils.logUser("❌ APK injection failed!");
                return false;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("APK injection error: " + e.getMessage());
            LogUtils.logUser("❌ APK injection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of MelonLoader files that need to be injected into APK
     */
    private static List<FileToInject> getMelonLoaderFiles(Context context, MelonLoaderManager.LoaderType loaderType) {
        List<FileToInject> files = new ArrayList<>();
        
        // Get runtime directory based on loader type
        File runtimeDir;
        if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
            runtimeDir = PathManager.getMelonLoaderNet8Dir(context, MelonLoaderManager.TERRARIA_PACKAGE);
        } else {
            runtimeDir = PathManager.getMelonLoaderNet35Dir(context, MelonLoaderManager.TERRARIA_PACKAGE);
        }
        
        // Core MelonLoader files to inject
        String[] coreFiles = {
            "MelonLoader.dll",
            "0Harmony.dll",
            "MonoMod.RuntimeDetour.dll", 
            "MonoMod.Utils.dll"
        };
        
        if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
            // Add NET8-specific files
            String[] net8Files = {
                "Il2CppInterop.Runtime.dll",
                "MelonLoader.deps.json",
                "MelonLoader.runtimeconfig.json"
            };
            coreFiles = combineArrays(coreFiles, net8Files);
        }
        
        // Check and add core files
        for (String fileName : coreFiles) {
            File file = new File(runtimeDir, fileName);
            if (file.exists() && file.length() > 0) {
                files.add(new FileToInject(file, "lib/arm64-v8a/" + fileName));
                LogUtils.logDebug("Will inject: " + fileName);
            } else {
                LogUtils.logDebug("Missing core file: " + fileName);
            }
        }
        
        // Add dependency files
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, MelonLoaderManager.TERRARIA_PACKAGE);
        File supportModulesDir = new File(depsDir, "SupportModules");
        
        if (supportModulesDir.exists()) {
            File[] supportFiles = supportModulesDir.listFiles((dir, name) -> name.endsWith(".dll"));
            if (supportFiles != null) {
                for (File file : supportFiles) {
                    if (file.length() > 0) {
                        files.add(new FileToInject(file, "lib/arm64-v8a/" + file.getName()));
                        LogUtils.logDebug("Will inject dependency: " + file.getName());
                    }
                }
            }
        }
        
        return files;
    }
    
    /**
     * Create modified APK with injected MelonLoader files
     */
    private static boolean createModifiedApk(File inputApk, File outputApk, List<FileToInject> filesToInject) {
        try {
            LogUtils.logUser("🔄 Creating modified APK...");
            
            // Create output APK by copying input and adding files
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(inputApk));
                 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputApk))) {
                
                // Copy all existing entries from input APK
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                int entriesCopied = 0;
                
                while ((entry = zis.getNextEntry()) != null) {
                    // Skip existing MelonLoader files if any
                    if (entry.getName().contains("MelonLoader") || 
                        entry.getName().contains("0Harmony") ||
                        entry.getName().contains("MonoMod")) {
                        LogUtils.logDebug("Skipping existing: " + entry.getName());
                        zis.closeEntry();
                        continue;
                    }
                    
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    
                    zos.closeEntry();
                    zis.closeEntry();
                    entriesCopied++;
                }
                
                LogUtils.logUser("📋 Copied " + entriesCopied + " original APK entries");
                
                // Add MelonLoader files
                int filesInjected = 0;
                for (FileToInject fileToInject : filesToInject) {
                    try {
                        zos.putNextEntry(new ZipEntry(fileToInject.targetPath));
                        
                        try (FileInputStream fis = new FileInputStream(fileToInject.sourceFile)) {
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        
                        zos.closeEntry();
                        filesInjected++;
                        LogUtils.logDebug("Injected: " + fileToInject.sourceFile.getName() + " -> " + fileToInject.targetPath);
                        
                    } catch (Exception e) {
                        LogUtils.logDebug("Failed to inject " + fileToInject.sourceFile.getName() + ": " + e.getMessage());
                    }
                }
                
                LogUtils.logUser("💉 Injected " + filesInjected + " MelonLoader files");
                
                // Add MelonLoader initialization code
                addMelonLoaderBootstrap(zos);
                
                return filesInjected > 0;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("APK creation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add MelonLoader bootstrap code to APK
     */
    private static void addMelonLoaderBootstrap(ZipOutputStream zos) throws IOException {
        LogUtils.logDebug("Adding MelonLoader bootstrap...");
        
        // Create a simple bootstrap script
        String bootstrapCode = "#!/system/bin/sh\n" +
                              "# MelonLoader Bootstrap\n" +
                              "# This script initializes MelonLoader for Terraria\n" +
                              "echo 'MelonLoader initialized'\n";
        
        zos.putNextEntry(new ZipEntry("assets/melonloader_bootstrap.sh"));
        zos.write(bootstrapCode.getBytes());
        zos.closeEntry();
        
        // Create MelonLoader config
        String config = "{\n" +
                       "  \"loader_type\": \"MelonLoader\",\n" +
                       "  \"version\": \"0.6.5\",\n" +
                       "  \"game\": \"Terraria\",\n" +
                       "  \"injected_by\": \"TerrariaLoader\"\n" +
                       "}";
        
        zos.putNextEntry(new ZipEntry("assets/melonloader_config.json"));
        zos.write(config.getBytes());
        zos.closeEntry();
        
        LogUtils.logDebug("Bootstrap files added");
    }
    
    /**
     * Helper class to represent a file to inject
     */
    private static class FileToInject {
        final File sourceFile;
        final String targetPath;
        
        FileToInject(File sourceFile, String targetPath) {
            this.sourceFile = sourceFile;
            this.targetPath = targetPath;
        }
    }
    
    /**
     * Utility method to combine arrays
     */
    private static String[] combineArrays(String[] array1, String[] array2) {
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    
    /**
     * Check if APK already has MelonLoader injected
     */
    public static boolean isApkPatched(File apkFile) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("MelonLoader.dll") || 
                    entry.getName().contains("melonloader_config.json")) {
                    return true;
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error checking APK: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get information about what would be injected
     */
    public static String getInjectionPreview(Context context, MelonLoaderManager.LoaderType loaderType) {
        List<FileToInject> files = getMelonLoaderFiles(context, loaderType);
        
        StringBuilder preview = new StringBuilder();
        preview.append("📋 INJECTION PREVIEW:\n\n");
        
        if (files.isEmpty()) {
            preview.append("❌ No MelonLoader files found!\n");
            preview.append("💡 Install MelonLoader first\n");
        } else {
            preview.append("Files to inject: ").append(files.size()).append("\n\n");
            
            long totalSize = 0;
            for (FileToInject file : files) {
                preview.append("• ").append(file.sourceFile.getName());
                preview.append(" (").append(FileUtils.formatFileSize(file.sourceFile.length())).append(")");
                preview.append(" → ").append(file.targetPath).append("\n");
                totalSize += file.sourceFile.length();
            }
            
            preview.append("\nTotal injection size: ").append(FileUtils.formatFileSize(totalSize));
        }
        
        return preview.toString();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/DiagnosticBundleExporter.java
// File: DiagnosticBundleExporter.java - Comprehensive Support Bundle Creator
// Path: /main/java/com/terrarialoader/util/DiagnosticBundleExporter.java

package com.terrarialoader.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.loader.ModManager;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates comprehensive diagnostic bundles for support purposes
 * Automatically compiles app logs, system info, mod info, and configuration
 */
public class DiagnosticBundleExporter {
    
    private static final String BUNDLE_NAME_FORMAT = "TerrariaLoader_Diagnostic_%s.zip";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    
    /**
     * Create a comprehensive diagnostic bundle
     * @param context Application context
     * @return File object of created bundle, or null if failed
     */
    public static File createDiagnosticBundle(Context context) {
        LogUtils.logUser("🔧 Creating comprehensive diagnostic bundle...");
        
        try {
            // Create bundle file
            String timestamp = TIMESTAMP_FORMAT.format(new Date());
            String bundleName = String.format(BUNDLE_NAME_FORMAT, timestamp);
            File exportsDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            File bundleFile = new File(exportsDir, bundleName);
            
            // Create ZIP bundle
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(bundleFile))) {
                
                // Add diagnostic report
                addDiagnosticReport(context, zos, timestamp);
                
                // Add system information
                addSystemInformation(context, zos);
                
                // Add application logs
                addApplicationLogs(context, zos);
                
                // Add game logs if available
                addGameLogs(context, zos);
                
                // Add mod information
                addModInformation(context, zos);
                
                // Add loader information
                addLoaderInformation(context, zos);
                
                // Add directory structure
                addDirectoryStructure(context, zos);
                
                // Add configuration files
                addConfigurationFiles(context, zos);
                
            }
            
            LogUtils.logUser("✅ Diagnostic bundle created: " + bundleName);
            LogUtils.logUser("📦 Size: " + FileUtils.formatFileSize(bundleFile.length()));
            
            return bundleFile;
            
        } catch (Exception e) {
            LogUtils.logDebug("Failed to create diagnostic bundle: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Add main diagnostic report
     */
    private static void addDiagnosticReport(Context context, ZipOutputStream zos, String timestamp) throws IOException {
        StringBuilder report = new StringBuilder();
        
        report.append("=== TERRARIA LOADER DIAGNOSTIC REPORT ===\n\n");
        report.append("Generated: ").append(new Date().toString()).append("\n");
        report.append("Bundle ID: ").append(timestamp).append("\n");
        report.append("Report Version: 2.0\n\n");
        
        // Executive summary
        report.append("=== EXECUTIVE SUMMARY ===\n");
        report.append("App Status: ").append(getAppStatus(context)).append("\n");
        report.append("Loader Status: ").append(getLoaderStatus(context)).append("\n");
        report.append("Mod Count: ").append(ModManager.getTotalModCount()).append("\n");
        report.append("Error Level: ").append(getErrorLevel(context)).append("\n\n");
        
        // Quick diagnostics
        report.append("=== QUICK DIAGNOSTICS ===\n");
        report.append(runQuickDiagnostics(context));
        report.append("\n");
        
        // Recommendations
        report.append("=== RECOMMENDATIONS ===\n");
        report.append(generateRecommendations(context));
        report.append("\n");
        
        // Bundle contents
        report.append("=== BUNDLE CONTENTS ===\n");
        report.append("1. diagnostic_report.txt - This report\n");
        report.append("2. system_info.txt - Device and OS information\n");
        report.append("3. app_logs/ - Application log files\n");
        report.append("4. game_logs/ - Game/MelonLoader log files (if available)\n");
        report.append("5. mod_info.txt - Installed mod information\n");
        report.append("6. loader_info.txt - MelonLoader installation details\n");
        report.append("7. directory_structure.txt - File system layout\n");
        report.append("8. configuration/ - Configuration files\n\n");
        
        addTextFile(zos, "diagnostic_report.txt", report.toString());
    }
    
    /**
     * Add system information
     */
    private static void addSystemInformation(Context context, ZipOutputStream zos) throws IOException {
        StringBuilder sysInfo = new StringBuilder();
        
        sysInfo.append("=== SYSTEM INFORMATION ===\n\n");
        
        // Device information
        sysInfo.append("Device Information:\n");
        sysInfo.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        sysInfo.append("Model: ").append(Build.MODEL).append("\n");
        sysInfo.append("Device: ").append(Build.DEVICE).append("\n");
        sysInfo.append("Product: ").append(Build.PRODUCT).append("\n");
        sysInfo.append("Hardware: ").append(Build.HARDWARE).append("\n");
        sysInfo.append("Board: ").append(Build.BOARD).append("\n");
        sysInfo.append("Brand: ").append(Build.BRAND).append("\n\n");
        
        // OS information
        sysInfo.append("Operating System:\n");
        sysInfo.append("Android Version: ").append(Build.VERSION.RELEASE).append("\n");
        sysInfo.append("API Level: ").append(Build.VERSION.SDK_INT).append("\n");
        sysInfo.append("Codename: ").append(Build.VERSION.CODENAME).append("\n");
        sysInfo.append("Incremental: ").append(Build.VERSION.INCREMENTAL).append("\n");
        sysInfo.append("Security Patch: ").append(Build.VERSION.SECURITY_PATCH).append("\n\n");
        
        // App information
        sysInfo.append("Application Information:\n");
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            sysInfo.append("Package Name: ").append(pInfo.packageName).append("\n");
            sysInfo.append("Version Name: ").append(pInfo.versionName).append("\n");
            sysInfo.append("Version Code: ").append(pInfo.versionCode).append("\n");
            sysInfo.append("Target SDK: ").append(pInfo.applicationInfo.targetSdkVersion).append("\n");
        } catch (Exception e) {
            sysInfo.append("Could not retrieve app information: ").append(e.getMessage()).append("\n");
        }
        sysInfo.append("\n");
        
        // Memory information
        sysInfo.append("Memory Information:\n");
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        sysInfo.append("Max Memory: ").append(FileUtils.formatFileSize(maxMemory)).append("\n");
        sysInfo.append("Total Memory: ").append(FileUtils.formatFileSize(totalMemory)).append("\n");
        sysInfo.append("Used Memory: ").append(FileUtils.formatFileSize(usedMemory)).append("\n");
        sysInfo.append("Free Memory: ").append(FileUtils.formatFileSize(freeMemory)).append("\n\n");
        
        // Storage information
        sysInfo.append("Storage Information:\n");
        try {
            File appDir = context.getExternalFilesDir(null);
            if (appDir != null) {
                sysInfo.append("App Directory: ").append(appDir.getAbsolutePath()).append("\n");
                sysInfo.append("Total Space: ").append(FileUtils.formatFileSize(appDir.getTotalSpace())).append("\n");
                sysInfo.append("Free Space: ").append(FileUtils.formatFileSize(appDir.getFreeSpace())).append("\n");
                sysInfo.append("Usable Space: ").append(FileUtils.formatFileSize(appDir.getUsableSpace())).append("\n");
            }
        } catch (Exception e) {
            sysInfo.append("Could not retrieve storage information: ").append(e.getMessage()).append("\n");
        }
        
        addTextFile(zos, "system_info.txt", sysInfo.toString());
    }
    
    /**
     * Add application logs
     */
    private static void addApplicationLogs(Context context, ZipOutputStream zos) throws IOException {
        try {
            // Add current logs
            String currentLogs = LogUtils.getLogs();
            if (!currentLogs.isEmpty()) {
                addTextFile(zos, "app_logs/current_session.txt", currentLogs);
            }
            
            // Add rotated log files
            List<File> logFiles = LogUtils.getAvailableLogFiles();
            for (int i = 0; i < logFiles.size(); i++) {
                File logFile = logFiles.get(i);
                if (logFile.exists() && logFile.length() > 0) {
                    String content = LogUtils.readLogFile(i);
                    addTextFile(zos, "app_logs/" + logFile.getName(), content);
                }
            }
            
        } catch (Exception e) {
            addTextFile(zos, "app_logs/error.txt", "Could not retrieve app logs: " + e.getMessage());
        }
    }
    
    /**
     * Add game logs if available
     */
    private static void addGameLogs(Context context, ZipOutputStream zos) throws IOException {
        try {
            File gameLogsDir = PathManager.getLogsDir(context, MelonLoaderManager.TERRARIA_PACKAGE);
            if (gameLogsDir.exists()) {
                File[] gameLogFiles = gameLogsDir.listFiles((dir, name) -> 
                    name.startsWith("Log") && name.endsWith(".txt"));
                
                if (gameLogFiles != null && gameLogFiles.length > 0) {
                    for (File logFile : gameLogFiles) {
                        try {
                            String content = readFileContent(logFile);
                            addTextFile(zos, "game_logs/" + logFile.getName(), content);
                        } catch (Exception e) {
                            addTextFile(zos, "game_logs/" + logFile.getName() + "_error.txt", 
                                "Could not read log file: " + e.getMessage());
                        }
                    }
                } else {
                    addTextFile(zos, "game_logs/no_logs.txt", "No game log files found");
                }
            } else {
                addTextFile(zos, "game_logs/directory_not_found.txt", 
                    "Game logs directory does not exist: " + gameLogsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            addTextFile(zos, "game_logs/error.txt", "Could not access game logs: " + e.getMessage());
        }
    }
    
    /**
     * Add mod information
     */
    private static void addModInformation(Context context, ZipOutputStream zos) throws IOException {
        StringBuilder modInfo = new StringBuilder();
        
        modInfo.append("=== MOD INFORMATION ===\n\n");
        
        try {
            // Statistics
            modInfo.append("Statistics:\n");
            modInfo.append("Total Mods: ").append(ModManager.getTotalModCount()).append("\n");
            modInfo.append("Enabled Mods: ").append(ModManager.getEnabledModCount()).append("\n");
            modInfo.append("Disabled Mods: ").append(ModManager.getDisabledModCount()).append("\n");
            modInfo.append("DEX Mods: ").append(ModManager.getDexModCount()).append("\n");
            modInfo.append("DLL Mods: ").append(ModManager.getDllModCount()).append("\n\n");
            
            // Available mods
            modInfo.append("Available Mods:\n");
            List<File> availableMods = ModManager.getAvailableMods();
            if (availableMods != null && !availableMods.isEmpty()) {
                for (File mod : availableMods) {
                    modInfo.append("- ").append(mod.getName());
                    modInfo.append(" (").append(FileUtils.formatFileSize(mod.length())).append(")");
                    modInfo.append(" [").append(ModManager.getModStatus(mod)).append("]");
                    modInfo.append(" {").append(ModManager.getModType(mod).getDisplayName()).append("}\n");
                }
            } else {
                modInfo.append("No mods found\n");
            }
            modInfo.append("\n");
            
            // Directory paths
            modInfo.append("Mod Directories:\n");
            modInfo.append("DEX Mods: ").append(ModManager.getModsDirectoryPath(context)).append("\n");
            modInfo.append("DLL Mods: ").append(ModManager.getDllModsDirectoryPath(context)).append("\n");
            
        } catch (Exception e) {
            modInfo.append("Error retrieving mod information: ").append(e.getMessage()).append("\n");
        }
        
        addTextFile(zos, "mod_info.txt", modInfo.toString());
    }
    
    /**
     * Add loader information
     */
    private static void addLoaderInformation(Context context, ZipOutputStream zos) throws IOException {
        StringBuilder loaderInfo = new StringBuilder();
        
        loaderInfo.append("=== LOADER INFORMATION ===\n\n");
        
        try {
            String gamePackage = MelonLoaderManager.TERRARIA_PACKAGE;
            
            // Status
            boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(context, gamePackage);
            boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(context, gamePackage);
            
            loaderInfo.append("Installation Status:\n");
            loaderInfo.append("MelonLoader Installed: ").append(melonInstalled).append("\n");
            loaderInfo.append("LemonLoader Installed: ").append(lemonInstalled).append("\n");
            
            if (melonInstalled || lemonInstalled) {
                loaderInfo.append("Version: ").append(MelonLoaderManager.getInstalledLoaderVersion()).append("\n");
            }
            loaderInfo.append("\n");
            
            // Validation report
            loaderInfo.append("Validation Report:\n");
            loaderInfo.append(MelonLoaderManager.getValidationReport(context, gamePackage));
            loaderInfo.append("\n");
            
            // Debug information
            loaderInfo.append("Debug Information:\n");
            loaderInfo.append(MelonLoaderManager.getDebugInfo(context, gamePackage));
            
        } catch (Exception e) {
            loaderInfo.append("Error retrieving loader information: ").append(e.getMessage()).append("\n");
        }
        
        addTextFile(zos, "loader_info.txt", loaderInfo.toString());
    }
    
    /**
     * Add directory structure
     */
    private static void addDirectoryStructure(Context context, ZipOutputStream zos) throws IOException {
        StringBuilder structure = new StringBuilder();
        
        structure.append("=== DIRECTORY STRUCTURE ===\n\n");
        
        try {
            // Path information
            structure.append("Path Information:\n");
            structure.append(PathManager.getPathInfo(context, MelonLoaderManager.TERRARIA_PACKAGE));
            structure.append("\n");
            
            // Directory tree
            structure.append("Directory Tree:\n");
            File baseDir = PathManager.getTerrariaBaseDir(context);
            if (baseDir.exists()) {
                structure.append(generateDirectoryTree(baseDir, ""));
            } else {
                structure.append("Base directory does not exist: ").append(baseDir.getAbsolutePath()).append("\n");
            }
            
        } catch (Exception e) {
            structure.append("Error generating directory structure: ").append(e.getMessage()).append("\n");
        }
        
        addTextFile(zos, "directory_structure.txt", structure.toString());
    }
    
    /**
     * Add configuration files
     */
    private static void addConfigurationFiles(Context context, ZipOutputStream zos) throws IOException {
        try {
            // App preferences
            addTextFile(zos, "configuration/app_preferences.txt", getAppPreferences(context));
            
            // Config directory files
            File configDir = PathManager.getConfigDir(context, MelonLoaderManager.TERRARIA_PACKAGE);
            if (configDir.exists()) {
                File[] configFiles = configDir.listFiles((dir, name) -> 
                    name.endsWith(".cfg") || name.endsWith(".json") || name.endsWith(".txt"));
                
                if (configFiles != null) {
                    for (File configFile : configFiles) {
                        try {
                            String content = readFileContent(configFile);
                            addTextFile(zos, "configuration/" + configFile.getName(), content);
                        } catch (Exception e) {
                            addTextFile(zos, "configuration/" + configFile.getName() + "_error.txt", 
                                "Could not read config file: " + e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            addTextFile(zos, "configuration/error.txt", "Could not retrieve configuration: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private static String getAppStatus(Context context) {
        try {
            return "Running normally";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private static String getLoaderStatus(Context context) {
        try {
            boolean installed = MelonLoaderManager.isMelonLoaderInstalled(context, MelonLoaderManager.TERRARIA_PACKAGE);
            return installed ? "Installed" : "Not installed";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private static String getErrorLevel(Context context) {
        try {
            String logs = LogUtils.getLogs();
            if (logs.toLowerCase().contains("error") || logs.toLowerCase().contains("crash")) {
                return "High";
            } else if (logs.toLowerCase().contains("warn")) {
                return "Medium";
            } else {
                return "Low";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private static String runQuickDiagnostics(Context context) {
        StringBuilder diagnostics = new StringBuilder();
        
        try {
            // Directory validation
            boolean directoriesValid = ModManager.validateModDirectories(context);
            diagnostics.append("Directory Structure: ").append(directoriesValid ? "✅ Valid" : "❌ Invalid").append("\n");
            
            // Health check
            boolean healthCheck = ModManager.performHealthCheck(context);
            diagnostics.append("Health Check: ").append(healthCheck ? "✅ Passed" : "❌ Failed").append("\n");
            
            // Migration status
            boolean needsMigration = PathManager.needsMigration(context);
            diagnostics.append("Migration Needed: ").append(needsMigration ? "⚠️ Yes" : "✅ No").append("\n");
            
        } catch (Exception e) {
            diagnostics.append("Diagnostic error: ").append(e.getMessage()).append("\n");
        }
        
        return diagnostics.toString();
    }
    
    private static String generateRecommendations(Context context) {
        StringBuilder recommendations = new StringBuilder();
        
        try {
            if (PathManager.needsMigration(context)) {
                recommendations.append("• Migrate to new directory structure\n");
            }
            
            if (!MelonLoaderManager.isMelonLoaderInstalled(context, MelonLoaderManager.TERRARIA_PACKAGE)) {
                recommendations.append("• Install MelonLoader for DLL mod support\n");
            }
            
            if (ModManager.getTotalModCount() == 0) {
                recommendations.append("• Install some mods to get started\n");
            }
            
            if (recommendations.length() == 0) {
                recommendations.append("• No specific recommendations at this time\n");
            }
            
        } catch (Exception e) {
            recommendations.append("• Could not generate recommendations: ").append(e.getMessage()).append("\n");
        }
        
        return recommendations.toString();
    }
    
    private static String getAppPreferences(Context context) {
        StringBuilder prefs = new StringBuilder();
        
        prefs.append("=== APP PREFERENCES ===\n\n");
        
        try {
            prefs.append("Mods Enabled: ").append(com.terrarialoader.ui.SettingsActivity.isModsEnabled(context)).append("\n");
            prefs.append("Debug Mode: ").append(com.terrarialoader.ui.SettingsActivity.isDebugMode(context)).append("\n");
            prefs.append("Sandbox Mode: ").append(com.terrarialoader.ui.SettingsActivity.isSandboxMode(context)).append("\n");
            prefs.append("Auto Save Logs: ").append(com.terrarialoader.ui.SettingsActivity.isAutoSaveEnabled(context)).append("\n");
        } catch (Exception e) {
            prefs.append("Could not retrieve preferences: ").append(e.getMessage()).append("\n");
        }
        
        return prefs.toString();
    }
    
    private static String generateDirectoryTree(File dir, String indent) {
        StringBuilder tree = new StringBuilder();
        
        if (dir == null || !dir.exists()) {
            return tree.toString();
        }
        
        tree.append(indent).append(dir.getName());
        if (dir.isDirectory()) {
            tree.append("/\n");
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (indent.length() < 20) { // Limit depth
                        tree.append(generateDirectoryTree(file, indent + "  "));
                    }
                }
            }
        } else {
            tree.append(" (").append(FileUtils.formatFileSize(dir.length())).append(")\n");
        }
        
        return tree.toString();
    }
    
    private static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private static void addTextFile(ZipOutputStream zos, String filename, String content) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/Downloader.java
// File: Downloader.java (Fixed Utility Class)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/Downloader.java

package com.terrarialoader.util;

import com.terrarialoader.util.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Downloader {

    /**
     * Downloads and extracts a ZIP file from a URL to a specified directory.
     * FIXED: Properly handles nested folder structures and flattens them correctly.
     *
     * @param fileUrl The URL of the ZIP file to download.
     * @param targetDirectory The directory where the contents will be extracted.
     * @return true if successful, false otherwise.
     */
    public static boolean downloadAndExtractZip(String fileUrl, File targetDirectory) {
        File zipFile = null;
        try {
            // Ensure the target directory exists
            if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                LogUtils.logDebug("❌ Failed to create target directory: " + targetDirectory.getAbsolutePath());
                return false;
            }
            LogUtils.logUser("📂 Target directory prepared: " + targetDirectory.getAbsolutePath());

            // --- Download Step ---
            LogUtils.logUser("🌐 Starting download from: " + fileUrl);
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LogUtils.logDebug("❌ Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                return false;
            }

            zipFile = new File(targetDirectory, "downloaded.zip");
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(zipFile)) {

                byte[] data = new byte[4096];
                int count;
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }
                LogUtils.logUser("✅ Download complete. Total size: " + FileUtils.formatFileSize(total));
            }

            // --- Extraction Step with Smart Path Handling ---
            LogUtils.logUser("📦 Starting extraction of " + zipFile.getName());
            try (InputStream is = new java.io.FileInputStream(zipFile);
                 ZipInputStream zis = new ZipInputStream(new java.io.BufferedInputStream(is))) {
                
                ZipEntry zipEntry;
                int extractedCount = 0;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    if (zipEntry.isDirectory()) {
                        zis.closeEntry();
                        continue;
                    }
                    
                    // FIXED: Smart path handling to flatten nested MelonLoader directories
                    String entryPath = zipEntry.getName();
                    String targetPath = getSmartTargetPath(entryPath);
                    
                    if (targetPath == null) {
                        LogUtils.logDebug("Skipping file: " + entryPath);
                        zis.closeEntry();
                        continue;
                    }
                    
                    File newFile = new File(targetDirectory, targetPath);
                    
                    // Prevent Zip Path Traversal Vulnerability
                    if (!newFile.getCanonicalPath().startsWith(targetDirectory.getCanonicalPath() + File.separator)) {
                        throw new SecurityException("Zip Path Traversal detected: " + zipEntry.getName());
                    }

                    // Create parent directories if they don't exist
                    newFile.getParentFile().mkdirs();
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        byte[] buffer = new byte[4096];
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    
                    extractedCount++;
                    LogUtils.logDebug("Extracted: " + entryPath + " -> " + targetPath);
                    zis.closeEntry();
                }
                LogUtils.logUser("✅ Extracted " + extractedCount + " files successfully.");
            }
            return true;

        } catch (Exception e) {
            LogUtils.logDebug("❌ Download and extraction failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // --- Cleanup Step ---
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
                LogUtils.logDebug("🧹 Cleaned up temporary zip file.");
            }
        }
    }
    
    /**
     * FIXED: Smart path mapping to handle nested MelonLoader directories properly
     * This function flattens the nested structure and maps files to correct locations
     */
    private static String getSmartTargetPath(String zipEntryPath) {
        // Normalize path separators
        String normalizedPath = zipEntryPath.replace('\\', '/');
        
        // Remove leading MelonLoader/ if it exists (to flatten nested structure)
        if (normalizedPath.startsWith("MelonLoader/")) {
            normalizedPath = normalizedPath.substring("MelonLoader/".length());
        }
        
        // Skip empty paths or root directory entries
        if (normalizedPath.isEmpty() || normalizedPath.equals("/")) {
            return null;
        }
        
        // Map specific directory structures
        if (normalizedPath.startsWith("net8/")) {
            return "net8/" + normalizedPath.substring("net8/".length());
        } else if (normalizedPath.startsWith("net35/")) {
            return "net35/" + normalizedPath.substring("net35/".length());
        } else if (normalizedPath.startsWith("Dependencies/")) {
            return "Dependencies/" + normalizedPath.substring("Dependencies/".length());
        } else if (normalizedPath.contains("/net8/")) {
            // Handle nested paths like "SomeFolder/net8/file.dll"
            int net8Index = normalizedPath.indexOf("/net8/");
            return "net8/" + normalizedPath.substring(net8Index + "/net8/".length());
        } else if (normalizedPath.contains("/net35/")) {
            // Handle nested paths like "SomeFolder/net35/file.dll"
            int net35Index = normalizedPath.indexOf("/net35/");
            return "net35/" + normalizedPath.substring(net35Index + "/net35/".length());
        } else if (normalizedPath.contains("/Dependencies/")) {
            // Handle nested paths like "SomeFolder/Dependencies/file.dll"
            int depsIndex = normalizedPath.indexOf("/Dependencies/");
            return "Dependencies/" + normalizedPath.substring(depsIndex + "/Dependencies/".length());
        } else {
            // For any other files, try to categorize them
            String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
            
            // Core MelonLoader files go to net8 by default
            if (fileName.equals("MelonLoader.dll") || 
                fileName.equals("0Harmony.dll") || 
                fileName.startsWith("MonoMod.") ||
                fileName.equals("Il2CppInterop.Runtime.dll")) {
                return "net8/" + fileName;
            }
            
            // Support files go to Dependencies/SupportModules
            if (fileName.endsWith(".dll") && !fileName.equals("MelonLoader.dll")) {
                return "Dependencies/SupportModules/" + fileName;
            }
            
            // Config files go to the root
            if (fileName.endsWith(".json") || fileName.endsWith(".cfg") || fileName.endsWith(".xml")) {
                return "net8/" + fileName;
            }
        }
        
        // Default: place in net8 directory
        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        return "net8/" + fileName;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/FileUtils.java
// File: FileUtils.java (Utility Class) - Phase 1 Complete (Error-Free)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/FileUtils.java

package com.terrarialoader.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import java.io.*;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static File ensureSubdirectory(Context context, String name) {
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }

        File subDir = new File(baseDir, name);
        if (!subDir.exists()) {
            if (!subDir.mkdirs()) {
                Log.e(TAG, "Directory creation failed: " + subDir.getAbsolutePath());
                LogUtils.logDebug("Failed to create directory: " + subDir.getAbsolutePath());
                return null;
            } else {
                LogUtils.logDebug("Created directory: " + subDir.getAbsolutePath());
            }
        }
        return subDir;
    }

    public static boolean copyUriToFile(Context context, Uri sourceUri, File destFile) {
        LogUtils.logDebug("Copying file from URI to: " + destFile.getAbsolutePath());
        
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            
            if (in == null) {
                LogUtils.logDebug("Failed to open input stream from URI");
                return false;
            }
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            LogUtils.logDebug("File copy completed: " + totalBytes + " bytes copied");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "File copy failed: " + e.getMessage(), e);
            LogUtils.logDebug("File copy failed: " + e.getMessage());
            
            // Clean up partial file on failure
            if (destFile.exists()) {
                destFile.delete();
            }
            
            return false;
        }
    }

    // Enhanced method to get filename from URI
    public static String getFilenameFromUri(Context context, Uri uri) {
        String filename = null;
        
        // Try to get filename from content resolver
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename from content resolver: " + e.getMessage());
        }
        
        // Fallback: try to extract from URI path
        if (filename == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                    filename = path.substring(lastSlash + 1);
                }
            }
        }
        
        LogUtils.logDebug("Extracted filename: " + filename);
        return filename;
    }

    // Enhanced file size calculation
    public static long getFileSize(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get file size: " + e.getMessage());
        }
        
        // Fallback: try to read the stream
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                return in.available();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not determine file size from stream: " + e.getMessage());
        }
        
        return -1;
    }

    public static boolean toggleModFile(File modFile) {
        if (modFile == null || !modFile.exists()) {
            LogUtils.logDebug("Cannot toggle non-existent file");
            return false;
        }

        String fileName = modFile.getName();
        File newFile;

        if (fileName.endsWith(".dex") || fileName.endsWith(".jar")) {
            // Disable mod
            newFile = new File(modFile.getParentFile(), fileName + ".disabled");
        } else if (fileName.endsWith(".dex.disabled")) {
            // Enable dex mod
            newFile = new File(modFile.getParentFile(), 
                fileName.substring(0, fileName.length() - ".disabled".length()));
        } else if (fileName.endsWith(".jar.disabled")) {
            // Enable jar mod
            newFile = new File(modFile.getParentFile(), 
                fileName.substring(0, fileName.length() - ".disabled".length()));
        } else {
            LogUtils.logDebug("Unknown file type for toggle: " + fileName);
            return false;
        }

        boolean success = modFile.renameTo(newFile);
        if (success) {
            LogUtils.logDebug("Toggled mod: " + fileName + " -> " + newFile.getName());
        } else {
            LogUtils.logDebug("Failed to toggle mod: " + fileName);
        }
        
        return success;
    }

    public static String[] listFilenamesInDir(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return new String[0];
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return new String[0];
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }

    // Enhanced directory operations
    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir == null) {
            return 0;
        }
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }

    public static int getFileCount(File dir) {
        if (dir == null) {
            return 0;
        }
        
        if (!dir.isDirectory()) {
            return dir.exists() ? 1 : 0;
        }
        
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += getFileCount(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    // File validation utilities
    public static boolean isValidModFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".dex") || name.endsWith(".dex.disabled") ||
               name.endsWith(".jar") || name.endsWith(".jar.disabled");
    }

    public static boolean isModEnabled(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".dex") || name.endsWith(".jar");
    }

    // Safe file operations
    public static boolean safeDelete(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            boolean deleted = file.delete();
            if (deleted) {
                LogUtils.logDebug("Successfully deleted: " + file.getName());
            } else {
                LogUtils.logDebug("Failed to delete: " + file.getName());
            }
            return deleted;
        } catch (Exception e) {
            LogUtils.logDebug("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    public static boolean safeRename(File source, File destination) {
        if (source == null || !source.exists() || destination == null) {
            return false;
        }
        
        try {
            boolean renamed = source.renameTo(destination);
            if (renamed) {
                LogUtils.logDebug("Successfully renamed: " + source.getName() + " -> " + destination.getName());
            } else {
                LogUtils.logDebug("Failed to rename: " + source.getName() + " -> " + destination.getName());
            }
            return renamed;
        } catch (Exception e) {
            LogUtils.logDebug("Error renaming file: " + e.getMessage());
            return false;
        }
    }

    // Create backup of file
    public static File createBackup(File originalFile) {
        if (originalFile == null || !originalFile.exists()) {
            return null;
        }
        
        String backupName = originalFile.getName() + ".backup." + System.currentTimeMillis();
        File backupFile = new File(originalFile.getParentFile(), backupName);
        
        try (FileInputStream in = new FileInputStream(originalFile);
             FileOutputStream out = new FileOutputStream(backupFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            LogUtils.logDebug("Created backup: " + backupName);
            return backupFile;
            
        } catch (Exception e) {
            LogUtils.logDebug("Failed to create backup: " + e.getMessage());
            if (backupFile.exists()) {
                backupFile.delete();
            }
            return null;
        }
    }

    // Format file size for display
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Clean up temporary files
    public static void cleanupTempFiles(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] tempFiles = cacheDir.listFiles((dir, name) -> 
                    name.startsWith("temp_") || name.endsWith(".tmp"));
                
                if (tempFiles != null) {
                    int deletedCount = 0;
                    for (File tempFile : tempFiles) {
                        if (tempFile.delete()) {
                            deletedCount++;
                        }
                    }
                    
                    if (deletedCount > 0) {
                        LogUtils.logDebug("Cleaned up " + deletedCount + " temporary files");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error cleaning up temp files: " + e.getMessage());
        }
    }

    // Check available storage space
    public static long getAvailableSpace(File dir) {
        try {
            if (dir != null && dir.exists()) {
                return dir.getFreeSpace();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get available space: " + e.getMessage());
        }
        return -1;
    }

    // Ensure sufficient space before operation
    public static boolean hasSufficientSpace(File dir, long requiredBytes) {
        long availableSpace = getAvailableSpace(dir);
        if (availableSpace < 0) {
            // Cannot determine space, assume it's available
            return true;
        }
        
        // Add 10% buffer
        long requiredWithBuffer = (long) (requiredBytes * 1.1);
        return availableSpace >= requiredWithBuffer;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/LogUtils.java
// File: LogUtils.java (FIXED) - Correct Log Rotation Logic
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/LogUtils.java

package com.terrarialoader.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogUtils {
    private static final StringBuilder debugLog = new StringBuilder();
    private static final StringBuilder userLog = new StringBuilder();
    private static Context appContext;
    private static final String PREFS_NAME = "terraria_loader_prefs";
    
    // FIXED: Add flag to prevent infinite recursion
    private static boolean isAutoSaving = false;
    
    // FIXED: Rotating log constants
    private static final int MAX_LOG_FILES = 5;
    private static final String GAME_LOG_PREFIX = "Log";
    private static final String APP_LOG_PREFIX = "AppLog";

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        
        // FIXED: Initialize app logs directory
        initializeAppLogsDirectory();
    }
    
    // FIXED: Initialize app logs directory with proper structure
    private static void initializeAppLogsDirectory() {
        if (appContext == null) return;
        
        try {
            // Create app logs directory in the new structure
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (!appLogsDir.exists()) {
                appLogsDir.mkdirs();
                logDebug("Created app logs directory: " + appLogsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            // Silent fail during initialization
        }
    }

    public static void debug(String message) {
        String line = getTime() + " [DEBUG] " + message;
        debugLog.append(line).append("\n");
        
        // FIXED: Only auto-save if enabled and we have substantial content (prevent empty file creation)
        if (isAutoSaveEnabled() && !isAutoSaving && debugLog.length() > 500) {
            saveToRotatingFile();
        }
    }

    public static void user(String message) {
        String line = getTime() + " [USER] " + message;
        userLog.append(line).append("\n");
        
        // FIXED: Only auto-save if enabled and we have substantial content (prevent empty file creation)
        if (isAutoSaveEnabled() && !isAutoSaving && (debugLog.length() + userLog.length()) > 500) {
            saveToRotatingFile();
        }
    }

    // Wrapper methods for compatibility
    public static void logDebug(String message) {
        debug(message);
    }

    public static void logUser(String message) {
        user(message);
    }

    public static String getLogs() {
        return debugLog.toString() + userLog.toString();
    }

    public static String getDebugLogs() {
        return debugLog.toString();
    }

    public static String getUserLogs() {
        return userLog.toString();
    }

    // Enhanced log filtering functionality
    public static String getFilteredLogs(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return getLogs();
        }
        
        String allLogs = getLogs();
        String[] lines = allLogs.split("\n");
        StringBuilder filtered = new StringBuilder();
        
        String filterLower = filter.toLowerCase();
        for (String line : lines) {
            if (line.toLowerCase().contains(filterLower)) {
                filtered.append(line).append("\n");
            }
        }
        
        return filtered.toString();
    }

    // Get logs by category
    public static String getLogsByCategory(String category) {
        String allLogs = getLogs();
        String[] lines = allLogs.split("\n");
        StringBuilder categoryLogs = new StringBuilder();
        
        for (String line : lines) {
            if (line.contains("[" + category.toUpperCase() + "]")) {
                categoryLogs.append(line).append("\n");
            }
        }
        
        return categoryLogs.toString();
    }

    // Get recent logs (last N entries)
    public static String getRecentLogs(int count) {
        String allLogs = getLogs();
        String[] lines = allLogs.split("\n");
        
        int startIndex = Math.max(0, lines.length - count);
        StringBuilder recent = new StringBuilder();
        
        for (int i = startIndex; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                recent.append(lines[i]).append("\n");
            }
        }
        
        return recent.toString();
    }

    private static String getTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public static void saveLogsToFile(File output) throws IOException {
        FileWriter writer = new FileWriter(output);
        writer.write(getLogs());
        writer.close();
    }

    // FIXED: Rotating log system implementation - Only save when there's actual content
    private static void saveToRotatingFile() {
        if (appContext == null || isAutoSaving) return;
        
        // Don't save if there's no actual log content
        String currentLogs = getLogs();
        if (currentLogs.trim().isEmpty()) {
            return;
        }
        
        try {
            isAutoSaving = true;
            
            // FIXED: Save to app logs directory with rotation
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (!appLogsDir.exists()) {
                appLogsDir.mkdirs();
            }
            
            // Get current log file
            File currentLogFile = new File(appLogsDir, APP_LOG_PREFIX + ".txt");
            
            // FIXED: Only rotate if current log exists, has content, AND we're about to write new content
            if (currentLogFile.exists() && currentLogFile.length() > 1000) { // Only rotate if file is substantial (>1KB)
                // Check if the new content is significantly different from existing
                if (shouldRotateLog(currentLogFile, currentLogs)) {
                    rotateLogFiles(appLogsDir, APP_LOG_PREFIX);
                }
            }
            
            // Write current log (always create/overwrite current log)
            try (FileWriter writer = new FileWriter(currentLogFile, false)) { // Overwrite mode
                writer.write("=== TerrariaLoader App Log ===\n");
                writer.write("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n");
                writer.write("===================================\n\n");
                writer.write(currentLogs);
            }
            
        } catch (IOException e) {
            // Silent fail for auto-save
        } finally {
            isAutoSaving = false;
        }
    }
    
    // FIXED: Implement log rotation (Log.txt -> Log1.txt -> Log2.txt -> ... -> Log5.txt)
    private static void rotateLogFiles(File logDir, String logPrefix) {
        try {
            // Delete the oldest log file (Log5.txt)
            File oldestLog = new File(logDir, logPrefix + MAX_LOG_FILES + ".txt");
            if (oldestLog.exists()) {
                oldestLog.delete();
            }
            
            // Rotate existing log files (Log4.txt -> Log5.txt, Log3.txt -> Log4.txt, etc.)
            for (int i = MAX_LOG_FILES - 1; i >= 1; i--) {
                File currentFile = new File(logDir, logPrefix + i + ".txt");
                File nextFile = new File(logDir, logPrefix + (i + 1) + ".txt");
                
                if (currentFile.exists()) {
                    currentFile.renameTo(nextFile);
                }
            }
            
            // Move current log to Log1.txt
            File currentLog = new File(logDir, logPrefix + ".txt");
            File firstBackup = new File(logDir, logPrefix + "1.txt");
            
            if (currentLog.exists()) {
                currentLog.renameTo(firstBackup);
            }
            
        } catch (Exception e) {
            // Silent fail for rotation
        }
    }
    
    // FIXED: Helper method to determine if log should be rotated
    private static boolean shouldRotateLog(File currentLogFile, String newContent) {
        try {
            // Read existing file content
            StringBuilder existingContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentLogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingContent.append(line).append("\n");
                }
            }
            
            String existing = existingContent.toString();
            
            // Only rotate if new content is significantly different (not just a few new lines)
            int existingLines = existing.split("\n").length;
            int newLines = newContent.split("\n").length;
            
            // Rotate if new content has 50+ more lines than existing, or if it's been more than an hour
            boolean significantContentChange = (newLines - existingLines) > 50;
            boolean timeBasedRotation = (System.currentTimeMillis() - currentLogFile.lastModified()) > (60 * 60 * 1000); // 1 hour
            
            return significantContentChange || timeBasedRotation;
            
        } catch (Exception e) {
            // If we can't read the existing file, don't rotate
            return false;
        }
    }
    public static void rotateGameLogs(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return;
        
        try {
            File gameBaseDir = new File(context.getExternalFilesDir(null), "TerrariaLoader/" + gamePackage);
            File gameLogsDir = new File(gameBaseDir, "Logs");
            if (!gameLogsDir.exists()) {
                gameLogsDir.mkdirs();
            }
            
            rotateLogFiles(gameLogsDir, GAME_LOG_PREFIX);
            
        } catch (Exception e) {
            logDebug("Game log rotation failed: " + e.getMessage());
        }
    }
    
    // FIXED: Manual log rotation trigger
    public static void forceLogRotation() {
        if (appContext == null) return;
        
        try {
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (!appLogsDir.exists()) {
                appLogsDir.mkdirs();
            }
            
            rotateLogFiles(appLogsDir, APP_LOG_PREFIX);
            logUser("Log rotation completed manually");
            
        } catch (Exception e) {
            logDebug("Manual log rotation failed: " + e.getMessage());
        }
    }

    public static void clearLogs() {
        debugLog.setLength(0);
        userLog.setLength(0);
        
        // FIXED: Only log this if not auto-saving
        if (!isAutoSaving) {
            logUser("Logs cleared at " + getTime());
        }
    }
    
    // FIXED: Clear all log files (including rotated ones)
    public static void clearAllLogFiles() {
        if (appContext == null) return;
        
        try {
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (appLogsDir.exists()) {
                // Delete all app log files
                for (int i = 0; i <= MAX_LOG_FILES; i++) {
                    String fileName = i == 0 ? APP_LOG_PREFIX + ".txt" : APP_LOG_PREFIX + i + ".txt";
                    File logFile = new File(appLogsDir, fileName);
                    if (logFile.exists()) {
                        logFile.delete();
                    }
                }
                logUser("All app log files cleared");
            }
        } catch (Exception e) {
            logDebug("Failed to clear log files: " + e.getMessage());
        }
    }

    // Settings integration
    private static boolean isAutoSaveEnabled() {
        if (appContext == null) return false;
        
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("auto_save_logs", false);
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        if (appContext == null) return;
        
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("auto_save_logs", enabled).apply();
        
        // FIXED: Only log this if not auto-saving
        if (!isAutoSaving) {
            logUser("Auto-save logs " + (enabled ? "enabled" : "disabled"));
        }
    }

    // Log statistics
    public static int getLogCount() {
        return getLogs().split("\n").length;
    }

    public static int getDebugLogCount() {
        return getDebugLogs().split("\n").length;
    }

    public static int getUserLogCount() {
        return getUserLogs().split("\n").length;
    }

    // FIXED: Get log files info
    public static String getLogFilesInfo() {
        if (appContext == null) return "Context not available";
        
        StringBuilder info = new StringBuilder();
        info.append("=== App Log Files ===\n");
        
        try {
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (appLogsDir.exists()) {
                for (int i = 0; i <= MAX_LOG_FILES; i++) {
                    String fileName = i == 0 ? APP_LOG_PREFIX + ".txt" : APP_LOG_PREFIX + i + ".txt";
                    File logFile = new File(appLogsDir, fileName);
                    
                    if (logFile.exists()) {
                        info.append("📄 ").append(fileName);
                        info.append(" (").append(formatFileSize(logFile.length())).append(")");
                        info.append(" - ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(logFile.lastModified())));
                        info.append("\n");
                    } else {
                        info.append("❌ ").append(fileName).append(" (not found)\n");
                    }
                }
                
                info.append("\nDirectory: ").append(appLogsDir.getAbsolutePath()).append("\n");
            } else {
                info.append("App logs directory not found\n");
            }
        } catch (Exception e) {
            info.append("Error reading log files: ").append(e.getMessage()).append("\n");
        }
        
        return info.toString();
    }

    // Export logs with metadata
    public static void exportLogsWithMetadata(File output) throws IOException {
        FileWriter writer = new FileWriter(output);
        
        // Write metadata header
        writer.write("=== Terraria Loader Log Export ===\n");
        writer.write("Export Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n");
        writer.write("Total Logs: " + getLogCount() + "\n");
        writer.write("Debug Logs: " + getDebugLogCount() + "\n");
        writer.write("User Logs: " + getUserLogCount() + "\n");
        writer.write("=====================================\n\n");
        
        // Write actual logs
        writer.write(getLogs());
        writer.close();
    }
    
    // FIXED: Export all log files (including rotated ones)
    public static boolean exportAllLogFiles(File exportDir) {
        if (appContext == null || exportDir == null) return false;
        
        try {
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (!appLogsDir.exists()) {
                return false;
            }
            
            int exportedCount = 0;
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            
            // Export all log files
            for (int i = 0; i <= MAX_LOG_FILES; i++) {
                String fileName = i == 0 ? APP_LOG_PREFIX + ".txt" : APP_LOG_PREFIX + i + ".txt";
                File sourceFile = new File(appLogsDir, fileName);
                
                if (sourceFile.exists() && sourceFile.length() > 0) {
                    String exportFileName = "TerrariaLoader_" + fileName.replace(".txt", "_" + timestamp + ".txt");
                    File exportFile = new File(exportDir, exportFileName);
                    
                    if (copyFile(sourceFile, exportFile)) {
                        exportedCount++;
                    }
                }
            }
            
            logUser("Exported " + exportedCount + " log files to: " + exportDir.getAbsolutePath());
            return exportedCount > 0;
            
        } catch (Exception e) {
            logDebug("Log export failed: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to copy files
    private static boolean copyFile(File source, File target) {
        try (java.io.FileInputStream in = new java.io.FileInputStream(source);
             java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    // FIXED: Get available log files list
    public static List<File> getAvailableLogFiles() {
        List<File> logFiles = new ArrayList<>();
        
        if (appContext == null) return logFiles;
        
        try {
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            if (appLogsDir.exists()) {
                for (int i = 0; i <= MAX_LOG_FILES; i++) {
                    String fileName = i == 0 ? APP_LOG_PREFIX + ".txt" : APP_LOG_PREFIX + i + ".txt";
                    File logFile = new File(appLogsDir, fileName);
                    
                    if (logFile.exists() && logFile.length() > 0) {
                        logFiles.add(logFile);
                    }
                }
            }
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return logFiles;
    }
    
    // FIXED: Read specific log file
    public static String readLogFile(int logNumber) {
        if (appContext == null) return "Context not available";
        
        try {
            File gameBaseDir = new File(appContext.getExternalFilesDir(null), "TerrariaLoader/com.and.games505.TerrariaPaid");
            File appLogsDir = new File(gameBaseDir, "AppLogs");
            String fileName = logNumber == 0 ? APP_LOG_PREFIX + ".txt" : APP_LOG_PREFIX + logNumber + ".txt";
            File logFile = new File(appLogsDir, fileName);
            
            if (!logFile.exists()) {
                return "Log file " + fileName + " not found";
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            return content.toString();
            
        } catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
    }

    // FIXED: Initialize logs when app starts (NO auto-rotation, NO immediate file creation)
    public static void initializeAppStartup() {
        if (appContext == null) return;
        
        try {
            // Just log startup - don't force any file operations
            logUser("=== TerrariaLoader Started ===");
            logUser("App Version: " + getAppVersion());
            logUser("Android Version: " + android.os.Build.VERSION.RELEASE);
            logUser("Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            logUser("Startup Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            logUser("==============================");
            
            // FIXED: Don't save immediately - let natural auto-save handle it when there's enough content
            
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private static String getAppVersion() {
        try {
            if (appContext != null) {
                android.content.pm.PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                return pInfo.versionName;
            }
        } catch (Exception e) {
            // Fall back to unknown
        }
        return "Unknown";
    }
    
    // Helper method to format file size
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/MelonLoaderDiagnostic.java
// File: MelonLoaderDiagnostic.java (Diagnostic Tool)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/MelonLoaderDiagnostic.java

package com.terrarialoader.util;

import android.content.Context;
import com.terrarialoader.loader.MelonLoaderManager;
import java.io.File;

public class MelonLoaderDiagnostic {
    
    public static String generateDetailedDiagnostic(Context context, String gamePackage) {
        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("=== DETAILED MELONLOADER DIAGNOSTIC ===\n\n");
        
        // Check all required directories and files
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        File melonLoaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
        File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
        
        diagnostic.append("📁 DIRECTORY STATUS:\n");
        diagnostic.append("Base Dir: ").append(checkDirectory(baseDir)).append("\n");
        diagnostic.append("MelonLoader Dir: ").append(checkDirectory(melonLoaderDir)).append("\n");
        diagnostic.append("NET8 Dir: ").append(checkDirectory(net8Dir)).append("\n");
        diagnostic.append("NET35 Dir: ").append(checkDirectory(net35Dir)).append("\n");
        diagnostic.append("Dependencies Dir: ").append(checkDirectory(depsDir)).append("\n\n");
        
        // Check for required NET8 files
        diagnostic.append("🔸 NET8 RUNTIME FILES:\n");
        String[] net8Files = {
            "MelonLoader.dll",
            "0Harmony.dll", 
            "MonoMod.RuntimeDetour.dll",
            "MonoMod.Utils.dll",
            "Il2CppInterop.Runtime.dll"
        };
        
        int net8Found = 0;
        for (String fileName : net8Files) {
            File file = new File(net8Dir, fileName);
            boolean exists = file.exists() && file.length() > 0;
            diagnostic.append("  ").append(exists ? "✅" : "❌").append(" ").append(fileName);
            if (exists) {
                net8Found++;
                diagnostic.append(" (").append(FileUtils.formatFileSize(file.length())).append(")");
            }
            diagnostic.append("\n");
        }
        diagnostic.append("NET8 Score: ").append(net8Found).append("/").append(net8Files.length).append("\n\n");
        
        // Check for required NET35 files
        diagnostic.append("🔸 NET35 RUNTIME FILES:\n");
        String[] net35Files = {
            "MelonLoader.dll",
            "0Harmony.dll",
            "MonoMod.RuntimeDetour.dll", 
            "MonoMod.Utils.dll"
        };
        
        int net35Found = 0;
        for (String fileName : net35Files) {
            File file = new File(net35Dir, fileName);
            boolean exists = file.exists() && file.length() > 0;
            diagnostic.append("  ").append(exists ? "✅" : "❌").append(" ").append(fileName);
            if (exists) {
                net35Found++;
                diagnostic.append(" (").append(FileUtils.formatFileSize(file.length())).append(")");
            }
            diagnostic.append("\n");
        }
        diagnostic.append("NET35 Score: ").append(net35Found).append("/").append(net35Files.length).append("\n\n");
        
        // Check Dependencies
        diagnostic.append("🔸 DEPENDENCY FILES:\n");
        File supportModulesDir = new File(depsDir, "SupportModules");
        File assemblyGenDir = new File(depsDir, "Il2CppAssemblyGenerator");
        
        diagnostic.append("Support Modules Dir: ").append(checkDirectory(supportModulesDir)).append("\n");
        diagnostic.append("Assembly Generator Dir: ").append(checkDirectory(assemblyGenDir)).append("\n");
        
        // List actual files found
        diagnostic.append("\n📋 FILES FOUND:\n");
        if (melonLoaderDir.exists()) {
            diagnostic.append(listDirectoryContents(melonLoaderDir, ""));
        } else {
            diagnostic.append("MelonLoader directory doesn't exist!\n");
        }
        
        // Generate recommendations
        diagnostic.append("\n💡 RECOMMENDATIONS:\n");
        if (net8Found == 0 && net35Found == 0) {
            diagnostic.append("❌ NO RUNTIME FILES FOUND!\n");
            diagnostic.append("SOLUTION: You need to install MelonLoader files.\n");
            diagnostic.append("Options:\n");
            diagnostic.append("1. Use 'Automated Installation' in Setup Guide\n");
            diagnostic.append("2. Manually download and extract MelonLoader files\n");
            diagnostic.append("3. Use the APK patcher to inject loader\n\n");
        } else if (net8Found > 0) {
            diagnostic.append("✅ Some NET8 files found, but incomplete installation\n");
            diagnostic.append("Missing files need to be added to: ").append(net8Dir.getAbsolutePath()).append("\n\n");
        } else if (net35Found > 0) {
            diagnostic.append("✅ Some NET35 files found, but incomplete installation\n");
            diagnostic.append("Missing files need to be added to: ").append(net35Dir.getAbsolutePath()).append("\n\n");
        }
        
        // Check if automated installation would work
        diagnostic.append("🌐 INTERNET CONNECTIVITY: ");
        if (OnlineInstaller.isOnlineInstallationAvailable()) {
            diagnostic.append("✅ Available - Automated installation possible\n");
            diagnostic.append("RECOMMENDED: Use 'Automated Installation' for easiest setup\n");
        } else {
            diagnostic.append("❌ Not available - Manual installation required\n");
            diagnostic.append("REQUIRED: Download MelonLoader files manually\n");
        }
        
        return diagnostic.toString();
    }
    
    private static String checkDirectory(File dir) {
        if (dir == null) return "❌ null";
        if (!dir.exists()) return "❌ doesn't exist (" + dir.getAbsolutePath() + ")";
        if (!dir.isDirectory()) return "❌ not a directory";
        
        File[] files = dir.listFiles();
        int fileCount = files != null ? files.length : 0;
        return "✅ exists (" + fileCount + " items)";
    }
    
    private static String listDirectoryContents(File dir, String indent) {
        StringBuilder contents = new StringBuilder();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return contents.toString();
        }
        
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            contents.append(indent).append("(empty)\n");
            return contents.toString();
        }
        
        for (File file : files) {
            contents.append(indent);
            if (file.isDirectory()) {
                contents.append("📁 ").append(file.getName()).append("/\n");
                if (indent.length() < 8) { // Limit recursion depth
                    contents.append(listDirectoryContents(file, indent + "  "));
                }
            } else {
                contents.append("📄 ").append(file.getName());
                contents.append(" (").append(FileUtils.formatFileSize(file.length())).append(")\n");
            }
        }
        
        return contents.toString();
    }
    
    // Quick fix suggestions
    public static String getQuickFixSuggestions(Context context, String gamePackage) {
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("🚀 QUICK FIX OPTIONS:\n\n");
        
        suggestions.append("1. AUTOMATED INSTALLATION (Recommended):\n");
        suggestions.append("   • Go to 'MelonLoader Setup Guide'\n");
        suggestions.append("   • Choose 'Automated Online Installation'\n");
        suggestions.append("   • Select MelonLoader or LemonLoader\n");
        suggestions.append("   • Wait for download and extraction\n\n");
        
        suggestions.append("2. MANUAL INSTALLATION:\n");
        suggestions.append("   • Download MelonLoader from GitHub\n");
        suggestions.append("   • Extract files to correct directories\n");
        suggestions.append("   • Follow the manual installation guide\n\n");
        
        suggestions.append("3. APK INJECTION:\n");
        suggestions.append("   • Use 'APK Patcher' feature\n");
        suggestions.append("   • Select Terraria APK\n");
        suggestions.append("   • Inject MelonLoader into APK\n");
        suggestions.append("   • Install modified APK\n\n");
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        suggestions.append("📍 TARGET DIRECTORY:\n");
        suggestions.append(baseDir.getAbsolutePath()).append("/Loaders/MelonLoader/\n\n");
        
        suggestions.append("⚠️ MAKE SURE TO:\n");
        suggestions.append("• Have stable internet connection (for automated)\n");
        suggestions.append("• Grant file manager permissions (for manual)\n");
        suggestions.append("• Use exact directory paths shown above\n");
        suggestions.append("• Restart TerrariaLoader after installation\n");
        
        return suggestions.toString();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/OfflineZipImporter.java
// File: OfflineZipImporter.java - Smart ZIP Import with Auto-Detection
// Path: /main/java/com/terrarialoader/util/OfflineZipImporter.java

package com.terrarialoader.util;

import android.content.Context;
import com.terrarialoader.loader.MelonLoaderManager;
import java.io.*;
import java.util.zip.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Smart offline ZIP importer that auto-detects NET8/NET35 and extracts to correct directories
 */
public class OfflineZipImporter {
    
    public static class ImportResult {
        public boolean success;
        public String message;
        public MelonLoaderManager.LoaderType detectedType;
        public int filesExtracted;
        public String errorDetails;
        
        public ImportResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    // File signatures for detection
    private static final String[] NET8_SIGNATURES = {
        "MelonLoader.deps.json",
        "MelonLoader.runtimeconfig.json", 
        "Il2CppInterop.Runtime.dll"
    };
    
    private static final String[] NET35_SIGNATURES = {
        "MelonLoader.dll",
        "0Harmony.dll"
    };
    
    private static final String[] CORE_FILES = {
        "MelonLoader.dll",
        "0Harmony.dll",
        "MonoMod.RuntimeDetour.dll",
        "MonoMod.Utils.dll"
    };
    
    /**
     * Import MelonLoader ZIP with auto-detection and smart extraction
     */
    public static ImportResult importMelonLoaderZip(Context context, android.net.Uri zipUri) {
        LogUtils.logUser("🔍 Starting smart ZIP import...");
        
        try {
            // Step 1: Analyze ZIP contents
            ZipAnalysis analysis = analyzeZipContents(context, zipUri);
            if (!analysis.isValid) {
                return new ImportResult(false, "Invalid MelonLoader ZIP file: " + analysis.error);
            }
            
            LogUtils.logUser("📋 Detected: " + analysis.detectedType.getDisplayName());
            LogUtils.logUser("📊 Found " + analysis.totalFiles + " files to extract");
            
            // Step 2: Prepare target directories
            String gamePackage = MelonLoaderManager.TERRARIA_PACKAGE;
            if (!PathManager.initializeGameDirectories(context, gamePackage)) {
                return new ImportResult(false, "Failed to create directory structure");
            }
            
            // Step 3: Extract files to appropriate locations
            int extractedCount = extractZipContents(context, zipUri, analysis, gamePackage);
            
            if (extractedCount > 0) {
                ImportResult result = new ImportResult(true, 
                    "Successfully imported " + analysis.detectedType.getDisplayName() + 
                    " (" + extractedCount + " files)");
                result.detectedType = analysis.detectedType;
                result.filesExtracted = extractedCount;
                
                LogUtils.logUser("✅ ZIP import completed: " + extractedCount + " files extracted");
                return result;
            } else {
                return new ImportResult(false, "No files were extracted from ZIP");
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("ZIP import error: " + e.getMessage());
            ImportResult result = new ImportResult(false, "Import failed: " + e.getMessage());
            result.errorDetails = e.toString();
            return result;
        }
    }
    
    /**
     * Analyze ZIP contents to detect loader type and validate files
     */
    private static ZipAnalysis analyzeZipContents(Context context, android.net.Uri zipUri) {
        ZipAnalysis analysis = new ZipAnalysis();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
            
            ZipEntry entry;
            Set<String> foundFiles = new HashSet<>();
            
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                
                String fileName = getCleanFileName(entry.getName());
                foundFiles.add(fileName.toLowerCase());
                analysis.totalFiles++;
                
                // Check for type indicators
                for (String signature : NET8_SIGNATURES) {
                    if (fileName.equalsIgnoreCase(signature)) {
                        analysis.hasNet8Indicators = true;
                        break;
                    }
                }
                
                for (String signature : NET35_SIGNATURES) {
                    if (fileName.equalsIgnoreCase(signature)) {
                        analysis.hasNet35Indicators = true;
                        break;
                    }
                }
                
                zis.closeEntry();
            }
            
            // Determine loader type
            if (analysis.hasNet8Indicators) {
                analysis.detectedType = MelonLoaderManager.LoaderType.MELONLOADER_NET8;
            } else if (analysis.hasNet35Indicators) {
                analysis.detectedType = MelonLoaderManager.LoaderType.MELONLOADER_NET35;
            } else {
                // Fallback: check for core files and default to NET8
                boolean hasCoreFiles = false;
                for (String coreFile : CORE_FILES) {
                    if (foundFiles.contains(coreFile.toLowerCase())) {
                        hasCoreFiles = true;
                        break;
                    }
                }
                
                if (hasCoreFiles) {
                    analysis.detectedType = MelonLoaderManager.LoaderType.MELONLOADER_NET8; // Default
                    LogUtils.logUser("⚠️ Auto-detected as MelonLoader (default)");
                } else {
                    analysis.isValid = false;
                    analysis.error = "No MelonLoader files detected in ZIP";
                    return analysis;
                }
            }
            
            // Validate we have minimum required files
            int coreFilesFound = 0;
            for (String coreFile : CORE_FILES) {
                if (foundFiles.contains(coreFile.toLowerCase())) {
                    coreFilesFound++;
                }
            }
            
            if (coreFilesFound < 2) { // At least 2 core files required
                analysis.isValid = false;
                analysis.error = "Insufficient MelonLoader core files (" + coreFilesFound + "/4)";
                return analysis;
            }
            
            analysis.isValid = true;
            LogUtils.logDebug("ZIP analysis complete - Type: " + analysis.detectedType.getDisplayName() + 
                            ", Files: " + analysis.totalFiles);
            
        } catch (Exception e) {
            analysis.isValid = false;
            analysis.error = "ZIP analysis failed: " + e.getMessage();
            LogUtils.logDebug("ZIP analysis error: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * Extract ZIP contents to appropriate directories based on detected type
     */
    private static int extractZipContents(Context context, android.net.Uri zipUri, ZipAnalysis analysis, String gamePackage) throws IOException {
        int extractedCount = 0;
        
        // Get target directories
        File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
        File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
        
        // Ensure directories exist
        PathManager.ensureDirectoryExists(net8Dir);
        PathManager.ensureDirectoryExists(net35Dir);
        PathManager.ensureDirectoryExists(depsDir);
        PathManager.ensureDirectoryExists(new File(depsDir, "SupportModules"));
        PathManager.ensureDirectoryExists(new File(depsDir, "CompatibilityLayers"));
        PathManager.ensureDirectoryExists(new File(depsDir, "Il2CppAssemblyGenerator"));
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
            
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                
                String fileName = getCleanFileName(entry.getName());
                File targetFile = determineTargetFile(fileName, analysis.detectedType, net8Dir, net35Dir, depsDir);
                
                if (targetFile != null) {
                    // Ensure parent directory exists
                    targetFile.getParentFile().mkdirs();
                    
                    // Extract file
                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    
                    extractedCount++;
                    LogUtils.logDebug("Extracted: " + fileName + " -> " + targetFile.getAbsolutePath());
                } else {
                    LogUtils.logDebug("Skipped: " + fileName + " (not needed)");
                }
                
                zis.closeEntry();
            }
        }
        
        return extractedCount;
    }
    
    /**
     * Determine target file location based on file type and loader type
     */
    private static File determineTargetFile(String fileName, MelonLoaderManager.LoaderType loaderType, 
                                          File net8Dir, File net35Dir, File depsDir) {
        String lowerName = fileName.toLowerCase();
        
        // Skip non-relevant files
        if (!isRelevantFile(fileName)) {
            return null;
        }
        
        // Core runtime files
        if (isCoreRuntimeFile(fileName)) {
            if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                return new File(net8Dir, fileName);
            } else {
                return new File(net35Dir, fileName);
            }
        }
        
        // Dependency files
        if (lowerName.contains("il2cpp") || lowerName.contains("interop")) {
            return new File(depsDir, "SupportModules/" + fileName);
        }
        
        if (lowerName.contains("unity") || lowerName.contains("assemblygenerator")) {
            return new File(depsDir, "Il2CppAssemblyGenerator/" + fileName);
        }
        
        if (lowerName.contains("compat")) {
            return new File(depsDir, "CompatibilityLayers/" + fileName);
        }
        
        // Default: place DLLs in appropriate runtime directory
        if (lowerName.endsWith(".dll")) {
            if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                return new File(net8Dir, fileName);
            } else {
                return new File(net35Dir, fileName);
            }
        }
        
        // Config files go to runtime directory
        if (lowerName.endsWith(".json") || lowerName.endsWith(".xml")) {
            if (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                return new File(net8Dir, fileName);
            } else {
                return new File(net35Dir, fileName);
            }
        }
        
        return null; // Skip unknown files
    }
    
    private static String getCleanFileName(String entryName) {
        // Remove directory paths and get just the filename
        String fileName = entryName;
        
        // Handle both forward and backward slashes
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash + 1);
        }
        
        return fileName;
    }
    
    private static boolean isRelevantFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".dll") || 
               lowerName.endsWith(".json") || 
               lowerName.endsWith(".xml") ||
               lowerName.endsWith(".pdb");
    }
    
    private static boolean isCoreRuntimeFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        for (String coreFile : CORE_FILES) {
            if (lowerName.equals(coreFile.toLowerCase())) {
                return true;
            }
        }
        return lowerName.contains("melonloader") || 
               lowerName.contains("runtimeconfig") ||
               lowerName.contains("deps.json");
    }
    
    /**
     * Helper class to store ZIP analysis results
     */
    private static class ZipAnalysis {
        boolean isValid = false;
        String error = "";
        MelonLoaderManager.LoaderType detectedType;
        boolean hasNet8Indicators = false;
        boolean hasNet35Indicators = false;
        int totalFiles = 0;
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/OnlineInstaller.java
// File: OnlineInstaller.java (Utility Class) - Complete Automated Installation System
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/OnlineInstaller.java

package com.terrarialoader.util;

import android.content.Context;
import com.terrarialoader.loader.MelonLoaderManager;
import java.io.File;

/**
 * OnlineInstaller - Complete automated installation system
 * Uses existing Downloader and PathManager for seamless MelonLoader/LemonLoader installation
 */
public class OnlineInstaller {
    
    // GitHub URLs for MelonLoader/LemonLoader releases
    public static final String MELONLOADER_URL = "https://github.com/LavaGang/MelonLoader/releases/download/0.6.5.1/melon_data.zip";
    public static final String LEMONLOADER_URL = "https://github.com/LemonLoader/MelonLoader/releases/download/0.6.5.1/melon_data.zip";
    
    // Installation result class
    public static class InstallationResult {
        public boolean success;
        public String message;
        public String errorDetails;
        public File installationPath;
        public int filesInstalled;
        public long totalSize;
        
        public InstallationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public InstallationResult(boolean success, String message, String errorDetails) {
            this.success = success;
            this.message = message;
            this.errorDetails = errorDetails;
        }
    }
    
    /**
     * Complete automated installation of MelonLoader
     * Downloads melon_data.zip and extracts to proper directory structure
     * 
     * @param context Application context
     * @param gamePackage Target game package (e.g., "com.and.games505.TerrariaPaid")
     * @param loaderType Type of loader to install (NET8 or NET35)
     * @return InstallationResult with success status and details
     */
    public static InstallationResult installMelonLoaderOnline(Context context, String gamePackage, MelonLoaderManager.LoaderType loaderType) {
        LogUtils.logUser("🚀 Starting automated MelonLoader installation...");
        LogUtils.logUser("Target: " + gamePackage + " (" + loaderType.getDisplayName() + ")");
        
        // Validate parameters
        if (context == null) {
            return new InstallationResult(false, "Context is null", "Application context is required");
        }
        
        if (gamePackage == null || gamePackage.trim().isEmpty()) {
            return new InstallationResult(false, "Invalid game package", "Game package cannot be null or empty");
        }
        
        if (loaderType == null) {
            return new InstallationResult(false, "Invalid loader type", "Loader type cannot be null");
        }
        
        try {
            // Step 1: Initialize directory structure
            LogUtils.logUser("📁 Step 1: Initializing directory structure...");
            if (!PathManager.initializeGameDirectories(context, gamePackage)) {
                return new InstallationResult(false, "Failed to create directory structure", "Could not create required directories");
            }
            LogUtils.logUser("✅ Directory structure ready");
            
            // Step 2: Determine download URL and target directory
            String downloadUrl = (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) ? MELONLOADER_URL : LEMONLOADER_URL;
            File targetDirectory = PathManager.getMelonLoaderDir(context, gamePackage);
            
            if (targetDirectory == null) {
                return new InstallationResult(false, "Cannot determine installation directory", "PathManager returned null directory");
            }
            
            LogUtils.logUser("📂 Installation directory: " + targetDirectory.getAbsolutePath());
            LogUtils.logUser("🌐 Download URL: " + downloadUrl);
            
            // Step 3: Download and extract
            LogUtils.logUser("⬇️ Step 2: Downloading and extracting MelonLoader files...");
            boolean downloadSuccess = Downloader.downloadAndExtractZip(downloadUrl, targetDirectory);
            
            if (!downloadSuccess) {
                return new InstallationResult(false, "Download or extraction failed", "Failed to download from: " + downloadUrl);
            }
            
            // Step 4: Organize files according to MelonLoader structure
            LogUtils.logUser("📋 Step 3: Organizing files into proper structure...");
            InstallationResult organizationResult = organizeExtractedFiles(context, gamePackage, targetDirectory, loaderType);
            
            if (!organizationResult.success) {
                return organizationResult;
            }
            
            // Step 5: Validate installation
            LogUtils.logUser("🔍 Step 4: Validating installation...");
            boolean validationSuccess = MelonLoaderManager.validateLoaderInstallation(context, gamePackage).isValid;
            
            if (!validationSuccess) {
                LogUtils.logUser("⚠️ Installation validation failed, attempting repair...");
                if (MelonLoaderManager.attemptRepair(context, gamePackage)) {
                    LogUtils.logUser("✅ Repair successful");
                    validationSuccess = true;
                } else {
                    return new InstallationResult(false, "Installation validation failed", "Files were downloaded but validation failed");
                }
            }
            
            // Step 6: Create final result
            InstallationResult result = new InstallationResult(true, "✅ " + loaderType.getDisplayName() + " installed successfully!");
            result.installationPath = targetDirectory;
            result.filesInstalled = countInstalledFiles(targetDirectory);
            result.totalSize = calculateDirectorySize(targetDirectory);
            
            LogUtils.logUser("🎉 Installation completed successfully!");
            LogUtils.logUser("📊 Files installed: " + result.filesInstalled);
            LogUtils.logUser("💾 Total size: " + FileUtils.formatFileSize(result.totalSize));
            LogUtils.logUser("📍 Installation path: " + result.installationPath.getAbsolutePath());
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = "Unexpected error during installation: " + e.getMessage();
            LogUtils.logDebug(errorMsg);
            e.printStackTrace();
            return new InstallationResult(false, "Installation failed with exception", errorMsg);
        }
    }
    
    /**
     * Organize extracted files into proper MelonLoader directory structure
     * Based on the MelonLoader_File_List.txt structure
     */
    private static InstallationResult organizeExtractedFiles(Context context, String gamePackage, File extractedDir, MelonLoaderManager.LoaderType loaderType) {
        try {
            LogUtils.logUser("🗂️ Organizing extracted files...");
            
            // Create target directories based on loader type
            File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
            File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
            File dependenciesDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
            
            // Ensure directories exist
            PathManager.ensureDirectoryExists(net8Dir);
            PathManager.ensureDirectoryExists(net35Dir);
            PathManager.ensureDirectoryExists(dependenciesDir);
            
            int organizedFiles = 0;
            
            // Process extracted files
            File[] extractedFiles = extractedDir.listFiles();
            if (extractedFiles != null) {
                for (File file : extractedFiles) {
                    if (organizeFile(file, net8Dir, net35Dir, dependenciesDir, loaderType)) {
                        organizedFiles++;
                    }
                }
            }
            
            LogUtils.logUser("📁 Organized " + organizedFiles + " files into proper structure");
            
            // Create additional required directories
            createAdditionalDirectories(context, gamePackage);
            
            InstallationResult result = new InstallationResult(true, "File organization completed");
            result.filesInstalled = organizedFiles;
            return result;
            
        } catch (Exception e) {
            return new InstallationResult(false, "File organization failed", e.getMessage());
        }
    }
    
    /**
     * Organize individual file based on its type and target loader
     */
    private static boolean organizeFile(File file, File net8Dir, File net35Dir, File dependenciesDir, MelonLoaderManager.LoaderType loaderType) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            String fileName = file.getName().toLowerCase();
            File targetDir = null;
            
            // Determine target directory based on file type and loader type
            if (fileName.contains("net8") && loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) {
                targetDir = net8Dir;
            } else if (fileName.contains("net35") && loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET35) {
                targetDir = net35Dir;
            } else if (fileName.contains("dependencies") || fileName.contains("supportmodules") || fileName.contains("il2cpp")) {
                targetDir = dependenciesDir;
            } else if (fileName.endsWith(".dll") || fileName.endsWith(".xml") || fileName.endsWith(".pdb")) {
                // Core files go to appropriate runtime directory
                targetDir = (loaderType == MelonLoaderManager.LoaderType.MELONLOADER_NET8) ? net8Dir : net35Dir;
            }
            
            if (targetDir != null) {
                File targetFile = new File(targetDir, file.getName());
                if (file.renameTo(targetFile)) {
                    LogUtils.logDebug("Moved: " + file.getName() + " -> " + targetDir.getName());
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LogUtils.logDebug("Error organizing file " + file.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create additional required directories for MelonLoader
     */
    private static void createAdditionalDirectories(Context context, String gamePackage) {
        try {
            // Create subdirectories in Dependencies
            File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
            
            String[] subDirs = {
                "SupportModules",
                "CompatibilityLayers", 
                "Il2CppAssemblyGenerator",
                "Il2CppAssemblyGenerator/Cpp2IL",
                "Il2CppAssemblyGenerator/Cpp2IL/cpp2il_out",
                "Il2CppAssemblyGenerator/Il2CppInterop",
                "Il2CppAssemblyGenerator/Il2CppInterop/Il2CppAssemblies",
                "Il2CppAssemblyGenerator/UnityDependencies",
                "Il2CppAssemblyGenerator/runtimes",
                "Il2CppAssemblyGenerator/runtimes/linux-arm64/native",
                "Il2CppAssemblyGenerator/runtimes/linux-arm/native",
                "Il2CppAssemblyGenerator/runtimes/linux-x64/native",
                "Il2CppAssemblyGenerator/runtimes/linux-x86/native"
            };
            
            for (String subDir : subDirs) {
                File dir = new File(depsDir, subDir);
                PathManager.ensureDirectoryExists(dir);
            }
            
            LogUtils.logDebug("Created additional MelonLoader directories");
            
        } catch (Exception e) {
            LogUtils.logDebug("Error creating additional directories: " + e.getMessage());
        }
    }
    
    /**
     * Count files in a directory recursively
     */
    private static int countInstalledFiles(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countInstalledFiles(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Calculate total size of directory
     */
    private static long calculateDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
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
            size = directory.length();
        }
        return size;
    }
    
    /**
     * Convenience method for installing MelonLoader (NET8)
     */
    public static InstallationResult installMelonLoader(Context context, String gamePackage) {
        return installMelonLoaderOnline(context, gamePackage, MelonLoaderManager.LoaderType.MELONLOADER_NET8);
    }
    
    /**
     * Convenience method for installing LemonLoader (NET35)  
     */
    public static InstallationResult installLemonLoader(Context context, String gamePackage) {
        return installMelonLoaderOnline(context, gamePackage, MelonLoaderManager.LoaderType.MELONLOADER_NET35);
    }
    
    /**
     * Install for Terraria specifically
     */
    public static InstallationResult installForTerraria(Context context, MelonLoaderManager.LoaderType loaderType) {
        return installMelonLoaderOnline(context, MelonLoaderManager.TERRARIA_PACKAGE, loaderType);
    }
    
    /**
     * Check if online installation is possible (internet connectivity)
     */
    public static boolean isOnlineInstallationAvailable() {
        try {
            // Simple connectivity check
            java.net.URL url = new java.net.URL("https://github.com");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            LogUtils.logDebug("Online installation not available: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get installation progress callback interface
     */
    public interface InstallationProgressCallback {
        void onProgress(String message, int percentage);
        void onComplete(InstallationResult result);
        void onError(String error);
    }
    
    /**
     * Asynchronous installation with progress callback
     */
    public static void installMelonLoaderAsync(Context context, String gamePackage, MelonLoaderManager.LoaderType loaderType, InstallationProgressCallback callback) {
        new Thread(() -> {
            try {
                if (callback != null) {
                    callback.onProgress("Starting installation...", 0);
                }
                
                InstallationResult result = installMelonLoaderOnline(context, gamePackage, loaderType);
                
                if (callback != null) {
                    if (result.success) {
                        callback.onProgress("Installation completed!", 100);
                        callback.onComplete(result);
                    } else {
                        callback.onError(result.message + (result.errorDetails != null ? ": " + result.errorDetails : ""));
                    }
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Installation failed: " + e.getMessage());
                }
            }
        }).start();
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/java/com/terrarialoader/util/PathManager.java
// File: PathManager.java (FIXED Part 1) - Centralized Path Management
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/main/java/com/terrarialoader/util/PathManager.java

package com.terrarialoader.util;

import android.content.Context;
import java.io.File;

/**
 * Centralized path management for TerrariaLoader
 * All file operations should use these standardized paths
 * FIXED: Added proper app logs directory support and correct structure
 */
public class PathManager {
    
    // Base directory: /storage/emulated/0/Android/data/com.terrarialoader/files
    private static File getAppDataDirectory(Context context) {
        return context.getExternalFilesDir(null);
    }
    
    // === TERRARIA LOADER STRUCTURE ===
    
    /**
     * Base TerrariaLoader directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader
     */
    public static File getTerrariaLoaderBaseDir(Context context) {
        return new File(getAppDataDirectory(context), "TerrariaLoader");
    }
    
    /**
     * Game-specific base directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}
     */
    public static File getGameBaseDir(Context context, String gamePackage) {
        return new File(getTerrariaLoaderBaseDir(context), gamePackage);
    }
    
    /**
     * Default Terraria directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/com.and.games505.TerrariaPaid
     */
    public static File getTerrariaBaseDir(Context context) {
        return getGameBaseDir(context, "com.and.games505.TerrariaPaid");
    }
    
    // === MOD DIRECTORIES ===
    
    /**
     * DLL Mods directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Mods/DLL
     */
    public static File getDllModsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Mods/DLL");
    }
    
    /**
     * DEX Mods directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Mods/DEX
     */
    public static File getDexModsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Mods/DEX");
    }
    
    /**
     * Legacy mods directory (for backward compatibility)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/mods
     */
    public static File getLegacyModsDir(Context context) {
        return new File(getAppDataDirectory(context), "mods");
    }
    
    // === MELONLOADER STRUCTURE ===
    
    /**
     * MelonLoader base directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Loaders/MelonLoader
     */
    public static File getMelonLoaderDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Loaders/MelonLoader");
    }
    
    /**
     * MelonLoader NET8 runtime directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Loaders/MelonLoader/net8
     */
    public static File getMelonLoaderNet8Dir(Context context, String gamePackage) {
        return new File(getMelonLoaderDir(context, gamePackage), "net8");
    }
    
    /**
     * MelonLoader NET35 runtime directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Loaders/MelonLoader/net35
     */
    public static File getMelonLoaderNet35Dir(Context context, String gamePackage) {
        return new File(getMelonLoaderDir(context, gamePackage), "net35");
    }
    
    /**
     * MelonLoader Dependencies directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Loaders/MelonLoader/Dependencies
     */
    public static File getMelonLoaderDependenciesDir(Context context, String gamePackage) {
        return new File(getMelonLoaderDir(context, gamePackage), "Dependencies");
    }
    
    // === PLUGINS AND USERLIBS (FIXED: Now at game root level) ===
    
    /**
     * FIXED: Plugins directory (at game root level, not inside MelonLoader)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Plugins
     */
    public static File getPluginsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Plugins");
    }
    
    /**
     * FIXED: UserLibs directory (at game root level, not inside MelonLoader)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/UserLibs
     */
    public static File getUserLibsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "UserLibs");
    }
    
    // === LOG DIRECTORIES ===
    
    /**
     * FIXED: Game logs directory (MelonLoader logs)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Logs
     */
    public static File getLogsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Logs");
    }
    
    /**
     * FIXED: App logs directory (TerrariaLoader app logs)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/AppLogs
     */
    public static File getAppLogsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "AppLogs");
    }
    
    /**
     * Legacy app logs directory (for backward compatibility)
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/logs
     */
    public static File getLegacyAppLogsDir(Context context) {
        return new File(getAppDataDirectory(context), "logs");
    }
    
    /**
     * Exports directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/exports
     */
    public static File getExportsDir(Context context) {
        return new File(getAppDataDirectory(context), "exports");
    }
    
    // === BACKUP DIRECTORIES ===
    
    /**
     * Backups directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Backups
     */
    public static File getBackupsDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Backups");
    }
    
    // === CONFIG DIRECTORIES ===
    
    /**
     * Config directory
     * @return /storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/{gamePackage}/Config
     */
    public static File getConfigDir(Context context, String gamePackage) {
        return new File(getGameBaseDir(context, gamePackage), "Config");
    }
    
    // === UTILITY METHODS ===
    
    /**
     * Ensure a directory exists, creating it if necessary
     * @param directory The directory to ensure exists
     * @return true if directory exists or was created successfully
     */
    public static boolean ensureDirectoryExists(File directory) {
        if (directory == null) {
            LogUtils.logDebug("Directory is null");
            return false;
        }
        
        if (directory.exists()) {
            return directory.isDirectory();
        }
        
        try {
            boolean created = directory.mkdirs();
            if (created) {
                LogUtils.logDebug("Created directory: " + directory.getAbsolutePath());
            } else {
                LogUtils.logDebug("Failed to create directory: " + directory.getAbsolutePath());
            }
            return created;
        } catch (Exception e) {
            LogUtils.logDebug("Error creating directory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * FIXED: Initialize all required directories for a game package
     * @param context Application context
     * @param gamePackage Game package name
     * @return true if all directories were created successfully
     */
    public static boolean initializeGameDirectories(Context context, String gamePackage) {
        LogUtils.logUser("Initializing directory structure for: " + gamePackage);
        
        File[] requiredDirs = {
            getGameBaseDir(context, gamePackage),
            getDllModsDir(context, gamePackage),
            getDexModsDir(context, gamePackage),
            getMelonLoaderDir(context, gamePackage),
            getMelonLoaderNet8Dir(context, gamePackage),
            getMelonLoaderNet35Dir(context, gamePackage),
            getMelonLoaderDependenciesDir(context, gamePackage),
            getPluginsDir(context, gamePackage),        // FIXED: Added Plugins
            getUserLibsDir(context, gamePackage),       // FIXED: Added UserLibs
            getLogsDir(context, gamePackage),           // Game logs
            getAppLogsDir(context, gamePackage),        // FIXED: Added App logs
            getBackupsDir(context, gamePackage),
            getConfigDir(context, gamePackage)
        };
        
        boolean allSuccess = true;
        int createdCount = 0;
        
        for (File dir : requiredDirs) {
            if (!dir.exists()) {
                if (ensureDirectoryExists(dir)) {
                    createdCount++;
                } else {
                    allSuccess = false;
                    LogUtils.logDebug("Failed to create: " + dir.getAbsolutePath());
                }
            }
        }
        
        LogUtils.logUser("Directory initialization complete: " + createdCount + " directories created");
        
        // Create README files
        if (allSuccess) {
            createReadmeFiles(context, gamePackage);
        }
        
        return allSuccess;
    }
    
    /**
     * FIXED: Create helpful README files in directories
     */
    private static void createReadmeFiles(Context context, String gamePackage) {
        try {
            // DLL Mods README
            File dllReadme = new File(getDllModsDir(context, gamePackage), "README.txt");
            if (!dllReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(dllReadme)) {
                    writer.write("=== TerrariaLoader - DLL Mods ===\n\n");
                    writer.write("Place your .dll mod files here.\n");
                    writer.write("Requires MelonLoader to be installed.\n\n");
                    writer.write("Supported formats:\n");
                    writer.write("• .dll files (enabled)\n");
                    writer.write("• .dll.disabled files (disabled)\n\n");
                    writer.write("Path: " + dllReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
            // DEX Mods README
            File dexReadme = new File(getDexModsDir(context, gamePackage), "README.txt");
            if (!dexReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(dexReadme)) {
                    writer.write("=== TerrariaLoader - DEX/JAR Mods ===\n\n");
                    writer.write("Place your .dex and .jar mod files here.\n");
                    writer.write("These are Java-based mods for Android.\n\n");
                    writer.write("Supported formats:\n");
                    writer.write("• .dex files (enabled)\n");
                    writer.write("• .jar files (enabled)\n");
                    writer.write("• .dex.disabled files (disabled)\n");
                    writer.write("• .jar.disabled files (disabled)\n\n");
                    writer.write("Path: " + dexReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
            // FIXED: Plugins README
            File pluginsReadme = new File(getPluginsDir(context, gamePackage), "README.txt");
            if (!pluginsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(pluginsReadme)) {
                    writer.write("=== MelonLoader Plugins Directory ===\n\n");
                    writer.write("This directory contains MelonLoader plugins.\n");
                    writer.write("Plugins extend MelonLoader functionality.\n\n");
                    writer.write("Files you might see here:\n");
                    writer.write("• .dll plugin files\n");
                    writer.write("• Plugin configuration files\n\n");
                    writer.write("Path: " + pluginsReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
            // FIXED: UserLibs README
            File userlibsReadme = new File(getUserLibsDir(context, gamePackage), "README.txt");
            if (!userlibsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(userlibsReadme)) {
                    writer.write("=== MelonLoader UserLibs Directory ===\n\n");
                    writer.write("This directory contains user libraries.\n");
                    writer.write("Libraries that mods depend on go here.\n\n");
                    writer.write("Files you might see here:\n");
                    writer.write("• .dll library files\n");
                    writer.write("• Shared mod dependencies\n\n");
                    writer.write("Path: " + userlibsReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
            // FIXED: Game logs README
            File gameLogsReadme = new File(getLogsDir(context, gamePackage), "README.txt");
            if (!gameLogsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(gameLogsReadme)) {
                    writer.write("=== MelonLoader Game Logs ===\n\n");
                    writer.write("This directory contains logs from MelonLoader and mods.\n");
                    writer.write("These are generated when running the patched game.\n\n");
                    writer.write("Log files follow rotation:\n");
                    writer.write("• Log.txt (current log)\n");
                    writer.write("• Log1.txt to Log5.txt (previous logs)\n");
                    writer.write("• Oldest logs are automatically deleted\n\n");
                    writer.write("Path: " + gameLogsReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
            // FIXED: App logs README
            File appLogsReadme = new File(getAppLogsDir(context, gamePackage), "README.txt");
            if (!appLogsReadme.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(appLogsReadme)) {
                    writer.write("=== TerrariaLoader App Logs ===\n\n");
                    writer.write("This directory contains logs from TerrariaLoader app.\n");
                    writer.write("These are generated when using this app.\n\n");
                    writer.write("Log files follow rotation:\n");
                    writer.write("• AppLog.txt (current log)\n");
                    writer.write("• AppLog1.txt to AppLog5.txt (previous logs)\n");
                    writer.write("• Oldest logs are automatically deleted\n\n");
                    writer.write("Path: " + appLogsReadme.getParentFile().getAbsolutePath() + "\n");
                }
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Error creating README files: " + e.getMessage());
        }
    }
    
    /**
     * FIXED: Get standardized path string for logging/display
     */
    public static String getPathInfo(Context context, String gamePackage) {
        StringBuilder info = new StringBuilder();
        info.append("=== TerrariaLoader Directory Structure ===\n");
        info.append("Base: ").append(getTerrariaLoaderBaseDir(context).getAbsolutePath()).append("\n");
        info.append("Game: ").append(getGameBaseDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("DLL Mods: ").append(getDllModsDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("DEX Mods: ").append(getDexModsDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("MelonLoader: ").append(getMelonLoaderDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("Plugins: ").append(getPluginsDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("UserLibs: ").append(getUserLibsDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("Game Logs: ").append(getLogsDir(context, gamePackage).getAbsolutePath()).append("\n");
        info.append("App Logs: ").append(getAppLogsDir(context, gamePackage).getAbsolutePath()).append("\n");
        return info.toString();
    }
    
    /**
     * Check if legacy structure exists and needs migration
     */
    public static boolean needsMigration(Context context) {
        File legacyMods = getLegacyModsDir(context);
        File legacyAppLogs = getLegacyAppLogsDir(context);
        File newStructure = getTerrariaBaseDir(context);
        
        return ((legacyMods.exists() && legacyMods.listFiles() != null && legacyMods.listFiles().length > 0) ||
                (legacyAppLogs.exists() && legacyAppLogs.listFiles() != null && legacyAppLogs.listFiles().length > 0)) && 
               !newStructure.exists();
    }
    
    /**
     * FIXED: Migrate from legacy structure to new structure
     */
    public static boolean migrateLegacyStructure(Context context) {
        if (!needsMigration(context)) {
            return true;
        }
        
        LogUtils.logUser("Migrating from legacy directory structure...");
        
        try {
            String gamePackage = "com.and.games505.TerrariaPaid";
            
            // Initialize new structure
            if (!initializeGameDirectories(context, gamePackage)) {
                LogUtils.logDebug("Failed to initialize new directory structure");
                return false;
            }
            
            // Migrate mods
            File legacyModsDir = getLegacyModsDir(context);
            File newDexMods = getDexModsDir(context, gamePackage);
            
            if (legacyModsDir.exists()) {
                File[] modFiles = legacyModsDir.listFiles();
                if (modFiles != null) {
                    int migratedCount = 0;
                    for (File modFile : modFiles) {
                        if (modFile.isFile()) {
                            File newLocation = new File(newDexMods, modFile.getName());
                            if (modFile.renameTo(newLocation)) {
                                migratedCount++;
                            }
                        }
                    }
                    LogUtils.logUser("Migrated " + migratedCount + " mod files to new structure");
                }
            }
            
            // Migrate legacy app logs
            File legacyAppLogs = getLegacyAppLogsDir(context);
            File newAppLogs = getAppLogsDir(context, gamePackage);
            
            if (legacyAppLogs.exists()) {
                File[] logFiles = legacyAppLogs.listFiles((dir, name) -> 
                    name.endsWith(".txt") || name.startsWith("auto_save_"));
                
                if (logFiles != null && logFiles.length > 0) {
                    LogUtils.logUser("Migrating " + logFiles.length + " legacy log files...");
                    
                    if (!newAppLogs.exists()) {
                        newAppLogs.mkdirs();
                    }
                    
                    int migratedLogCount = 0;
                    for (File logFile : logFiles) {
                        // Rename to new format
                        String newName = "AppLog" + (migratedLogCount + 1) + ".txt";
                        File newLocation = new File(newAppLogs, newName);
                        
                        if (logFile.renameTo(newLocation)) {
                            migratedLogCount++;
                        }
                    }
                    LogUtils.logUser("Migrated " + migratedLogCount + " log files to new structure");
                }
            }
            
            LogUtils.logUser("✅ Migration completed successfully");
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("Migration failed: " + e.getMessage());
            return false;
        }
    }
}

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/drawable/gradient_background_135.xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="135"
        android:startColor="#E8F5E8"
        android:endColor="#F1F8E9"
        android:type="linear" />
</shape>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/drawable/ic_arrow_back.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="@android:color/black"
      android:pathData="M20,11L7.83,11l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13L20,13z"/>
</vector>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/drawable/ic_launcher_background.xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#00000000"
        android:pathData="M9,0L9,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,0L19,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M29,0L29,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M39,0L39,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M49,0L49,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M59,0L59,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M69,0L69,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M79,0L79,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M89,0L89,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M99,0L99,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,9L108,9"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,19L108,19"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,29L108,29"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,39L108,39"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,49L108,49"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,59L108,59"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,69L108,69"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,79L108,79"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,89L108,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,99L108,99"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,29L89,29"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,39L89,39"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,49L89,49"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,59L89,59"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,69L89,69"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,79L89,79"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M29,19L29,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M39,19L39,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M49,19L49,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M59,19L59,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M69,19L69,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M79,19L79,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
</vector>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/drawable-v24/ic_launcher_foreground.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M31,63.928c0,0 6.4,-11 12.1,-13.1c7.2,-2.6 26,-1.4 26,-1.4l38.1,38.1L107,108.928l-32,-1L31,63.928z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:endX="85.84757"
                android:endY="92.4963"
                android:startX="42.9492"
                android:startY="49.59793"
                android:type="linear">
                <item
                    android:color="#44000000"
                    android:offset="0.0" />
                <item
                    android:color="#00000000"
                    android:offset="1.0" />
            </gradient>
        </aapt:attr>
    </path>
    <path
        android:fillColor="#FFFFFF"
        android:fillType="nonZero"
        android:pathData="M65.3,45.828l3.8,-6.6c0.2,-0.4 0.1,-0.9 -0.3,-1.1c-0.4,-0.2 -0.9,-0.1 -1.1,0.3l-3.9,6.7c-6.3,-2.8 -13.4,-2.8 -19.7,0l-3.9,-6.7c-0.2,-0.4 -0.7,-0.5 -1.1,-0.3C38.8,38.328 38.7,38.828 38.9,39.228l3.8,6.6C36.2,49.428 31.7,56.028 31,63.928h46C76.3,56.028 71.8,49.428 65.3,45.828zM43.4,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2c-0.3,-0.7 -0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C45.3,56.528 44.5,57.328 43.4,57.328L43.4,57.328zM64.6,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2s-0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C66.5,56.528 65.6,57.328 64.6,57.328L64.6,57.328z"
        android:strokeWidth="1"
        android:strokeColor="#00000000" />
</vector>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_about.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Terraria Loader"
            android:textStyle="bold"
            android:textSize="20sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Author: Jonie"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Description:\n\nThis app lets you inject custom loaders into Terraria APKs for modding purposes. It also provides tools to manage logs and exported APKs.\n\nUse responsibly and only with legal copies of the game."
            android:textSize="14sp"
            android:lineSpacingExtra="4dp" />
    </LinearLayout>
</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_dll_mod.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Header Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="DLL Mod Manager"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="16dp" />

        <!-- Status Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#F5F5F5"
            android:padding="12dp"
            android:layout_marginBottom="16dp">

            <TextView
                android:id="@+id/statusText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="DLL Mods: 0 enabled, 0 disabled, 0 total"
                android:textSize="16sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <TextView
                android:id="@+id/loaderStatusText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="❌ No loader installed - DLL mods will not work"
                android:textSize="14sp" />

        </LinearLayout>

        <!-- Loader Installation Section -->
        <LinearLayout
            android:id="@+id/loaderInfoSection"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#E8F5E8"
            android:padding="12dp"
            android:layout_marginBottom="16dp"
            android:visibility="gone">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Loader Installation"
                android:textSize="16sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <Button
                android:id="@+id/installLoaderBtn"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Install Loader"
                android:layout_marginBottom="8dp" />

            <Button
                android:id="@+id/selectApkBtn"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Select Terraria APK"
                android:layout_marginBottom="8dp" />

        </LinearLayout>

        <!-- Mod Management Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="DLL Mod Management"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/installDllBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Install DLL Mod"
            android:layout_marginBottom="16dp" />

        <!-- Mod List -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/dllModRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="300dp"
            android:background="#F9F9F9"
            android:padding="8dp"
            android:layout_marginBottom="16dp" />

        <!-- Action Buttons -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center"
            android:layout_marginTop="16dp">

            <Button
                android:id="@+id/refreshBtn"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Refresh"
                android:layout_marginEnd="8dp" />

            <Button
                android:id="@+id/viewLogsBtn"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="View Logs"
                android:layout_marginStart="8dp" />

        </LinearLayout>

        <!-- Information Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="DLL mods require MelonLoader or LemonLoader to be installed. Use 'Install Loader' to set up the required components."
            android:textSize="12sp"
            android:textColor="#666666"
            android:layout_marginTop="16dp"
            android:gravity="center" />

    </LinearLayout>

</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_instructions.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.InstructionsActivity">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <TextView
            android:id="@+id/tv_instructions_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="Manual Installation Instructions"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:layout_marginTop="16dp" />

        <TextView
            android:id="@+id/tv_instructions"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textSize="16sp"
            android:textIsSelectable="true"
            app:layout_constraintTop_toBottomOf="@id/tv_instructions_title"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            tools:text="Detailed instructions will appear here..." />

    </androidx.constraintlayout.widget.ConstraintLayout>
</ScrollView>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_log.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/log_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <ScrollView
        android:id="@+id/log_scroll"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true">

        <TextView
            android:id="@+id/log_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textIsSelectable="true"
            android:textAppearance="?android:textAppearanceSmall"
            android:textColor="#FFFFFF"
            android:background="#222222"
            android:padding="10dp" />
    </ScrollView>

    <Button
        android:id="@+id/export_logs_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Export Logs" />
</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_log_viewer_enhanced.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F8F9FA">

    <!-- Header Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#FFFFFF"
        android:elevation="4dp"
        android:padding="16dp">

        <!-- Title -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="📋 Advanced Log Viewer"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#2E7D32"
            android:gravity="center"
            android:layout_marginBottom="8dp" />

        <!-- Search Bar -->
        <EditText
            android:id="@+id/searchEditText"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:hint="🔍 Search logs (supports regex)..."
            android:inputType="text"
            android:background="@android:drawable/editbox_background"
            android:padding="12dp"
            android:textSize="14sp"
            android:layout_marginBottom="12dp" />

        <!-- Filter Controls Row 1 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:layout_marginEnd="8dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Category"
                    android:textSize="12sp"
                    android:textColor="#666666" />

                <Spinner
                    android:id="@+id/filterTypeSpinner"
                    android:layout_width="match_parent"
                    android:layout_height="36dp" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:layout_marginStart="8dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Time Range"
                    android:textSize="12sp"
                    android:textColor="#666666" />

                <Spinner
                    android:id="@+id/timeRangeSpinner"
                    android:layout_width="match_parent"
                    android:layout_height="36dp" />

            </LinearLayout>

        </LinearLayout>

        <!-- Filter Controls Row 2 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginEnd="8dp">

                <Switch
                    android:id="@+id/regexModeSwitch"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Regex Mode"
                    android:textSize="12sp"
                    android:textColor="#666666" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginStart="8dp">

                <Switch
                    android:id="@+id/highlightModeSwitch"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:checked="true" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Highlight"
                    android:textSize="12sp"
                    android:textColor="#666666" />

            </LinearLayout>

        </LinearLayout>

        <!-- Status Row -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp">

            <TextView
                android:id="@+id/logCountText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Showing 0 of 0 entries"
                android:textSize="12sp"
                android:textColor="#666666" />

            <Button
                android:id="@+id/clearFiltersBtn"
                android:layout_width="wrap_content"
                android:layout_height="32dp"
                android:text="Clear Filters"
                android:textSize="10sp"
                android:background="#FF9800"
                android:textColor="@android:color/white"
                android:minWidth="80dp" />

        </LinearLayout>

        <TextView
            android:id="@+id/filterStatusText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="No filters active"
            android:textSize="11sp"
            android:textColor="#999999"
            android:gravity="center" />

        <!-- Advanced Filters Toggle -->
        <LinearLayout
            android:id="@+id/advancedFiltersToggle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center"
            android:background="?android:attr/selectableItemBackground"
            android:padding="8dp"
            android:layout_marginTop="8dp">

            <TextView
                android:id="@+id/advancedFiltersToggleText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="▶ Show Advanced Filters"
                android:textSize="12sp"
                android:textColor="#2196F3"
                android:textStyle="bold" />

        </LinearLayout>

        <!-- Advanced Filters Section (Initially Hidden) -->
        <LinearLayout
            android:id="@+id/advancedFiltersLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#F5F5F5"
            android:padding="12dp"
            android:layout_marginTop="8dp"
            android:visibility="gone">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Advanced Filters"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="#333333"
                android:layout_marginBottom="8dp" />

            <!-- Additional filter controls can be added here -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Coming soon: Level filters, thread filters, and more..."
                android:textSize="12sp"
                android:textColor="#666666"
                android:textStyle="italic" />

        </LinearLayout>

    </LinearLayout>

    <!-- Main Log Display -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/logRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="#FFFFFF"
        android:scrollbars="vertical"
        android:padding="8dp" />

    <!-- Action Buttons Footer -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#FFFFFF"
        android:elevation="4dp"
        android:padding="12dp">

        <!-- Primary Actions Row -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp">

            <Button
                android:id="@+id/exportDiagnosticBtn"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="48dp"
                android:text="📦 Export Bundle"
                android:textSize="14sp"
                android:background="#4CAF50"
                android:textColor="@android:color/white"
                android:layout_marginEnd="6dp" />

            <Button
                android:id="@+id/refreshBtn"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="48dp"
                android:text="🔄 Refresh"
                android:textSize="14sp"
                android:background="#2196F3"
                android:textColor="@android:color/white"
                android:layout_marginStart="6dp" />

        </LinearLayout>

        <!-- Status Text -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="💡 Use filters above to search logs. Export bundle includes all diagnostic data."
            android:textSize="11sp"
            android:textColor="#666666"
            android:gravity="center"
            android:lineSpacingExtra="2dp" />

    </LinearLayout>

</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp">

    <Button
        android:id="@+id/universal_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Universal"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/specific_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Specific Version" />
</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_mod_list.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginBottom="16dp">

        <ImageButton
            android:id="@+id/backButton"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_alignParentStart="true"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@drawable/ic_arrow_back"
            android:contentDescription="Back"
            android:tint="@android:color/black" />

        <TextView
            android:id="@+id/modCountTextView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="Total Mods: 0 (Enabled: 0)"
            android:textSize="16sp"
            android:textStyle="bold" />

    </RelativeLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewMods"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:scrollbars="vertical" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginTop="16dp">

        <Button
            android:id="@+id/addModButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Add Mod"
            android:layout_marginEnd="8dp" />

        <Button
            android:id="@+id/refreshModsButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Refresh Mods" />
    </LinearLayout>

</LinearLayout>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_mod_management.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        android:background="#F8F9FA">

        <!-- Header Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:background="#FFFFFF"
            android:padding="20dp"
            android:layout_marginBottom="16dp"
            android:elevation="2dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="🎮 Mod Management"
                android:textSize="28sp"
                android:textStyle="bold"
                android:textColor="#2E7D32"
                android:gravity="center"
                android:layout_marginBottom="8dp" />

            <!-- Status Section -->
            <TextView
                android:id="@+id/statusText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="📊 Loading mod statistics..."
                android:textSize="14sp"
                android:textColor="#4CAF50"
                android:gravity="center"
                android:padding="8dp"
                android:background="#E8F5E8"
                android:layout_marginBottom="8dp" />

            <!-- Loader Status -->
            <TextView
                android:id="@+id/loaderStatusText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Checking loader status..."
                android:textSize="12sp"
                android:gravity="center"
                android:padding="6dp" />

        </LinearLayout>

        <!-- Loader Info Section (shown when loader is installed) -->
        <LinearLayout
            android:id="@+id/loaderInfoSection"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#E3F2FD"
            android:padding="16dp"
            android:layout_marginBottom="16dp"
            android:visibility="gone"
            android:elevation="2dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="ℹ️ Loader Information"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="#1565C0"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="• DLL mods will be loaded by MelonLoader\n• DEX/JAR mods are loaded directly by TerrariaLoader\n• Enable/disable mods using the switches below"
                android:textSize="12sp"
                android:textColor="#1976D2"
                android:lineSpacingExtra="2dp" />

        </LinearLayout>

        <!-- Add Mods Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="8dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="📥 Add New Mods"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#333333"
                    android:layout_marginBottom="12dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:weightSum="2">

                    <Button
                        android:id="@+id/addDllModBtn"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📥 Add DLL Mod"
                        android:textSize="14sp"
                        android:background="#4CAF50"
                        android:textColor="@android:color/white"
                        android:layout_marginEnd="6dp"
                        android:minHeight="48dp" />

                    <Button
                        android:id="@+id/addDexModBtn"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📱 Add DEX/JAR Mod"
                        android:textSize="14sp"
                        android:background="#2196F3"
                        android:textColor="@android:color/white"
                        android:layout_marginStart="6dp"
                        android:minHeight="48dp" />

                </LinearLayout>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="💡 DLL mods require MelonLoader • DEX/JAR mods work without a loader"
                    android:textSize="11sp"
                    android:textColor="#666666"
                    android:gravity="center"
                    android:layout_marginTop="8dp" />

            </LinearLayout>

        </androidx.cardview.widget.CardView>

        <!-- Mod List Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="8dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#FFFFFF">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="vertical"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="12dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📋 Installed Mods"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="#333333" />

                    <Button
                        android:id="@+id/refreshBtn"
                        android:layout_width="wrap_content"
                        android:layout_height="36dp"
                        android:text="🔄 Refresh"
                        android:textSize="12sp"
                        android:background="#FF9800"
                        android:textColor="@android:color/white"
                        android:minWidth="80dp" />

                </LinearLayout>

                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/modRecyclerView"
                    android:layout_width="match_parent"
                    android:layout_height="0dp"
                    android:layout_weight="1"
                    android:background="#F9F9F9"
                    android:padding="8dp" />

            </LinearLayout>

        </androidx.cardview.widget.CardView>

        <!-- Navigation Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center"
            android:layout_marginTop="8dp">

            <Button
                android:id="@+id/backBtn"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="← Back"
                android:textSize="14sp"
                android:background="@android:color/transparent"
                android:textColor="#666666"
                android:minHeight="40dp"
                android:layout_marginEnd="16dp" />

            <TextView
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Manage your mods after installation"
                android:textSize="12sp"
                android:textColor="#999999"
                android:gravity="center" />

        </LinearLayout>

        <!-- Tips Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="💡 Tips:\n• Toggle mods with switches\n• Delete mods with trash icon\n• DLL mods require patched Terraria APK\n• DEX/JAR mods work with any Terraria version"
            android:textSize="11sp"
            android:textColor="#888888"
            android:background="#F0F0F0"
            android:padding="12dp"
            android:layout_marginTop="16dp"
            android:lineSpacingExtra="2dp" />

    </LinearLayout>

</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_offline_diagnostic.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="#f5f5f5">

    <!-- Diagnostic Report Scroll -->
    <ScrollView
        android:id="@+id/report_scroll"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="#ffffff"
        android:elevation="2dp"
        android:padding="12dp">

        <TextView
            android:id="@+id/report_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Diagnostics report will appear here..."
            android:textColor="#333333"
            android:textSize="14sp"
            android:lineSpacingExtra="4dp"
            android:typeface="monospace" />
    </ScrollView>

    <!-- Action Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="12dp"
        android:gravity="center">

        <Button
            android:id="@+id/run_diagnostics_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="🔍 Run Diagnostics"
            android:backgroundTint="#4CAF50"
            android:textColor="#ffffff" />

        <View android:layout_width="8dp" android:layout_height="match_parent"/>

        <Button
            android:id="@+id/repair_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="🛠 Attempt Repair"
            android:backgroundTint="#FF9800"
            android:textColor="#ffffff" />
    </LinearLayout>

    <Button
        android:id="@+id/export_report_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="📤 Export Report"
        android:layout_marginTop="8dp"
        android:backgroundTint="#2196F3"
        android:textColor="#ffffff" />
</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_settings.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <!-- Header -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Terraria Loader Settings"
            android:textSize="20sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp"
            android:gravity="center" />

        <!-- Mod Settings Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Mod Settings"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginBottom="8dp" />

        <CheckBox
            android:id="@+id/enableModsCheck"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Enable Mods by Default"
            android:checked="true"
            android:layout_marginBottom="8dp" />

        <CheckBox
            android:id="@+id/sandboxModeCheck"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Enable Sandbox Mode"
            android:layout_marginBottom="16dp" />

        <!-- Log Settings Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Log Settings"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginBottom="8dp" />

        <CheckBox
            android:id="@+id/autoSaveLogsCheck"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Auto-save Logs"
            android:layout_marginBottom="8dp" />

        <CheckBox
            android:id="@+id/debugModeCheck"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Enable Debug Mode"
            android:layout_marginBottom="16dp" />

        <!-- Action Buttons Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Actions"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/clearLogsBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Clear All Logs"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/resetModsBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Reset All Mods"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/clearCacheBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Clear Cache"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/resetSettingsBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Reset Settings to Default"
            android:layout_marginBottom="16dp" />

        <!-- Storage Info Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Storage Information"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/storageInfo"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Loading storage info..."
            android:textSize="14sp"
            android:textColor="#666666"
            android:padding="8dp"
            android:background="#F5F5F5"
            android:layout_marginBottom="16dp" />

        <!-- Footer -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Settings are saved automatically"
            android:textSize="12sp"
            android:textColor="#999999"
            android:gravity="center"
            android:layout_marginTop="16dp" />

    </LinearLayout>
</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_setup_guide.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp"
        android:background="@drawable/gradient_background_135">

        <!-- Header Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:background="#FFFFFF"
            android:padding="32dp"
            android:layout_marginBottom="24dp"
            android:elevation="8dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="🚀 MelonLoader Setup Guide"
                android:textSize="28sp"
                android:textStyle="bold"
                android:textColor="#2E7D32"
                android:gravity="center"
                android:layout_marginBottom="12dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Choose your preferred installation method"
                android:textSize="16sp"
                android:textColor="#4CAF50"
                android:gravity="center"
                android:lineSpacingExtra="4dp" />

        </LinearLayout>

        <!-- Installation Options -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginBottom="24dp">

            <!-- Online Installation Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="#E3F2FD">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="🌐 Automated Online Installation"
                        android:textSize="20sp"
                        android:textStyle="bold"
                        android:textColor="#1565C0"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Automatically downloads from GitHub\n• No manual file handling\n• Always gets latest version\n• Requires internet connection"
                        android:textSize="14sp"
                        android:textColor="#1976D2"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="16dp" />

                    <Button
                        android:id="@+id/btn_online_install"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="🌐 Start Online Installation"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:background="#2196F3"
                        android:textColor="@android:color/white"
                        android:minHeight="56dp"
                        android:elevation="4dp" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Offline Import Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="#FFF3E0">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="📦 Offline ZIP Import"
                        android:textSize="20sp"
                        android:textStyle="bold"
                        android:textColor="#E65100"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Import pre-downloaded ZIP files\n• Works without internet\n• Auto-detects NET8/NET35\n• Extracts to correct directories"
                        android:textSize="14sp"
                        android:textColor="#F57C00"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="16dp" />

                    <Button
                        android:id="@+id/btn_offline_import"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="📦 Import ZIP File"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:background="#FF9800"
                        android:textColor="@android:color/white"
                        android:minHeight="56dp"
                        android:elevation="4dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="💡 Supports: melon_data.zip, lemon_data.zip, custom packages"
                        android:textSize="11sp"
                        android:textColor="#BF360C"
                        android:gravity="center"
                        android:layout_marginTop="8dp" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Manual Installation Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="#F3E5F5">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="20dp">

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="📖 Manual Installation Guide"
                        android:textSize="20sp"
                        android:textStyle="bold"
                        android:textColor="#7B1FA2"
                        android:layout_marginBottom="12dp" />

                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="• Step-by-step instructions\n• Full control over installation\n• Troubleshooting included\n• For advanced users"
                        android:textSize="14sp"
                        android:textColor="#8E24AA"
                        android:lineSpacingExtra="4dp"
                        android:layout_marginBottom="16dp" />

                    <Button
                        android:id="@+id/btn_manual_instructions"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="📖 View Manual Guide"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:background="#9C27B0"
                        android:textColor="@android:color/white"
                        android:minHeight="56dp"
                        android:elevation="4dp" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

        </LinearLayout>

        <!-- Information Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="8dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="#E8F5E8">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="ℹ️ What happens after installation?"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#2E7D32"
                    android:layout_marginBottom="8dp" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="1. 🚀 Unified Loader opens automatically\n2. 📱 Select your Terraria APK file\n3. ⚡ Patch APK with MelonLoader\n4. 📲 Install the patched APK\n5. 🎮 Add DLL mods and enjoy!"
                    android:textSize="14sp"
                    android:textColor="#388E3C"
                    android:lineSpacingExtra="4dp" />

            </LinearLayout>

        </androidx.cardview.widget.CardView>

        <!-- Requirements Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="#FFFFFF"
            android:padding="16dp"
            android:layout_marginTop="8dp"
            android:elevation="2dp">

            <TextView
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="📋 Requirements:\n• 50MB+ free space\n• Terraria APK file\n• File manager permissions"
                android:textSize="12sp"
                android:textColor="#666666"
                android:lineSpacingExtra="2dp" />

            <TextView
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="🎯 Recommended:\n• Use Online Installation\n• Keep APK backup\n• Enable unknown sources"
                android:textSize="12sp"
                android:textColor="#666666"
                android:lineSpacingExtra="2dp" />

        </LinearLayout>

    </LinearLayout>

</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_specific_selection.xml
<?xml version="1.0" encoding="utf-8"?>

<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <LinearLayout
        android:id="@+id/specific_selection_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center">

        <!-- Header -->
        <TextView
            android:id="@+id/headerText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Choose Game/App to Mod"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="32dp" />

        <!-- Terraria Button -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="@android:drawable/dialog_frame"
            android:padding="16dp"
            android:layout_marginBottom="16dp">

            <Button
                android:id="@+id/terraria_button"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="🌍 Terraria"
                android:textSize="20sp"
                android:textStyle="bold"
                android:minHeight="60dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="• Support for DEX/JAR mods\n• Support for DLL mods (via MelonLoader)\n• APK patching and installation\n• Full mod management"
                android:textSize="14sp"
                android:textColor="@android:color/darker_gray"
                android:layout_marginTop="8dp" />

        </LinearLayout>

        <!-- Future Games Section -->
        <LinearLayout
            android:id="@+id/futureGamesSection"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginTop="24dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Coming Soon"
                android:textSize="18sp"
                android:textStyle="bold"
                android:gravity="center"
                android:layout_marginBottom="16dp" />

            <!-- Placeholder cards for future games -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:background="@android:drawable/dialog_frame"
                android:padding="12dp"
                android:layout_marginBottom="8dp"
                android:alpha="0.5">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="🟫 Minecraft PE"
                        android:textSize="16sp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Soon"
                        android:textSize="12sp"
                        android:textColor="@android:color/darker_gray" />

                </LinearLayout>
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:background="@android:drawable/dialog_frame"
                android:padding="12dp"
                android:layout_marginBottom="8dp"
                android:alpha="0.5">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="🚀 Among Us"
                        android:textSize="16sp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Soon"
                        android:textSize="12sp"
                        android:textColor="@android:color/darker_gray" />

                </LinearLayout>
            </LinearLayout>

        </LinearLayout>

        <!-- Back Button -->
        <Button
            android:id="@+id/backToMainButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="← Back to Main Menu"
            android:layout_marginTop="32dp" />

    </LinearLayout>
</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_terraria_specific_updated.xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:id="@+id/rootLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        android:background="#E8F5E8">

        <!-- Header Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:layout_marginBottom="24dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="🌍 Terraria Mod Loader"
                android:textSize="28sp"
                android:textStyle="bold"
                android:textColor="#2E7D32"
                android:gravity="center"
                android:layout_marginBottom="8dp" />

            <TextView
                android:id="@+id/loaderStatusText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Checking loader status..."
                android:textSize="14sp"
                android:textColor="#4CAF50"
                android:gravity="center"
                android:padding="12dp"
                android:background="#F1F8E9"
                android:layout_marginTop="8dp" />
        </LinearLayout>

        <!-- Setup & Installation Section -->
        <androidx.cardview.widget.CardView
            android:id="@+id/setupCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="#F1F8E9">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="🚀 Setup &amp; Installation"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:textColor="#2E7D32"
                    android:layout_marginBottom="16dp" />

                <Button
                    android:id="@+id/unifiedSetupButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="🎯 Complete Setup Wizard"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:background="#4CAF50"
                    android:textColor="@android:color/white"
                    android:layout_marginBottom="12dp"
                    android:minHeight="56dp"
                    android:elevation="2dp" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="All-in-one wizard for MelonLoader installation and APK patching"
                    android:textSize="12sp"
                    android:textColor="#66BB6A"
                    android:layout_marginBottom="16dp"
                    android:gravity="center" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:weightSum="2">

                    <Button
                        android:id="@+id/setupGuideButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📖 Setup Guide"
                        android:textSize="14sp"
                        android:background="#81C784"
                        android:textColor="@android:color/white"
                        android:layout_marginEnd="6dp"
                        android:minHeight="48dp" />

                    <Button
                        android:id="@+id/manualInstructionsButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📋 Manual Steps"
                        android:textSize="14sp"
                        android:background="#A5D6A7"
                        android:textColor="#2E7D32"
                        android:layout_marginStart="6dp"
                        android:minHeight="48dp" />
                </LinearLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Mod Management Section -->
        <androidx.cardview.widget.CardView
            android:id="@+id/modManagementCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="#E3F2FD">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="📦 Mod Management"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:textColor="#1565C0"
                    android:layout_marginBottom="16dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:weightSum="2">

                    <Button
                        android:id="@+id/dexModManagerButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📱 DEX/JAR Mods"
                        android:textSize="14sp"
                        android:background="#2196F3"
                        android:textColor="@android:color/white"
                        android:layout_marginEnd="6dp"
                        android:minHeight="48dp" />

                    <Button
                        android:id="@+id/dllModManagerButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="🔧 DLL Mods"
                        android:textSize="14sp"
                        android:background="#FF9800"
                        android:textColor="@android:color/white"
                        android:layout_marginStart="6dp"
                        android:minHeight="48dp" />
                </LinearLayout>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="• DEX/JAR: Android Java mods (always available)\n• DLL: C# mods (requires MelonLoader)"
                    android:textSize="12sp"
                    android:textColor="#42A5F5"
                    android:layout_marginTop="12dp"
                    android:lineSpacingExtra="2dp" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Tools & Utilities Section -->
        <androidx.cardview.widget.CardView
            android:id="@+id/toolsCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="#F3E5F5">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="🛠️ Tools &amp; Utilities"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:textColor="#7B1FA2"
                    android:layout_marginBottom="16dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:weightSum="3">

                    <Button
                        android:id="@+id/logViewerButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="📋 Logs"
                        android:textSize="12sp"
                        android:background="#9C27B0"
                        android:textColor="@android:color/white"
                        android:layout_marginEnd="4dp"
                        android:minHeight="44dp" />

                    <Button
                        android:id="@+id/settingsButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="⚙️ Settings"
                        android:textSize="12sp"
                        android:background="#BA68C8"
                        android:textColor="@android:color/white"
                        android:layout_marginHorizontal="4dp"
                        android:minHeight="44dp" />

                    <Button
                        android:id="@+id/sandboxButton"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="wrap_content"
                        android:text="🧪 Sandbox"
                        android:textSize="12sp"
                        android:background="#CE93D8"
                        android:textColor="#4A148C"
                        android:layout_marginStart="4dp"
                        android:minHeight="44dp" />
                </LinearLayout>

                <!-- ✅ Fixed Diagnostic Button -->
                <Button
                    android:id="@+id/diagnosticButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="🧪 Offline Diagnostic &amp; Repair"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:backgroundTint="#9C27B0"
                    android:textColor="#FFFFFF"
                    android:layout_marginTop="12dp"
                    android:minHeight="48dp" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Navigation Section -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:layout_marginTop="16dp">

            <Button
                android:id="@+id/backButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="← Back to App Selection"
                android:textSize="14sp"
                android:background="@android:color/transparent"
                android:textColor="#666666"
                android:minHeight="40dp" />
        </LinearLayout>

        <!-- Info Footer -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="💡 Tip: Start with 'Complete Setup Wizard' for the easiest experience!"
            android:textSize="12sp"
            android:textColor="#81C784"
            android:gravity="center"
            android:background="#F1F8E9"
            android:padding="16dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp" />
    </LinearLayout>
</ScrollView>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_unified_loader.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@android:color/white">

    <!-- Header Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#E8F5E8"
        android:padding="16dp"
        android:elevation="4dp">

        <!-- Progress Bar -->
        <ProgressBar
            android:id="@+id/stepProgressBar"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="match_parent"
            android:layout_height="8dp"
            android:layout_marginBottom="12dp"
            android:max="4"
            android:progress="0"
            android:progressTint="#4CAF50"
            android:progressBackgroundTint="#E0E0E0" />

        <!-- Step Indicator -->
        <TextView
            android:id="@+id/stepIndicatorText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Step 1 of 5"
            android:textSize="12sp"
            android:textColor="#666666"
            android:gravity="center"
            android:layout_marginBottom="8dp" />

        <!-- Step Title -->
        <TextView
            android:id="@+id/stepTitleText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Welcome"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="#2E7D32"
            android:gravity="center"
            android:layout_marginBottom="8dp" />

        <!-- Step Description -->
        <TextView
            android:id="@+id/stepDescriptionText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Welcome to the MelonLoader Setup Wizard"
            android:textSize="14sp"
            android:textColor="#4CAF50"
            android:gravity="center"
            android:lineSpacingExtra="4dp" />

    </LinearLayout>

    <!-- Main Content Area -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true">

        <LinearLayout
            android:id="@+id/stepContentContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp"
            android:minHeight="400dp">

            <!-- Dynamic content will be added here -->

        </LinearLayout>

    </ScrollView>

    <!-- Navigation Footer -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#F5F5F5"
        android:padding="16dp"
        android:elevation="4dp">

        <!-- Action Button (context-sensitive) -->
        <Button
            android:id="@+id/actionButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="🚀 Start Setup"
            android:textSize="16sp"
            android:textStyle="bold"
            android:background="#4CAF50"
            android:textColor="@android:color/white"
            android:layout_marginBottom="12dp"
            android:minHeight="48dp" />

        <!-- Navigation Buttons -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:weightSum="2">

            <Button
                android:id="@+id/previousButton"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="← Previous"
                android:textSize="14sp"
                android:background="@android:color/transparent"
                android:textColor="#666666"
                android:layout_marginEnd="8dp"
                android:enabled="false" />

            <Button
                android:id="@+id/nextButton"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Next →"
                android:textSize="14sp"
                android:background="#2196F3"
                android:textColor="@android:color/white"
                android:layout_marginStart="8dp" />

        </LinearLayout>

        <!-- Progress Text (for operations) -->
        <TextView
            android:id="@+id/progressText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text=""
            android:textSize="12sp"
            android:textColor="#666666"
            android:gravity="center"
            android:layout_marginTop="8dp"
            android:visibility="gone" />

    </LinearLayout>

    <!-- Hidden Status Views (referenced by activity) -->
    <TextView
        android:id="@+id/loaderStatusText"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:visibility="gone" />

    <TextView
        android:id="@+id/apkStatusText"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:visibility="gone" />

    <ProgressBar
        android:id="@+id/actionProgressBar"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:visibility="gone" />

</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/activity_universal.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <Button
        android:id="@+id/select_apk_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Select Universal APK" />

    <Button
        android:id="@+id/select_zip_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Select Loader ZIP" />

    <Button
        android:id="@+id/inject_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Inject Loader" />

    <TextView
        android:id="@+id/status_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Status"
        android:paddingTop="16dp"
        android:textAppearance="?android:attr/textAppearanceMedium" />

</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/item_log_entry.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp"
    android:background="?android:attr/selectableItemBackground"
    android:minHeight="56dp">

    <!-- Level Indicator Bar -->
    <View
        android:id="@+id/levelIndicator"
        android:layout_width="4dp"
        android:layout_height="match_parent"
        android:layout_marginEnd="8dp"
        android:background="#2196F3" />

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <!-- Header Row (Timestamp, Level, Tag) -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="4dp">

            <TextView
                android:id="@+id/logTimestamp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="00:00:00"
                android:textSize="11sp"
                android:textColor="#666666"
                android:fontFamily="monospace"
                android:layout_marginEnd="8dp" />

            <TextView
                android:id="@+id/logLevel"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="INFO"
                android:textSize="11sp"
                android:textStyle="bold"
                android:textColor="#2196F3"
                android:layout_marginEnd="8dp"
                android:minWidth="48dp" />

            <TextView
                android:id="@+id/logTag"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="TAG"
                android:textSize="11sp"
                android:textColor="#666666"
                android:textStyle="italic"
                android:ellipsize="end"
                android:maxLines="1" />

        </LinearLayout>

        <!-- Message Content -->
        <TextView
            android:id="@+id/logMessage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Log message content goes here and can span multiple lines if needed"
            android:textSize="14sp"
            android:textColor="#333333"
            android:lineSpacingExtra="2dp"
            android:textIsSelectable="true"
            android:maxLines="10"
            android:ellipsize="end" />

    </LinearLayout>

</LinearLayout>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/layout/item_mod.xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center_vertical">

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/modNameTextView"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mod Name"
                android:textSize="18sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/modDescription"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mod description goes here. This can be multiline."
                android:textSize="14sp"
                android:textColor="@android:color/darker_gray"
                android:layout_marginTop="4dp" />

        </LinearLayout>

        <Switch
            android:id="@+id/modSwitch"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp" />

        <ImageButton
            android:id="@+id/modDeleteButton"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginStart="8dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_delete"
            android:contentDescription="Delete Mod"
            app:tint="@android:color/holo_red_dark" />

    </LinearLayout>
</androidx.cardview.widget.CardView>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/menu/log_viewer_menu.xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/action_export_filtered"
        android:title="Export Filtered Logs"
        android:icon="@android:drawable/ic_menu_save"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_copy_selected"
        android:title="Copy Selected"
        android:icon="@android:drawable/ic_menu_edit"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_save_search"
        android:title="Save Current Search"
        android:icon="@android:drawable/ic_menu_add"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_refresh_logs"
        android:title="Refresh Logs"
        android:icon="@android:drawable/ic_popup_sync"
        app:showAsAction="ifRoom" />

    <item
        android:id="@+id/action_settings"
        android:title="Log Settings"
        android:icon="@android:drawable/ic_menu_preferences"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_help"
        android:title="Help"
        android:icon="@android:drawable/ic_menu_help"
        app:showAsAction="never" />

</menu>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/menu/main_menu.xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:id="@+id/action_log"
        android:title="View Logs"
        android:icon="@android:drawable/ic_menu_info_details"
        android:showAsAction="never" />

    <item
        android:id="@+id/action_about"
        android:title="About"
        android:icon="@android:drawable/ic_menu_help"
        android:showAsAction="never" />

    <item
        android:id="@+id/action_dark_mode"
        android:title="Toggle Dark Mode"
        android:icon="@android:drawable/ic_menu_day"
        android:showAsAction="never" />

    <item
        android:id="@+id/action_export_apk"
        android:title="Export Modified APK"
        android:icon="@android:drawable/ic_menu_save"
        android:showAsAction="never" />

    <item
        android:id="@+id/action_export_logs"
        android:title="Export Logs"
        android:icon="@android:drawable/ic_menu_upload"
        android:showAsAction="never" />
</menu>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/mipmap-anydpi-v26/ic_launcher_round.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/values/colors.xml
<resources>
    <color name="purple_200">#BB86FC</color>
    <color name="purple_500">#6200EE</color>
    <color name="purple_700">#3700B3</color>
    <color name="teal_200">#03DAC5</color>
    <color name="teal_700">#018786</color>
    <color name="black">#000000</color>
    <color name="white">#FFFFFF</color>
    <color name="colorPrimary">#6200EE</color>
</resources>


/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/values/strings.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Terraria ML</string>

</resources>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/values/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme (Light) -->
    <style name="Theme.TerrariaML" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/purple_500</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_700</item>
        <item name="colorOnSecondary">@color/black</item>
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
    </style>
</resources>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/values-night/colors.xml
<resources>
    <color name="white">#000000</color>
    <color name="black">#FFFFFF</color>
</resources>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/values-night/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Night mode theme -->
    <style name="Theme.TerrariaML" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/purple_200</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/black</item>
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_700</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
    </style>
</resources>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/xml/backup_rules.xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <!-- Include app-specific files -->
    <include domain="file" path="." />
    <include domain="database" path="." />
    <include domain="sharedpref" path="." />
    <include domain="external" path="Android/data/com.terrarialoader/" />

    <!-- Exclude cache and logs if needed -->
    <exclude domain="cache" path="." />
    <exclude domain="file" path="logs/" />
</full-backup-content>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/xml/data_extraction_rules.xml
<?xml version="1.0" encoding="utf-8"?><!--
   Sample data extraction rules file; uncomment and customize as necessary.
   See https://developer.android.com/about/versions/12/backup-restore#xml-changes
   for details.
-->
<data-extraction-rules>
  <cloud-backup>
    <!-- TODO: Use <include> and <exclude> to control what is backed up.
        <include .../>
        <exclude .../>
        -->
  </cloud-backup>
  <!--
    <device-transfer>
        <include .../>
        <exclude .../>
    </device-transfer>
    -->
</data-extraction-rules>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/xml/file_paths.xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="external_files"
        path="." />
</paths>

/storage/emulated/0/AndroidIDEProjects/TerrariaMLmain/res/xml/paths.xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="external_files"
        path="." />
</paths>

