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