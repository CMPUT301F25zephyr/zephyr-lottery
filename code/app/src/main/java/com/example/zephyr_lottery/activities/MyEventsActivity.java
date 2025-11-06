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

public class MyEventsActivity extends AppCompatActivity {
    private Button back_my_event_button;
    private Button add_event_my_event_button;
    private ListView myEventListView;
    private ArrayList<Event> myEventArrayList;
    private ArrayAdapter<Event> myEventArrayAdapter;

    //databases
    private FirebaseFirestore db;
    private CollectionReference eventsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.my_events_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        //set the list view to be the arraylist of events.
        myEventListView = findViewById(R.id.ListView_my_events);
        myEventArrayList = new ArrayList<>();
        myEventArrayAdapter = new EventArrayAdapter(this, myEventArrayList);
        myEventListView.setAdapter(myEventArrayAdapter);

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //listener. updates array when created and when database changes.
        //only for events with this email as the organizer.
        eventsRef
                .whereEqualTo("organizer_email", user_email)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        myEventArrayList.clear();
                        for (QueryDocumentSnapshot snapshot : value) {
                            String name = snapshot.getString("name");
                            String times = snapshot.getString("times");
                            String orgEmail = snapshot.getString("organizer_email");

                            Event event = new Event(name, times, orgEmail);

                            //add additional fields (if they exist?)
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

                            myEventArrayList.add(event);
                        }
                        myEventArrayAdapter.notifyDataSetChanged();
                    }
                });

        //listener for button to return to ORGNAIZER homescreen.
        back_my_event_button = findViewById(R.id.button_my_event_back);
        back_my_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(MyEventsActivity.this, HomeOrgActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        //add event button listener
        add_event_my_event_button = findViewById(R.id.button_my_event_add_event);
        add_event_my_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(MyEventsActivity.this, AddEventActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });




    }
}
