// File: ModInstaller.java (FIXED) - Updated to use new directory structure
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/installer/ModInstaller.java

package com.terrarialoader.installer;

import android.content.Context;
import android.net.Uri;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.PathManager;
import com.terrarialoader.loader.ModManager;
import java.io.File;

public class ModInstaller {
    // Enhanced file extension whitelist
    private static final String[] ALLOWED_EXTENSIONS = {
        ".dex", ".jar", ".dll", ".dex.disabled", ".jar.disabled", ".dll.disabled"
    };
    
    // Maximum file size (50MB)
    private static final long MAX_MOD_SIZE = 50 * 1024 * 1024;

    public static boolean installMod(Context context, Uri sourceUri, String filename) {
        LogUtils.logUser("Starting mod installation: " + filename);
        
        // Enhanced input validation
        if (context == null) {
            LogUtils.logUser("❌ Installation failed: context is null");
            return false;
        }
        
        if (sourceUri == null) {
            LogUtils.logUser("❌ Installation failed: source URI is null");
            return false;
        }
        
        if (filename == null || filename.trim().isEmpty()) {
            LogUtils.logUser("❌ Installation failed: filename is invalid");
            return false;
        }

        // Sanitize filename
        filename = sanitizeFilename(filename);
        
        // Check file extension
        if (!isValidModFile(filename)) {
            LogUtils.logUser("❌ Invalid mod file type: " + filename);
            LogUtils.logDebug("Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
            return false;
        }

        // FIXED: Determine mod directory based on file type using new structure
        File modDir = getModDirectoryByType(context, filename);
        if (modDir == null) {
            LogUtils.logUser("❌ Cannot determine mod directory for file type");
            return false;
        }

        // Ensure mod directory exists
        if (!PathManager.ensureDirectoryExists(modDir)) {
            LogUtils.logUser("❌ Cannot create mod directory: " + modDir.getAbsolutePath());
            return false;
        }

        // Check for existing mod with same name
        File outputFile = new File(modDir, filename);
        if (outputFile.exists()) {
            LogUtils.logUser("⚠️ Mod already exists: " + filename);
            // Create backup of existing mod
            File backupFile = new File(modDir, filename + ".backup." + System.currentTimeMillis());
            if (outputFile.renameTo(backupFile)) {
                LogUtils.logUser("📦 Created backup: " + backupFile.getName());
            }
        }

        // Validate file size before copying
        try {
            long fileSize = context.getContentResolver().openInputStream(sourceUri).available();
            if (fileSize > MAX_MOD_SIZE) {
                LogUtils.logUser("❌ Mod file too large: " + formatFileSize(fileSize) + " (max: " + formatFileSize(MAX_MOD_SIZE) + ")");
                return false;
            }
            LogUtils.logDebug("Mod file size: " + formatFileSize(fileSize));
        } catch (Exception e) {
            LogUtils.logDebug("Could not determine file size: " + e.getMessage());
        }

        // Install the mod
        boolean success = FileUtils.copyUriToFile(context, sourceUri, outputFile);
        
        if (success) {
            // Validate the copied file
            if (validateInstalledMod(outputFile)) {
                LogUtils.logUser("✅ Successfully installed: " + filename);
                LogUtils.logUser("📁 Location: " + outputFile.getAbsolutePath());
                
                // Auto-enable mod if it was installed with .disabled extension and user wants it enabled
                if (filename.endsWith(".disabled")) {
                    LogUtils.logUser("ℹ️ Mod installed as disabled. Enable it in mod manager to use.");
                } else {
                    LogUtils.logUser("ℹ️ Mod installed and ready to use.");
                }
                
                // Refresh mod list
                ModManager.loadMods(context);
                
                return true;
            } else {
                LogUtils.logUser("❌ Installed mod file validation failed");
                outputFile.delete(); // Clean up invalid file
                return false;
            }
        } else {
            LogUtils.logUser("❌ Failed to install: " + filename);
            return false;
        }
    }

    /**
     * FIXED: Get appropriate mod directory based on file type using new structure
     */
    private static File getModDirectoryByType(Context context, String filename) {
        String gamePackage = "com.and.games505.TerrariaPaid"; // Default to Terraria
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
            // DLL mods go to DLL directory
            return PathManager.getDllModsDir(context, gamePackage);
        } else if (lowerFilename.endsWith(".dex") || lowerFilename.endsWith(".dex.disabled") ||
                   lowerFilename.endsWith(".jar") || lowerFilename.endsWith(".jar.disabled")) {
            // DEX/JAR mods go to DEX directory
            return PathManager.getDexModsDir(context, gamePackage);
        }
        
        return null; // Unknown file type
    }

