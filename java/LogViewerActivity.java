// File: LogViewerActivity.java (Enhanced) - Advanced Logging and Export Options
// Path: /main/java/com/terrarialoader/ui/LogViewerActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.DiagnosticBundleExporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Enhanced LogViewerActivity with advanced filtering, categorization, and diagnostic export
 */
public class LogViewerActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView logRecyclerView;
    private LogCategoryAdapter categoryAdapter;
    private EditText searchEditText;
    private Spinner filterTypeSpinner;
    private Spinner timeRangeSpinner;
    private Switch regexModeSwitch;
    private Switch highlightModeSwitch;
    private TextView logCountText;
    private TextView filterStatusText;
    private Button exportDiagnosticBtn;
    private Button clearFiltersBtn;
    private LinearLayout advancedFiltersLayout;
    private View advancedFiltersToggle;

    // State management
    private String currentSearchQuery = "";
    private LogFilterType currentFilterType = LogFilterType.ALL;
    private TimeRange currentTimeRange = TimeRange.ALL;
    private boolean isRegexMode = false;
    private boolean isHighlightMode = true;
    private boolean showAdvancedFilters = false;
    private List<LogEntry> allLogEntries = new ArrayList<>();
    private List<LogEntry> filteredLogEntries = new ArrayList<>();

    // Filter types
    public enum LogFilterType {
        ALL("All Logs", ""),
        INSTALLATION("Installation", "install|setup|download|extract|patch"),
        MOD_LOADING("Mod Loading", "mod|load|enable|disable|dex|dll"),
        ERRORS("Errors & Warnings", "error|warning|fail|exception|crash"),
        SYSTEM("System Info", "system|device|version|permission"),
        USER_ACTIONS("User Actions", "user|button|click|select"),
        DEBUG("Debug Info", "debug|trace|verbose");

        private final String displayName;
        private final String keywords;

        LogFilterType(String displayName, String keywords) {
            this.displayName = displayName;
            this.keywords = keywords;
        }

        public String getDisplayName() { return displayName; }
        public String getKeywords() { return keywords; }
    }

    // Time ranges
    public enum TimeRange {
        ALL("All Time", 0),
        LAST_HOUR("Last Hour", 60 * 60 * 1000L),
        LAST_DAY("Last 24 Hours", 24 * 60 * 60 * 1000L),
        LAST_WEEK("Last Week", 7 * 24 * 60 * 60 * 1000L);

        private final String displayName;
        private final long milliseconds;

        TimeRange(String displayName, long milliseconds) {
            this.displayName = displayName;
            this.milliseconds = milliseconds;
        }

        public String getDisplayName() { return displayName; }
        public long getMilliseconds() { return milliseconds; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer_enhanced);

        setTitle("📋 Advanced Log Viewer");

        initializeViews();
        setupSpinners();
        setupListeners();
        loadLogEntries();
        applyFilters();
    }

    private void initializeViews() {
        logRecyclerView = findViewById(R.id.logRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner);
        timeRangeSpinner = findViewById(R.id.timeRangeSpinner);
        regexModeSwitch = findViewById(R.id.regexModeSwitch);
        highlightModeSwitch = findViewById(R.id.highlightModeSwitch);
        logCountText = findViewById(R.id.logCountText);
        filterStatusText = findViewById(R.id.filterStatusText);
        exportDiagnosticBtn = findViewById(R.id.exportDiagnosticBtn);
        clearFiltersBtn = findViewById(R.id.clearFiltersBtn);
        advancedFiltersLayout = findViewById(R.id.advancedFiltersLayout);
        advancedFiltersToggle = findViewById(R.id.advancedFiltersToggle);

        // Setup RecyclerView
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new LogCategoryAdapter(this);
        logRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupSpinners() {
        // Filter Type Spinner
        String[] filterTypes = new String[LogFilterType.values().length];
        for (int i = 0; i < LogFilterType.values().length; i++) {
            filterTypes[i] = LogFilterType.values()[i].getDisplayName();
        }
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, filterTypes);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(filterAdapter);

        // Time Range Spinner
        String[] timeRanges = new String[TimeRange.values().length];
        for (int i = 0; i < TimeRange.values().length; i++) {
            timeRanges[i] = TimeRange.values()[i].getDisplayName();
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, timeRanges);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeRangeSpinner.setAdapter(timeAdapter);
    }

    private void setupListeners() {
        // Real-time search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter type changes
        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterType = LogFilterType.values()[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time range changes
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTimeRange = TimeRange.values()[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Regex mode toggle
        regexModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRegexMode = isChecked;
            applyFilters();
        });

        // Highlight mode toggle
        highlightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighlightMode = isChecked;
            categoryAdapter.setHighlightMode(isChecked);
            categoryAdapter.setSearchQuery(currentSearchQuery);
            categoryAdapter.notifyDataSetChanged();
        });

        // Export diagnostic bundle
        exportDiagnosticBtn.setOnClickListener(v -> exportDiagnosticBundle());

        // Clear filters
        clearFiltersBtn.setOnClickListener(v -> clearAllFilters());

        // Advanced filters toggle
        advancedFiltersToggle.setOnClickListener(v -> toggleAdvancedFilters());
    }

    private void loadLogEntries() {
        LogUtils.logDebug("Loading log entries for advanced viewer");
        allLogEntries.clear();

        // Load different types of logs
        loadAppLogs();
        loadGameLogs();
        loadSystemLogs();

        LogUtils.logUser("Loaded " + allLogEntries.size() + " log entries for advanced analysis");
    }

    private void loadAppLogs() {
        try {
            // Load current app logs
            String currentLogs = LogUtils.getLogs();
            parseLogString(currentLogs, LogEntry.LogType.APP, "Current Session");

            // Load rotated app logs
            List<File> logFiles = LogUtils.getAvailableLogFiles();
            for (int i = 0; i < logFiles.size(); i++) {
                File logFile = logFiles.get(i);
                String content = LogUtils.readLogFile(i);
                parseLogString(content, LogEntry.LogType.APP, logFile.getName());
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error loading app logs: " + e.getMessage());
        }
    }

    private void loadGameLogs() {
        try {
            // Try to load MelonLoader game logs if available
            File gameLogsDir = new File(getExternalFilesDir(null), 
                "TerrariaLoader/com.and.games505.TerrariaPaid/Logs");
            
            if (gameLogsDir.exists()) {
                File[] gameLogFiles = gameLogsDir.listFiles((dir, name) -> 
                    name.startsWith("Log") && name.endsWith(".txt"));
                
                if (gameLogFiles != null) {
                    for (File logFile : gameLogFiles) {
                        try {
                            String content = readFileContent(logFile);
                            parseLogString(content, LogEntry.LogType.GAME, logFile.getName());
                        } catch (Exception e) {
                            LogUtils.logDebug("Error reading game log: " + logFile.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Error loading game logs: " + e.getMessage());
        }
    }

    private void loadSystemLogs() {
        try {
            // Add system information as log entries
            addSystemInfoEntry("Device", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            addSystemInfoEntry("Android Version", android.os.Build.VERSION.RELEASE);
            addSystemInfoEntry("API Level", String.valueOf(android.os.Build.VERSION.SDK_INT));
            addSystemInfoEntry("Architecture", System.getProperty("os.arch", "unknown"));
            
            // Add app information
            try {
                android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                addSystemInfoEntry("App Version", pInfo.versionName);
                addSystemInfoEntry("Version Code", String.valueOf(pInfo.versionCode));
            } catch (Exception e) {
                addSystemInfoEntry("App Version", "Unknown");
            }

            // Add storage information
            File appDir = getExternalFilesDir(null);
            if (appDir != null) {
                long totalSpace = appDir.getTotalSpace();
                long freeSpace = appDir.getFreeSpace();
                addSystemInfoEntry("Storage Total", formatFileSize(totalSpace));
                addSystemInfoEntry("Storage Free", formatFileSize(freeSpace));
            }

        } catch (Exception e) {
            LogUtils.logDebug("Error loading system info: " + e.getMessage());
        }
    }

    private void addSystemInfoEntry(String key, String value) {
        LogEntry entry = new LogEntry(
            System.currentTimeMillis(),
            LogEntry.LogLevel.INFO,
            LogEntry.LogType.SYSTEM,
            "SYSTEM",
            key + ": " + value
        );
        allLogEntries.add(entry);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private void parseLogString(String logString, LogEntry.LogType type, String source) {
        if (logString == null || logString.trim().isEmpty()) {
            return;
        }

        String[] lines = logString.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            LogEntry entry = parseLogLine(line, type, source);
            if (entry != null) {
                allLogEntries.add(entry);
            }
        }
    }

    private LogEntry parseLogLine(String line, LogEntry.LogType type, String source) {
        try {
            // Try to parse timestamp and level from line
            // Format: "HH:mm:ss [LEVEL] message" or similar
            
            long timestamp = System.currentTimeMillis(); // Default to now
            LogEntry.LogLevel level = LogEntry.LogLevel.INFO; // Default level
            String tag = source;
            String message = line;

            // Parse timestamp
            if (line.matches("^\\d{2}:\\d{2}:\\d{2}.*")) {
                // Extract timestamp (rough parsing)
                String timeStr = line.substring(0, 8);
                // For simplicity, use current date with parsed time
                // In a real implementation, you'd want more sophisticated parsing
            }

            // Parse log level
            if (line.contains("[DEBUG]")) {
                level = LogEntry.LogLevel.DEBUG;
                message = line.replaceFirst(".*\\[DEBUG\\]\\s*", "");
            } else if (line.contains("[ERROR]") || line.toLowerCase().contains("error")) {
                level = LogEntry.LogLevel.ERROR;
                message = line.replaceFirst(".*\\[ERROR\\]\\s*", "");
            } else if (line.contains("[WARN]") || line.toLowerCase().contains("warning")) {
                level = LogEntry.LogLevel.WARN;
                message = line.replaceFirst(".*\\[WARN\\]\\s*", "");
            } else if (line.contains("[USER]")) {
                level = LogEntry.LogLevel.INFO;
                message = line.replaceFirst(".*\\[USER\\]\\s*", "");
                tag = "USER";
            }

            return new LogEntry(timestamp, level, type, tag, message);
            
        } catch (Exception e) {
            // Fallback: create basic entry
            return new LogEntry(
                System.currentTimeMillis(),
                LogEntry.LogLevel.INFO,
                type,
                source,
                line
            );
        }
    }

    private void applyFilters() {
        filteredLogEntries.clear();
        
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - currentTimeRange.getMilliseconds();

        for (LogEntry entry : allLogEntries) {
            if (!passesFilters(entry, timeThreshold)) {
                continue;
            }
            filteredLogEntries.add(entry);
        }

        // Update UI
        categoryAdapter.updateEntries(filteredLogEntries);
        updateFilterStatus();
    }

    private boolean passesFilters(LogEntry entry, long timeThreshold) {
        // Time range filter
        if (currentTimeRange != TimeRange.ALL && entry.getTimestamp() < timeThreshold) {
            return false;
        }

        // Category filter
        if (currentFilterType != LogFilterType.ALL) {
            String keywords = currentFilterType.getKeywords();
            if (!keywords.isEmpty()) {
                String message = entry.getMessage().toLowerCase();
                String[] keywordArray = keywords.split("\\|");
                boolean matchesKeyword = false;
                for (String keyword : keywordArray) {
                    if (message.contains(keyword.toLowerCase())) {
                        matchesKeyword = true;
                        break;
                    }
                }
                if (!matchesKeyword) {
                    return false;
                }
            }
        }

        // Search query filter
        if (!currentSearchQuery.trim().isEmpty()) {
            String searchText = currentSearchQuery.toLowerCase();
            String message = entry.getMessage().toLowerCase();
            String tag = entry.getTag().toLowerCase();

            if (isRegexMode) {
                try {
                    Pattern pattern = Pattern.compile(currentSearchQuery, Pattern.CASE_INSENSITIVE);
                    if (!pattern.matcher(message).find() && !pattern.matcher(tag).find()) {
                        return false;
                    }
                } catch (PatternSyntaxException e) {
                    // Fall back to simple text search if regex is invalid
                    if (!message.contains(searchText) && !tag.contains(searchText)) {
                        return false;
                    }
                }
            } else {
                if (!message.contains(searchText) && !tag.contains(searchText)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateFilterStatus() {
        int totalCount = allLogEntries.size();
        int filteredCount = filteredLogEntries.size();
        
        logCountText.setText(String.format("Showing %d of %d entries", filteredCount, totalCount));
        
        StringBuilder status = new StringBuilder();
        if (currentFilterType != LogFilterType.ALL) {
            status.append("Category: ").append(currentFilterType.getDisplayName()).append(" • ");
        }
        if (currentTimeRange != TimeRange.ALL) {
            status.append("Time: ").append(currentTimeRange.getDisplayName()).append(" • ");
        }
        if (!currentSearchQuery.trim().isEmpty()) {
            status.append("Search: '").append(currentSearchQuery).append("'");
            if (isRegexMode) status.append(" (regex)");
            status.append(" • ");
        }
        
        String statusText = status.toString();
        if (statusText.endsWith(" • ")) {
            statusText = statusText.substring(0, statusText.length() - 3);
        }
        
        filterStatusText.setText(statusText.isEmpty() ? "No filters active" : statusText);
    }

    private void clearAllFilters() {
        searchEditText.setText("");
        filterTypeSpinner.setSelection(0);
        timeRangeSpinner.setSelection(0);
        regexModeSwitch.setChecked(false);
        highlightModeSwitch.setChecked(true);
        
        currentSearchQuery = "";
        currentFilterType = LogFilterType.ALL;
        currentTimeRange = TimeRange.ALL;
        isRegexMode = false;
        isHighlightMode = true;
        
        applyFilters();
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void toggleAdvancedFilters() {
        showAdvancedFilters = !showAdvancedFilters;
        advancedFiltersLayout.setVisibility(showAdvancedFilters ? View.VISIBLE : View.GONE);
        
        TextView toggleText = advancedFiltersToggle.findViewById(R.id.advancedFiltersToggleText);
        toggleText.setText(showAdvancedFilters ? "▼ Hide Advanced Filters" : "▶ Show Advanced Filters");
    }

    private void exportDiagnosticBundle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Diagnostic Bundle");
        builder.setMessage("This will create a comprehensive diagnostic bundle including:\n\n" +
                          "• All application logs\n" +
                          "• Game logs (if available)\n" +
                          "• System information\n" +
                          "• Device specifications\n" +
                          "• Installed mod information\n" +
                          "• Directory structure\n\n" +
                          "This bundle can be shared with support for troubleshooting.");
        
        builder.setPositiveButton("Export Bundle", (dialog, which) -> {
            performDiagnosticExport();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performDiagnosticExport() {
        Toast.makeText(this, "Creating diagnostic bundle...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                File bundleFile = DiagnosticBundleExporter.createDiagnosticBundle(this);
                
                runOnUiThread(() -> {
                    if (bundleFile != null && bundleFile.exists()) {
                        shareDiagnosticBundle(bundleFile);
                    } else {
                        Toast.makeText(this, "Failed to create diagnostic bundle", Toast.LENGTH_LONG).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error creating bundle: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                LogUtils.logDebug("Diagnostic export error: " + e.getMessage());
            }
        }).start();
    }

    private void shareDiagnosticBundle(File bundleFile) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, getPackageName() + ".provider", bundleFile));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TerrariaLoader Diagnostic Bundle");
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "TerrariaLoader diagnostic bundle generated on " + 
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Diagnostic Bundle"));
            
            Toast.makeText(this, "Diagnostic bundle ready to share", Toast.LENGTH_SHORT).show();
            LogUtils.logUser("Diagnostic bundle exported: " + bundleFile.getName());
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share bundle: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Bundle sharing error: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_export_filtered) {
            exportFilteredLogs();
            return true;
        } else if (id == R.id.action_copy_selected) {
            copySelectedLogs();
            return true;
        } else if (id == R.id.action_save_search) {
            saveCurrentSearch();
            return true;
        } else if (id == R.id.action_refresh_logs) {
            refreshLogs();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void exportFilteredLogs() {
        try {
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) exportDir.mkdirs();

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", 
                java.util.Locale.getDefault()).format(new java.util.Date());
            File exportFile = new File(exportDir, "filtered_logs_" + timestamp + ".txt");

            try (java.io.FileWriter writer = new java.io.FileWriter(exportFile)) {
                writer.write("=== TerrariaLoader Filtered Logs Export ===\n");
                writer.write("Export Date: " + new java.util.Date().toString() + "\n");
                writer.write("Total Entries: " + filteredLogEntries.size() + "\n");
                writer.write("Filters Applied: " + filterStatusText.getText() + "\n");
                writer.write("=" + "=".repeat(50) + "\n\n");

                for (LogEntry entry : filteredLogEntries) {
                    writer.write(entry.toFormattedString() + "\n");
                }
            }

            Toast.makeText(this, "Filtered logs exported to: " + exportFile.getName(), Toast.LENGTH_LONG).show();
            LogUtils.logUser("Filtered logs exported: " + exportFile.getName());

        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Filtered export error: " + e.getMessage());
        }
    }

    private void copySelectedLogs() {
        // Implementation for copying selected logs to clipboard
        Toast.makeText(this, "Copy selected logs feature - implementation pending", Toast.LENGTH_SHORT).show();
    }

    private void saveCurrentSearch() {
        // Implementation for saving current search/filter configuration
        Toast.makeText(this, "Save current search feature - implementation pending", Toast.LENGTH_SHORT).show();
    }

    private void refreshLogs() {
        loadLogEntries();
        applyFilters();
        Toast.makeText(this, "Logs refreshed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh logs when returning to activity
        refreshLogs();
    }

    // Utility method
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}