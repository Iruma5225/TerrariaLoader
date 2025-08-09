// File: ModManagementActivity.java - Pure Mod Management (Post-Installation)
// Path: /main/java/com/terrarialoader/ui/ModManagementActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.installer.ModInstaller;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.ui.ModListAdapter;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

/**
 * Pure mod management activity - assumes loader is already installed
 * Focused solely on managing DLL and DEX mods
 */
public class ModManagementActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_DLL = 1001;
    private static final int REQUEST_SELECT_DEX = 1002;
    
    // UI Components
    private TextView statusText;
    private TextView loaderStatusText;
    private RecyclerView modRecyclerView;
    private ModListAdapter modAdapter;
    private Button addDllModBtn;
    private Button addDexModBtn;
    private Button refreshBtn;
    private Button backBtn;
    private LinearLayout loaderInfoSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_management);
        
        setTitle("🎮 Mod Management");
        
        initializeViews();
        setupListeners();
        loadMods();
        updateStatus();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        modRecyclerView = findViewById(R.id.modRecyclerView);
        addDllModBtn = findViewById(R.id.addDllModBtn);
        addDexModBtn = findViewById(R.id.addDexModBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        backBtn = findViewById(R.id.backBtn);
        loaderInfoSection = findViewById(R.id.loaderInfoSection);
        
        // Setup RecyclerView
        modRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        addDllModBtn.setOnClickListener(v -> {
            if (!MelonLoaderManager.isMelonLoaderInstalled(this) && !MelonLoaderManager.isLemonLoaderInstalled(this)) {
                showLoaderRequiredDialog();
                return;
            }
            
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DLL);
        });
        
        addDexModBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DEX);
        });
        
        refreshBtn.setOnClickListener(v -> {
            loadMods();
            updateStatus();
            Toast.makeText(this, "🔄 Mods refreshed", Toast.LENGTH_SHORT).show();
        });
        
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadMods() {
        ModManager.loadMods(this);
        List<File> allMods = ModManager.getAvailableMods();
        
        if (modAdapter == null) {
            modAdapter = new ModListAdapter(this, allMods);
            modRecyclerView.setAdapter(modAdapter);
        } else {
            // Update existing adapter
            modAdapter.updateMods(allMods);
        }
        
        LogUtils.logUser("Loaded " + allMods.size() + " mods for management");
    }

    private void updateStatus() {
        // Check loader status
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(this);
        
        if (melonInstalled) {
            loaderStatusText.setText("✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " - DLL mods supported");
            loaderStatusText.setTextColor(0xFF4CAF50); // Green
            addDllModBtn.setEnabled(true);
            addDllModBtn.setText("📥 Add DLL Mod");
            loaderInfoSection.setVisibility(View.VISIBLE);
        } else if (lemonInstalled) {
            loaderStatusText.setText("✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " - DLL mods supported");
            loaderStatusText.setTextColor(0xFF4CAF50); // Green
            addDllModBtn.setEnabled(true);
            addDllModBtn.setText("📥 Add DLL Mod");
            loaderInfoSection.setVisibility(View.VISIBLE);
        } else {
            loaderStatusText.setText("⚠️ No loader installed - DLL mods unavailable");
            loaderStatusText.setTextColor(0xFFF44336); // Red
            addDllModBtn.setEnabled(false);
            addDllModBtn.setText("❌ Install Loader First");
            loaderInfoSection.setVisibility(View.GONE);
        }
        
        // Update mod counts
        int enabledCount = ModManager.getEnabledModCount();
        int totalCount = ModManager.getTotalModCount();
        int dexCount = ModManager.getDexModCount();
        int dllCount = ModManager.getDllModCount();
        
        statusText.setText(String.format("📊 Total: %d mods (%d enabled) | DEX/JAR: %d | DLL: %d", 
            totalCount, enabledCount, dexCount, dllCount));
    }

    private void showLoaderRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔧 Loader Required");
        builder.setMessage("DLL mods require MelonLoader or LemonLoader to be installed.\n\n" +
                          "Would you like to set up a loader now?");
        
        builder.setPositiveButton("🚀 Setup Loader", (dialog, which) -> {
            // Go to unified loader setup
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
        });
        
        builder.setNegativeButton("📖 Manual Guide", (dialog, which) -> {
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        String filename = getFilenameFromUri(uri);
        
        if (filename == null) {
            Toast.makeText(this, "Could not determine filename", Toast.LENGTH_SHORT).show();
            return;
        }
        
        switch (requestCode) {
            case REQUEST_SELECT_DLL:
                handleDllModInstallation(uri, filename);
                break;
                
            case REQUEST_SELECT_DEX:
                handleDexModInstallation(uri, filename);
                break;
        }
    }

    private void handleDllModInstallation(Uri uri, String filename) {
        if (!filename.toLowerCase().endsWith(".dll")) {
            Toast.makeText(this, "⚠️ Please select a .dll file", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📥 Install DLL Mod");
        builder.setMessage("Install '" + filename + "' as a DLL mod?\n\n" +
                          "This mod will be loaded by MelonLoader when Terraria starts.");
        
        builder.setPositiveButton("Install", (dialog, which) -> {
            boolean success = ModInstaller.installMod(this, uri, filename);
            if (success) {
                Toast.makeText(this, "✅ DLL mod installed: " + filename, Toast.LENGTH_SHORT).show();
                loadMods();
                updateStatus();
            } else {
                Toast.makeText(this, "❌ Failed to install DLL mod", Toast.LENGTH_LONG).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void handleDexModInstallation(Uri uri, String filename) {
        String lowerName = filename.toLowerCase();
        if (!lowerName.endsWith(".dex") && !lowerName.endsWith(".jar")) {
            Toast.makeText(this, "⚠️ Please select a .dex or .jar file", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📥 Install DEX/JAR Mod");
        builder.setMessage("Install '" + filename + "' as a DEX/JAR mod?\n\n" +
                          "This mod will be loaded directly by TerrariaLoader.");
        
        builder.setPositiveButton("Install", (dialog, which) -> {
            boolean success = ModInstaller.installMod(this, uri, filename);
            if (success) {
                Toast.makeText(this, "✅ DEX/JAR mod installed: " + filename, Toast.LENGTH_SHORT).show();
                loadMods();
                updateStatus();
            } else {
                Toast.makeText(this, "❌ Failed to install DEX/JAR mod", Toast.LENGTH_LONG).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getFilenameFromUri(Uri uri) {
        String filename = null;
        
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.logDebug("Could not get filename from URI: " + e.getMessage());
        }
        
        if (filename == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                    filename = path.substring(lastSlash + 1);
                }
            }
        }
        
        return filename;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMods();
        updateStatus();
    }
}