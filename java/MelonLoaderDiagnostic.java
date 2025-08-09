// File: MelonLoaderDiagnostic.java (Diagnostic Tool)
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/util/MelonLoaderDiagnostic.java

package com.terrarialoader.util;

import android.content.Context;
import com.terrarialoader.loader.MelonLoaderManager;
import java.io.File;

public class MelonLoaderDiagnostic {
    
    public static String generateDetailedDiagnostic(Context context, String gamePackage) {
        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("=== DETAILED MELONLOADER DIAGNOSTIC ===\n\n");
        
        // Check all required directories and files
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        File melonLoaderDir = PathManager.getMelonLoaderDir(context, gamePackage);
        File net8Dir = PathManager.getMelonLoaderNet8Dir(context, gamePackage);
        File net35Dir = PathManager.getMelonLoaderNet35Dir(context, gamePackage);
        File depsDir = PathManager.getMelonLoaderDependenciesDir(context, gamePackage);
        
        diagnostic.append("📁 DIRECTORY STATUS:\n");
        diagnostic.append("Base Dir: ").append(checkDirectory(baseDir)).append("\n");
        diagnostic.append("MelonLoader Dir: ").append(checkDirectory(melonLoaderDir)).append("\n");
        diagnostic.append("NET8 Dir: ").append(checkDirectory(net8Dir)).append("\n");
        diagnostic.append("NET35 Dir: ").append(checkDirectory(net35Dir)).append("\n");
        diagnostic.append("Dependencies Dir: ").append(checkDirectory(depsDir)).append("\n\n");
        
        // Check for required NET8 files
        diagnostic.append("🔸 NET8 RUNTIME FILES:\n");
        String[] net8Files = {
            "MelonLoader.dll",
            "0Harmony.dll", 
            "MonoMod.RuntimeDetour.dll",
            "MonoMod.Utils.dll",
            "Il2CppInterop.Runtime.dll"
        };
        
        int net8Found = 0;
        for (String fileName : net8Files) {
            File file = new File(net8Dir, fileName);
            boolean exists = file.exists() && file.length() > 0;
            diagnostic.append("  ").append(exists ? "✅" : "❌").append(" ").append(fileName);
            if (exists) {
                net8Found++;
                diagnostic.append(" (").append(FileUtils.formatFileSize(file.length())).append(")");
            }
            diagnostic.append("\n");
        }
        diagnostic.append("NET8 Score: ").append(net8Found).append("/").append(net8Files.length).append("\n\n");
        
        // Check for required NET35 files
        diagnostic.append("🔸 NET35 RUNTIME FILES:\n");
        String[] net35Files = {
            "MelonLoader.dll",
            "0Harmony.dll",
            "MonoMod.RuntimeDetour.dll", 
            "MonoMod.Utils.dll"
        };
        
        int net35Found = 0;
        for (String fileName : net35Files) {
            File file = new File(net35Dir, fileName);
            boolean exists = file.exists() && file.length() > 0;
            diagnostic.append("  ").append(exists ? "✅" : "❌").append(" ").append(fileName);
            if (exists) {
                net35Found++;
                diagnostic.append(" (").append(FileUtils.formatFileSize(file.length())).append(")");
            }
            diagnostic.append("\n");
        }
        diagnostic.append("NET35 Score: ").append(net35Found).append("/").append(net35Files.length).append("\n\n");
        
        // Check Dependencies
        diagnostic.append("🔸 DEPENDENCY FILES:\n");
        File supportModulesDir = new File(depsDir, "SupportModules");
        File assemblyGenDir = new File(depsDir, "Il2CppAssemblyGenerator");
        
        diagnostic.append("Support Modules Dir: ").append(checkDirectory(supportModulesDir)).append("\n");
        diagnostic.append("Assembly Generator Dir: ").append(checkDirectory(assemblyGenDir)).append("\n");
        
        // List actual files found
        diagnostic.append("\n📋 FILES FOUND:\n");
        if (melonLoaderDir.exists()) {
            diagnostic.append(listDirectoryContents(melonLoaderDir, ""));
        } else {
            diagnostic.append("MelonLoader directory doesn't exist!\n");
        }
        
        // Generate recommendations
        diagnostic.append("\n💡 RECOMMENDATIONS:\n");
        if (net8Found == 0 && net35Found == 0) {
            diagnostic.append("❌ NO RUNTIME FILES FOUND!\n");
            diagnostic.append("SOLUTION: You need to install MelonLoader files.\n");
            diagnostic.append("Options:\n");
            diagnostic.append("1. Use 'Automated Installation' in Setup Guide\n");
            diagnostic.append("2. Manually download and extract MelonLoader files\n");
            diagnostic.append("3. Use the APK patcher to inject loader\n\n");
        } else if (net8Found > 0) {
            diagnostic.append("✅ Some NET8 files found, but incomplete installation\n");
            diagnostic.append("Missing files need to be added to: ").append(net8Dir.getAbsolutePath()).append("\n\n");
        } else if (net35Found > 0) {
            diagnostic.append("✅ Some NET35 files found, but incomplete installation\n");
            diagnostic.append("Missing files need to be added to: ").append(net35Dir.getAbsolutePath()).append("\n\n");
        }
        
        // Check if automated installation would work
        diagnostic.append("🌐 INTERNET CONNECTIVITY: ");
        if (OnlineInstaller.isOnlineInstallationAvailable()) {
            diagnostic.append("✅ Available - Automated installation possible\n");
            diagnostic.append("RECOMMENDED: Use 'Automated Installation' for easiest setup\n");
        } else {
            diagnostic.append("❌ Not available - Manual installation required\n");
            diagnostic.append("REQUIRED: Download MelonLoader files manually\n");
        }
        
        return diagnostic.toString();
    }
    
