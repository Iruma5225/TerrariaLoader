// File: UnifiedLoaderActivity.java - Wizard-Style Unified Loader Interface
// Path: /main/java/com/terrarialoader/ui/UnifiedLoaderActivity.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.R;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;

/**
 * Unified Loader Activity - Wizard-style interface for complete setup
 * Replaces both TerrariaActivity and DllModActivity with elegant workflow
 */
public class UnifiedLoaderActivity extends AppCompatActivity implements UnifiedLoaderController.UnifiedLoaderCallback {

    private static final int REQUEST_SELECT_APK = 1001;
    private static final int REQUEST_SELECT_ZIP = 1002;
    
    // UI Components
    private ProgressBar stepProgressBar;
    private TextView stepTitleText;
    private TextView stepDescriptionText;
    private TextView stepIndicatorText;
    private LinearLayout stepContentContainer;
    private Button previousButton;
    private Button nextButton;
    private Button actionButton;
    
    // Current step content views
    private LinearLayout welcomeContent;
    private LinearLayout loaderInstallContent;
    private LinearLayout apkSelectionContent;
    private LinearLayout patchingContent;
    private LinearLayout completionContent;
    
    // Status indicators
    private TextView loaderStatusText;
    private TextView apkStatusText;
    private TextView progressText;
    private ProgressBar actionProgressBar;
    
