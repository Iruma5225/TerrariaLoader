// File: LogCategoryAdapter.java - Advanced Log Display Adapter
// Path: /main/java/com/terrarialoader/ui/LogCategoryAdapter.java

package com.terrarialoader.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.terrarialoader.R;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced RecyclerView adapter for displaying categorized log entries
 * with highlighting, filtering, and rich formatting
 */
public class LogCategoryAdapter extends RecyclerView.Adapter<LogCategoryAdapter.LogViewHolder> {
    
    private final Context context;
    private List<LogEntry> logEntries = new ArrayList<>();
    private String searchQuery = "";
    private boolean highlightMode = true;
    private LogEntry.LogLevel filterLevel = null;
    private LogEntry.LogType filterType = null;
    
    // Interface for item interactions
    public interface LogItemClickListener {
        void onLogItemClick(LogEntry logEntry);
        void onLogItemLongClick(LogEntry logEntry);
    }
    
    private LogItemClickListener clickListener;
    
    public LogCategoryAdapter(Context context) {
        this.context = context;
    }
    
    public void setClickListener(LogItemClickListener listener) {
        this.clickListener = listener;
    }
    
    public void updateEntries(List<LogEntry> entries) {
        this.logEntries = entries != null ? entries : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
        notifyDataSetChanged();
    }
    
    public void setHighlightMode(boolean enabled) {
        this.highlightMode = enabled;
        notifyDataSetChanged();
    }
    
    public void setLevelFilter(LogEntry.LogLevel level) {
        this.filterLevel = level;
        notifyDataSetChanged();
    }
    
