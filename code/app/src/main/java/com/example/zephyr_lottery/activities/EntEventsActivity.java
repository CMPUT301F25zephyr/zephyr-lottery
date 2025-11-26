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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
        loadEvents();

        //listener. updates array when created and when database changes.
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                eventArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String time = snapshot.getString("time");
                    String organizer_email = snapshot.getString("organizer_email");
                    //add any future attributes for event here.

                    Event event = new Event(name, time, organizer_email);

                    //add additional fields if they exist
                    if (snapshot.contains("description")) {
                        event.setDescription(snapshot.getString("description"));
                    }
                    if (snapshot.contains("price")) {
                        event.setPrice(snapshot.getDouble("price").floatValue());
                    }
                    if (snapshot.contains("location")) {
                        event.setLocation(snapshot.getString("location"));
                    }
                    if (snapshot.contains("weekday")) {
                        event.setWeekday(snapshot.getLong("weekday").intValue());
                    }
                    if (snapshot.contains("period")) {
                        event.setPeriod(snapshot.getString("period"));
                    }

                    eventArrayList.add(event);
                }
                eventArrayAdapter.notifyDataSetChanged();
            }
        });

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        // Send to event details when clicked
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event e = eventArrayList.get(position);
                Intent intent = new Intent(EntEventsActivity.this, EntEventDetailActivity.class);
                intent.putExtra("USER_EMAIL", user_email);
                intent.putExtra("EVENT", String.valueOf(e.hashCode()));
                intent.putExtra("FROM_ACTIVITY", "ALL_EVENTS");
                startActivity(intent);
            }
        });
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


        //listener for button to return to homescreen.
        back_latest_event_button = findViewById(R.id.button_latest_event_back);
        back_latest_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventsActivity.this, HomeEntActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }
}
