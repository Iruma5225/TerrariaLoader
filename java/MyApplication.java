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