    // Enhanced mod installation with auto-detection
    public static boolean installModAuto(Context context, Uri sourceUri) {
        try {
            // Try to get filename from URI
            String filename = FileUtils.getFilenameFromUri(context, sourceUri);
            if (filename == null) {
                filename = "mod_" + System.currentTimeMillis() + ".dex";
                LogUtils.logDebug("Auto-generated filename: " + filename);
            }
            
            return installMod(context, sourceUri, filename);
            
        } catch (Exception e) {
            LogUtils.logUser("❌ Auto-installation failed: " + e.getMessage());
            LogUtils.logDebug("Auto-install error: " + e.toString());
            return false;
        }
    }

    // Batch mod installation
    public static int installMultipleMods(Context context, Uri[] sourceUris, String[] filenames) {
        if (sourceUris == null || filenames == null || sourceUris.length != filenames.length) {
            LogUtils.logUser("❌ Batch install failed: invalid parameters");
            return 0;
        }
        
        int successCount = 0;
        LogUtils.logUser("Starting batch installation of " + sourceUris.length + " mods");
        
        for (int i = 0; i < sourceUris.length; i++) {
            if (installMod(context, sourceUris[i], filenames[i])) {
                successCount++;
            }
        }
        
        LogUtils.logUser("Batch installation complete: " + successCount + "/" + sourceUris.length + " mods installed");
        return successCount;
    }

    private static boolean isValidModFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static String sanitizeFilename(String filename) {
        // Remove dangerous characters and limit length
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (filename.length() > 100) {
            filename = filename.substring(0, 100);
        }
        return filename;
    }

    private static boolean validateInstalledMod(File modFile) {
        if (!modFile.exists()) {
            LogUtils.logDebug("Validation failed: file does not exist");
            return false;
        }
        
        if (modFile.length() == 0) {
            LogUtils.logDebug("Validation failed: file is empty");
            return false;
        }
        
        if (modFile.length() > MAX_MOD_SIZE) {
            LogUtils.logDebug("Validation failed: file too large");
            return false;
        }
        
        // Basic file header validation for .dex files
        if (modFile.getName().toLowerCase().endsWith(".dex")) {
            try {
                byte[] header = new byte[8];
                java.io.FileInputStream fis = new java.io.FileInputStream(modFile);
                int bytesRead = fis.read(header);
                fis.close();
                
                if (bytesRead >= 8) {
                    // Check DEX magic number
                    String magic = new String(header, 0, 4);
                    if (!"dex\n".equals(magic)) {
                        LogUtils.logDebug("Validation failed: invalid DEX magic number");
                        return false;
                    }
                }
            } catch (Exception e) {
                LogUtils.logDebug("Validation warning: could not read file header: " + e.getMessage());
                // Don't fail validation just because we can't read header
            }
        }
        
        LogUtils.logDebug("Mod validation passed: " + modFile.getName());
        return true;
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // FIXED: Uninstall mod from correct directory
    public static boolean uninstallMod(Context context, String filename) {
        String gamePackage = "com.and.games505.TerrariaPaid";
        
        // Try to find the mod in appropriate directory
        File modFile = findModFile(context, gamePackage, filename);
        
        if (modFile == null || !modFile.exists()) {
            LogUtils.logUser("❌ Mod not found: " + filename);
            return false;
        }
        
        if (modFile.delete()) {
            LogUtils.logUser("🗑️ Uninstalled mod: " + filename);
            LogUtils.logUser("📁 From: " + modFile.getParent());
            ModManager.loadMods(context); // Refresh mod list
            return true;
        } else {
            LogUtils.logUser("❌ Failed to uninstall: " + filename);
            return false;
        }
    }

    /**
     * FIXED: Find mod file in appropriate directory based on file type
     */
    private static File findModFile(Context context, String gamePackage, String filename) {
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
            File dllDir = PathManager.getDllModsDir(context, gamePackage);
            File modFile = new File(dllDir, filename);
            if (modFile.exists()) {
                return modFile;
            }
        }
        
        if (lowerFilename.endsWith(".dex") || lowerFilename.endsWith(".dex.disabled") ||
            lowerFilename.endsWith(".jar") || lowerFilename.endsWith(".jar.disabled")) {
            File dexDir = PathManager.getDexModsDir(context, gamePackage);
            File modFile = new File(dexDir, filename);
            if (modFile.exists()) {
                return modFile;
            }
        }
        
        // Fallback: check legacy directory for backward compatibility
        File legacyDir = PathManager.getLegacyModsDir(context);
        File modFile = new File(legacyDir, filename);
        if (modFile.exists()) {
            LogUtils.logDebug("Found mod in legacy directory: " + filename);
            return modFile;
        }
        
        return null;
    }

