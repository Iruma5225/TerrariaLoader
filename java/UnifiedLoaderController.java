// File: UnifiedLoaderController.java - Business Logic Facade for Unified Loader
// Path: /main/java/com/terrarialoader/ui/UnifiedLoaderController.java

package com.terrarialoader.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;
import com.terrarialoader.util.FileUtils;
import com.terrarialoader.util.OnlineInstaller;
import com.terrarialoader.util.OfflineZipImporter;
import com.terrarialoader.util.ApkPatcher;
import java.io.File;

/**
 * Unified Loader Controller - Facade pattern to handle all loader operations
 * Keeps the main activity clean and focused on UI
 */
public class UnifiedLoaderController {
    
    public enum LoaderStep {
        WELCOME(0, "Welcome"),
        LOADER_INSTALL(1, "Install Loader"), 
        APK_SELECTION(2, "Select APK"),
        APK_PATCHING(3, "Patch APK"),
        COMPLETION(4, "Complete");
        
        private final int stepNumber;
        private final String title;
        
        LoaderStep(int stepNumber, String title) {
            this.stepNumber = stepNumber;
            this.title = title;
        }
        
        public int getStepNumber() { return stepNumber; }
        public String getTitle() { return title; }
        public static LoaderStep fromStep(int step) {
            for (LoaderStep s : values()) {
                if (s.stepNumber == step) return s;
            }
            return WELCOME;
        }
    }
    
    // State management
    private final Activity activity;
    private LoaderStep currentStep = LoaderStep.WELCOME;
    private MelonLoaderManager.LoaderType selectedLoaderType;
    private Uri selectedApkUri;
    private File patchedApkFile;
    private boolean loaderInstalled = false;
    
    // Callback interface for UI updates
    public interface UnifiedLoaderCallback {
        void onStepChanged(LoaderStep step, String message);
        void onProgress(String message, int percentage);
        void onSuccess(String message);
        void onError(String error);
        void onLoaderStatusChanged(boolean installed, String statusText);
        void updateStepIndicator(int currentStep, int totalSteps);
    }
    
    private UnifiedLoaderCallback callback;
    
    public UnifiedLoaderController(Activity activity) {
        this.activity = activity;
        updateLoaderStatus();
    }
    
    public void setCallback(UnifiedLoaderCallback callback) {
        this.callback = callback;
    }
    
    // === STEP MANAGEMENT ===
    
    public void nextStep() {
        int nextStepNum = Math.min(currentStep.getStepNumber() + 1, LoaderStep.COMPLETION.getStepNumber());
        setCurrentStep(LoaderStep.fromStep(nextStepNum));
    }
    
    public void previousStep() {
        int prevStepNum = Math.max(currentStep.getStepNumber() - 1, LoaderStep.WELCOME.getStepNumber());
        setCurrentStep(LoaderStep.fromStep(prevStepNum));
    }
    
    public void setCurrentStep(LoaderStep step) {
        this.currentStep = step;
        String message = getStepMessage(step);
        
        if (callback != null) {
            callback.onStepChanged(step, message);
            callback.updateStepIndicator(step.getStepNumber(), LoaderStep.values().length - 1);
        }
        
        LogUtils.logUser("Wizard step: " + step.getTitle());
    }
    
