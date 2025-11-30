package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.location.Location;

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

    // For saving entrant location
    private FusedLocationProviderClient fusedLocationClient;
    private EventRepository eventRepository;
    private String eventId;   // Firestore document ID for this event

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

        // ----- UI refs -----
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
        eventImageView = findViewById(R.id.imageView_ent_eventImage);

        register_button = findViewById(R.id.button_register);
        leave_button = findViewById(R.id.button_leave_waitlist);
        back_event_details_button = findViewById(R.id.button_event_details_back);

        register_button = findViewById(R.id.button_register);
        leave_button = findViewById(R.id.button_leave_waitlist);
        back_event_details_button = findViewById(R.id.button_event_details_back);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        eventRepository = new EventRepository();

        // ----- Get user email -----
        String user_email = getIntent().getStringExtra("USER_EMAIL");
        if (user_email == null || user_email.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                user_email = currentUser.getEmail();
            }
        }
        final String currentUserEmail = user_email;

        String eventHash = getIntent().getStringExtra("EVENT");

        eventId = eventHash; // keep a copy for waitingList path

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        docRef = eventsRef.document(eventHash);

        loadEventDetails();

        String finalUserEmail = currentUserEmail;

        register_button.setOnClickListener(view -> {
            if (finalUserEmail == null || finalUserEmail.isEmpty()) {
                Toast.makeText(
                        EntEventDetailActivity.this,
                        "Unable to determine your account. Please sign in again.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {
                if (!currentEvent.exists()) {
                    Toast.makeText(
                            EntEventDetailActivity.this,
                            "Event not found.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                //add this user to the all entrants arraylist
                List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                if (entrantsList == null) {
                    entrantsList = new ArrayList<>();
                } else {
                    entrantsList = new ArrayList<>(entrantsList);
                    entrantsList.remove(null);
                }

                //add this user to the pending invitation arraylist
                List<String> entrantsWaitlist = (List<String>) currentEvent.get("entrants_waitlist");
                if (entrantsWaitlist == null) {
                    entrantsWaitlist = new ArrayList<>();
                } else {
                    entrantsWaitlist = new ArrayList<>(entrantsWaitlist);
                    entrantsWaitlist.remove(null);
                }

                if (entrantsList.contains(finalUserEmail)) {
                    Toast.makeText(
                            EntEventDetailActivity.this,
                            "You are already on the waiting list.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
                List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
                List<String> winners_list = (List<String>) currentEvent.get("winners");

                //calculate the number of entrants total.
                //if waitlist is not null, set it's size to be the number of entrants
                int num_entrants = (waitlist_list != null ? waitlist_list.size() : 0);
                //if accepted list is not null, add it's size to number of entrants
                num_entrants += (accepted_list != null ? accepted_list.size() : 0) ;
                //if pending invitations list is not null, add it's size to number of entrants
                num_entrants += (winners_list != null ? winners_list.size() : 0);

                //checking entrant limit
                Long limitLong = currentEvent.getLong("limit");
                boolean hasLimit = limitLong != null;
                int limitValue = 0;

                //return and send toast if the event is already full
                if (hasLimit) {
                    limitValue = limitLong.intValue();
                    if (num_entrants >= limitValue) {
                        Toast.makeText(EntEventDetailActivity.this,
                            "This event is full.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                int currentSize = num_entrants; // this is needed because of lambda expressions :(
                String limitDisplay = hasLimit ? String.valueOf(limitValue) : "?";

                docRef.update("entrants", FieldValue.arrayUnion(finalUserEmail))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(
                                    EntEventDetailActivity.this,
                                    "Joined waiting list.",
                                    Toast.LENGTH_LONG
                            ).show();
                            saveWaitingListLocation(eventHash, finalUserEmail);

                            //if succeded, we also need to add to entrants_waitlist
                            docRef.update("entrants_waitlist", FieldValue.arrayUnion(finalUserEmail))
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(
                                                EntEventDetailActivity.this,
                                                "Joined waiting list.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EntEventDetail", "Error adding entrant to waitlist", e);
                                        Toast.makeText(
                                                EntEventDetailActivity.this,
                                                "Failed to join. Please leave and try again.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });


                            int newCount = currentSize + 1;
                            entrantNumbers.setText(
                                    "Current Entrants: " + newCount + "/" + limitDisplay + " slots"
                            );
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EntEventDetail", "Error adding entrant", e);
                            Toast.makeText(
                                    EntEventDetailActivity.this,
                                    "Failed to join. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });
        });

        //leave waitlist button listener
        leave_button.setOnClickListener(view -> {
            if (finalUserEmail == null || finalUserEmail.isEmpty()) {
                Toast.makeText(
                        EntEventDetailActivity.this,
                        "Unable to determine your account. Please sign in again.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            docRef.get().addOnSuccessListener(currentEvent -> {
                if (!currentEvent.exists()) {
                    Toast.makeText(
                            EntEventDetailActivity.this,
                            "Event not found.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                //leave all entrants arraylist
                List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                if (entrantsList == null) {
                    entrantsList = new ArrayList<>();
                } else {
                    entrantsList = new ArrayList<>(entrantsList);
                    entrantsList.remove(null);
                }

                //leave entrants waitlist arraylist
                List<String> entrantsWaitlist = (List<String>) currentEvent.get("entrants_waitlist");
                if (entrantsWaitlist == null) {
                    entrantsWaitlist = new ArrayList<>();
                } else {
                    entrantsWaitlist = new ArrayList<>(entrantsWaitlist);
                    entrantsWaitlist.remove(null);
                }

                if (!entrantsList.contains(finalUserEmail)) {
                    Toast.makeText(
                            EntEventDetailActivity.this,
                            "You are not on the waiting list.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Long limitLong = currentEvent.getLong("limit");
                String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";

                List<String> waitlist_list = (List<String>) currentEvent.get("entrants_waitlist");
                List<String> accepted_list = (List<String>) currentEvent.get("accepted_entrants");
                List<String> winners_list = (List<String>) currentEvent.get("winners");

                //calculate the number of entrants total.
                //if waitlist is not null, set it's size to be the number of entrants
                int num_entrants = (waitlist_list != null ? waitlist_list.size() : 0);
                //if accepted list is not null, add it's size to number of entrants
                num_entrants += (accepted_list != null ? accepted_list.size() : 0) ;
                //if pending invitations list is not null, add it's size to number of entrants
                num_entrants += (winners_list != null ? winners_list.size() : 0);

                int currentSize = num_entrants; // i think this is needed because of lambda expressions :(
                // 1) Remove from entrants array
                docRef.update("entrants", FieldValue.arrayRemove(finalUserEmail))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(
                                    EntEventDetailActivity.this,
                                    "You have left the entrant list.",
                                    Toast.LENGTH_LONG
                            ).show();


                            //if successful, we also need to update the entrants_waitlist
                            docRef.update("entrants_waitlist", FieldValue.arrayRemove(finalUserEmail))
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(
                                                EntEventDetailActivity.this,
                                                "You have left waiting list.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EntEventDetail", "Error removing entrant from waitlist", e);
                                        Toast.makeText(
                                                EntEventDetailActivity.this,
                                                "Failed to leave. Please rejoin and try again.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });


                            int newCount = Math.max(0, currentSize - 1);
                            entrantNumbers.setText(
                                    "Current Entrants: " + newCount + "/" + limitDisplay + " slots"
                            );

                            // 2) Also remove from waitingList collection
                            String eventDocId = (eventId != null) ? eventId : eventHash;

                            FirebaseFirestore.getInstance()
                                    .collection("events")
                                    .document(eventDocId)
                                    .collection("waitingList")
                                    .document(finalUserEmail)
                                    .delete()
                                    .addOnSuccessListener(v ->
                                            Log.d(TAG, "Removed from waitingList collection: " + finalUserEmail)
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to delete waitingList entry", e)
                                    );
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error removing entrant", e);
                            Toast.makeText(
                                    EntEventDetailActivity.this,
                                    "Failed to leave. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });
        });


        String finalUserEmailForBack = currentUserEmail;
        back_event_details_button.setOnClickListener(view -> {
            String fromActivity = getIntent().getStringExtra("FROM_ACTIVITY");
            Intent intent;

            if ("MY_EVENTS".equals(fromActivity)) {
                // Return to My Events
                intent = new Intent(EntEventDetailActivity.this, EntEventHistoryActivity.class);
            } else if ("HOME_ENT".equals(fromActivity)) {
                //return to entrant home (path taken if qr code was scanned)
                intent = new Intent(EntEventDetailActivity.this, HomeEntActivity.class);
            } else {
                // Default: Return to All Events
                intent = new Intent(EntEventDetailActivity.this, EntEventsActivity.class);
            }

            intent.putExtra("USER_EMAIL", finalUserEmailForBack);
            startActivity(intent);
            finish();
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

            //get number of current entrants: people in waitlist, people who have acceptedinvitations, peple with pending invitations.
            List<String> waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            List<String> accepted = (List<String>) currentEvent.get("accepted_entrants");
            List<String> winners = (List<String>) currentEvent.get("winners");

            int entrantCount = (waitlist != null ? waitlist.size() : 0);
            entrantCount += (accepted != null ? accepted.size() : 0) ;
            entrantCount += (winners != null ? winners.size() : 0);

            Long limitLong = currentEvent.getLong("limit");
            String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";
            entrantNumbers.setText("Current Entrants: " + entrantCount + "/" + limitDisplay + " slots");

            Long sampleSizeLong = currentEvent.getLong("sampleSize");
            int sampleSize = sampleSizeLong != null ? sampleSizeLong.intValue() : 0;
            lotteryWinners.setText("Lottery Winners: " + sampleSize);

            //get image from database, convert to bitmap, display image.
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
     *   events/{eventId}/waitingList/{userEmail}
     */
    private void saveWaitingListLocation(String eventId, String userEmail) {
        if (eventId == null || userEmail == null) {
            Log.e(TAG, "saveWaitingListLocation: eventId or userEmail is null");
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

                    eventRepository.addEntrantLocationToWaitingList(eventId, userEmail, lat, lng);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to get device location", e)
                );
    }
}
