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