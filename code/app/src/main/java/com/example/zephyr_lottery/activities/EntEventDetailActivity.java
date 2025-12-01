package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntEventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EntEventDetail";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    private Button back_event_details_button;
    private Button register_button;
    private Button leave_button;

    private TextView title;
    private TextView closingDate;
    private TextView startEnd;
    private TextView dayTime;
    private TextView location;
    private TextView price;
    private TextView description;
    private TextView entrantNumbers;
    private TextView lotteryWinners;
    private ImageView eventImageView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference eventsRef;
    private DocumentReference docRef;

    private FusedLocationProviderClient fusedLocationClient;
    private EventRepository eventRepository;
    private String eventId;
    private String firebaseUid;
    private String androidId;  // Will be fetched from profile

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ent_eventdetail_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI refs
        title = findViewById(R.id.textView_ed_title);
        closingDate = findViewById(R.id.textView_closingdate);
        startEnd = findViewById(R.id.textView_startenddates);
        dayTime = findViewById(R.id.textView_day_time);
        location = findViewById(R.id.textView_location_string);
        price = findViewById(R.id.textView_price_string);
        description = findViewById(R.id.textView_description_string);
        entrantNumbers = findViewById(R.id.textView_currententrants);
        lotteryWinners = findViewById(R.id.textView_lotterywinners);
        eventImageView = findViewById(R.id.imageView_ent_eventImage);

        register_button = findViewById(R.id.button_register);
        leave_button = findViewById(R.id.button_leave_waitlist);
        back_event_details_button = findViewById(R.id.button_event_details_back);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        eventRepository = new EventRepository();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        // Get Firebase UID
        if (mAuth.getCurrentUser() != null) {
            firebaseUid = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get Android ID from profile
        fetchAndroidIdFromProfile();

        String eventHash = getIntent().getStringExtra("EVENT");
        eventId = eventHash;
        docRef = eventsRef.document(eventHash);

        loadEventDetails();

        // Register button listener
        register_button.setOnClickListener(view -> {
            if (androidId == null) {
                Toast.makeText(this, "Loading profile...", Toast.LENGTH_SHORT).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {
                if (!currentEvent.exists()) {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add this user to the all entrants arraylist
                List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                if (entrantsList == null) {
                    entrantsList = new ArrayList<>();
                } else {
                    entrantsList = new ArrayList<>(entrantsList);
                    entrantsList.remove(null);
                }

                // Check if already registered (using Android ID)
                if (entrantsList.contains(androidId)) {
                    Toast.makeText(this, "You are already on the waiting list.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
                List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
                List<String> winners_list = (List<String>) currentEvent.get("winners");

                // Calculate the number of entrants total
                int num_entrants = (waitlist_list != null ? waitlist_list.size() : 0);
                num_entrants += (accepted_list != null ? accepted_list.size() : 0);
                num_entrants += (winners_list != null ? winners_list.size() : 0);

                // Checking entrant limit
                Long limitLong = currentEvent.getLong("limit");
                boolean hasLimit = limitLong != null;
                int limitValue = 0;

                if (hasLimit) {
                    limitValue = limitLong.intValue();
                    if (num_entrants >= limitValue) {
                        Toast.makeText(this, "This event is full.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                int currentSize = num_entrants;
                String limitDisplay = hasLimit ? String.valueOf(limitValue) : "?";

                // Add to entrants array (using Android ID)
                docRef.update("entrants", FieldValue.arrayUnion(androidId))
                        .addOnSuccessListener(aVoid -> {
                            // Also add to entrants_waitlist
                            docRef.update("entrants_waitlist", FieldValue.arrayUnion(androidId))
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(this, "Joined waiting list.",
                                                Toast.LENGTH_LONG).show();

                                        saveWaitingListLocation(eventHash, androidId);

                                        int newCount = currentSize + 1;
                                        entrantNumbers.setText(
                                                "Current Entrants: " + newCount + "/" + limitDisplay + " slots"
                                        );
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error adding to waitlist", e);
                                        Toast.makeText(this,
                                                "Failed to join. Please leave and try again.",
                                                Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding entrant", e);
                            Toast.makeText(this, "Failed to join. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        });
            });
        });

        // Leave waitlist button listener
        leave_button.setOnClickListener(view -> {
            if (androidId == null) {
                Toast.makeText(this, "Loading profile...", Toast.LENGTH_SHORT).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {
                if (!currentEvent.exists()) {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if in entrants list (using Android ID)
                List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                if (entrantsList == null || !entrantsList.contains(androidId)) {
                    Toast.makeText(this, "You are not on the waiting list.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Long limitLong = currentEvent.getLong("limit");
                String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";

                List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
                List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
                List<String> winners_list = (List<String>) currentEvent.get("winners");

                int num_entrants = (waitlist_list != null ? waitlist_list.size() : 0);
                num_entrants += (accepted_list != null ? accepted_list.size() : 0);
                num_entrants += (winners_list != null ? winners_list.size() : 0);

                int currentSize = num_entrants;

                // Remove from entrants array (using Android ID)
                docRef.update("entrants", FieldValue.arrayRemove(androidId))
                        .addOnSuccessListener(aVoid -> {
                            // Also remove from entrants_waitlist
                            docRef.update("entrants_waitlist", FieldValue.arrayRemove(androidId))
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(this, "You have left the waiting list.",
                                                Toast.LENGTH_LONG).show();

                                        int newCount = Math.max(0, currentSize - 1);
                                        entrantNumbers.setText(
                                                "Current Entrants: " + newCount + "/" + limitDisplay + " slots"
                                        );

                                        // Remove from waitingList collection (using Android ID)
                                        db.collection("events")
                                                .document(eventId)
                                                .collection("waitingList")
                                                .document(androidId)
                                                .delete()
                                                .addOnSuccessListener(v ->
                                                        Log.d(TAG, "Removed from waitingList collection")
                                                )
                                                .addOnFailureListener(e ->
                                                        Log.e(TAG, "Failed to delete waitingList entry", e)
                                                );
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error removing from waitlist", e);
                                        Toast.makeText(this,
                                                "Failed to leave. Please rejoin and try again.",
                                                Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error removing entrant", e);
                            Toast.makeText(this, "Failed to leave. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        });
            });
        });

        // Back button listener
        back_event_details_button.setOnClickListener(view -> {
            String fromActivity = getIntent().getStringExtra("FROM_ACTIVITY");
            Intent intent;

            if ("MY_EVENTS".equals(fromActivity)) {
                intent = new Intent(this, EntEventHistoryActivity.class);
            } else if ("HOME_ENT".equals(fromActivity)) {
                intent = new Intent(this, HomeEntActivity.class);
            } else {
                intent = new Intent(this, EntEventsActivity.class);
            }

            intent.putExtra("FIREBASE_UID", firebaseUid);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Fetch Android ID from the user's profile
     */
    private void fetchAndroidIdFromProfile() {
        db.collection("accounts")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        androidId = documentSnapshot.getString("androidId");
                        Log.d(TAG, "Fetched Android ID: " + androidId);
                    } else {
                        Log.e(TAG, "Profile not found");
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile", e);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventDetails() {
        docRef.get().addOnSuccessListener(currentEvent -> {
            if (!currentEvent.exists()) {
                Log.e("Firestore", "Event not found.");
                return;
            }

            title.setText(currentEvent.getString("name"));
            closingDate.setText("");
            startEnd.setText("");

            String weekdayString = currentEvent.getString("weekdayString");
            String timeString = currentEvent.getString("time");
            StringBuilder dayTimeText = new StringBuilder();
            if (weekdayString != null) {
                dayTimeText.append(weekdayString).append(" ");
            }
            if (timeString != null) {
                dayTimeText.append(timeString);
            }
            dayTime.setText(dayTimeText.toString());

            location.setText(currentEvent.getString("location"));

            Object priceObj = currentEvent.get("price");
            if (priceObj != null) {
                price.setText("$" + priceObj.toString());
            } else {
                price.setText("$0");
            }

            description.setText(currentEvent.getString("description"));

            // Get number of current entrants
            List<String> waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            List<String> accepted = (List<String>) currentEvent.get("accepted_entrants");
            List<String> winners = (List<String>) currentEvent.get("winners");

            int entrantCount = (waitlist != null ? waitlist.size() : 0);
            entrantCount += (accepted != null ? accepted.size() : 0);
            entrantCount += (winners != null ? winners.size() : 0);

            Long limitLong = currentEvent.getLong("limit");
            String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";
            entrantNumbers.setText("Current Entrants: " + entrantCount + "/" + limitDisplay + " slots");

            Long sampleSizeLong = currentEvent.getLong("sampleSize");
            int sampleSize = sampleSizeLong != null ? sampleSizeLong.intValue() : 0;
            lotteryWinners.setText("Lottery Winners: " + sampleSize);

            // Get image from database, convert to bitmap, display image
            String image_base64 = currentEvent.getString("posterImage");
            if (image_base64 != null) {
                byte[] decodedBytes = Base64.decode(image_base64, Base64.DEFAULT);
                Bitmap image_bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                eventImageView.setImageBitmap(image_bitmap);
            }
        });
    }

    /**
     * Save the entrant's approximate join location into:
     *   events/{eventId}/waitingList/{androidId}
     */
    private void saveWaitingListLocation(String eventId, String androidId) {
        if (eventId == null || androidId == null) {
            Log.e(TAG, "saveWaitingListLocation: eventId or androidId is null");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    Double lat = null;
                    Double lng = null;

                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }

                    // Use Android ID as the document ID in waitingList
                    eventRepository.addEntrantLocationToWaitingList(eventId, androidId, lat, lng);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to get device location", e)
                );
    }
}