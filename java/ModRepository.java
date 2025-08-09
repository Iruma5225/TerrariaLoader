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