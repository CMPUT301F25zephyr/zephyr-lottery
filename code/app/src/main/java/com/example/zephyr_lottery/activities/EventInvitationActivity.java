package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventInvitationActivity extends AppCompatActivity {

    private EventRepository repo;
    private String eventId;
    private String userId;

    private Button btnAccept;
    private Button btnDecline;

    private TextView textEventTitle, textEventTime, textEventLocation, textEventPrice, textEventDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_invitation);

        repo = new EventRepository();

        // get extras safely
        eventId = getIntent().getStringExtra("EVENT_ID");
        userId = getIntent().getStringExtra("USER_ID");

        if (eventId == null || userId == null) {
            Toast.makeText(this, "Error: missing event/user ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bind views
        textEventTitle = findViewById(R.id.text_event_title);
        textEventTime = findViewById(R.id.text_event_time);
        textEventLocation = findViewById(R.id.text_event_location);
        textEventPrice = findViewById(R.id.text_event_price);
        textEventDescription = findViewById(R.id.text_event_description);

        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        // Fetch event details
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        textEventTitle.setText(snapshot.getString("name"));
                        textEventTime.setText(snapshot.getString("time"));
                        textEventLocation.setText(snapshot.getString("location"));
                        Double price = snapshot.getDouble("price");
                        textEventPrice.setText(price != null ? "Price: $" + price : "Price: N/A");
                        textEventDescription.setText(snapshot.getString("description"));
                    } else {
                        textEventTitle.setText("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    textEventTitle.setText("Event details unavailable");
                });

        // Accept button
        btnAccept.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Accept invitation?")
                        .setMessage("Confirm you want to participate.")
                        .setPositiveButton("Accept", (d, w) ->
                                repo.acceptInvitation(eventId, userId,
                                        () -> Toast.makeText(this, "Accepted", Toast.LENGTH_SHORT).show(),
                                        e -> Toast.makeText(this, "Failed to accept", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        // Decline button
        btnDecline.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Decline invitation?")
                        .setMessage("Confirm you want to decline. The next entrant may be invited.")
                        .setPositiveButton("Decline", (d, w) ->
                                repo.declineInvitation(eventId, userId,
                                        () -> Toast.makeText(this, "Declined", Toast.LENGTH_SHORT).show(),
                                        e -> Toast.makeText(this, "Failed to decline", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }
}