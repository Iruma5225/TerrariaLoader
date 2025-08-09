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