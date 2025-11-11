package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrgMyEventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView detailsText;
    private Button backButton;
    private Button entrantsButton;
    private Button generateQrButton;

    private int eventCode;
    private String userEmail;

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

        db = FirebaseFirestore.getInstance();

        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        detailsText = findViewById(R.id.text_placeholder);
        backButton = findViewById(R.id.button_org_event_details_back);
        entrantsButton = findViewById(R.id.button_entrants);
        generateQrButton = findViewById(R.id.button_generate_qr);

        loadEventDetails();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        entrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventEntrantsActivity.class);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        generateQrButton.setOnClickListener(v ->
                Toast.makeText(this, "QR generation not implemented yet", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadEventDetails() {
        if (eventCode == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = Integer.toString(eventCode);
        DocumentReference docRef = db.collection("events").document(docId);
        docRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Event e = snapshot.toObject(Event.class);
                    if (e == null) {
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = e.getName() != null ? e.getName() : "";
                    String time = e.getTime() != null ? e.getTime() : "";
                    String weekday = e.getWeekdayString() != null ? e.getWeekdayString() : "";
                    String location = e.getLocation() != null ? e.getLocation() : "";
                    String description = e.getDescription() != null ? e.getDescription() : "";
                    String period = e.getPeriod() != null ? e.getPeriod() : "";
                    float price = e.getPrice();
                    int limit = e.getLimit();
                    int sampleSize = e.getSampleSize();

                    String text = ""
                            + "Name: " + name + "\n"
                            + "Time: " + time + " (" + weekday + ")\n"
                            + "Location: " + location + "\n"
                            + "Price: " + price + "\n"
                            + "Registration period: " + period + "\n"
                            + "Entrant limit: " + limit + "\n"
                            + "Sample size (lottery winners): " + sampleSize + "\n"
                            + "\nDescription:\n" + description;

                    detailsText.setText(text);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }
}
