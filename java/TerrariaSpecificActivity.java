// File: TerrariaSpecificActivity.java (Updated) - Elegant UI with Dynamic Theming
// Path: /main/java/com/terrarialoader/TerrariaSpecificActivity.java

package com.terrarialoader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.terrarialoader.R;
import com.terrarialoader.ui.UnifiedLoaderActivity;
import com.terrarialoader.ui.ModListActivity;
import com.terrarialoader.ui.SettingsActivity;
import com.terrarialoader.ui.LogViewerActivity;
import com.terrarialoader.ui.InstructionsActivity;
import com.terrarialoader.SandboxActivity;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.ui.OfflineDiagnosticActivity;


public class TerrariaSpecificActivity extends AppCompatActivity {

    private LinearLayout rootLayout;
    private TextView loaderStatusText;
    private CardView setupCard;
    private CardView modManagementCard;
    private CardView toolsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terraria_specific_updated);
        
        setTitle("🌍 Terraria Mod Loader");
        
        LogUtils.logUser("Terraria Menu Activity opened");
        
        initializeViews();
        applyTerrariaTheme();
        setupUI();
        updateLoaderStatus();
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.rootLayout);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        setupCard = findViewById(R.id.setupCard);
        modManagementCard = findViewById(R.id.modManagementCard);
        toolsCard = findViewById(R.id.toolsCard);
    }

    private void applyTerrariaTheme() {
        // Dynamic Terraria-inspired green/blue theme
        int primaryGreen = Color.parseColor("#4CAF50");
        int secondaryBlue = Color.parseColor("#2196F3");
        int accentGreen = Color.parseColor("#81C784");
        
        // Apply theme to root layout
        rootLayout.setBackgroundColor(Color.parseColor("#E8F5E8"));
        
        // Apply theme to cards
        setupCard.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
        modManagementCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        toolsCard.setCardBackgroundColor(Color.parseColor("#F3E5F5"));
        
        // Set status bar color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryGreen);
        }
    }

    private void setupUI() {
        // === SETUP SECTION ===
        
        // Unified Loader Setup (NEW - replaces multiple buttons)
        Button unifiedSetupBtn = findViewById(R.id.unifiedSetupButton);
        unifiedSetupBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Unified MelonLoader Setup");
            Intent intent = new Intent(this, UnifiedLoaderActivity.class);
            startActivity(intent);
        });
        
        // Quick Setup Guide
        Button setupGuideBtn = findViewById(R.id.setupGuideButton);
        setupGuideBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Setup Guide");
            Intent intent = new Intent(this, com.terrarialoader.ui.SetupGuideActivity.class);
            startActivity(intent);
        });
        
        // Manual Instructions (fallback)
        Button manualInstructionsBtn = findViewById(R.id.manualInstructionsButton);
        manualInstructionsBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Manual Installation Instructions");
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        });
        
        // === MOD MANAGEMENT SECTION ===
        
        // DEX/JAR Mod Manager
        Button dexModManagerBtn = findViewById(R.id.dexModManagerButton);
        dexModManagerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening DEX/JAR Mod Manager");
            Intent intent = new Intent(this, ModListActivity.class);
            startActivity(intent);
        });
        
        // DLL Mod Manager (redirects to unified system)
        Button dllModManagerBtn = findViewById(R.id.dllModManagerButton);
        dllModManagerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening DLL Mod Management via Unified Loader");
            
            // Check if loader is installed
            if (MelonLoaderManager.isMelonLoaderInstalled(this) || MelonLoaderManager.isLemonLoaderInstalled(this)) {
                // If loader is installed, go directly to mod management
                Intent intent = new Intent(this, com.terrarialoader.ui.ModManagementActivity.class);
                startActivity(intent);
            } else {
                // If no loader, start unified setup wizard
                Intent intent = new Intent(this, UnifiedLoaderActivity.class);
                startActivity(intent);
            }
        });
        
        // === TOOLS SECTION ===
        
        // Log Viewer
        Button logViewerBtn = findViewById(R.id.logViewerButton);
        logViewerBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Log Viewer");
            Intent intent = new Intent(this, LogViewerActivity.class);
            startActivity(intent);
        });
        
        // Settings
        Button settingsBtn = findViewById(R.id.settingsButton);
        settingsBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Settings");
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // Sandbox Mode
        Button sandboxBtn = findViewById(R.id.sandboxButton);
        sandboxBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Sandbox Mode");
            Intent intent = new Intent(this, SandboxActivity.class);
            startActivity(intent);
            
        });    
            
        // Diagnostic Tool
        Button diagnosticBtn = findViewById(R.id.diagnosticButton);
        diagnosticBtn.setOnClickListener(v -> {
            LogUtils.logUser("Opening Offline Diagnostic Tool");
            Intent intent = new Intent(this, OfflineDiagnosticActivity.class);
            startActivity(intent);
   
        });
        
        // === NAVIGATION ===
        
        // Back to App Selection
        Button backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            LogUtils.logUser("Returning to app selection");
            finish();
        });
    }

    private void updateLoaderStatus() {
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(this);
        
        String statusText;
        int statusColor;
        
        if (melonInstalled) {
            statusText = "✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " ready for DLL mods";
            statusColor = Color.parseColor("#4CAF50"); // Green
        } else if (lemonInstalled) {
            statusText = "✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " ready for DLL mods";
            statusColor = Color.parseColor("#4CAF50"); // Green
        } else {
            statusText = "⚠️ No loader installed - Use 'Complete Setup Wizard' to install MelonLoader";
            statusColor = Color.parseColor("#FF9800"); // Orange
        }
        
        loaderStatusText.setText(statusText);
        loaderStatusText.setTextColor(statusColor);
        
        // Update button states based on loader status
        updateButtonStates(melonInstalled || lemonInstalled);
    }

    private void updateButtonStates(boolean loaderInstalled) {
        Button dllModManagerBtn = findViewById(R.id.dllModManagerButton);
        
        if (loaderInstalled) {
            dllModManagerBtn.setText("🔧 Manage DLL Mods");
            dllModManagerBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            dllModManagerBtn.setText("🔧 Setup DLL Support");
            dllModManagerBtn.setBackgroundColor(Color.parseColor("#FF9800"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loader status when returning to this activity
        updateLoaderStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.logUser("Terraria Menu Activity closed");
    }
}