    private String getStepMessage(LoaderStep step) {
        switch (step) {
            case WELCOME:
                return "Welcome to the Unified MelonLoader Setup Wizard!\n\nThis wizard will guide you through:\n• Installing MelonLoader/LemonLoader\n• Patching your Terraria APK\n• Setting up DLL mod support";
            case LOADER_INSTALL:
                return loaderInstalled ? 
                    "✅ Loader is installed and ready!\n\nYou can reinstall or proceed to APK selection." :
                    "🔧 Choose how to install MelonLoader:\n\n• Online: Automatic download and setup\n• Offline: Import your own ZIP file";
            case APK_SELECTION:
                return selectedApkUri != null ?
                    "✅ APK selected: " + getFilenameFromUri(selectedApkUri) + "\n\nReady to patch with " + (selectedLoaderType != null ? selectedLoaderType.getDisplayName() : "MelonLoader") :
                    "📱 Select your Terraria APK file to patch with MelonLoader";
            case APK_PATCHING:
                return "⚡ Patching APK with " + (selectedLoaderType != null ? selectedLoaderType.getDisplayName() : "MelonLoader") + "...\n\nThis may take a few minutes.";
            case COMPLETION:
                return patchedApkFile != null ?
                    "🎉 Success! Your modded Terraria APK is ready!\n\n📱 Install the patched APK\n🎮 Add DLL mods\n🚀 Enjoy modded Terraria!" :
                    "❌ Setup incomplete. Please review the previous steps.";
            default:
                return "Processing...";
        }
    }
    
    // === LOADER INSTALLATION ===
    
    public void installLoaderOnline(MelonLoaderManager.LoaderType loaderType) {
        this.selectedLoaderType = loaderType;
        
        if (callback != null) {
            callback.onProgress("Starting online installation...", 0);
        }
        
        OnlineInstaller.installMelonLoaderAsync(activity, MelonLoaderManager.TERRARIA_PACKAGE, loaderType,
            new OnlineInstaller.InstallationProgressCallback() {
                @Override
                public void onProgress(String message, int percentage) {
                    if (callback != null) {
                        callback.onProgress(message, percentage);
                    }
                }
                
                @Override
                public void onComplete(OnlineInstaller.InstallationResult result) {
                    activity.runOnUiThread(() -> {
                        if (result.success) {
                            loaderInstalled = true;
                            updateLoaderStatus();
                            if (callback != null) {
                                callback.onSuccess("✅ " + loaderType.getDisplayName() + " installed successfully!");
                            }
                            nextStep(); // Auto-advance to APK selection
                        } else {
                            if (callback != null) {
                                callback.onError("Installation failed: " + result.message);
                            }
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onError("Installation error: " + error);
                        }
                    });
                }
            });
    }
    
