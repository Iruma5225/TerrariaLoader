// File: LoaderValidator.java (Fixed Component) - Complete Path Management Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/LoaderValidator.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.io.FileInputStream;

public class LoaderValidator {
    private static final String MELONLOADER_VERSION = "0.6.5";
    
    // Essential files mapping based on MelonLoader_File_List.txt
    private static final String[] MELONLOADER_NET8_FILES = {
        "net8/MelonLoader.dll",
        "net8/MelonLoader.deps.json",
        "net8/MelonLoader.runtimeconfig.json",
        "net8/MelonLoader.xml",
        "net8/MelonLoader.NativeHost.deps.json",
        "net8/MelonLoader.NativeHost.dll",
        "net8/0Harmony.dll",
        "net8/0Harmony.pdb",
        "net8/0Harmony.xml",
        "net8/Il2CppInterop.Runtime.dll",
        "net8/Il2CppInterop.Runtime.xml",
        "net8/MonoMod.RuntimeDetour.dll",
        "net8/MonoMod.RuntimeDetour.pdb",
        "net8/MonoMod.RuntimeDetour.xml",
        "net8/MonoMod.Utils.dll",
        "net8/MonoMod.Utils.pdb",
        "net8/MonoMod.Utils.xml"
    };
    
    private static final String[] MELONLOADER_NET35_FILES = {
        "net35/MelonLoader.dll",
        "net35/MelonLoader.xml",
        "net35/0Harmony.dll",
        "net35/0Harmony.pdb",
        "net35/0Harmony.xml",
        "net35/MonoMod.RuntimeDetour.dll",
        "net35/MonoMod.RuntimeDetour.pdb",
        "net35/MonoMod.RuntimeDetour.xml",
        "net35/MonoMod.Utils.dll",
        "net35/MonoMod.Utils.pdb",
        "net35/MonoMod.Utils.xml"
    };
    
    private static final String[] MELONLOADER_SUPPORT_FILES = {
        "Dependencies/SupportModules/Il2Cpp.dll",
        "Dependencies/SupportModules/Il2Cpp.deps.json",
        "Dependencies/SupportModules/Il2CppInterop.Runtime.dll",
        "Dependencies/SupportModules/Il2CppInterop.Runtime.xml",
        "Dependencies/SupportModules/Il2CppInterop.HarmonySupport.dll",
        "Dependencies/SupportModules/Il2CppInterop.HarmonySupport.xml",
        "Dependencies/Il2CppAssemblyGenerator/Il2CppAssemblyGenerator.dll",
        "Dependencies/Il2CppAssemblyGenerator/Il2CppAssemblyGenerator.deps.json"
    };

    // Enhanced detection with PathManager
    public boolean isMelonLoaderInstalled(Context context, String gamePackage) {
        if (context == null || gamePackage == null) {
            LogUtils.logDebug("Context or gamePackage is null");
            return false;
        }
        
        // Check if directories need to be initialized
        if (PathManager.needsMigration(context)) {
            LogUtils.logUser("Migrating from legacy directory structure...");
            PathManager.migrateLegacyStructure(context);
        }
        
        // Auto-initialize directories if they don't exist
        if (!PathManager.initializeGameDirectories(context, gamePackage)) {
            LogUtils.logDebug("Failed to initialize game directories for: " + gamePackage);
        }
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
        File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        // Check for core files (either net8 or net35)
        boolean hasNet8 = checkCoreFiles(loaderDir, MELONLOADER_NET8_FILES);
        boolean hasNet35 = checkCoreFiles(loaderDir, MELONLOADER_NET35_FILES);
        boolean hasSupportFiles = checkCoreFiles(loaderDir, MELONLOADER_SUPPORT_FILES);
        
        boolean installed = loaderDir != null && loaderDir.exists() && 
                           depsDir != null && depsDir.exists() && 
                           dllModsDir != null && dllModsDir.exists() && 
                           (hasNet8 || hasNet35) && hasSupportFiles;
        
        if (installed) {
            LogUtils.logDebug("MelonLoader detected for: " + gamePackage + 
                             " (NET8: " + hasNet8 + ", NET35: " + hasNet35 + ")");
        } else {
            LogUtils.logDebug("MelonLoader not detected for: " + gamePackage);
            LogUtils.logDebug("  Loader dir exists: " + (loaderDir != null && loaderDir.exists()));
            LogUtils.logDebug("  Dependencies exist: " + (depsDir != null && depsDir.exists()));
            LogUtils.logDebug("  DLL mods dir exists: " + (dllModsDir != null && dllModsDir.exists()));
            LogUtils.logDebug("  Has NET8: " + hasNet8);
            LogUtils.logDebug("  Has NET35: " + hasNet35);
            LogUtils.logDebug("  Has support files: " + hasSupportFiles);
        }
        
        return installed;
    }
    
