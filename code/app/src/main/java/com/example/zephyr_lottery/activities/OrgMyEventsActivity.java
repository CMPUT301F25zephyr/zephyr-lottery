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

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.EventArrayAdapter;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrgMyEventsActivity extends AppCompatActivity {

    private static final String TAG = "OrgMyEvents";

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private ListView myEventsListView;
    private Button backButton;
    private Button addEventButton;
    private Button filterButton;

    private final ArrayList<Event> myEventsList = new ArrayList<>();
    // Parallel list of Firestore document IDs for each event in myEventsList
    private final ArrayList<String> eventIdList = new ArrayList<>();

    private EventArrayAdapter myEventsAdapter;

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

        myEventsListView = findViewById(R.id.ListView_my_events);
        backButton = findViewById(R.id.button_my_event_back);
        addEventButton = findViewById(R.id.button_my_event_add_event);
        filterButton = findViewById(R.id.button_my_event_filter);

        myEventsAdapter = new EventArrayAdapter(this, myEventsList);
        myEventsListView.setAdapter(myEventsAdapter);

        listenForEvents();

        myEventsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= eventIdList.size()) return;

            String eventId = eventIdList.get(position);
            int eventCode;
            try {
                eventCode = Integer.parseInt(eventId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid event ID: " + eventId, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OrgMyEventsActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventsActivity.this, HomeOrgActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventsActivity.this, AddEventActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        filterButton.setOnClickListener(v ->
                Toast.makeText(this, "Filter not implemented yet", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Listen to all events in Firestore and keep the list updated.
     */
    private void listenForEvents() {
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to listen for events", error);
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value == null) {
                return;
            }

            myEventsList.clear();
            eventIdList.clear();

            for (QueryDocumentSnapshot doc : value) {
                Event e = doc.toObject(Event.class);
                if (e != null) {
                    myEventsList.add(e);
                    eventIdList.add(doc.getId());
                }
            }

            Log.d(TAG, "Loaded " + myEventsList.size() + " events");
            myEventsAdapter.notifyDataSetChanged();
        });
    }
}
