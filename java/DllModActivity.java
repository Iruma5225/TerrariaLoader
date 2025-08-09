// File: DllModActivity.java (Updated Activity) - Uses Controller Pattern
// Path: /storage/emulated/0/AndroidIDEProjects/main/java/com/terrarialoader/ui/DllModActivity.java

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

/**
 * DllModActivity now delegates business logic to DllModController
 * This keeps the activity focused on UI management while the controller handles the logic
 */
public class DllModActivity extends Activity implements DllModController.DllModCallback {

    private static final int REQUEST_SELECT_APK = 1001;
    private static final int REQUEST_SELECT_DLL = 1002;
    
    // UI Components
    private TextView statusText;
    private TextView loaderStatusText;
    private RecyclerView dllModRecyclerView;
    private Button installLoaderBtn;
    private Button selectApkBtn;
    private Button installDllBtn;
    private Button viewLogsBtn;
    private Button refreshBtn;
    private LinearLayout loaderInfoSection;
    
    // Controller and Adapter
    private DllModController controller;
    private DllModAdapter adapter;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dll_mod);
        
        setTitle("DLL Mod Manager");
        
        // Initialize controller
        controller = new DllModController(this);
        controller.setCallback(this);
        
        initializeViews();
        setupUI();
        
        // Load initial data
        controller.loadDllMods();
        controller.updateLoaderStatus();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        loaderStatusText = findViewById(R.id.loaderStatusText);
        dllModRecyclerView = findViewById(R.id.dllModRecyclerView);
        installLoaderBtn = findViewById(R.id.installLoaderBtn);
        selectApkBtn = findViewById(R.id.selectApkBtn);
        installDllBtn = findViewById(R.id.installDllBtn);
        viewLogsBtn = findViewById(R.id.viewLogsBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        loaderInfoSection = findViewById(R.id.loaderInfoSection);
    }

    private void setupUI() {
        // Setup RecyclerView
        dllModRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DllModAdapter(this, controller);
        dllModRecyclerView.setAdapter(adapter);

        // Button listeners - delegate to controller
        installLoaderBtn.setOnClickListener(v -> controller.showLoaderInstallDialog());
        
        selectApkBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_APK);
        });
        
        installDllBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all files, we'll filter by extension
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_DLL);
        });
        
        viewLogsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LogViewerActivity.class));
        });
        
        refreshBtn.setOnClickListener(v -> {
            controller.refresh();
            Toast.makeText(this, "Refreshed DLL mod list", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        
        switch (requestCode) {
            case REQUEST_SELECT_APK:
                controller.handleApkSelection(uri);
                break;
                
            case REQUEST_SELECT_DLL:
                String filename = getFilenameFromUri(uri);
                controller.handleDllSelection(uri, filename);
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
            LogUtils.logDebug("Could not get filename from URI: " + e.getMessage());
        }
        
        return filename;
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.refresh();
    }

    // === DllModController.DllModCallback Implementation ===
    
    @Override
    public void onModsLoaded(List<File> mods) {
        runOnUiThread(() -> {
            adapter.updateMods(mods);
            statusText.setText(controller.getStatusText());
        });
    }

    @Override
    public void onLoaderStatusChanged(boolean installed, String statusText, int textColor) {
        runOnUiThread(() -> {
            loaderStatusText.setText(statusText);
            loaderStatusText.setTextColor(textColor);
            
            if (installed) {
                installLoaderBtn.setText("Reinstall Loader");
                loaderInfoSection.setVisibility(View.VISIBLE);
                installDllBtn.setEnabled(true);
                installDllBtn.setText("Install DLL Mod");
            } else {
                installLoaderBtn.setText("Install Loader");
                loaderInfoSection.setVisibility(View.GONE);
                installDllBtn.setEnabled(false);
                installDllBtn.setText("Install Loader First");
            }
        });
    }

    @Override
    public void onInstallationProgress(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            
            if (message.contains("Please select")) {
                // This is a request for user action, not a progress message
                return;
            }
            
            progressDialog = new AlertDialog.Builder(this)
                .setTitle("Installing Loader")
                .setMessage(message)
                .setCancelable(false)
                .show();
        });
    }

    @Override
    public void onInstallationComplete(boolean success, String message) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            
            if (success) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    // === Simple adapter class for DLL mods ===
    private static class DllModAdapter extends RecyclerView.Adapter<DllModAdapter.ViewHolder> {
        private final Activity activity;
        private final DllModController controller;
        private List<File> mods;

        public DllModAdapter(Activity activity, DllModController controller) {
            this.activity = activity;
            this.controller = controller;
            this.mods = controller.getDllMods();
        }

        public void updateMods(List<File> newMods) {
            this.mods = newMods;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mod, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File mod = mods.get(position);
            String name = mod.getName();
            boolean isEnabled = !name.endsWith(".disabled");
            
            holder.modDescription.setText(name);
            holder.modSwitch.setChecked(isEnabled);
            
            holder.modSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                controller.toggleMod(mod, isChecked);
            });
            
            holder.modDeleteButton.setOnClickListener(v -> {
                controller.deleteMod(mod, () -> {
                    mods.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mods.size());
                });
            });
        }

        @Override
        public int getItemCount() {
            return mods.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView modDescription;
            android.widget.Switch modSwitch;
            android.widget.ImageButton modDeleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                modDescription = itemView.findViewById(R.id.modDescription);
                modSwitch = itemView.findViewById(R.id.modSwitch);
                modDeleteButton = itemView.findViewById(R.id.modDeleteButton);
            }
        }
    }
}