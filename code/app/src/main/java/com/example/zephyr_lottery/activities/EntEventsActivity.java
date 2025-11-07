package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class EntEventsActivity extends AppCompatActivity {

    private Button filter_latest_event_button; //doesn't do anything yet
    private Button back_latest_event_button;
    private ListView eventListView;
    private ArrayList<Event> eventArrayList;
    private ArrayAdapter<Event> eventArrayAdapter;

    //databases
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

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

        //set the list view to be the arraylist of events.
        eventListView = findViewById(R.id.ListView_latest_events);
        eventArrayList = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(this, eventArrayList);
        eventListView.setAdapter(eventArrayAdapter);

        //listener. updates array when created and when database changes.
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("EntEventsActivity", "Firestore listen error: " + error.toString());
                return;
            }
            if(value != null && !value.isEmpty()){
                eventArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    // map to Event model
                    Event ev = snapshot.toObject(Event.class);
                    if (ev == null) continue;
                    // store Firestore document id for later lookup
                    ev.setId(snapshot.getId());
                    eventArrayList.add(ev);
                }
                eventArrayAdapter.notifyDataSetChanged();
            }
        });

        // item click: open EventDetailActivity with event id
        eventListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            if (position < 0 || position >= eventArrayList.size()) return;
            Event selected = eventArrayList.get(position);
            if (selected == null || selected.getId() == null) {
                Toast.makeText(this, "Unable to open event details", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(EntEventsActivity.this, com.example.zephyr_lottery.activities.EventDetailActivity.class);
            i.putExtra("EVENT_ID", selected.getId());
            startActivity(i);
        });

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //listener for button to return to homescreen.
        back_latest_event_button = findViewById(R.id.button_latest_event_back);
        back_latest_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventsActivity.this, HomeEntActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
            finish();
        });

        // (optional) wire filter button if you add filtering later
        filterLatestEventButton = findViewById(R.id.button_latest_event_filter);
    }
}