    // Helper method to check if core files exist
    private boolean checkCoreFiles(File baseDir, String[] filePaths) {
        if (baseDir == null || !baseDir.exists() || filePaths == null) {
            return false;
        }
        
        int foundFiles = 0;
        int requiredFiles = Math.max(1, filePaths.length / 2); // At least half the files should exist
        
        for (String filePath : filePaths) {
            File file = new File(baseDir, filePath);
            if (file.exists() && file.length() > 0) {
                foundFiles++;
            }
        }
        
        return foundFiles >= requiredFiles;
    }

    // Validate DLL mod file
    public boolean validateDllMod(File dllFile) {
        if (dllFile == null || !dllFile.exists() || !dllFile.getName().toLowerCase().endsWith(".dll")) {
            return false;
        }
        
        if (dllFile.length() == 0) {
            LogUtils.logDebug("DLL file is empty: " + dllFile.getName());
            return false;
        }
        
        // Basic PE header validation
        try (FileInputStream fis = new FileInputStream(dllFile)) {
            byte[] header = new byte[2];
            if (fis.read(header) == 2) {
                // Check for PE signature (MZ)
                return header[0] == 0x4D && header[1] == 0x5A;
            }
        } catch (Exception e) {
            LogUtils.logDebug("DLL validation error: " + e.getMessage());
        }
        
        return false;
    }

    // Check loader type availability
    public boolean isNet8Available(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return false;
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        return checkCoreFiles(loaderDir, MELONLOADER_NET8_FILES);
    }

    public boolean isNet35Available(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return false;
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        return checkCoreFiles(loaderDir, MELONLOADER_NET35_FILES);
    }

    // Get active loader type
    public MelonLoaderManager.LoaderType getActiveLoaderType(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return null;
        
        if (isNet8Available(context, gamePackage)) {
            return MelonLoaderManager.LoaderType.MELONLOADER_NET8;
        } else if (isNet35Available(context, gamePackage)) {
            return MelonLoaderManager.LoaderType.MELONLOADER_NET35;
        }
        return null;
    }

    // Comprehensive validation of loader installation
    public ValidationResult validateLoaderInstallation(Context context, String gamePackage) {
        ValidationResult result = new ValidationResult();
        result.gamePackage = gamePackage;
        
        if (context == null || gamePackage == null) {
            result.isValid = false;
            result.issues.add("Context or game package is null");
            return result;
        }
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        result.basePathExists = baseDir != null && baseDir.exists();
        
        if (!result.basePathExists) {
            result.isValid = false;
            result.issues.add("Base directory does not exist: " + (baseDir != null ? baseDir.getAbsolutePath() : "null"));
            
            // Try to create the directory structure
            LogUtils.logUser("Attempting to create missing directory structure...");
            if (PathManager.initializeGameDirectories(context, gamePackage)) {
                LogUtils.logUser("✅ Directory structure created successfully");
                result.basePathExists = true;
                // Re-check after creation
                baseDir = PathManager.getGameBaseDir(context, gamePackage);
            } else {
                LogUtils.logUser("❌ Failed to create directory structure");
                return result;
            }
        }
        
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        result.loaderDirExists = loaderDir != null && loaderDir.exists();
        
        if (!result.loaderDirExists && loaderDir != null) {
            result.issues.add("Loader directory does not exist: " + loaderDir.getAbsolutePath());
        }
        
        // Check runtime availability
        result.hasNet8 = isNet8Available(context, gamePackage);
        result.hasNet35 = isNet35Available(context, gamePackage);
        
        if (!result.hasNet8 && !result.hasNet35) {
            result.issues.add("No valid runtime found (neither NET8 nor NET35)");
        }
        
        // Check support files
        result.hasSupportFiles = checkCoreFiles(loaderDir, MELONLOADER_SUPPORT_FILES);
        if (!result.hasSupportFiles) {
            result.issues.add("Missing support files in Dependencies directory");
        }
        
        // Check mod directories
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        result.dllModsDirExists = dllModsDir != null && dllModsDir.exists();
        result.dexModsDirExists = dexModsDir != null && dexModsDir.exists();
        
        if (!result.dllModsDirExists) {
            result.issues.add("DLL mods directory does not exist");
        }
        if (!result.dexModsDirExists) {
            result.issues.add("DEX mods directory does not exist");
        }
        
        // Overall validation
        result.isValid = result.basePathExists && result.loaderDirExists && 
                        (result.hasNet8 || result.hasNet35) && result.hasSupportFiles &&
                        result.dllModsDirExists && result.dexModsDirExists;
        
        if (result.isValid) {
            result.activeLoaderType = getActiveLoaderType(context, gamePackage);
        }
        
        return result;
    }

