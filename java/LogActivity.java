package com.terrarialoader;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.terrarialoader.util.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogActivity extends AppCompatActivity {

    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Logs");
        setContentView(R.layout.activity_log);

        logText = findViewById(R.id.log_text);
        ScrollView scrollView = findViewById(R.id.log_scroll);

        logText.setText(LogUtils.getLogs());
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

        findViewById(R.id.export_logs_button).setOnClickListener(v -> exportLogs());
    }

    private void exportLogs() {
        try {
            File logDir = new File(getExternalFilesDir(null), "exports");
            if (!logDir.exists()) logDir.mkdirs();

            File logFile = new File(logDir, "logs.txt");
            FileWriter writer = new FileWriter(logFile);
            writer.write(LogUtils.getLogs());
            writer.close();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, getPackageName() + ".provider", logFile));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Logs"));

            Toast.makeText(this, "Logs exported", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export logs", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}