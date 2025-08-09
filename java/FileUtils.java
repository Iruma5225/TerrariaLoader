// File: FileUtils.java (Utility Class) - Phase 1 Complete (Error-Free)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/FileUtils.java

package com.terrarialoader.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import java.io.*;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static File ensureSubdirectory(Context context, String name) {
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }

        File subDir = new File(baseDir, name);
        if (!subDir.exists()) {
            if (!subDir.mkdirs()) {
                Log.e(TAG, "Directory creation failed: " + subDir.getAbsolutePath());
                LogUtils.logDebug("Failed to create directory: " + subDir.getAbsolutePath());
                return null;
            } else {
                LogUtils.logDebug("Created directory: " + subDir.getAbsolutePath());
            }
        }
        return subDir;
    }

    public static boolean copyUriToFile(Context context, Uri sourceUri, File destFile) {
        LogUtils.logDebug("Copying file from URI to: " + destFile.getAbsolutePath());
        
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            
            if (in == null) {
                LogUtils.logDebug("Failed to open input stream from URI");
                return false;
            }
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            LogUtils.logDebug("File copy completed: " + totalBytes + " bytes copied");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "File copy failed: " + e.getMessage(), e);
            LogUtils.logDebug("File copy failed: " + e.getMessage());
            
            // Clean up partial file on failure
            if (destFile.exists()) {
                destFile.delete();
            }
            
            return false;
        }
    }

    // Enhanced method to get filename from URI
    public static String getFilenameFromUri(Context context, Uri uri) {
        String filename = null;
        
        // Try to get filename from content resolver
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename from content resolver: " + e.getMessage());
        }
        
        // Fallback: try to extract from URI path
        if (filename == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                    filename = path.substring(lastSlash + 1);
                }
            }
        }
        
        LogUtils.logDebug("Extracted filename: " + filename);
        return filename;
    }

    // Enhanced file size calculation
    public static long getFileSize(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get file size: " + e.getMessage());
        }
        
        // Fallback: try to read the stream
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                return in.available();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not determine file size from stream: " + e.getMessage());
        }
        
        return -1;
    }

    public static boolean toggleModFile(File modFile) {
        if (modFile == null || !modFile.exists()) {
            LogUtils.logDebug("Cannot toggle non-existent file");
            return false;
        }

        String fileName = modFile.getName();
        File newFile;

        if (fileName.endsWith(".dex") || fileName.endsWith(".jar")) {
            // Disable mod
            newFile = new File(modFile.getParentFile(), fileName + ".disabled");
        } else if (fileName.endsWith(".dex.disabled")) {
            // Enable dex mod
            newFile = new File(modFile.getParentFile(), 
                fileName.substring(0, fileName.length() - ".disabled".length()));
        } else if (fileName.endsWith(".jar.disabled")) {
            // Enable jar mod
            newFile = new File(modFile.getParentFile(), 
                fileName.substring(0, fileName.length() - ".disabled".length()));
        } else {
            LogUtils.logDebug("Unknown file type for toggle: " + fileName);
            return false;
        }

        boolean success = modFile.renameTo(newFile);
        if (success) {
            LogUtils.logDebug("Toggled mod: " + fileName + " -> " + newFile.getName());
        } else {
            LogUtils.logDebug("Failed to toggle mod: " + fileName);
        }
        
        return success;
    }

    public static String[] listFilenamesInDir(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return new String[0];
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return new String[0];
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }

    // Enhanced directory operations
    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir == null) {
            return 0;
        }
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }

    public static int getFileCount(File dir) {
        if (dir == null) {
            return 0;
        }
        
        if (!dir.isDirectory()) {
            return dir.exists() ? 1 : 0;
        }
        
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += getFileCount(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    // File validation utilities
    public static boolean isValidModFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".dex") || name.endsWith(".dex.disabled") ||
               name.endsWith(".jar") || name.endsWith(".jar.disabled");
    }

    public static boolean isModEnabled(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".dex") || name.endsWith(".jar");
    }

    // Safe file operations
    public static boolean safeDelete(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            boolean deleted = file.delete();
            if (deleted) {
                LogUtils.logDebug("Successfully deleted: " + file.getName());
            } else {
                LogUtils.logDebug("Failed to delete: " + file.getName());
            }
            return deleted;
        } catch (Exception e) {
            LogUtils.logDebug("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    public static boolean safeRename(File source, File destination) {
        if (source == null || !source.exists() || destination == null) {
            return false;
        }
        
        try {
            boolean renamed = source.renameTo(destination);
            if (renamed) {
                LogUtils.logDebug("Successfully renamed: " + source.getName() + " -> " + destination.getName());
            } else {
                LogUtils.logDebug("Failed to rename: " + source.getName() + " -> " + destination.getName());
            }
            return renamed;
        } catch (Exception e) {
            LogUtils.logDebug("Error renaming file: " + e.getMessage());
            return false;
        }
    }

    // Create backup of file
    public static File createBackup(File originalFile) {
        if (originalFile == null || !originalFile.exists()) {
            return null;
        }
        
        String backupName = originalFile.getName() + ".backup." + System.currentTimeMillis();
        File backupFile = new File(originalFile.getParentFile(), backupName);
        
        try (FileInputStream in = new FileInputStream(originalFile);
             FileOutputStream out = new FileOutputStream(backupFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            LogUtils.logDebug("Created backup: " + backupName);
            return backupFile;
            
        } catch (Exception e) {
            LogUtils.logDebug("Failed to create backup: " + e.getMessage());
            if (backupFile.exists()) {
                backupFile.delete();
            }
            return null;
        }
    }

    // Format file size for display
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Clean up temporary files
    public static void cleanupTempFiles(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] tempFiles = cacheDir.listFiles((dir, name) -> 
                    name.startsWith("temp_") || name.endsWith(".tmp"));
                
                if (tempFiles != null) {
                    int deletedCount = 0;
                    for (File tempFile : tempFiles) {
                        if (tempFile.delete()) {
                            deletedCount++;
                        }
                    }
                    
                    if (deletedCount > 0) {
                        LogUtils.logDebug("Cleaned up " + deletedCount + " temporary files");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error cleaning up temp files: " + e.getMessage());
        }
    }

    // Check available storage space
    public static long getAvailableSpace(File dir) {
        try {
            if (dir != null && dir.exists()) {
                return dir.getFreeSpace();
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get available space: " + e.getMessage());
        }
        return -1;
    }

    // Ensure sufficient space before operation
    public static boolean hasSufficientSpace(File dir, long requiredBytes) {
        long availableSpace = getAvailableSpace(dir);
        if (availableSpace < 0) {
            // Cannot determine space, assume it's available
            return true;
        }
        
        // Add 10% buffer
        long requiredWithBuffer = (long) (requiredBytes * 1.1);
        return availableSpace >= requiredWithBuffer;
    }
}