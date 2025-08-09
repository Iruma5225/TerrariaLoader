// File: InstructionsActivity.java (Fixed) - Corrected method calls with Context parameter
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/InstructionsActivity.java

package com.terrarialoader.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.R;
import com.terrarialoader.loader.MelonLoaderManager;
import com.terrarialoader.util.LogUtils;

public class InstructionsActivity extends AppCompatActivity {

    private TextView tvInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        setTitle("Manual Installation Guide");

        tvInstructions = findViewById(R.id.tv_instructions);
        
        setupInstructions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Copy GitHub URLs");
        menu.add(0, 2, 0, "Open GitHub");
        menu.add(0, 3, 0, "Check Installation");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                copyGitHubUrls();
                return true;
            case 2:
                openGitHubDialog();
                return true;
            case 3:
                checkInstallationStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupInstructions() {
        // Get the actual path that the app uses
        String actualBasePath = getExternalFilesDir(null) + "/TerrariaLoader/com.and.games505.TerrariaPaid";
        
        String manualInstructions = "📱 Manual MelonLoader/LemonLoader Installation Guide\n\n" +
                
                "🔗 STEP 1: Download Required Files\n" +
                "──────────────────────────────────\n" +
                "Visit GitHub and download ONE of these:\n\n" +
                
                "🔸 For MelonLoader (Full Features):\n" +
                "• Go to: github.com/LavaGang/MelonLoader/releases\n" +
                "• Download 'melon_data.zip' from latest release\n" +
                "• File size: ~40MB\n\n" +
                
                "🔸 For LemonLoader (Lightweight):\n" +
                "• Go to: github.com/LemonLoader/LemonLoader/releases\n" +
                "• Download 'lemon_data.zip' or installer APK\n" +
                "• File size: ~15MB\n\n" +
                
                "📁 STEP 2: Create Directory Structure\n" +
                "────────────────────────────────────\n" +
                "⚠️ IMPORTANT: Use the CORRECT path for your device!\n\n" +
                
                "Using a file manager, create this structure:\n" +
                actualBasePath + "/\n\n" +
                
                "Alternative path (if first doesn't work):\n" +
                "/storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/com.and.games505.TerrariaPaid/\n\n" +
                
                "Create these folders inside the above path:\n" +
                "├── Loaders/MelonLoader/\n" +
                "│   ├── net8/                    (for MelonLoader)\n" +
                "│   ├── net35/                   (for LemonLoader)\n" +
                "│   └── Dependencies/\n" +
                "│       ├── SupportModules/\n" +
                "│       ├── CompatibilityLayers/\n" +
                "│       └── Il2CppAssemblyGenerator/\n" +
                "│           ├── Cpp2IL/cpp2il_out/\n" +
                "│           ├── UnityDependencies/\n" +
                "│           └── Il2CppInterop/Il2CppAssemblies/\n" +
                "├── Mods/\n" +
                "│   ├── DLL/                     (for your DLL mods)\n" +
                "│   └── DEX/                     (for your DEX/JAR mods)\n" +
                "├── Logs/\n" +
                "├── Config/\n" +
                "└── Backups/\n\n" +
                
                "📦 STEP 3: Extract Files\n" +
                "───────────────────────\n" +
                "Extract the downloaded ZIP file:\n\n" +
                
                "🔸 Core Files (place in Loaders/MelonLoader/net8/ or net35/):\n" +
                "• MelonLoader.dll\n" +
                "• 0Harmony.dll\n" +
                "• MonoMod.RuntimeDetour.dll\n" +
                "• MonoMod.Utils.dll\n" +
                "• Il2CppInterop.Runtime.dll (MelonLoader only)\n\n" +
                
                "🔸 Dependencies (place in Loaders/MelonLoader/Dependencies/):\n" +
                "• All remaining DLL files go in appropriate subdirectories\n" +
                "• Unity assemblies go in UnityDependencies/\n" +
                "• Il2Cpp files go in Il2CppAssemblyGenerator/\n\n" +
                
                "⚠️ IMPORTANT FILE PLACEMENT:\n" +
                "────────────────────────────\n" +
                "• MelonLoader files → Loaders/MelonLoader/net8/\n" +
                "• LemonLoader files → Loaders/MelonLoader/net35/\n" +
                "• Support modules → Loaders/MelonLoader/Dependencies/SupportModules/\n" +
                "• Your mod DLLs → Mods/DLL/\n" +
                "• Your DEX/JAR mods → Mods/DEX/\n\n" +
                
                "✅ STEP 4: Verify Installation\n" +
                "─────────────────────────────\n" +
                "Return to TerrariaLoader and:\n" +
                "1. Go to 'DLL Mod Manager'\n" +
                "2. Check loader status\n" +
                "3. Should show '✅ Loader Installed'\n" +
                "4. If not, check file paths carefully\n\n" +
                
                "🎮 STEP 5: Use Your Loader\n" +
                "─────────────────────────\n" +
                "1. Place DLL mods in Mods/DLL/ folder\n" +
                "2. Select Terraria APK in DLL Manager\n" +
                "3. Patch APK with loader\n" +
                "4. Install patched Terraria\n" +
                "5. Launch and enjoy mods!\n\n" +
                
                "🔧 TROUBLESHOOTING:\n" +
                "──────────────────\n" +
                "• Loader not detected → Check file paths exactly\n" +
                "• Can't find directory → Try both paths mentioned in Step 2\n" +
                "• APK patch fails → Verify all DLL files present\n" +
                "• Mods don't load → Check Logs/ folder for errors\n" +
                "• Slow performance → Try LemonLoader instead\n" +
                "• Permission denied → Enable 'All files access' for file manager\n\n" +
                
                "📋 REQUIRED FILES CHECKLIST:\n" +
                "───────────────────────────\n" +
                "☐ MelonLoader.dll (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ 0Harmony.dll (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ MonoMod files (in Loaders/MelonLoader/net8/ or net35/)\n" +
                "☐ Il2CppInterop files (Dependencies/SupportModules/)\n" +
                "☐ Unity dependencies (Dependencies/Il2CppAssemblyGenerator/UnityDependencies/)\n" +
                "☐ Directory structure matches exactly\n" +
                "☐ Using correct base path for your device\n\n" +
                
                "💡 TIPS:\n" +
                "──────\n" +
                "• Use a good file manager (like Solid Explorer)\n" +
                "• Enable 'Show hidden files' in your file manager\n" +
                "• Grant 'All files access' permission to your file manager\n" +
                "• Double-check spelling of folder names\n" +
                "• Keep backup of original Terraria APK\n" +
                "• Start with LemonLoader if you have storage issues\n" +
                "• Copy the exact path from Step 2 to avoid typos\n\n" +
                
                "📍 PATH VERIFICATION:\n" +
                "────────────────────\n" +
                "Your device should use:\n" + actualBasePath + "\n\n" +
                
                "If that doesn't work, try:\n" +
                "/storage/emulated/0/Android/data/com.terrarialoader/files/TerrariaLoader/com.and.games505.TerrariaPaid/\n\n" +
                
                "Need help? Use the menu (⋮) for quick actions or check the logs in TerrariaLoader for detailed error messages!";

        tvInstructions.setText(manualInstructions);
    }

