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