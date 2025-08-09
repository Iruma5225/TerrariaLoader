package com.terrarialoader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.terrarialoader.R;
import com.terrarialoader.diagnostic.DiagnosticManager;
import com.terrarialoader.util.LogUtils;

import java.io.File;

public class OfflineDiagnosticActivity extends Activity {

    private TextView reportText;
    private ScrollView reportScroll;
    private Button runDiagnosticsBtn;
    private Button attemptRepairBtn;
    private Button exportReportBtn;
    private DiagnosticManager diagnosticManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_diagnostic);
        setTitle("🧪 Offline Diagnostic & Repair");

        LogUtils.logUser("OfflineDiagnosticActivity launched");

        diagnosticManager = new DiagnosticManager(this);

        reportText = findViewById(R.id.report_text);
        reportScroll = findViewById(R.id.report_scroll);
        runDiagnosticsBtn = findViewById(R.id.run_diagnostics_button);
        attemptRepairBtn = findViewById(R.id.repair_button);
        exportReportBtn = findViewById(R.id.export_report_button);

        runDiagnosticsBtn.setOnClickListener(v -> runDiagnostics());
        attemptRepairBtn.setOnClickListener(v -> attemptRepair());
        exportReportBtn.setOnClickListener(v -> exportReport());

        runDiagnostics(); // Auto-run on open
    }

    private void runDiagnostics() {
        String report = diagnosticManager.runFullDiagnostics();
        reportText.setText(report);
        reportScroll.post(() -> reportScroll.fullScroll(View.FOCUS_DOWN));
        Toast.makeText(this, "Diagnostics complete", Toast.LENGTH_SHORT).show();
    }

    private void attemptRepair() {
        boolean result = diagnosticManager.attemptSelfRepair();
        Toast.makeText(this, result ? "Repair complete ✅" : "Repair finished with issues ⚠️", Toast.LENGTH_LONG).show();
        runDiagnostics();
    }

    private void exportReport() {
        File exportFile = new File(getExternalFilesDir("exports"), "diagnostic_report.txt");
        boolean success = diagnosticManager.exportReportToFile(exportFile);
        Toast.makeText(this, success ? "Report exported to: " + exportFile.getName() : "Failed to export", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.logUser("OfflineDiagnosticActivity closed");
    }
}