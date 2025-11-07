package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.repositories.EventRepository;

public class OrgMyEventDetailsActivity extends AppCompatActivity {

    private Button button_generateQR;
    private Button button_notifyAll;

    private EventRepository repo;
    private String userEmail;
    private String eventId; // use String for Firestore doc ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_details_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repo = new EventRepository();

        // get extras from intent safely
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        eventId = getIntent().getStringExtra("EVENT_ID"); // use EVENT_ID consistently

        if (eventId == null) {
            Toast.makeText(this, "Error: missing event ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Generate QR button
        button_generateQR = findViewById(R.id.button_generate_qr);
        button_generateQR.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, QRCodeActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        // Notify All button
        button_notifyAll = findViewById(R.id.button_notify_all);
        button_notifyAll.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Notify all waiting list entrants?")
                    .setMessage("This will send a notification to everyone still waiting.")
                    .setPositiveButton("Notify", (d, w) -> {
                        repo.notifyAllWaitingList(eventId,
                                () -> Toast.makeText(this, "Notifications sent", Toast.LENGTH_SHORT).show(),
                                e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}