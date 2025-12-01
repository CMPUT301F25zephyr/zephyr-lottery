package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.NotificationLogArrayAdapter;
import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.models.NotificationLog;
import com.example.zephyr_lottery.repositories.NotificationLogRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdmNotificationLogsActivity extends AppCompatActivity {

    private static final String TAG = "AdmNotificationLogs";
    private ListView logsListView;
    private Button backButton;
    private NotificationLogArrayAdapter adapter;
    private final ArrayList<NotificationLog> logs = new ArrayList<>();
    private final NotificationLogRepository logRepository = new NotificationLogRepository();
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.adm_notification_logs_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logsListView = findViewById(R.id.listView_adm_logs);
        backButton = findViewById(R.id.button_adm_logs_back);

        adapter = new NotificationLogArrayAdapter(this, logs);
        logsListView.setAdapter(adapter);

        String adminEmail = getIntent().getStringExtra("USER_EMAIL");

        listenerRegistration = logRepository.listenToAllLogs(
                this::updateLogs,
                e -> {
                    Log.e(TAG, "Failed to load logs", e);
                    Toast.makeText(this, "Failed to load logs.", Toast.LENGTH_SHORT).show();
                }
        );

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdmNotificationLogsActivity.this, HomeAdmActivity.class);
            intent.putExtra("USER_EMAIL", adminEmail);
            startActivity(intent);
        });
    }

    private void updateLogs(List<NotificationLog> entries) {
        logs.clear();
        logs.addAll(entries);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
