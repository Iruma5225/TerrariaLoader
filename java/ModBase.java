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