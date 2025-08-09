// File: LogEntry.java - Enhanced Log Entry Model for Advanced Logging
// Path: /main/java/com/terrarialoader/ui/LogEntry.java

package com.terrarialoader.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Enhanced log entry model for advanced logging system
 * Supports categorization, filtering, and rich metadata
 */
public class LogEntry {
    
    public enum LogLevel {
        DEBUG("DEBUG", "#808080"),
        INFO("INFO", "#2196F3"),
        WARN("WARN", "#FF9800"),
        ERROR("ERROR", "#F44336"),
        USER("USER", "#4CAF50");
        
        private final String displayName;
        private final String color;
        
        LogLevel(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    public enum LogType {
        APP("App", "Application logs"),
        GAME("Game", "Game/MelonLoader logs"),
        SYSTEM("System", "System information");
        
        private final String displayName;
        private final String description;
        
        LogType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final long timestamp;
    private final LogLevel level;
    private final LogType type;
    private final String tag;
    private final String message;
    private final String threadName;
    private final String className;
    
    // Primary constructor
    public LogEntry(long timestamp, LogLevel level, LogType type, String tag, String message) {
        this.timestamp = timestamp;
        this.level = level != null ? level : LogLevel.INFO;
        this.type = type != null ? type : LogType.APP;
        this.tag = tag != null ? tag : "UNKNOWN";
        this.message = message != null ? message : "";
        this.threadName = Thread.currentThread().getName();
        this.className = getCallingClassName();
    }
    
    // Convenience constructor for app logs
    public LogEntry(LogLevel level, String tag, String message) {
        this(System.currentTimeMillis(), level, LogType.APP, tag, message);
    }
    
    // Convenience constructor for simple messages
    public LogEntry(String message) {
        this(System.currentTimeMillis(), LogLevel.INFO, LogType.APP, "APP", message);
    }
    
    // Getters
    public long getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }
    public LogType getType() { return type; }
    public String getTag() { return tag; }
    public String getMessage() { return message; }
    public String getThreadName() { return threadName; }
    public String getClassName() { return className; }
    
    // Formatted timestamp
    public String getFormattedTimestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date(timestamp));
    }
    
    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }
    
    public String getFormattedDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }
    
    // Formatted string representation
    public String toFormattedString() {
        return String.format("[%s] [%s] [%s] %s: %s", 
            getFormattedTimestamp(), 
            level.getDisplayName(), 
            type.getDisplayName(),
            tag, 
            message);
    }
    
    // Detailed string representation
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp: ").append(getFormattedDateTime()).append("\n");
        sb.append("Level: ").append(level.getDisplayName()).append("\n");
        sb.append("Type: ").append(type.getDisplayName()).append("\n");
        sb.append("Tag: ").append(tag).append("\n");
        sb.append("Thread: ").append(threadName).append("\n");
        if (className != null && !className.isEmpty()) {
            sb.append("Class: ").append(className).append("\n");
        }
        sb.append("Message: ").append(message);
        return sb.toString();
    }
    
    // JSON representation
    public String toJson() {
        return String.format(
            "{\"timestamp\":%d,\"level\": %s\",\"type\":\"%s\",\"tag\":\"%s\",\"message\":\"%s\",\"thread\":\"%s\"}",
            timestamp,
            level.getDisplayName(),
            type.getDisplayName(),
            escapeJson(tag),
            escapeJson(message),
            escapeJson(threadName)
        );
    }
    
    // Filtering helpers
    public boolean matchesFilter(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }
        
        String query = searchQuery.toLowerCase();
        return message.toLowerCase().contains(query) ||
               tag.toLowerCase().contains(query) ||
               level.getDisplayName().toLowerCase().contains(query) ||
               type.getDisplayName().toLowerCase().contains(query);
    }
    
    public boolean matchesLevel(LogLevel filterLevel) {
        return filterLevel == null || this.level == filterLevel;
    }
    
    public boolean matchesType(LogType filterType) {
        return filterType == null || this.type == filterType;
    }
    
    public boolean matchesTimeRange(long startTime, long endTime) {
        return timestamp >= startTime && timestamp <= endTime;
    }
    
    // Priority for sorting (higher = more important)
    public int getPriority() {
        switch (level) {
            case ERROR: return 4;
            case WARN: return 3;
            case USER: return 2;
            case INFO: return 1;
            case DEBUG: return 0;
            default: return 0;
        }
    }
    
    // Get severity color for UI display
    public String getSeverityColor() {
        return level.getColor();
    }
    
    // Check if this is an important log entry
    public boolean isImportant() {
        return level == LogLevel.ERROR || level == LogLevel.WARN || 
               message.toLowerCase().contains("fail") ||
               message.toLowerCase().contains("error") ||
               message.toLowerCase().contains("crash");
    }
    
    // Get calling class name for debugging
    private String getCallingClassName() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // Skip the first few elements (Thread.getStackTrace, this constructor, etc.)
            for (int i = 3; i < stackTrace.length && i < 8; i++) {
                String className = stackTrace[i].getClassName();
                if (!className.startsWith("java.") && 
                    !className.startsWith("android.") &&
                    !className.equals(LogEntry.class.getName())) {
                    return className.substring(className.lastIndexOf('.') + 1);
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during class name detection
        }
        return null;
    }
    
    // Helper method to escape JSON strings
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Factory methods for common log types
    public static LogEntry debug(String tag, String message) {
        return new LogEntry(LogLevel.DEBUG, tag, message);
    }
    
    public static LogEntry info(String tag, String message) {
        return new LogEntry(LogLevel.INFO, tag, message);
    }
    
    public static LogEntry warn(String tag, String message) {
        return new LogEntry(LogLevel.WARN, tag, message);
    }
    
    public static LogEntry error(String tag, String message) {
        return new LogEntry(LogLevel.ERROR, tag, message);
    }
    
    public static LogEntry user(String tag, String message) {
        return new LogEntry(LogLevel.USER, tag, message);
    }
    
    public static LogEntry system(String message) {
        return new LogEntry(System.currentTimeMillis(), LogLevel.INFO, LogType.SYSTEM, "SYSTEM", message);
    }
    
    public static LogEntry game(String tag, String message) {
        return new LogEntry(System.currentTimeMillis(), LogLevel.INFO, LogType.GAME, tag, message);
    }
    
    @Override
    public String toString() {
        return toFormattedString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LogEntry logEntry = (LogEntry) obj;
        return timestamp == logEntry.timestamp &&
               level == logEntry.level &&
               type == logEntry.type &&
               tag.equals(logEntry.tag) &&
               message.equals(logEntry.message);
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(timestamp);
        result = 31 * result + level.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}