    // Get detailed information about loader installation
    public String getValidationReport(Context context, String gamePackage) {
        ValidationResult result = validateLoaderInstallation(context, gamePackage);
        StringBuilder report = new StringBuilder();
        
        report.append("=== Loader Validation Report ===\n");
        report.append("Game Package: ").append(gamePackage != null ? gamePackage : "null").append("\n");
        report.append("Overall Status: ").append(result.isValid ? "✅ VALID" : "❌ INVALID").append("\n\n");
        
        report.append("Directory Structure:\n");
        report.append("- Base Path: ").append(result.basePathExists ? "✅" : "❌").append("\n");
        report.append("- Loader Dir: ").append(result.loaderDirExists ? "✅" : "❌").append("\n");
        report.append("- DLL Mods Dir: ").append(result.dllModsDirExists ? "✅" : "❌").append("\n");
        report.append("- DEX Mods Dir: ").append(result.dexModsDirExists ? "✅" : "❌").append("\n\n");
        
        report.append("Runtime Support:\n");
        report.append("- NET8 Runtime: ").append(result.hasNet8 ? "✅" : "❌").append("\n");
        report.append("- NET35 Runtime: ").append(result.hasNet35 ? "✅" : "❌").append("\n");
        report.append("- Support Files: ").append(result.hasSupportFiles ? "✅" : "❌").append("\n\n");
        
        if (result.activeLoaderType != null) {
            report.append("Active Loader: ").append(result.activeLoaderType.getDisplayName()).append("\n\n");
        }
        
        if (!result.issues.isEmpty()) {
            report.append("Issues Found:\n");
            for (String issue : result.issues) {
                report.append("- ").append(issue).append("\n");
            }
        }
        
        // Add path information
        if (context != null && gamePackage != null) {
            report.append("\n").append(PathManager.getPathInfo(context, gamePackage));
        }
        
        return report.toString();
    }

    // Get installed loader version
    public String getInstalledLoaderVersion() {
        return MELONLOADER_VERSION;
    }

    // Helper class for validation results
    public static class ValidationResult {
        public String gamePackage;
        public boolean isValid = false;
        public boolean basePathExists = false;
        public boolean loaderDirExists = false;
        public boolean dllModsDirExists = false;
        public boolean dexModsDirExists = false;
        public boolean hasNet8 = false;
        public boolean hasNet35 = false;
        public boolean hasSupportFiles = false;
        public MelonLoaderManager.LoaderType activeLoaderType = null;
        public java.util.List<String> issues = new java.util.ArrayList<>();
        
        @Override
        public String toString() {
            return String.format("ValidationResult[%s]: %s (%d issues)", 
                               gamePackage != null ? gamePackage : "null", 
                               isValid ? "VALID" : "INVALID",
                               issues.size());
        }
    }

    // Quick validation methods for backward compatibility
    public boolean isLemonLoaderInstalled(Context context, String gamePackage) {
        return isMelonLoaderInstalled(context, gamePackage);
    }

    public boolean isLemonLoaderInstalled(Context context) {
        return isMelonLoaderInstalled(context, MelonLoaderManager.TERRARIA_PACKAGE);
    }

    // File integrity check
    public boolean checkFileIntegrity(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        if (file.length() == 0) {
            LogUtils.logDebug("File is empty: " + file.getName());
            return false;
        }
        
        // Check if file is readable
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead = fis.read(buffer);
            return bytesRead > 0;
        } catch (Exception e) {
            LogUtils.logDebug("File integrity check failed: " + e.getMessage());
            return false;
        }
    }

    // Repair corrupted installation
    public boolean attemptRepair(Context context, String gamePackage) {
        LogUtils.logUser("Attempting to repair loader installation for: " + gamePackage);
        
        if (context == null || gamePackage == null) {
            LogUtils.logUser("❌ Cannot repair: context or gamePackage is null");
            return false;
        }
        
        ValidationResult result = validateLoaderInstallation(context, gamePackage);
        
        if (result.isValid) {
            LogUtils.logUser("Installation is already valid, no repair needed");
            return true;
        }
        
        boolean repaired = false;
        
        // Try to create missing directories
        if (!result.basePathExists || !result.dllModsDirExists || !result.dexModsDirExists) {
            LogUtils.logDebug("Creating missing directories");
            if (PathManager.initializeGameDirectories(context, gamePackage)) {
                LogUtils.logDebug("Successfully created missing directories");
                repaired = true;
            }
        }
        
        // Re-validate after repair attempts
        ValidationResult newResult = validateLoaderInstallation(context, gamePackage);
        
        if (newResult.isValid) {
            LogUtils.logUser("✅ Loader installation repaired successfully");
            return true;
        } else {
            LogUtils.logUser("❌ Could not fully repair installation");
            LogUtils.logDebug("Remaining issues: " + newResult.issues.size());
            for (String issue : newResult.issues) {
                LogUtils.logDebug("  - " + issue);
            }
            return repaired; // Return true if we made some progress
        }
    }
}