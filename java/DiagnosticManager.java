package com.terrarialoader.diagnostic;

import android.content.Context;
import android.os.Environment;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiagnosticManager {
    private final Context context;
    private final File gameBaseDir;
    private final List<String> reportLines = new ArrayList<>();
    private final Map<String, Boolean> checks = new LinkedHashMap<>();
    private final String gamePackage = "com.and.games505.TerrariaPaid";

    public DiagnosticManager(Context context) {
        this.context = context;
        this.gameBaseDir = new File(context.getExternalFilesDir(null), "TerrariaLoader/" + gamePackage);
    }

    public String runFullDiagnostics() {
        log("🔧 Starting Offline Diagnostics...");
        reportLines.clear();
        checks.clear();

        checkDirectoryStructure();
        checkLoaderPresence();
        checkFileIntegrity();
        checkPermissions();
        checkLogRotation();
        checkReadmes();

        log("📊 Diagnostics completed.");
        generateReport();
        return getFormattedReport();
    }

    public boolean attemptSelfRepair() {
        log("🛠️ Starting repair process...");
        boolean success = true;

        for (String path : getExpectedDirectories()) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    log("✅ Created: " + dir.getAbsolutePath());
                } else {
                    log("❌ Failed to create: " + dir.getAbsolutePath());
                    success = false;
                }
            }
        }

        createReadmes();
        rotateLogsIfNeeded();

        log(success ? "✅ Repair completed successfully." : "⚠️ Repair completed with issues.");
        return success;
    }

    private void checkDirectoryStructure() {
        log("📂 Checking directory structure...");
        for (String path : getExpectedDirectories()) {
            File f = new File(path);
            boolean exists = f.exists() && f.isDirectory();
            checks.put("Directory: " + f.getName(), exists);
        }
    }

    private void checkLoaderPresence() {
        log("🛠️ Checking loader status...");
        File melonDir = new File(gameBaseDir, "Loaders/MelonLoader");
        checks.put("MelonLoader Installed", melonDir.exists());

        File net8 = new File(melonDir, "net8");
        File net35 = new File(melonDir, "net35");
        File deps = new File(melonDir, "Dependencies");
        checks.put("ML/net8 Present", net8.exists());
        checks.put("ML/net35 Present", net35.exists());
        checks.put("ML/Dependencies Present", deps.exists());
    }

    private void checkFileIntegrity() {
        log("🔍 Checking file integrity...");
        File logsDir = new File(gameBaseDir, "Logs");
        File log = new File(logsDir, "Log.txt");
        checks.put("Log.txt Exists", log.exists());

        File[] oldLogs = logsDir.listFiles((dir, name) -> name.matches("Log[1-5]\\.txt"));
        checks.put("Old Logs Present (1-5)", oldLogs != null && oldLogs.length > 0);
    }

    private void checkPermissions() {
        log("🔐 Checking permissions...");
        File test = new File(context.getExternalFilesDir(null), "perm_test.txt");
        try {
            FileWriter writer = new FileWriter(test);
            writer.write("test");
            writer.close();
            checks.put("Write Permission", true);
            test.delete();
        } catch (IOException e) {
            checks.put("Write Permission", false);
        }
    }

    private void checkLogRotation() {
        log("🕒 Checking log rotation...");
        File logsDir = new File(gameBaseDir, "AppLogs");
        File[] logFiles = logsDir.listFiles((dir, name) -> name.startsWith("AppLog"));
        checks.put("AppLogs Present", logFiles != null && logFiles.length > 0);
    }

    private void checkReadmes() {
        log("📘 Checking README files...");
        File[] dirs = new File[]{
            new File(gameBaseDir, "Mods/DEX"),
            new File(gameBaseDir, "Mods/DLL"),
            new File(gameBaseDir, "Plugins"),
            new File(gameBaseDir, "UserLibs"),
            new File(gameBaseDir, "Logs"),
            new File(gameBaseDir, "AppLogs")
        };

        for (File dir : dirs) {
            File readme = new File(dir, "README.txt");
            checks.put(dir.getName() + "/README Present", readme.exists());
        }
    }

    private void rotateLogsIfNeeded() {
        File logsDir = new File(gameBaseDir, "AppLogs");
        File currentLog = new File(logsDir, "AppLog.txt");
        if (currentLog.exists()) {
            for (int i = 5; i >= 1; i--) {
                File prev = new File(logsDir, "AppLog" + i + ".txt");
                if (prev.exists() && i == 5) prev.delete();
                else if (prev.exists())
                    prev.renameTo(new File(logsDir, "AppLog" + (i + 1) + ".txt"));
            }
            currentLog.renameTo(new File(logsDir, "AppLog1.txt"));
        }
    }

    private void createReadmes() {
        String[] dirs = {
            "Mods/DEX", "Mods/DLL", "Plugins", "UserLibs", "Logs", "AppLogs"
        };

        for (String relPath : dirs) {
            File dir = new File(gameBaseDir, relPath);
            File readme = new File(dir, "README.txt");
            if (!readme.exists()) {
                try {
                    FileWriter writer = new FileWriter(readme);
                    writer.write("README for " + relPath + "\nThis directory is used by TerrariaLoader.");
                    writer.close();
                    log("📘 Created README in: " + relPath);
                } catch (IOException e) {
                    log("❌ Failed to write README in: " + relPath);
                }
            }
        }
    }

    private void generateReport() {
        reportLines.add("=== TerrariaLoader Offline Diagnostic Report ===");
        reportLines.add("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        reportLines.add("Base Directory: " + gameBaseDir.getAbsolutePath());
        reportLines.add("\nChecks:");

        for (Map.Entry<String, Boolean> entry : checks.entrySet()) {
            String icon = entry.getValue() ? "✅" : "❌";
            reportLines.add(icon + " " + entry.getKey());
        }

        reportLines.add("\nSuggestions:");
        for (Map.Entry<String, Boolean> entry : checks.entrySet()) {
            if (!entry.getValue()) {
                reportLines.add("• Fix: " + entry.getKey());
            }
        }
    }

    public boolean exportReportToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : reportLines) {
                writer.write(line + "\n");
            }
            log("✅ Report exported: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log("❌ Failed to export report: " + e.getMessage());
            return false;
        }
    }

    public String getFormattedReport() {
        StringBuilder sb = new StringBuilder();
        for (String line : reportLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private void log(String message) {
        LogUtils.logDebug("[DiagnosticManager] " + message);
    }

    private String[] getExpectedDirectories() {
        String base = gameBaseDir.getAbsolutePath();
        return new String[]{
            base,
            base + "/Mods", base + "/Mods/DEX", base + "/Mods/DLL",
            base + "/Config", base + "/Logs", base + "/AppLogs", base + "/Backups",
            base + "/Loaders/MelonLoader", base + "/Loaders/MelonLoader/net8",
            base + "/Loaders/MelonLoader/net35", base + "/Loaders/MelonLoader/Dependencies",
            base + "/Plugins", base + "/UserLibs"
        };
    }
}