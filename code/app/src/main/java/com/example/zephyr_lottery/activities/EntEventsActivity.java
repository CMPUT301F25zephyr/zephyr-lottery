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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class EntEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<Event> eventList;
    private EventArrayAdapter eventAdapter;

    private ListView listView;
    private Button backButton;
    private Button filterButton;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ent_events_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        listView = findViewById(R.id.ListView_latest_events);
        backButton = findViewById(R.id.button_latest_event_back);
        filterButton = findViewById(R.id.button_latest_event_filter);

        eventList = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(this, eventList);
        listView.setAdapter(eventAdapter);

        loadEvents();

        // When clicking an event
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selected = eventList.get(position);

            Intent i = new Intent(EntEventsActivity.this, EntEventDetailActivity.class);
            i.putExtra("EVENT", selected.getEventId());
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        backButton.setOnClickListener(v -> finish());
        filterButton.setOnClickListener(v -> Toast.makeText(this, "Filter not implemented.", Toast.LENGTH_SHORT).show());
    }


    private void loadEvents() {
        db.collection("events")
                .orderBy("date_created", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    eventList.clear();

                    for (DocumentSnapshot snap : query.getDocuments()) {
                        Event e = snap.toObject(Event.class);
                        if (e == null) continue;

                        e.setEventId(snap.getId());  // 🔥 REQUIRED
                        eventList.add(e);
                    }

                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("EntEventsActivity", "Failed loading events", e)
                );
    }
}