    // Get installation status
    public static String[] getSupportedExtensions() {
        return ALLOWED_EXTENSIONS.clone();
    }

    public static long getMaxModSize() {
        return MAX_MOD_SIZE;
    }

    /**
     * FIXED: Check if migration is needed from legacy structure
     */
    public static boolean needsMigration(Context context) {
        File legacyModsDir = PathManager.getLegacyModsDir(context);
        if (!legacyModsDir.exists()) {
            return false;
        }
        
        File[] legacyMods = legacyModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : ALLOWED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        return legacyMods != null && legacyMods.length > 0;
    }

    /**
     * FIXED: Migrate legacy mods to new directory structure
     */
    public static boolean migrateLegacyMods(Context context) {
        if (!needsMigration(context)) {
            LogUtils.logDebug("No legacy mods to migrate");
            return true;
        }
        
        LogUtils.logUser("🔄 Migrating legacy mods to new structure...");
        
        String gamePackage = "com.and.games505.TerrariaPaid";
        File legacyModsDir = PathManager.getLegacyModsDir(context);
        
        // Ensure new directories exist
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (!PathManager.ensureDirectoryExists(dexModsDir) || 
            !PathManager.ensureDirectoryExists(dllModsDir)) {
            LogUtils.logUser("❌ Failed to create new mod directories");
            return false;
        }
        
        File[] legacyMods = legacyModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : ALLOWED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        if (legacyMods == null || legacyMods.length == 0) {
            LogUtils.logDebug("No legacy mod files found");
            return true;
        }
        
        int migratedCount = 0;
        int dexCount = 0;
        int dllCount = 0;
        
        for (File legacyMod : legacyMods) {
            String filename = legacyMod.getName();
            String lowerFilename = filename.toLowerCase();
            
            File targetDir;
            if (lowerFilename.endsWith(".dll") || lowerFilename.endsWith(".dll.disabled")) {
                targetDir = dllModsDir;
                dllCount++;
            } else {
                targetDir = dexModsDir;
                dexCount++;
            }
            
            File newLocation = new File(targetDir, filename);
            if (legacyMod.renameTo(newLocation)) {
                migratedCount++;
                LogUtils.logDebug("Migrated: " + filename + " -> " + targetDir.getName());
            } else {
                LogUtils.logDebug("Failed to migrate: " + filename);
            }
        }
        
        LogUtils.logUser("✅ Migration completed:");
        LogUtils.logUser("📊 Total migrated: " + migratedCount + "/" + legacyMods.length);
        LogUtils.logUser("📊 DEX/JAR mods: " + dexCount);
        LogUtils.logUser("📊 DLL mods: " + dllCount);
        
        // Remove legacy directory if it's empty
        File[] remainingFiles = legacyModsDir.listFiles();
        if (remainingFiles == null || remainingFiles.length == 0) {
            if (legacyModsDir.delete()) {
                LogUtils.logDebug("Removed empty legacy mods directory");
            }
        }
        
        return migratedCount > 0;
    }

    /**
     * Get mod installation statistics
     */
    public static String getInstallationStats(Context context) {
        String gamePackage = "com.and.games505.TerrariaPaid";
        
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        int dexCount = 0, dllCount = 0;
        int dexEnabled = 0, dllEnabled = 0;
        
        // Count DEX mods
        if (dexModsDir.exists()) {
            File[] dexMods = dexModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dex") || lowerName.endsWith(".jar") ||
                       lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
            });
            
            if (dexMods != null) {
                dexCount = dexMods.length;
                for (File mod : dexMods) {
                    if (!mod.getName().endsWith(".disabled")) {
                        dexEnabled++;
                    }
                }
            }
        }
        
        // Count DLL mods
        if (dllModsDir.exists()) {
            File[] dllMods = dllModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dll") || lowerName.endsWith(".dll.disabled");
            });
            
            if (dllMods != null) {
                dllCount = dllMods.length;
                for (File mod : dllMods) {
                    if (!mod.getName().endsWith(".disabled")) {
                        dllEnabled++;
                    }
                }
            }
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== Mod Installation Statistics ===\n");
        stats.append("DEX/JAR Mods: ").append(dexEnabled).append("/").append(dexCount).append(" enabled\n");
        stats.append("DLL Mods: ").append(dllEnabled).append("/").append(dllCount).append(" enabled\n");
        stats.append("Total Mods: ").append(dexEnabled + dllEnabled).append("/").append(dexCount + dllCount).append(" enabled\n");
        stats.append("\nDirectories:\n");
        stats.append("DEX: ").append(dexModsDir.getAbsolutePath()).append("\n");
        stats.append("DLL: ").append(dllModsDir.getAbsolutePath()).append("\n");
        
        return stats.toString();
    }
}