package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.EventArrayAdapter;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrgMyEventsActivity extends AppCompatActivity {

    private static final String TAG = "OrgMyEventsActivity";

    private ListView eventsListView;
    private ArrayList<Event> myEvents;
    private EventArrayAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_events_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        eventsListView = findViewById(R.id.org_my_events_list);
        myEvents = new ArrayList<>();
        adapter = new EventArrayAdapter(this, myEvents);
        eventsListView.setAdapter(adapter);

        Button backButton = findViewById(R.id.button_back_home_org);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventsActivity.this, HomeOrgActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= myEvents.size()) {
                return;
            }
            Event clickedEvent = myEvents.get(position);
            if (clickedEvent == null) {
                return;
            }

            int eventCode = clickedEvent.hashCode();
            String eventId = Integer.toString(eventCode);

            Intent intent = new Intent(OrgMyEventsActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode); // for QR / old code
            intent.putExtra("EVENT_ID", eventId);             // for Firestore lookup
            startActivity(intent);
        });

        loadMyEvents();
    }

    private void loadMyEvents() {
        if (userEmail == null || userEmail.isEmpty()) {
            Log.w(TAG, "User email missing, cannot load events");
            return;
        }

        eventsRef.whereEqualTo("organizer_email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myEvents.clear();
                    querySnapshot.getDocuments().forEach(doc -> {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            myEvents.add(e);
                        }
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load organizer events", e));
    }
}
