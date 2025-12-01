package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.EventArrayAdapter;
import com.example.zephyr_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class EntEventHistoryActivity extends AppCompatActivity {
    private Button filter_latest_event_button;
    private Button back_latest_event_button;
    private ListView eventListView;
    private ArrayList<Event> eventArrayList;
    private ArrayList<Event> allEventsList;
    private ArrayAdapter<Event> eventArrayAdapter;

    // Filter values
    private String filterEventName = "";
    private String filterEventLocation = "";
    private Long filterStartDateBefore = null;
    private Long filterStartDateAfter = null;
    private Long filterEndDateBefore = null;
    private Long filterEndDateAfter = null;
    private Double filterPriceMin = null;
    private Double filterPriceMax = null;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference eventsRef;
    private String firebaseUid;
    private String androidId;
    private ListenerRegistration eventsListener;

    private ActivityResultLauncher<Intent> filterActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ent_event_history_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        eventsRef = db.collection("events");

        // Get Firebase UID
        if (mAuth.getCurrentUser() != null) {
            firebaseUid = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the list view to be the arraylist of events
        eventListView = findViewById(R.id.ListView_latest_events);
        eventArrayList = new ArrayList<>();
        allEventsList = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(this, eventArrayList);
        eventListView.setAdapter(eventArrayAdapter);

        // Fetch Android ID from profile, then load events
        fetchAndroidIdAndLoadEvents();

        // Set up filter activity launcher
        filterActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Get filter values
                        filterEventName = data.getStringExtra("FILTER_EVENT_NAME");
                        filterEventLocation = data.getStringExtra("FILTER_EVENT_LOCATION");

                        if (data.hasExtra("FILTER_START_DATE_BEFORE")) {
                            filterStartDateBefore = data.getLongExtra("FILTER_START_DATE_BEFORE", 0);
                        } else {
                            filterStartDateBefore = null;
                        }
                        if (data.hasExtra("FILTER_START_DATE_AFTER")) {
                            filterStartDateAfter = data.getLongExtra("FILTER_START_DATE_AFTER", 0);
                        } else {
                            filterStartDateAfter = null;
                        }
                        if (data.hasExtra("FILTER_END_DATE_BEFORE")) {
                            filterEndDateBefore = data.getLongExtra("FILTER_END_DATE_BEFORE", 0);
                        } else {
                            filterEndDateBefore = null;
                        }
                        if (data.hasExtra("FILTER_END_DATE_AFTER")) {
                            filterEndDateAfter = data.getLongExtra("FILTER_END_DATE_AFTER", 0);
                        } else {
                            filterEndDateAfter = null;
                        }
                        if (data.hasExtra("FILTER_PRICE_MIN")) {
                            filterPriceMin = data.getDoubleExtra("FILTER_PRICE_MIN", 0);
                        } else {
                            filterPriceMin = null;
                        }
                        if (data.hasExtra("FILTER_PRICE_MAX")) {
                            filterPriceMax = data.getDoubleExtra("FILTER_PRICE_MAX", 0);
                        } else {
                            filterPriceMax = null;
                        }

                        // Apply filters to event list
                        applyFilters();
                    }
                }
        );

        // Send to event details when clicked
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event e = eventArrayList.get(position);
                Intent intent = new Intent(EntEventHistoryActivity.this, EntEventDetailActivity.class);
                intent.putExtra("FIREBASE_UID", firebaseUid);
                intent.putExtra("EVENT", String.valueOf(e.hashCode()));
                intent.putExtra("FROM_ACTIVITY", "MY_EVENTS");
                startActivity(intent);
            }
        });

        // Listener for button to return to homescreen
        back_latest_event_button = findViewById(R.id.button_latest_event_back);
        back_latest_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventHistoryActivity.this, HomeEntActivity.class);
            intent.putExtra("FIREBASE_UID", firebaseUid);
            startActivity(intent);
        });

        // Filter button listener
        filter_latest_event_button = findViewById(R.id.button_latest_event_filter);
        filter_latest_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventHistoryActivity.this, FilterEventActivity.class);
            intent.putExtra("SOURCE_ACTIVITY", "EntEventHistoryActivity");
            intent.putExtra("FIREBASE_UID", firebaseUid);

            // Pass existing filter values
            if (filterEventName != null && !filterEventName.isEmpty()) {
                intent.putExtra("FILTER_EVENT_NAME", filterEventName);
            }
            if (filterEventLocation != null && !filterEventLocation.isEmpty()) {
                intent.putExtra("FILTER_EVENT_LOCATION", filterEventLocation);
            }
            if (filterStartDateBefore != null) {
                intent.putExtra("FILTER_START_DATE_BEFORE", filterStartDateBefore);
            }
            if (filterStartDateAfter != null) {
                intent.putExtra("FILTER_START_DATE_AFTER", filterStartDateAfter);
            }
            if (filterEndDateBefore != null) {
                intent.putExtra("FILTER_END_DATE_BEFORE", filterEndDateBefore);
            }
            if (filterEndDateAfter != null) {
                intent.putExtra("FILTER_END_DATE_AFTER", filterEndDateAfter);
            }
            if (filterPriceMin != null) {
                intent.putExtra("FILTER_PRICE_MIN", filterPriceMin);
            }
            if (filterPriceMax != null) {
                intent.putExtra("FILTER_PRICE_MAX", filterPriceMax);
            }

            filterActivityLauncher.launch(intent);
        });
    }

    /**
     * Fetch Android ID from user profile, then set up events listener
     */
    private void fetchAndroidIdAndLoadEvents() {
        db.collection("accounts")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        androidId = documentSnapshot.getString("androidId");

                        if (androidId != null && !androidId.isEmpty()) {
                            Log.d("EntEventHistory", "Fetched Android ID: " + androidId);
                            setupEventsListener();
                        } else {
                            Log.e("EntEventHistory", "Android ID is null or empty");
                            Toast.makeText(this, "Profile error: No device ID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("EntEventHistory", "Profile not found");
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntEventHistory", "Error fetching profile", e);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Set up listener for events where user is an entrant (using Android ID)
     */
    private void setupEventsListener() {
        if (androidId == null || androidId.isEmpty()) {
            Log.e("EntEventHistory", "Cannot setup listener: Android ID is null");
            return;
        }

        // Query events using Android ID (event arrays now contain Android IDs)
        eventsListener = eventsRef.whereArrayContains("entrants", androidId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error listening to events", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        allEventsList.clear();

                        for (QueryDocumentSnapshot snapshot : value) {
                            String name = snapshot.getString("name");
                            String time = snapshot.getString("time");
                            String organizer_email = snapshot.getString("organizer_email");

                            Event event = new Event(name, time, organizer_email);

                            // Add additional fields if they exist
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

                            allEventsList.add(event);
                        }

                        applyFilters();
                    } else {
                        // No events found
                        allEventsList.clear();
                        applyFilters();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener when activity is destroyed
        if (eventsListener != null) {
            eventsListener.remove();
            Log.d("EntEventHistory", "Events listener removed");
        }
    }

    /**
     * Applies the current filters to the event list
     */
    private void applyFilters() {
        eventArrayList.clear();

        for (Event event : allEventsList) {
            boolean shouldAdd = true;

            // Filter by event name
            if (filterEventName != null && !filterEventName.isEmpty()) {
                if (event.getName() == null ||
                        !event.getName().toLowerCase().contains(filterEventName.toLowerCase())) {
                    shouldAdd = false;
                }
            }

            // Filter by location
            if (shouldAdd && filterEventLocation != null && !filterEventLocation.isEmpty()) {
                if (event.getLocation() == null ||
                        !event.getLocation().toLowerCase().contains(filterEventLocation.toLowerCase())) {
                    shouldAdd = false;
                }
            }

            // Filter by price range
            if (shouldAdd && filterPriceMin != null) {
                if (event.getPrice() < filterPriceMin) {
                    shouldAdd = false;
                }
            }
            if (shouldAdd && filterPriceMax != null) {
                if (event.getPrice() > filterPriceMax) {
                    shouldAdd = false;
                }
            }

            // Filter by starting date (lott_start_date)
            if (shouldAdd && event.getLott_start_date() != null) {
                long eventStartMillis = convertLocalDateTimeToMillis(event.getLott_start_date());

                if (filterStartDateBefore != null) {
                    if (eventStartMillis > filterStartDateBefore) {
                        shouldAdd = false;
                    }
                }

                if (shouldAdd && filterStartDateAfter != null) {
                    if (eventStartMillis < filterStartDateAfter) {
                        shouldAdd = false;
                    }
                }
            }

            // Filter by ending date (lott_end_date)
            if (shouldAdd && event.getLott_end_date() != null) {
                long eventEndMillis = convertLocalDateTimeToMillis(event.getLott_end_date());

                if (filterEndDateBefore != null) {
                    if (eventEndMillis > filterEndDateBefore) {
                        shouldAdd = false;
                    }
                }

                if (shouldAdd && filterEndDateAfter != null) {
                    if (eventEndMillis < filterEndDateAfter) {
                        shouldAdd = false;
                    }
                }
            }

            if (shouldAdd) {
                eventArrayList.add(event);
            }
        }

        eventArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Converts LocalDateTime to milliseconds using Calendar (API 24 compatible)
     */
    private long convertLocalDateTimeToMillis(java.time.LocalDateTime localDateTime) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDateTime.atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, localDateTime.getYear());
            calendar.set(Calendar.MONTH, localDateTime.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, localDateTime.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, localDateTime.getHour());
            calendar.set(Calendar.MINUTE, localDateTime.getMinute());
            calendar.set(Calendar.SECOND, localDateTime.getSecond());
            calendar.set(Calendar.MILLISECOND, localDateTime.getNano() / 1_000_000);
            return calendar.getTimeInMillis();
        }
    }
}