    public void setTypeFilter(LogEntry.LogType type) {
        this.filterType = type;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log_entry, parent, false);
        return new LogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return logEntries.size();
    }
    
    public class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView timestampText;
        private final TextView levelText;
        private final TextView tagText;
        private final TextView messageText;
        private final View levelIndicator;
        private final View rootView;
        
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampText = itemView.findViewById(R.id.logTimestamp);
            levelText = itemView.findViewById(R.id.logLevel);
            tagText = itemView.findViewById(R.id.logTag);
            messageText = itemView.findViewById(R.id.logMessage);
            levelIndicator = itemView.findViewById(R.id.levelIndicator);
            rootView = itemView;
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.onLogItemClick(logEntries.get(pos));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.onLogItemLongClick(logEntries.get(pos));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(LogEntry entry) {
            // Set timestamp
            timestampText.setText(entry.getFormattedTimestamp());
            
            // Set level with color
            levelText.setText(entry.getLevel().getDisplayName());
            levelText.setTextColor(Color.parseColor(entry.getLevel().getColor()));
            
            // Set tag
            tagText.setText(entry.getTag());
            
            // Set message with highlighting
            if (highlightMode && !searchQuery.trim().isEmpty()) {
                messageText.setText(highlightText(entry.getMessage(), searchQuery));
            } else {
                messageText.setText(entry.getMessage());
            }
            
            // Set level indicator color
            levelIndicator.setBackgroundColor(Color.parseColor(entry.getLevel().getColor()));
            
            // Set background based on log type and importance
            setItemBackground(entry);
            
            // Set typography based on level
            setTypography(entry);
            
            // Add type badge for system/game logs
            addTypeBadge(entry);
        }
        
        private SpannableString highlightText(String text, String query) {
            SpannableString spannableString = new SpannableString(text);
            
            if (query.trim().isEmpty()) {
                return spannableString;
            }
            
            try {
                // Try regex highlighting first
                if (isValidRegex(query)) {
                    Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(text);
                    
                    while (matcher.find()) {
                        spannableString.setSpan(
                            new BackgroundColorSpan(Color.YELLOW),
                            matcher.start(),
                            matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        spannableString.setSpan(
                            new ForegroundColorSpan(Color.BLACK),
                            matcher.start(),
                            matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                } else {
                    // Fall back to simple text highlighting
                    String lowerText = text.toLowerCase();
                    String lowerQuery = query.toLowerCase();
                    int index = lowerText.indexOf(lowerQuery);
                    
                    while (index >= 0) {
                        spannableString.setSpan(
                            new BackgroundColorSpan(Color.YELLOW),
                            index,
                            index + query.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        spannableString.setSpan(
                            new ForegroundColorSpan(Color.BLACK),
                            index,
                            index + query.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        
                        index = lowerText.indexOf(lowerQuery, index + 1);
                    }
                }
            } catch (Exception e) {
                // If highlighting fails, return original text
                return spannableString;
            }
            
            return spannableString;
        }
        
        private boolean isValidRegex(String regex) {
            try {
                Pattern.compile(regex);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        private void setItemBackground(LogEntry entry) {
            int backgroundColor;
            
            switch (entry.getLevel()) {
                case ERROR:
                    backgroundColor = Color.parseColor("#FFEBEE");
                    break;
                case WARN:
                    backgroundColor = Color.parseColor("#FFF8E1");
                    break;
                case USER:
                    backgroundColor = Color.parseColor("#E8F5E8");
                    break;
                case DEBUG:
                    backgroundColor = Color.parseColor("#F5F5F5");
                    break;
                default:
                    backgroundColor = Color.WHITE;
                    break;
            }
            
            rootView.setBackgroundColor(backgroundColor);
        }
        
        private void setTypography(LogEntry entry) {
            switch (entry.getLevel()) {
                case ERROR:
                    messageText.setTypeface(null, Typeface.BOLD);
                    break;
                case WARN:
                    messageText.setTypeface(null, Typeface.ITALIC);
                    break;
                case USER:
                    messageText.setTypeface(null, Typeface.BOLD);
                    break;
                default:
                    messageText.setTypeface(null, Typeface.NORMAL);
                    break;
            }
        }
        
        private void addTypeBadge(LogEntry entry) {
            if (entry.getType() != LogEntry.LogType.APP) {
                // Add type indicator to tag
                String tagWithType = "[" + entry.getType().getDisplayName() + "] " + entry.getTag();
                tagText.setText(tagWithType);
                
                // Color code the type
                switch (entry.getType()) {
                    case GAME:
                        tagText.setTextColor(Color.parseColor("#FF9800"));
                        break;
                    case SYSTEM:
                        tagText.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    default:
                        tagText.setTextColor(Color.parseColor("#666666"));
                        break;
                }
            } else {
                tagText.setTextColor(Color.parseColor("#666666"));
            }
        }
    }
    
    // Filter methods
    public List<LogEntry> getFilteredEntries() {
        List<LogEntry> filtered = new ArrayList<>();
        
        for (LogEntry entry : logEntries) {
            if (matchesFilters(entry)) {
                filtered.add(entry);
            }
        }
        
        return filtered;
    }
    
    private boolean matchesFilters(LogEntry entry) {
        // Level filter
        if (filterLevel != null && entry.getLevel() != filterLevel) {
            return false;
        }
        
        // Type filter
        if (filterType != null && entry.getType() != filterType) {
            return false;
        }
        
        // Search query filter
        if (!searchQuery.trim().isEmpty()) {
            return entry.matchesFilter(searchQuery);
        }
        
        return true;
    }
    
    // Statistics methods
    public int getErrorCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == LogEntry.LogLevel.ERROR) {
                count++;
            }
        }
        return count;
    }
    
    public int getWarningCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == LogEntry.LogLevel.WARN) {
                count++;
            }
        }
        return count;
    }
    
    public int getImportantCount() {
        int count = 0;
        for (LogEntry entry : logEntries) {
            if (entry.isImportant()) {
                count++;
            }
        }
        return count;
    }
    
    // Export filtered entries
    public String exportFilteredEntries() {
        StringBuilder export = new StringBuilder();
        export.append("=== Filtered Log Export ===\n");
        export.append("Total entries: ").append(getItemCount()).append("\n");
        export.append("Errors: ").append(getErrorCount()).append("\n");
        export.append("Warnings: ").append(getWarningCount()).append("\n");
        export.append("Important: ").append(getImportantCount()).append("\n");
        export.append("Export time: ").append(new java.util.Date().toString()).append("\n");
        export.append("=" + "=".repeat(50)).append("\n\n");
        
        for (LogEntry entry : getFilteredEntries()) {
            export.append(entry.toFormattedString()).append("\n");
        }
        
        return export.toString();
    }
    
    // Clear all entries
    public void clear() {
        logEntries.clear();
        notifyDataSetChanged();
    }
    
    // Add single entry
    public void addEntry(LogEntry entry) {
        if (entry != null) {
            logEntries.add(entry);
            notifyItemInserted(logEntries.size() - 1);
        }
    }
    
    // Add multiple entries
    public void addEntries(List<LogEntry> entries) {
        if (entries != null && !entries.isEmpty()) {
            int startPosition = logEntries.size();
            logEntries.addAll(entries);
            notifyItemRangeInserted(startPosition, entries.size());
        }
    }
}