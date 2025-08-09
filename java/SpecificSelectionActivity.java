// File: SpecificSelectionActivity.java (Fixed Activity Class) - Fixed App Selection
// Path: /storage/emulated/0/AndroidIDEProjects/TerrariaML/app/src/main/java/com/terrarialoader/SpecificSelectionActivity.java

package com.terrarialoader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.util.LogUtils;

public class SpecificSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_selection);
        setTitle("Select Game/App");

        LogUtils.logDebug("SpecificSelectionActivity started");

        // Add header text
        TextView headerText = findViewById(R.id.headerText);
        if (headerText != null) {
            headerText.setText("Choose which game/app to mod:");
        }

        // Setup Terraria button - FIXED: Changed to TerrariaSpecificActivity
        Button terrariaBtn = findViewById(R.id.terraria_button);
        terrariaBtn.setOnClickListener(v -> {
            LogUtils.logUser("Terraria selected for specific modding");
            Intent intent = new Intent(this, TerrariaSpecificActivity.class);
            startActivity(intent);
        });

        // Add future games section
        LinearLayout futureGamesSection = findViewById(R.id.futureGamesSection);
        if (futureGamesSection != null) {
            TextView futureText = new TextView(this);
            futureText.setText("More games coming soon:\n• Minecraft PE\n• Among Us\n• Other Unity Games");
            futureText.setTextSize(14);
            futureText.setPadding(16, 16, 16, 16);
            futureText.setTextColor(getColor(android.R.color.darker_gray));
            futureGamesSection.addView(futureText);
        }
    }
}