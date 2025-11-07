package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OrgMyEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrgMyEventDetails";

    private FirebaseFirestore db;
    private String eventId;
    private int eventCode;
    private String organizerEmail;

    private TextView tvName;
    private TextView tvRegPeriod;
    private TextView tvTimes;
    private TextView tvLocation;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvEntrants;
    private TextView tvWinners;

    private Button buttonGenerateQr;
    private Button buttonViewEntrants;
    private Button buttonDrawLottery;
    private Button buttonBack;

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

        organizerEmail = getIntent().getStringExtra("USER_EMAIL");
        eventId = getIntent().getStringExtra("EVENT_ID");
        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);

        tvName = findViewById(R.id.text_org_event_name);
        tvRegPeriod = findViewById(R.id.text_org_registration_period_value);
        tvTimes = findViewById(R.id.text_org_times_value);
        tvLocation = findViewById(R.id.text_org_location_value);
        tvPrice = findViewById(R.id.text_org_price_value);
        tvDescription = findViewById(R.id.text_org_description_value);
        tvEntrants = findViewById(R.id.text_org_current_entrants_value);
        tvWinners = findViewById(R.id.text_org_lottery_winners_value);

        buttonGenerateQr = findViewById(R.id.button_generate_qr);
        buttonViewEntrants = findViewById(R.id.button_entrants);
        buttonDrawLottery = findViewById(R.id.button_draw_lottery);
        buttonBack = findViewById(R.id.button_org_event_details_back);

        db = FirebaseFirestore.getInstance();

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEventDetails();

        buttonBack.setOnClickListener(v -> finish());

        buttonViewEntrants.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventEntrantsActivity.class);
            intent.putExtra("USER_EMAIL", organizerEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        buttonGenerateQr.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, QRCodeActivity.class);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });

        buttonDrawLottery.setOnClickListener(v -> drawLotteryWinners());
    }

    private void loadEventDetails() {
        DocumentReference ref = db.collection("events").document(eventId);
        ref.get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    tvName.setText(event.getName());

                    String period = event.getPeriod() != null ? event.getPeriod() : "Not set";
                    tvRegPeriod.setText(period);

                    String weekday = event.getWeekdayString();
                    String time = event.getTime();
                    tvTimes.setText(weekday + " " + time);

                    tvLocation.setText(event.getLocation() != null ? event.getLocation() : "Not set");

                    tvPrice.setText(String.valueOf(event.getPrice()));

                    tvDescription.setText(
                            event.getDescription() != null ? event.getDescription() : "No description"
                    );

                    ArrayList<String> entrants = event.getEntrants();
                    int entrantsCount = entrants != null ? entrants.size() : 0;
                    tvEntrants.setText(entrantsCount + "/" + event.getLimit() + " slots");

                    tvWinners.setText(String.valueOf(event.getSampleSize()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event details", e);
                    Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void drawLotteryWinners() {
        DocumentReference ref = db.collection("events").document(eventId);
        ref.get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> entrants = event.getEntrants();
                    if (entrants == null || entrants.isEmpty()) {
                        Toast.makeText(this, "No entrants to draw from", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int sampleSize = event.getSampleSize();
                    if (sampleSize <= 0) {
                        Toast.makeText(this, "Sample size is not set", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Collections.shuffle(entrants);
                    int winnersToPick = Math.min(sampleSize, entrants.size());
                    ArrayList<String> winners = new ArrayList<>(entrants.subList(0, winnersToPick));

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("selectedEntrants", winners);

                    ref.update(updates)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Lottery drawn", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save winners", e);
                                Toast.makeText(this, "Error saving winners", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load entrants for drawing", e);
                    Toast.makeText(this, "Error loading entrants", Toast.LENGTH_SHORT).show();
                });
    }
}
