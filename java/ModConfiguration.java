package com.terrarialoader.loader;

import android.content.Context;
import android.content.SharedPreferences;
import com.terrarialoader.util.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModConfiguration {
    private static final String PREFS_PREFIX = "mod_config_";
    private final String modName;
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, Object> defaultValues;

    public ModConfiguration(Context context, String modName) {
        this.context = context;
        this.modName = modName;
        this.prefs = context.getSharedPreferences(PREFS_PREFIX + modName, Context.MODE_PRIVATE);
        this.defaultValues = new HashMap<>();
        
        LogUtils.logDebug("Created configuration for mod: " + modName);
    }

    // Set default values
    public void setDefault(String key, Object defaultValue) {
        defaultValues.put(key, defaultValue);
    }

    public void setDefaults(Map<String, Object> defaults) {
        defaultValues.putAll(defaults);
    }

    // Get values with type safety
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, (String) defaultValues.getOrDefault(key, defaultValue));
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, (Integer) defaultValues.getOrDefault(key, defaultValue));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, (Boolean) defaultValues.getOrDefault(key, defaultValue));
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, (Float) defaultValues.getOrDefault(key, defaultValue));
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, (Long) defaultValues.getOrDefault(key, defaultValue));
    }

    // Set values
    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    public void setLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
        LogUtils.logDebug("Set " + modName + " config: " + key + " = " + value);
    }

    // Check if key exists
    public boolean contains(String key) {
        return prefs.contains(key) || defaultValues.containsKey(key);
    }

    // Remove key
    public void remove(String key) {
        prefs.edit().remove(key).apply();
        LogUtils.logDebug("Removed " + modName + " config: " + key);
    }

    // Clear all configuration
    public void clear() {
        prefs.edit().clear().apply();
        LogUtils.logUser("Cleared all configuration for mod: " + modName);
    }

    // Reset to defaults
    public void resetToDefaults() {
        prefs.edit().clear().apply();
        
        // Apply default values
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(entry.getKey(), (Float) value);
            } else if (value instanceof Long) {
                editor.putLong(entry.getKey(), (Long) value);
            }
        }
        editor.apply();
        
        LogUtils.logUser("Reset " + modName + " configuration to defaults");
    }

    // Export configuration as JSON
    public String exportToJson() {
        JSONObject json = new JSONObject();
        try {
            Map<String, ?> allPrefs = prefs.getAll();
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            LogUtils.logDebug("Failed to export config to JSON: " + e.getMessage());
        }
        return json.toString();
    }

    // Import configuration from JSON
    public boolean importFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            SharedPreferences.Editor editor = prefs.edit();
            
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);
                
                if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Double) {
                    editor.putFloat(key, ((Double) value).floatValue());
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                }
            }
            
            editor.apply();
            LogUtils.logUser("Imported configuration for mod: " + modName);
            return true;
            
        } catch (JSONException e) {
            LogUtils.logDebug("Failed to import config from JSON: " + e.getMessage());
            return false;
        }
    }

    // Get all configuration keys
    public String[] getAllKeys() {
        Map<String, ?> allPrefs = prefs.getAll();
        return allPrefs.keySet().toArray(new String[0]);
    }

    // Get configuration summary
    public String getConfigSummary() {
        Map<String, ?> allPrefs = prefs.getAll();
        StringBuilder summary = new StringBuilder();
        summary.append("Configuration for ").append(modName).append(":\n");
        
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            summary.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        if (allPrefs.isEmpty()) {
            summary.append("No configuration set (using defaults)\n");
        }
        
        return summary.toString();
    }
}