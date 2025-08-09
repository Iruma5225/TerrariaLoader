// File: ModListAdapter.java (Fixed Adapter Class) - NullPointerException Fix
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/ui/ModListAdapter.java

package com.terrarialoader.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terrarialoader.R;
import com.terrarialoader.installer.ModInstaller;
import com.terrarialoader.loader.ModManager;
import com.terrarialoader.loader.ModMetadata;
import com.terrarialoader.loader.ModBase;
import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.util.List;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.ModViewHolder> {

    private final Context context;
    private List<File> mods; // Changed to non-final to allow updates

    public ModListAdapter(Context context, List<File> mods) {
        this.context = context;
        this.mods = mods;
    }

    @NonNull
    @Override
    public ModViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mod, parent, false);
        return new ModViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModViewHolder holder, int position) {
        File modFile = mods.get(position);
        String modName = modFile.getName();
        boolean isEnabled = !modName.endsWith(".disabled");

        holder.modNameTextView.setText(modName);
        
        // FIXED: Null pointer protection for metadata
        try {
            // Get metadata safely
            String cleanModName = modName.replace(".disabled", "").replace(".dex", "").replace(".jar", "").replace(".dll", "");
            ModMetadata metadata = ModManager.getMetadata(cleanModName);
            
            if (metadata != null) {
                // Use metadata if available
                ModBase.ModType modType = metadata.getModType();
                if (modType != null) {
                    holder.modDescriptionTextView.setText("Type: " + modType.getDisplayName());
                } else {
                    holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
                }
            } else {
                // Fallback to file extension detection
                holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
            }
        } catch (Exception e) {
            // Ultimate fallback
            LogUtils.logDebug("Error getting mod metadata: " + e.getMessage());
            holder.modDescriptionTextView.setText("Type: " + getModTypeFromFileName(modName));
        }

        holder.modSwitch.setChecked(isEnabled);
        holder.modSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                if (isChecked) {
                    ModManager.enableMod(context, modFile);
                } else {
                    ModManager.disableMod(context, modFile);
                }
                // Refresh the adapter after mod state change
                // Note: The list of files is not actually changing here, only their names.
                // A better approach would be to reload the list of files entirely.
                // For now, we will simply notify that the item has changed.
                notifyItemChanged(position);
            } catch (Exception e) {
                LogUtils.logDebug("Error toggling mod: " + e.getMessage());
                Toast.makeText(context, "Error toggling mod: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.modDeleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Mod")
                    .setMessage("Are you sure you want to delete " + modName + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            if (ModInstaller.uninstallMod(context, modName)) {
                                // Remove from list and notify adapter
                                mods.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mods.size());
                                Toast.makeText(context, modName + " deleted.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete " + modName, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            LogUtils.logDebug("Error deleting mod: " + e.getMessage());
                            Toast.makeText(context, "Error deleting mod: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    
    // Helper method to determine mod type from filename
    private String getModTypeFromFileName(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".dex") || lowerName.endsWith(".dex.disabled")) {
            return "DEX (Java)";
        } else if (lowerName.endsWith(".jar") || lowerName.endsWith(".jar.disabled")) {
            return "JAR (Java Library)";
        } else if (lowerName.endsWith(".dll") || lowerName.endsWith(".dll.disabled")) {
            return "DLL (C#/Native)";
        } else {
            return "Unknown";
        }
    }

    @Override
    public int getItemCount() {
        return mods.size();
    }

    /**
     * Updates the adapter's data set with a new list of mods.
     * @param newMods The new list of mods to display.
     */
    public void updateMods(List<File> newMods) {
        this.mods = newMods;
        notifyDataSetChanged();
    }

    public static class ModViewHolder extends RecyclerView.ViewHolder {
        TextView modNameTextView;
        TextView modDescriptionTextView;
        Switch modSwitch;
        ImageButton modDeleteButton;

        public ModViewHolder(@NonNull View itemView) {
            super(itemView);
            modNameTextView = itemView.findViewById(R.id.modNameTextView);
            modDescriptionTextView = itemView.findViewById(R.id.modDescription);
            modSwitch = itemView.findViewById(R.id.modSwitch);
            modDeleteButton = itemView.findViewById(R.id.modDeleteButton);
        }
    }
}
