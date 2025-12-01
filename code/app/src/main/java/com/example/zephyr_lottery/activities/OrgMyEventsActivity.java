package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class OrgMyEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private ListView myEventsListView;
    private Button backButton;
    private Button addEventButton;
    private Button filterButton;

    private ArrayList<Event> myEventsList;
    private ArrayList<Event> allMyEventsList; // Keep original unfiltered list
    private EventArrayAdapter myEventsAdapter;

    // Filter values
    private String filterEventName = "";
    private String filterEventLocation = "";
    private Long filterStartDateBefore = null;
    private Long filterStartDateAfter = null;
    private Long filterEndDateBefore = null;
    private Long filterEndDateAfter = null;
    private Double filterPriceMin = null;
    private Double filterPriceMax = null;

    private String userEmail;

    private ActivityResultLauncher<Intent> filterActivityLauncher;

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
        allMyEventsList = new ArrayList<>(); // Initialize full list
        myEventsAdapter = new EventArrayAdapter(this, myEventsList);
        myEventsListView.setAdapter(myEventsAdapter);

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

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventsActivity.this, FilterEventActivity.class);
            intent.putExtra("SOURCE_ACTIVITY", "OrgMyEventsActivity");
            intent.putExtra("USER_EMAIL", userEmail);

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

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        allMyEventsList.clear();
        myEventsList.clear();
        myEventsAdapter.notifyDataSetChanged();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Organizer email missing", Toast.LENGTH_SHORT).show();
            return;
        }

        eventsRef.whereEqualTo("organizer_email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allMyEventsList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            allMyEventsList.add(e);
                        }
                    }
                    applyFilters(); // Apply filters after loading data
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Applies the current filters to the event list
     */
    private void applyFilters() {
        myEventsList.clear();

        for (Event event : allMyEventsList) {
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
                myEventsList.add(event);
            }
        }

        myEventsAdapter.notifyDataSetChanged();
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