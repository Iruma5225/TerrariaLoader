package com.terrarialoader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.terrarialoader.util.LogUtils;

public class UniversalActivity extends AppCompatActivity {

    private Uri apkUri;
    private Uri zipUri;

    private TextView statusText;

    private final ActivityResultLauncher<Intent> apkPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    apkUri = result.getData().getData();
                    statusText.setText("Universal APK selected:\n" + apkUri.getPath());
                    LogUtils.logUser("Universal - Selected APK: " + apkUri);
                }
            });

    private final ActivityResultLauncher<Intent> zipPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    zipUri = result.getData().getData();
                    statusText.setText("Loader ZIP selected:\n" + zipUri.getPath());
                    LogUtils.logUser("Universal - Selected ZIP: " + zipUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universal);

        LogUtils.logDebug("UniversalActivity launched");

        Button selectApkBtn = findViewById(R.id.select_apk_button);
        Button selectZipBtn = findViewById(R.id.select_zip_button);
        Button injectBtn = findViewById(R.id.inject_button);
        statusText = findViewById(R.id.status_text);

        selectApkBtn.setOnClickListener(v -> openApkPicker());
        selectZipBtn.setOnClickListener(v -> openZipPicker());

        injectBtn.setOnClickListener(v -> {
            if (apkUri == null || zipUri == null) {
                Toast.makeText(this, "Please select both APK and ZIP", Toast.LENGTH_SHORT).show();
                return;
            }
            injectLoader();
        });
    }

    private void openApkPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        apkPickerLauncher.launch(Intent.createChooser(intent, "Select APK"));
    }

    private void openZipPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        zipPickerLauncher.launch(Intent.createChooser(intent, "Select Loader ZIP"));
    }

    private void injectLoader() {
        LogUtils.logUser("Universal - Injecting loader...");
        statusText.setText("Injecting loader into APK...\n(This is a placeholder operation)");

        Toast.makeText(this, "Injection (mocked)", Toast.LENGTH_SHORT).show();
        LogUtils.logUser("Universal - Injection completed (simulated)");
    }

    private void exportModifiedApk() {
        Toast.makeText(this, "Exporting modified APK (not implemented)", Toast.LENGTH_SHORT).show();
    }

    private void exportLogs() {
        Toast.makeText(this, "Exporting logs (not implemented)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_log) {
            startActivity(new Intent(this, LogActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_dark_mode) {
            Toast.makeText(this, "Toggle dark mode (not implemented)", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_export_apk) {
            exportModifiedApk();
            return true;
        } else if (id == R.id.action_export_logs) {
            exportLogs();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}