    // Controller
    private UnifiedLoaderController controller;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_loader);
        
        setTitle("🚀 MelonLoader Setup Wizard");
        
        // Initialize controller
        controller = new UnifiedLoaderController(this);
        controller.setCallback(this);
        
        initializeViews();
        setupStepContents();
        setupListeners();
        
        // Start wizard
        controller.setCurrentStep(UnifiedLoaderController.LoaderStep.WELCOME);
    }

    private void initializeViews() {
        stepProgressBar = findViewById(R.id.stepProgressBar);
        stepTitleText = findViewById(R.id.stepTitleText);
        stepDescriptionText = findViewById(R.id.stepDescriptionText);
        stepIndicatorText = findViewById(R.id.stepIndicatorText);
        stepContentContainer = findViewById(R.id.stepContentContainer);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        actionButton = findViewById(R.id.actionButton);
        
        loaderStatusText = findViewById(R.id.loaderStatusText);
        apkStatusText = findViewById(R.id.apkStatusText);
        progressText = findViewById(R.id.progressText);
        actionProgressBar = findViewById(R.id.actionProgressBar);
    }

    private void setupStepContents() {
        // Create step content views dynamically
        welcomeContent = createWelcomeContent();
        loaderInstallContent = createLoaderInstallContent();
        apkSelectionContent = createApkSelectionContent();
        patchingContent = createPatchingContent();
        completionContent = createCompletionContent();
    }

    private LinearLayout createWelcomeContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        
        TextView welcomeText = new TextView(this);
        welcomeText.setText("🎮 Welcome to MelonLoader Setup!\n\nThis wizard will guide you through:\n\n• Installing MelonLoader/LemonLoader\n• Patching your Terraria APK\n• Setting up DLL mod support\n\nClick 'Next' to begin!");
        welcomeText.setTextSize(16);
        welcomeText.setLineSpacing(8, 1.0f);
        content.addView(welcomeText);
        
        return content;
    }

    private LinearLayout createLoaderInstallContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        // Loader status
        TextView statusLabel = new TextView(this);
        statusLabel.setText("Current Status:");
        statusLabel.setTextSize(14);
        statusLabel.setTypeface(null, Typeface.BOLD);
        content.addView(statusLabel);
        
        loaderStatusText = new TextView(this);
        loaderStatusText.setText("Checking...");
        loaderStatusText.setTextSize(14);
        loaderStatusText.setPadding(0, 8, 0, 16);
        content.addView(loaderStatusText);
        
        // Installation options
        TextView optionsLabel = new TextView(this);
        optionsLabel.setText("Installation Options:");
        optionsLabel.setTextSize(14);
        optionsLabel.setTypeface(null, Typeface.BOLD);
        content.addView(optionsLabel);
        
        Button onlineInstallBtn = new Button(this);
        onlineInstallBtn.setText("🌐 Online Installation (Recommended)");
        onlineInstallBtn.setOnClickListener(v -> showOnlineInstallOptions());
        content.addView(onlineInstallBtn);
        
        Button offlineInstallBtn = new Button(this);
        offlineInstallBtn.setText("📦 Offline ZIP Import");
        offlineInstallBtn.setOnClickListener(v -> selectOfflineZip());
        content.addView(offlineInstallBtn);
        
        return content;
    }

    private LinearLayout createApkSelectionContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        TextView instructionText = new TextView(this);
        instructionText.setText("📱 Select your Terraria APK file to patch with MelonLoader:");
        instructionText.setTextSize(16);
        content.addView(instructionText);
        
        Button selectApkBtn = new Button(this);
        selectApkBtn.setText("📱 Select Terraria APK");
        selectApkBtn.setOnClickListener(v -> selectApkFile());
        content.addView(selectApkBtn);
        
        apkStatusText = new TextView(this);
        apkStatusText.setText("No APK selected");
        apkStatusText.setTextSize(14);
        apkStatusText.setPadding(0, 16, 0, 0);
        content.addView(apkStatusText);
        
        return content;
    }

    private LinearLayout createPatchingContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        content.setGravity(android.view.Gravity.CENTER);
        
        TextView patchingText = new TextView(this);
        patchingText.setText("⚡ Patching APK with MelonLoader...");
        patchingText.setTextSize(18);
        patchingText.setGravity(android.view.Gravity.CENTER);
        content.addView(patchingText);
        
        actionProgressBar = new ProgressBar(this);
        actionProgressBar.setIndeterminate(true);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressParams.topMargin = 24;
        progressParams.gravity = android.view.Gravity.CENTER;
        actionProgressBar.setLayoutParams(progressParams);
        content.addView(actionProgressBar);
        
        progressText = new TextView(this);
        progressText.setText("Initializing...");
        progressText.setTextSize(14);
        progressText.setGravity(android.view.Gravity.CENTER);
        progressText.setPadding(0, 16, 0, 0);
        content.addView(progressText);
        
        return content;
    }

    private LinearLayout createCompletionContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        
        TextView completionText = new TextView(this);
        completionText.setText("🎉 Setup Complete!\n\nYour modded Terraria APK is ready!");
        completionText.setTextSize(18);
        completionText.setGravity(android.view.Gravity.CENTER);
        content.addView(completionText);
        
        Button installApkBtn = new Button(this);
        installApkBtn.setText("📲 Install Patched APK");
        installApkBtn.setOnClickListener(v -> controller.installPatchedApk());
        content.addView(installApkBtn);
        
        Button manageModsBtn = new Button(this);
        manageModsBtn.setText("🔧 Manage DLL Mods");
        manageModsBtn.setOnClickListener(v -> openModManagement());
        content.addView(manageModsBtn);
        
        Button viewLogsBtn = new Button(this);
        viewLogsBtn.setText("📋 View Logs");
        viewLogsBtn.setOnClickListener(v -> startActivity(new Intent(this, LogViewerActivity.class)));
        content.addView(viewLogsBtn);
        
        return content;
    }

    private void setupListeners() {
        previousButton.setOnClickListener(v -> {
            if (controller.canProceedToPreviousStep()) {
                controller.previousStep();
            }
        });
        
        nextButton.setOnClickListener(v -> {
            if (controller.canProceedToNextStep()) {
                handleNextStep();
            } else {
                showStepRequirements();
            }
        });
        
        actionButton.setOnClickListener(v -> handleActionButton());
    }

    private void handleNextStep() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        
        switch (currentStep) {
            case WELCOME:
                controller.nextStep();
                break;
            case LOADER_INSTALL:
                if (controller.isLoaderInstalled()) {
                    controller.nextStep();
                } else {
                    Toast.makeText(this, "Please install a loader first", Toast.LENGTH_SHORT).show();
                }
                break;
            case APK_SELECTION:
                if (controller.getSelectedApkUri() != null) {
                    controller.nextStep();
                    controller.patchApk(); // Auto-start patching
                } else {
                    Toast.makeText(this, "Please select an APK first", Toast.LENGTH_SHORT).show();
                }
                break;
            case APK_PATCHING:
                // Patching in progress, disable navigation
                break;
            case COMPLETION:
                finish(); // Exit wizard
                break;
        }
    }

    private void handleActionButton() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        
        switch (currentStep) {
            case WELCOME:
                controller.nextStep();
                break;
            case LOADER_INSTALL:
                showOnlineInstallOptions();
                break;
            case APK_SELECTION:
                selectApkFile();
                break;
            case APK_PATCHING:
                // No action during patching
                break;
            case COMPLETION:
                controller.installPatchedApk();
                break;
        }
    }

    private void showOnlineInstallOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Loader Type");
        builder.setMessage("Select which loader to install:\n\n🔸 MelonLoader: Full-featured, larger size\n🔸 LemonLoader: Lightweight, smaller size");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            controller.installLoaderOnline(MelonLoaderManager.LoaderType.MELONLOADER_NET8);
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            controller.installLoaderOnline(MelonLoaderManager.LoaderType.MELONLOADER_NET35);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void selectOfflineZip() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_SELECT_ZIP);
    }

    private void selectApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_SELECT_APK);
    }

    private void openModManagement() {
        Intent intent = new Intent(this, ModManagementActivity.class);
        startActivity(intent);
    }

    private void showStepRequirements() {
        UnifiedLoaderController.LoaderStep currentStep = controller.getCurrentStep();
        String message = "";
        
        switch (currentStep) {
            case LOADER_INSTALL:
                message = "Please install MelonLoader or LemonLoader first";
                break;
            case APK_SELECTION:
                message = "Please select a Terraria APK file";
                break;
            default:
                message = "Please complete the current step";
                break;
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        
        switch (requestCode) {
            case REQUEST_SELECT_APK:
                controller.selectApk(uri);
                String filename = getFilenameFromUri(uri);
                apkStatusText.setText("✅ Selected: " + filename);
                break;
                
            case REQUEST_SELECT_ZIP:
                controller.installLoaderOffline(uri);
                break;
        }
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
            LogUtils.logDebug("Could not get filename: " + e.getMessage());
        }
        return filename != null ? filename : "Unknown file";
    }

    // === UnifiedLoaderController.UnifiedLoaderCallback Implementation ===

    @Override
    public void onStepChanged(UnifiedLoaderController.LoaderStep step, String message) {
        runOnUiThread(() -> {
            stepTitleText.setText(step.getTitle());
            stepDescriptionText.setText(message);
            
            // Clear previous content
            stepContentContainer.removeAllViews();
            
            // Add appropriate content
            switch (step) {
                case WELCOME:
                    stepContentContainer.addView(welcomeContent);
                    actionButton.setText("🚀 Start Setup");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case LOADER_INSTALL:
                    stepContentContainer.addView(loaderInstallContent);
                    actionButton.setText("🌐 Install Online");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case APK_SELECTION:
                    stepContentContainer.addView(apkSelectionContent);
                    actionButton.setText("📱 Select APK");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case APK_PATCHING:
                    stepContentContainer.addView(patchingContent);
                    actionButton.setVisibility(View.GONE);
                    nextButton.setEnabled(false);
                    previousButton.setEnabled(false);
                    break;
                case COMPLETION:
                    stepContentContainer.addView(completionContent);
                    actionButton.setText("📲 Install APK");
                    actionButton.setVisibility(View.VISIBLE);
                    nextButton.setText("🏁 Finish");
                    nextButton.setEnabled(true);
                    previousButton.setEnabled(true);
                    break;
            }
            
            // Update navigation buttons
            previousButton.setEnabled(controller.canProceedToPreviousStep());
            nextButton.setEnabled(controller.canProceedToNextStep());
        });
    }

    @Override
    public void onProgress(String message, int percentage) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage(message + (percentage > 0 ? " (" + percentage + "%)" : ""));
            }
            if (progressText != null) {
                progressText.setText(message);
            }
        });
    }

    @Override
    public void onSuccess(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage(error);
            builder.setPositiveButton("OK", null);
            builder.show();
            
            // Re-enable navigation
            nextButton.setEnabled(controller.canProceedToNextStep());
            previousButton.setEnabled(controller.canProceedToPreviousStep());
        });
    }

    @Override
    public void onLoaderStatusChanged(boolean installed, String statusText) {
        runOnUiThread(() -> {
            if (loaderStatusText != null) {
                loaderStatusText.setText(statusText);
                loaderStatusText.setTextColor(installed ? 0xFF4CAF50 : 0xFFF44336);
            }
        });
    }

    @Override
    public void updateStepIndicator(int currentStep, int totalSteps) {
        runOnUiThread(() -> {
            stepProgressBar.setMax(totalSteps);
            stepProgressBar.setProgress(currentStep);
            stepIndicatorText.setText("Step " + (currentStep + 1) + " of " + (totalSteps + 1));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loader status when returning to activity
        if (controller != null) {
            // Update current step to refresh status
            controller.setCurrentStep(controller.getCurrentStep());
        }
    }
}