    public void installLoaderOffline(Uri zipUri) {
        if (callback != null) {
            callback.onProgress("Importing ZIP file...", 0);
        }
        
        new Thread(() -> {
            try {
                OfflineZipImporter.ImportResult result = OfflineZipImporter.importMelonLoaderZip(activity, zipUri);
                
                activity.runOnUiThread(() -> {
                    if (result.success) {
                        selectedLoaderType = result.detectedType;
                        loaderInstalled = true;
                        updateLoaderStatus();
                        if (callback != null) {
                            callback.onSuccess("✅ " + result.detectedType.getDisplayName() + " imported successfully!");
                        }
                        nextStep(); // Auto-advance to APK selection
                    } else {
                        if (callback != null) {
                            callback.onError("Import failed: " + result.message);
                        }
                    }
                });
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onError("Import error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    // === APK OPERATIONS ===
    
    public void selectApk(Uri apkUri) {
        this.selectedApkUri = apkUri;
        LogUtils.logUser("APK selected: " + getFilenameFromUri(apkUri));
        setCurrentStep(currentStep); // Refresh step message
    }
    
    public void patchApk() {
        if (selectedApkUri == null || selectedLoaderType == null) {
            if (callback != null) {
                callback.onError("Please select APK and install loader first");
            }
            return;
        }
        
        setCurrentStep(LoaderStep.APK_PATCHING);
        
        if (callback != null) {
            callback.onProgress("Patching APK with " + selectedLoaderType.getDisplayName() + "...", 0);
        }
        
        new Thread(() -> {
            try {
                // Create temp input file
                File tempApk = File.createTempFile("input_", ".apk", activity.getCacheDir());
                FileUtils.copyUriToFile(activity, selectedApkUri, tempApk);
                
                // Create output file
                String originalName = getFilenameFromUri(selectedApkUri);
                String baseName = originalName.replace(".apk", "");
                patchedApkFile = new File(activity.getExternalFilesDir(null), 
                    baseName + "_" + selectedLoaderType.name().toLowerCase() + "_modded_" + 
                    System.currentTimeMillis() + ".apk");
                
                // Patch APK
                boolean success = ApkPatcher.injectMelonLoaderIntoApk(activity, tempApk, patchedApkFile, selectedLoaderType);
                
                // Cleanup
                tempApk.delete();
                
                activity.runOnUiThread(() -> {
                    if (success && patchedApkFile.exists()) {
                        if (callback != null) {
                            callback.onProgress("Patching completed!", 100);
                            callback.onSuccess("✅ APK patched successfully!");
                        }
                        nextStep(); // Move to completion
                    } else {
                        if (callback != null) {
                            callback.onError("APK patching failed");
                        }
                    }
                });
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onError("Patching error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    public void installPatchedApk() {
        if (patchedApkFile == null || !patchedApkFile.exists()) {
            if (callback != null) {
                callback.onError("No patched APK available");
            }
            return;
        }
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(
                    activity, 
                    activity.getPackageName() + ".provider", 
                    patchedApkFile
                ),
                "application/vnd.android.package-archive"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
            
            LogUtils.logUser("📲 Launching APK installer for: " + patchedApkFile.getName());
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Cannot install APK: " + e.getMessage());
            }
        }
    }
    
    // === STATUS CHECKS ===
    
    private void updateLoaderStatus() {
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(activity);
        boolean lemonInstalled = MelonLoaderManager.isLemonLoaderInstalled(activity);
        
        loaderInstalled = melonInstalled || lemonInstalled;
        
        String statusText;
        if (melonInstalled) {
            selectedLoaderType = MelonLoaderManager.LoaderType.MELONLOADER_NET8;
            statusText = "✅ MelonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
        } else if (lemonInstalled) {
            selectedLoaderType = MelonLoaderManager.LoaderType.MELONLOADER_NET35;
            statusText = "✅ LemonLoader " + MelonLoaderManager.getInstalledLoaderVersion() + " installed";
        } else {
            statusText = "❌ No loader installed";
        }
        
        if (callback != null) {
            callback.onLoaderStatusChanged(loaderInstalled, statusText);
        }
    }
    
    // === UTILITY METHODS ===
    
    private String getFilenameFromUri(Uri uri) {
        String filename = FileUtils.getFilenameFromUri(activity, uri);
        return filename != null ? filename : "Unknown APK";
    }
    
    public boolean canProceedToNextStep() {
        switch (currentStep) {
            case WELCOME:
                return true;
            case LOADER_INSTALL:
                return loaderInstalled;
            case APK_SELECTION:
                return selectedApkUri != null && loaderInstalled;
            case APK_PATCHING:
                return patchedApkFile != null && patchedApkFile.exists();
            case COMPLETION:
                return false; // Final step
            default:
                return false;
        }
    }
    
    public boolean canProceedToPreviousStep() {
        return currentStep.getStepNumber() > LoaderStep.WELCOME.getStepNumber();
    }
    
    // === GETTERS ===
    
    public LoaderStep getCurrentStep() { return currentStep; }
    public boolean isLoaderInstalled() { return loaderInstalled; }
    public Uri getSelectedApkUri() { return selectedApkUri; }
    public File getPatchedApkFile() { return patchedApkFile; }
    public MelonLoaderManager.LoaderType getSelectedLoaderType() { return selectedLoaderType; }
    
    // === RESET/RESTART ===
    
    public void resetWizard() {
        currentStep = LoaderStep.WELCOME;
        selectedApkUri = null;
        patchedApkFile = null;
        // Don't reset loader installation status
        setCurrentStep(LoaderStep.WELCOME);
    }
}