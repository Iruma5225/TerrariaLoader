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