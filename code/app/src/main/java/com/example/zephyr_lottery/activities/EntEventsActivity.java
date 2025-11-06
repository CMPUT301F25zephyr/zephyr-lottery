package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.EventArrayAdapter;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EntEventsActivity extends AppCompatActivity {

    private static final String TAG = "EntEventsActivity";

    private Button joinLeaveButton;
    private Button backButton;
    private ListView eventListView;

    private ArrayList<Event> eventList;
    private ArrayList<String> eventIdList;
    private ArrayAdapter<Event> eventAdapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private String userEmail;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ent_events_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        eventListView = findViewById(R.id.ListView_latest_events);
        eventList = new ArrayList<>();
        eventIdList = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(this, eventList);
        eventListView.setAdapter(eventAdapter);

        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            updateJoinLeaveButtonLabel();
        });

        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Firestore error", error);
                return;
            }
            if (value != null && !value.isEmpty()) {
                eventList.clear();
                eventIdList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String eventId = snapshot.getId();
                    String name = snapshot.getString("name");
                    String times = snapshot.getString("times");

                    Event event = new Event(name, times);
                    eventList.add(event);
                    eventIdList.add(eventId);
                }
                eventAdapter.notifyDataSetChanged();
                selectedPosition = -1;
                joinLeaveButton.setEnabled(false);
                joinLeaveButton.setText("Join Waiting List");
            }
        });

        backButton = findViewById(R.id.button_latest_event_back);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventsActivity.this, HomeEntActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        joinLeaveButton = findViewById(R.id.button_latest_event_filter);
        joinLeaveButton.setEnabled(false);
        joinLeaveButton.setOnClickListener(view -> toggleJoinLeave());
    }

    private void updateJoinLeaveButtonLabel() {
        if (selectedPosition < 0 || selectedPosition >= eventIdList.size()) {
            joinLeaveButton.setText("Join Waiting List");
            joinLeaveButton.setEnabled(false);
            return;
        }
        joinLeaveButton.setEnabled(true);

        String eventId = eventIdList.get(selectedPosition);
        DocumentReference waitRef = eventsRef
                .document(eventId)
                .collection("waitingList")
                .document(userEmail);

        waitRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        joinLeaveButton.setText("Leave Waiting List");
                    } else {
                        joinLeaveButton.setText("Join Waiting List");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "check waiting list failed", e);
                    joinLeaveButton.setText("Join Waiting List");
                });
    }

    private void toggleJoinLeave() {
        if (selectedPosition < 0 || selectedPosition >= eventIdList.size()) {
            Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence label = joinLeaveButton.getText();
        if (label != null && label.toString().startsWith("Join")) {
            joinWaitingList();
        } else {
            leaveWaitingList();
        }
    }

    // US 01.01.01
    private void joinWaitingList() {
        String eventId = eventIdList.get(selectedPosition);
        Event selectedEvent = eventList.get(selectedPosition);

        DocumentReference waitRef = eventsRef
                .document(eventId)
                .collection("waitingList")
                .document(userEmail);

        Map<String, Object> data = new HashMap<>();
        data.put("email", userEmail);
        data.put("joinedAt", FieldValue.serverTimestamp());

        waitRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    selectedEvent.addEntrant(userEmail);  // update model
                    eventAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Joined waiting list: " + eventId);
                    updateJoinLeaveButtonLabel();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "join waiting list error", e);
                });
    }

    // US 01.01.02
    private void leaveWaitingList() {
        String eventId = eventIdList.get(selectedPosition);
        Event selectedEvent = eventList.get(selectedPosition);

        DocumentReference waitRef = eventsRef
                .document(eventId)
                .collection("waitingList")
                .document(userEmail);

        waitRef.delete()
                .addOnSuccessListener(aVoid -> {
                    selectedEvent.removeEntrant(userEmail);  // update model
                    eventAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Left waiting list: " + eventId);
                    updateJoinLeaveButtonLabel();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "leave waiting list error", e);
                });
    }
}
