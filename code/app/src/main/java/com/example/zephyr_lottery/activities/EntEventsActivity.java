package com.example.zephyr_lottery.activities;

import android.os.Bundle;
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

import java.util.ArrayList;

public class EntEventsActivity extends AppCompatActivity {

    //private Button filter_latest_event_button; //not used yet. will implement soon
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

        //set the list view. not working yet
        eventListView = findViewById(R.id.ListView_latest_events);
        eventArrayList = new ArrayList<>();
        //put stuff into arraylist here.
        eventArrayAdapter = new EventArrayAdapter(this, eventArrayList);
        eventListView.setAdapter(eventArrayAdapter);

    }
}
