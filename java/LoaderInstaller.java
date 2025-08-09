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