    private static String checkDirectory(File dir) {
        if (dir == null) return "❌ null";
        if (!dir.exists()) return "❌ doesn't exist (" + dir.getAbsolutePath() + ")";
        if (!dir.isDirectory()) return "❌ not a directory";
        
        File[] files = dir.listFiles();
        int fileCount = files != null ? files.length : 0;
        return "✅ exists (" + fileCount + " items)";
    }
    
    private static String listDirectoryContents(File dir, String indent) {
        StringBuilder contents = new StringBuilder();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return contents.toString();
        }
        
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            contents.append(indent).append("(empty)\n");
            return contents.toString();
        }
        
        for (File file : files) {
            contents.append(indent);
            if (file.isDirectory()) {
                contents.append("📁 ").append(file.getName()).append("/\n");
                if (indent.length() < 8) { // Limit recursion depth
                    contents.append(listDirectoryContents(file, indent + "  "));
                }
            } else {
                contents.append("📄 ").append(file.getName());
                contents.append(" (").append(FileUtils.formatFileSize(file.length())).append(")\n");
            }
        }
        
        return contents.toString();
    }
    
    // Quick fix suggestions
    public static String getQuickFixSuggestions(Context context, String gamePackage) {
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("🚀 QUICK FIX OPTIONS:\n\n");
        
        suggestions.append("1. AUTOMATED INSTALLATION (Recommended):\n");
        suggestions.append("   • Go to 'MelonLoader Setup Guide'\n");
        suggestions.append("   • Choose 'Automated Online Installation'\n");
        suggestions.append("   • Select MelonLoader or LemonLoader\n");
        suggestions.append("   • Wait for download and extraction\n\n");
        
        suggestions.append("2. MANUAL INSTALLATION:\n");
        suggestions.append("   • Download MelonLoader from GitHub\n");
        suggestions.append("   • Extract files to correct directories\n");
        suggestions.append("   • Follow the manual installation guide\n\n");
        
        suggestions.append("3. APK INJECTION:\n");
        suggestions.append("   • Use 'APK Patcher' feature\n");
        suggestions.append("   • Select Terraria APK\n");
        suggestions.append("   • Inject MelonLoader into APK\n");
        suggestions.append("   • Install modified APK\n\n");
        
        File baseDir = PathManager.getGameBaseDir(context, gamePackage);
        suggestions.append("📍 TARGET DIRECTORY:\n");
        suggestions.append(baseDir.getAbsolutePath()).append("/Loaders/MelonLoader/\n\n");
        
        suggestions.append("⚠️ MAKE SURE TO:\n");
        suggestions.append("• Have stable internet connection (for automated)\n");
        suggestions.append("• Grant file manager permissions (for manual)\n");
        suggestions.append("• Use exact directory paths shown above\n");
        suggestions.append("• Restart TerrariaLoader after installation\n");
        
        return suggestions.toString();
    }
}