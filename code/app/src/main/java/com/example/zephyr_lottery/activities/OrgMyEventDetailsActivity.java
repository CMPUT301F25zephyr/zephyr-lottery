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
    private Button button_notifySelected;

    private EventRepository repo;
    private String user_email;
    private int event_code;

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

        // get extras from intent
        user_email = getIntent().getStringExtra("USER_EMAIL");
        event_code = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);

        // Generate QR button
        button_generateQR = findViewById(R.id.button_generate_qr);
        button_generateQR.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, QRCodeActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            intent.putExtra("EVENT_CLICKED_CODE", event_code);
            startActivity(intent);
        });

        // Notify Selected Entrants button
        button_notifySelected = findViewById(R.id.button_notify_selected);
        button_notifySelected.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Notify selected entrants?")
                    .setMessage("This will send a notification to all selected entrants.")
                    .setPositiveButton("Notify", (d, w) -> {
                        repo.notifyAllSelectedEntrants(String.valueOf(event_code),
                                () -> Toast.makeText(this, "Notifications sent", Toast.LENGTH_SHORT).show(),
                                e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}