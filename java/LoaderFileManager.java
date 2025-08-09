// File: LoaderFileManager.java (Fixed Component) - Complete Path Management Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/loader/LoaderFileManager.java

package com.terrarialoader.loader;

import android.content.Context;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.PathManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LoaderFileManager {
    
    // Install a DLL mod into standardized structure
    public boolean installDllMod(Context context, File dllFile, String gamePackage) {
        if (!isMelonLoaderInstalled(context, gamePackage)) {
            LogUtils.logUser("❌ No MelonLoader installed for: " + gamePackage);
            return false;
        }
        
        try {
            File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
            PathManager.ensureDirectoryExists(dllModsDir);
            
            File targetFile = new File(dllModsDir, dllFile.getName());
            
            if (copyFile(dllFile, targetFile)) {
                LogUtils.logUser("✅ DLL mod installed: " + dllFile.getName());
                LogUtils.logUser("📁 Location: " + targetFile.getAbsolutePath());
                return true;
            } else {
                LogUtils.logUser("❌ Failed to install DLL mod: " + dllFile.getName());
                return false;
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("DLL mod installation failed: " + e.getMessage());
            return false;
        }
    }

    // Get list of installed DLL mods for a game
    public File[] getInstalledDllMods(Context context, String gamePackage) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (!dllModsDir.exists()) {
            return new File[0];
        }
        
        File[] dllFiles = dllModsDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".dll") && !name.toLowerCase().endsWith(".disabled"));
        
        return dllFiles != null ? dllFiles : new File[0];
    }
    
    // Get list of installed DEX mods for a game
    public File[] getInstalledDexMods(Context context, String gamePackage) {
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        
        if (!dexModsDir.exists()) {
            // Check legacy location for backward compatibility
            File legacyModsDir = PathManager.getLegacyModsDir(context);
            if (legacyModsDir.exists()) {
                LogUtils.logDebug("Using legacy mods directory for DEX mods");
                File[] legacyFiles = legacyModsDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return (lowerName.endsWith(".dex") || lowerName.endsWith(".jar")) && 
                           !lowerName.endsWith(".disabled");
                });
                return legacyFiles != null ? legacyFiles : new File[0];
            }
            return new File[0];
        }
        
        File[] dexFiles = dexModsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return (lowerName.endsWith(".dex") || lowerName.endsWith(".jar")) && 
                   !lowerName.endsWith(".disabled");
        });
        
        return dexFiles != null ? dexFiles : new File[0];
    }

    // Enable/disable DLL mod
    public boolean toggleDllMod(Context context, String gamePackage, String modName, boolean enable) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        
        if (enable) {
            // Enable: Remove .disabled extension
            File disabledFile = new File(dllModsDir, modName + ".disabled");
            File enabledFile = new File(dllModsDir, modName);
            
            if (disabledFile.exists()) {
                return disabledFile.renameTo(enabledFile);
            }
        } else {
            // Disable: Add .disabled extension
            File enabledFile = new File(dllModsDir, modName);
            File disabledFile = new File(dllModsDir, modName + ".disabled");
            
            if (enabledFile.exists()) {
                return enabledFile.renameTo(disabledFile);
            }
        }
        
        return false;
    }

    // Delete DLL mod
    public boolean deleteDllMod(Context context, String gamePackage, String modName) {
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        File modFile = new File(dllModsDir, modName);
        File disabledFile = new File(dllModsDir, modName + ".disabled");
        
        boolean deleted = false;
        
        if (modFile.exists()) {
            deleted = modFile.delete();
        }
        
        if (disabledFile.exists()) {
            deleted = disabledFile.delete() || deleted;
        }
        
        if (deleted) {
            LogUtils.logUser("🗑️ Deleted DLL mod: " + modName);
        }
        
        return deleted;
    }

    // Get all available mods (both DLL and DEX) with legacy support
    public List<File> getAllMods(Context context, String gamePackage) {
        List<File> allMods = new ArrayList<>();
        
        // Add DLL mods
        File[] dllMods = getInstalledDllMods(context, gamePackage);
        for (File mod : dllMods) {
            allMods.add(mod);
        }
        
        // Add disabled DLL mods
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        if (dllModsDir.exists()) {
            File[] disabledDllMods = dllModsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".dll.disabled"));
            if (disabledDllMods != null) {
                for (File mod : disabledDllMods) {
                    allMods.add(mod);
                }
            }
        }
        
        // Add DEX mods (with legacy support)
        File[] dexMods = getInstalledDexMods(context, gamePackage);
        for (File mod : dexMods) {
            allMods.add(mod);
        }
        
        // Add disabled DEX mods
        File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
        if (dexModsDir.exists()) {
            File[] disabledDexMods = dexModsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
            });
            if (disabledDexMods != null) {
                for (File mod : disabledDexMods) {
                    allMods.add(mod);
                }
            }
        } else {
            // Check legacy location for disabled DEX mods
            File legacyModsDir = PathManager.getLegacyModsDir(context);
            if (legacyModsDir.exists()) {
                File[] disabledDexMods = legacyModsDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".dex.disabled") || lowerName.endsWith(".jar.disabled");
                });
                if (disabledDexMods != null) {
                    for (File mod : disabledDexMods) {
                        allMods.add(mod);
                    }
                }
            }
        }
        
        return allMods;
    }

    // Check if mod is enabled
    public boolean isModEnabled(File modFile) {
        return !modFile.getName().toLowerCase().endsWith(".disabled");
    }

    // Get mod type from file
    public String getModType(File modFile) {
        String fileName = modFile.getName().toLowerCase();
        if (fileName.endsWith(".dll") || fileName.endsWith(".dll.disabled")) {
            return "DLL";
        } else if (fileName.endsWith(".dex") || fileName.endsWith(".dex.disabled")) {
            return "DEX";
        } else if (fileName.endsWith(".jar") || fileName.endsWith(".jar.disabled")) {
            return "JAR";
        }
        return "Unknown";
    }

    // Toggle mod enabled/disabled state
    public boolean toggleMod(Context context, String gamePackage, File modFile) {
        boolean isEnabled = isModEnabled(modFile);
        String modType = getModType(modFile);
        
        if ("DLL".equals(modType)) {
            return toggleDllMod(context, gamePackage, modFile.getName(), !isEnabled);
        } else if ("DEX".equals(modType) || "JAR".equals(modType)) {
            // Handle DEX/JAR toggling through FileUtils
            return FileUtils.toggleModFile(modFile);
        }
        
        return false;
    }

    // Get formatted mod info
    public String getModInfo(File modFile) {
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(modFile.getName()).append("\n");
        info.append("Type: ").append(getModType(modFile)).append("\n");
        info.append("Size: ").append(FileUtils.formatFileSize(modFile.length())).append("\n");
        info.append("Status: ").append(isModEnabled(modFile) ? "Enabled" : "Disabled").append("\n");
        info.append("Path: ").append(modFile.getAbsolutePath()).append("\n");
        
        if (modFile.exists()) {
            info.append("Last Modified: ").append(new java.util.Date(modFile.lastModified()).toString()).append("\n");
        }
        
        return info.toString();
    }

    // Backup existing mods before operations
    public boolean backupMods(Context context, String gamePackage) {
        try {
            File modsDir = PathManager.getGameBaseDir(context, gamePackage);
            if (modsDir == null || !modsDir.exists()) {
                LogUtils.logDebug("No game directory to backup for: " + gamePackage);
                return true;
            }
            
            File backupDir = new File(PathManager.getBackupsDir(context, gamePackage), "mods_" + System.currentTimeMillis());
            PathManager.ensureDirectoryExists(backupDir.getParentFile());
            
            return copyDirectory(modsDir, backupDir);
            
        } catch (Exception e) {
            LogUtils.logDebug("Mod backup failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to copy directory
    private boolean copyDirectory(File source, File target) {
        try {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    target.mkdirs();
                }
                
                File[] files = source.listFiles();
                if (files != null) {
                    for (File file : files) {
                        File targetFile = new File(target, file.getName());
                        if (!copyDirectory(file, targetFile)) {
                            return false;
                        }
                    }
                }
            } else {
                return copyFile(source, target);
            }
            return true;
        } catch (Exception e) {
            LogUtils.logDebug("Directory copy failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to copy files
    private boolean copyFile(File source, File target) {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(target)) {
            
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

    // Calculate directory size
    private long calculateDirectorySize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else if (dir != null && dir.isFile()) {
            size = dir.length();
        }
        return size;
    }

    // Count files recursively
    private int countFiles(File dir) {
        int count = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countFiles(file);
                    } else {
                        count++;
                    }
                }
            }
        } else if (dir != null && dir.isFile()) {
            count = 1;
        }
        return count;
    }

    // Get file statistics for a game package
    public FileStatistics getFileStatistics(Context context, String gamePackage) {
        FileStatistics stats = new FileStatistics();
        stats.gamePackage = gamePackage;
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        if (baseDir != null && baseDir.exists()) {
            stats.totalFiles = countFiles(baseDir);
            stats.totalSize = calculateDirectorySize(baseDir);
            
            // Count mods specifically
            File[] dllMods = getInstalledDllMods(context, gamePackage);
            File[] dexMods = getInstalledDexMods(context, gamePackage);
            List<File> allMods = getAllMods(context, gamePackage);
            
            stats.dllModCount = dllMods != null ? dllMods.length : 0;
            stats.dexModCount = dexMods != null ? dexMods.length : 0;
            stats.totalModCount = allMods != null ? allMods.size() : 0;
            
            // Count enabled vs disabled
            if (allMods != null) {
                for (File mod : allMods) {
                    if (isModEnabled(mod)) {
                        stats.enabledModCount++;
                    } else {
                        stats.disabledModCount++;
                    }
                }
            }
        }
        
        return stats;
    }

    // Clean up old backup files
    public void cleanupOldBackups(Context context, String gamePackage, int maxBackups) {
        try {
            File backupDir = PathManager.getBackupsDir(context, gamePackage);
            if (!backupDir.exists()) {
                return;
            }
            
            File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("mods_"));
            if (backupFiles == null || backupFiles.length <= maxBackups) {
                return;
            }
            
            // Sort by timestamp (oldest first)
            java.util.Arrays.sort(backupFiles, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            
            // Delete oldest backups
            int toDelete = backupFiles.length - maxBackups;
            for (int i = 0; i < toDelete; i++) {
                deleteDirectory(backupFiles[i]);
                LogUtils.logDebug("Cleaned up old backup: " + backupFiles[i].getName());
            }
            
        } catch (Exception e) {
            LogUtils.logDebug("Backup cleanup failed: " + e.getMessage());
        }
    }

    // Helper method to delete directory recursively
    private boolean deleteDirectory(File dir) {
        if (dir == null) return false;
        
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

    // Check if MelonLoader is installed (uses PathManager)
    private boolean isMelonLoaderInstalled(Context context, String gamePackage) {
        File loaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
        return loaderDir != null && loaderDir.exists() && dllModsDir != null && dllModsDir.exists();
    }

    // File Statistics helper class
    public static class FileStatistics {
        public String gamePackage;
        public int totalFiles = 0;
        public long totalSize = 0;
        public int dllModCount = 0;
        public int dexModCount = 0;
        public int totalModCount = 0;
        public int enabledModCount = 0;
        public int disabledModCount = 0;
        
        @Override
        public String toString() {
            return String.format("FileStats[%s]: %d files, %s, %d mods (%d enabled, %d disabled)", 
                               gamePackage != null ? gamePackage : "null", 
                               totalFiles,
                               FileUtils.formatFileSize(totalSize),
                               totalModCount, 
                               enabledModCount, 
                               disabledModCount);
        }
        
        public String getDetailedInfo() {
            StringBuilder info = new StringBuilder();
            info.append("=== File Statistics for ").append(gamePackage != null ? gamePackage : "Unknown").append(" ===\n");
            info.append("Total Files: ").append(totalFiles).append("\n");
            info.append("Total Size: ").append(FileUtils.formatFileSize(totalSize)).append("\n");
            info.append("DLL Mods: ").append(dllModCount).append("\n");
            info.append("DEX Mods: ").append(dexModCount).append("\n");
            info.append("Total Mods: ").append(totalModCount).append("\n");
            info.append("Enabled Mods: ").append(enabledModCount).append("\n");
            info.append("Disabled Mods: ").append(disabledModCount).append("\n");
            return info.toString();
        }
    }

    // Export mod list to text file
    public boolean exportModList(Context context, String gamePackage, File outputFile) {
        try {
            List<File> allMods = getAllMods(context, gamePackage);
            
            try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
                writer.write("=== TerrariaLoader Mod List ===\n");
                writer.write("Game Package: " + gamePackage + "\n");
                writer.write("Export Date: " + new java.util.Date().toString() + "\n");
                writer.write("Total Mods: " + (allMods != null ? allMods.size() : 0) + "\n\n");
                
                if (allMods != null) {
                    for (File mod : allMods) {
                        writer.write("Mod: " + mod.getName() + "\n");
                        writer.write("  Type: " + getModType(mod) + "\n");
                        writer.write("  Status: " + (isModEnabled(mod) ? "Enabled" : "Disabled") + "\n");
                        writer.write("  Size: " + FileUtils.formatFileSize(mod.length()) + "\n");
                        writer.write("  Path: " + mod.getAbsolutePath() + "\n\n");
                    }
                }
            }
            
            LogUtils.logUser("✅ Mod list exported to: " + outputFile.getName());
            return true;
            
        } catch (Exception e) {
            LogUtils.logDebug("Mod list export failed: " + e.getMessage());
            return false;
        }
    }

    // Validate mod directory structure
    public boolean validateModDirectories(Context context, String gamePackage) {
        try {
            File baseDir = PathManager.getGameBaseDir(context, gamePackage);
            File dllModsDir = PathManager.getDllModsDir(context, gamePackage);
            File dexModsDir = PathManager.getDexModsDir(context, gamePackage);
            
            boolean valid = true;
            
            if (baseDir == null) {
                LogUtils.logDebug("Base directory is null for: " + gamePackage);
                return false;
            }
            
            if (!baseDir.exists()) {
                LogUtils.logDebug("Creating base directory: " + baseDir.getAbsolutePath());
                if (!PathManager.ensureDirectoryExists(baseDir)) {
                    valid = false;
                }
            }
            
            if (dllModsDir == null || !dllModsDir.exists()) {
                LogUtils.logDebug("Creating DLL mods directory");
                if (dllModsDir != null && !PathManager.ensureDirectoryExists(dllModsDir)) {
                    valid = false;
                }
            }
            
            if (dexModsDir == null || !dexModsDir.exists()) {
                LogUtils.logDebug("Creating DEX mods directory");
                if (dexModsDir != null && !PathManager.ensureDirectoryExists(dexModsDir)) {
                    valid = false;
                }
            }
            
            return valid;
            
        } catch (Exception e) {
            LogUtils.logDebug("Directory validation failed: " + e.getMessage());
            return false;
        }
    }
}