    private void copyGitHubUrls() {
        String githubUrls = "MelonLoader: https://github.com/LavaGang/MelonLoader/releases\n" +
                           "LemonLoader: https://github.com/LemonLoader/LemonLoader/releases";
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("GitHub URLs", githubUrls);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "GitHub URLs copied to clipboard!", Toast.LENGTH_SHORT).show();
        LogUtils.logUser("User copied GitHub URLs to clipboard");
    }

    private void openGitHubDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Open GitHub Repository");
        builder.setMessage("Which repository would you like to visit?");
        
        builder.setPositiveButton("MelonLoader", (dialog, which) -> {
            openUrl("https://github.com/LavaGang/MelonLoader/releases");
        });
        
        builder.setNegativeButton("LemonLoader", (dialog, which) -> {
            openUrl("https://github.com/LemonLoader/LemonLoader/releases");
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            LogUtils.logUser("User opened GitHub URL: " + url);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open URL. Please visit manually.", Toast.LENGTH_LONG).show();
            LogUtils.logDebug("Failed to open URL: " + e.getMessage());
        }
    }

    private void checkInstallationStatus() {
        LogUtils.logUser("User checking manual installation status");
        
        // FIXED: Pass context to MelonLoaderManager methods
        boolean melonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        boolean lemonInstalled = MelonLoaderManager.isMelonLoaderInstalled(this);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        
        if (melonInstalled || lemonInstalled) {
            String loaderType = melonInstalled ? "MelonLoader" : "LemonLoader";
            String version = MelonLoaderManager.getInstalledLoaderVersion();
            
            builder.setTitle("✅ Installation Detected!");
            builder.setMessage("Great! " + loaderType + " v" + version + " is properly installed.\n\n" +
                              "You can now:\n" +
                              "• Go to DLL Mod Manager\n" +
                              "• Install DLL mods\n" +
                              "• Patch Terraria APK\n" +
                              "• Start modding!\n\n" +
                              "Installation path:\n" + 
                              MelonLoaderManager.getStatus(this, MelonLoaderManager.TERRARIA_PACKAGE).basePath);
            
            builder.setPositiveButton("Open DLL Manager", (dialog, which) -> {
                Intent intent = new Intent(this, DllModActivity.class);
                startActivity(intent);
            });
            
            builder.setNegativeButton("Stay Here", null);
            
        } else {
            builder.setTitle("❌ Installation Not Found");
            builder.setMessage("No loader installation detected.\n\n" +
                              "Please check:\n" +
                              "• Files are in correct directories\n" +
                              "• Directory names match exactly\n" +
                              "• Core DLL files are present\n" +
                              "• File permissions are correct\n\n" +
                              "Expected path:\n" +
                              "/storage/emulated/0/TerrariaLoader/com.and.games505.TerrariaPaid/");
            
            builder.setPositiveButton("View Debug Info", (dialog, which) -> {
                showDebugInfo();
            });
            
            builder.setNegativeButton("OK", null);
        }
        
        builder.show();
    }

    private void showDebugInfo() {
        // FIXED: Pass context to MelonLoaderManager.getDebugInfo method
        String debugInfo = MelonLoaderManager.getDebugInfo(this, MelonLoaderManager.TERRARIA_PACKAGE);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Debug Information");
        builder.setMessage(debugInfo);
        builder.setPositiveButton("Copy to Clipboard", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Debug Info", debugInfo);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Debug info copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
}