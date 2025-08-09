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