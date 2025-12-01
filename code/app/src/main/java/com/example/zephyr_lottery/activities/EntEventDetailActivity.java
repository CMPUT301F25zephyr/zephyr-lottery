package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
    private CollectionReference eventsRef;
    private DocumentReference docRef;

    private FusedLocationProviderClient fusedLocationClient;
    private EventRepository eventRepository;

    private String eventId;

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

        // UI
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

        // Email
        String user_email = getIntent().getStringExtra("USER_EMAIL");
        if (user_email == null || user_email.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                user_email = currentUser.getEmail();
            }
        }
        final String currentUserEmail = user_email;

        // Event hash
        String eventHash = getIntent().getStringExtra("EVENT");
        eventId = eventHash;

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        docRef = eventsRef.document(eventHash);

        loadEventDetails();

        String finalUserEmail = currentUserEmail;


        register_button.setOnClickListener(view -> {
            if (finalUserEmail == null || finalUserEmail.isEmpty()) {
                Toast.makeText(this, "Unable to determine your account. Please sign in again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {

                if (!currentEvent.exists()) {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }


                Boolean geoRequired = currentEvent.getBoolean("geolocationRequired");

                if (geoRequired != null && geoRequired) {

                    Double eventLat = currentEvent.getDouble("eventLatitude");
                    Double eventLng = currentEvent.getDouble("eventLongitude");
                    Double radius = currentEvent.getDouble("allowedRadiusMeters");

                    if (eventLat == null || eventLng == null || radius == null) {
                        Toast.makeText(this, "Event geolocation data missing.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE
                        );
                        return;
                    }

                    fusedLocationClient.getLastLocation().addOnSuccessListener(userLoc -> {

                        if (userLoc == null) {
                            Toast.makeText(this,
                                    "Cannot determine your location. Enable GPS.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        double userLat = userLoc.getLatitude();
                        double userLng = userLoc.getLongitude();
                        double dist = distanceMeters(userLat, userLng, eventLat, eventLng);

                        if (dist > radius) {
                            Toast.makeText(this,
                                    "You must be near the event to join (" +
                                            (int) dist + "m away, limit " + radius + "m)",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // If user passes geolocation check → go to normal join logic
                        continueRegistrationFlow(currentEvent, finalUserEmail);
                    });

                    return; // STOP HERE until geolocation finishes
                }

                // No geolocation required: normal join
                continueRegistrationFlow(currentEvent, finalUserEmail);
            });
        });


        leave_button.setOnClickListener(view -> {
            if (finalUserEmail == null || finalUserEmail.isEmpty()) {
                Toast.makeText(this,
                        "Unable to determine your account. Please sign in again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {

                if (!currentEvent.exists()) {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                if (entrantsList == null) entrantsList = new ArrayList<>();
                entrantsList.remove(null);

                if (!entrantsList.contains(finalUserEmail)) {
                    Toast.makeText(this,
                            "You are not on the waiting list.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
                List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
                List<String> winners_list = (List<String>) currentEvent.get("winners");

                int num_entrants =
                        (waitlist_list != null ? waitlist_list.size() : 0)
                                + (accepted_list != null ? accepted_list.size() : 0)
                                + (winners_list != null ? winners_list.size() : 0);

                Long limitLong = currentEvent.getLong("limit");
                int limitValue;
                if (limitLong != null) {
                    limitValue = limitLong.intValue();
                } else {
                    limitValue = -1;
                }

                String limitDisplay;
                if (limitValue == -1) {
                    limitDisplay = "infinite";
                } else {
                    limitDisplay = String.valueOf(limitValue);
                }

                int currentSize = num_entrants;

                // Remove from Firestore lists
                docRef.update("entrants", FieldValue.arrayRemove(finalUserEmail))
                        .addOnSuccessListener(aVoid -> {

                            docRef.update("entrants_waitlist", FieldValue.arrayRemove(finalUserEmail));
                            int newCount = Math.max(0, currentSize - 1);

                            entrantNumbers.setText(
                                    "Current Entrants: " + newCount + "/" + limitDisplay + " slots"
                            );

                            // Also delete waitingList/{email}
                            FirebaseFirestore.getInstance()
                                    .collection("events")
                                    .document(eventId)
                                    .collection("waitingList")
                                    .document(finalUserEmail)
                                    .delete();

                            Toast.makeText(this,
                                    "You have left the waiting list.",
                                    Toast.LENGTH_LONG).show();
                        });
            });
        });

        // BACK BUTTON — unchanged
        String finalEmailForBack = currentUserEmail;
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

            intent.putExtra("USER_EMAIL", finalEmailForBack);
            startActivity(intent);
            finish();
        });
    }


    private void continueRegistrationFlow(DocumentSnapshot currentEvent, String finalUserEmail) {

        List<String> entrantsList = (List<String>) currentEvent.get("entrants");
        if (entrantsList == null) entrantsList = new ArrayList<>();
        entrantsList.remove(null);

        if (entrantsList.contains(finalUserEmail)) {
            Toast.makeText(this,
                    "You are already on the waiting list.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
        List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
        List<String> winners_list = (List<String>) currentEvent.get("winners");

        int num_entrants =
                (waitlist_list != null ? waitlist_list.size() : 0)
                        + (accepted_list != null ? accepted_list.size() : 0)
                        + (winners_list != null ? winners_list.size() : 0);

        Long limitLong = currentEvent.getLong("limit");
        boolean hasLimit = limitLong != null;
        int limitValue = hasLimit ? limitLong.intValue() : -1;

        if (limitValue != -1) { //if -1, the limit is infinite.
            if (hasLimit && num_entrants >= limitValue) {
                Toast.makeText(this,
                        "This event is full.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        String limitDisplay_old = "";
        if (limitValue == -1){
            limitDisplay_old = "infinite";
        } else {
            limitDisplay_old = hasLimit ? String.valueOf(limitValue) : "?";
        }
        String limitDisplay = limitDisplay_old;


        docRef.update("entrants", FieldValue.arrayUnion(finalUserEmail))
                .addOnSuccessListener(aVoid -> {

                    saveWaitingListLocation(eventId, finalUserEmail);

                    docRef.update("entrants_waitlist", FieldValue.arrayUnion(finalUserEmail));

                    entrantNumbers.setText(
                            "Current Entrants: " + (num_entrants + 1) + "/" + limitDisplay + " slots"
                    );

                    Toast.makeText(this,
                            "Joined waiting list.",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to join. Please try again.",
                                Toast.LENGTH_LONG).show());
    }


    private void loadEventDetails() {
        docRef.get().addOnSuccessListener(currentEvent -> {
            if (!currentEvent.exists()) return;

            title.setText(currentEvent.getString("name"));
            startEnd.setText("");
            closingDate.setText("");

            String weekdayString = currentEvent.getString("weekdayString");
            String timeString = currentEvent.getString("time");
            dayTime.setText((weekdayString != null ? weekdayString + " " : "") +
                    (timeString != null ? timeString : ""));

            location.setText(currentEvent.getString("location"));

            Object priceObj = currentEvent.get("price");
            price.setText(priceObj != null ? "$" + priceObj : "$0");

            description.setText(currentEvent.getString("description"));

            List<String> waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            List<String> accepted = (List<String>) currentEvent.get("accepted_entrants");
            List<String> winners = (List<String>) currentEvent.get("winners");

            int entrantCount =
                    (waitlist != null ? waitlist.size() : 0)
                            + (accepted != null ? accepted.size() : 0)
                            + (winners != null ? winners.size() : 0);

            Long limitLong = currentEvent.getLong("limit");
            int limitValue;
            if (limitLong != null) {
                limitValue = limitLong.intValue();
            } else {
                limitValue = -1;
            }

            String limitDisplay;
            if (limitValue == -1) {
                limitDisplay = "infinite";
            } else {
                limitDisplay = String.valueOf(limitValue);
            }
            entrantNumbers.setText("Current Entrants: " + entrantCount + "/" + limitDisplay + " slots");
            entrantNumbers.setText("Current Entrants: " + entrantCount + "/" + limitDisplay + " slots");

            Long sampleSizeLong = currentEvent.getLong("sampleSize");
            lotteryWinners.setText("Lottery Winners: " +
                    (sampleSizeLong != null ? sampleSizeLong : 0));

            String image_base64 = currentEvent.getString("posterImage");
            if (image_base64 != null) {
                byte[] decodedBytes = Base64.decode(image_base64, Base64.DEFAULT);
                Bitmap image_bitmap =
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                eventImageView.setImageBitmap(image_bitmap);
            }
        });
    }


    private void saveWaitingListLocation(String eventId, String userEmail) {
        if (eventId == null || userEmail == null) return;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    Double lat = null, lng = null;
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                    eventRepository.addEntrantLocationToWaitingList(eventId, userEmail, lat, lng);
                });
    }


    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
