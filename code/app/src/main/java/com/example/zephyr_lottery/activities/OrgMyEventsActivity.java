package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

public class OrgMyEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private ListView myEventsListView;
    private Button backButton;
    private Button addEventButton;
    private Button filterButton;

    private ArrayList<Event> myEventsList;
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

        myEventsList = new ArrayList<>();
        myEventsAdapter = new EventArrayAdapter(this, myEventsList);
        myEventsListView.setAdapter(myEventsAdapter);

        loadMyEvents();

        myEventsListView.setOnItemClickListener((parent, view, position, id) -> {
            Event clicked = myEventsList.get(position);
            if (clicked == null) return;

            String eventId = Integer.toString(clicked.hashCode());

            Intent intent = new Intent(OrgMyEventsActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("EVENT_CLICKED_CODE", Integer.parseInt(eventId));
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

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        myEventsList.clear();
        myEventsAdapter.notifyDataSetChanged();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Organizer email missing", Toast.LENGTH_SHORT).show();
            return;
        }

        eventsRef.whereEqualTo("organizer_email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myEventsList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            myEventsList.add(e);
                        }
                    }
                    myEventsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }
}
