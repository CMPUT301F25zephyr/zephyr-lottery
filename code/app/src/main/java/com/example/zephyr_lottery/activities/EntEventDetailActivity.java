package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntEventDetailActivity extends AppCompatActivity {
    private Button back_event_details_button;
    private Button register_button;
    private TextView title;
    private TextView closingDate;
    private TextView startEnd;
    private TextView dayTime;
    private TextView location;
    private TextView price;
    private TextView description;
    private TextView entrantNumbers;
    private TextView lotteryWinners;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ent_eventdetail_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Match TextViews to IDs
        title = findViewById(R.id.textView_ed_title);
        closingDate = findViewById(R.id.textView_closingdate);
        startEnd = findViewById(R.id.textView_startenddates);
        dayTime = findViewById(R.id.textView_day_time);
        location = findViewById(R.id.textView_location_string);
        price = findViewById(R.id.textView_price_string);
        description = findViewById(R.id.textView_description_string);
        entrantNumbers = findViewById(R.id.textView_currententrants);
        lotteryWinners = findViewById(R.id.textView_lotterywinners);

        //get email and event hash from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");
        String eventHash = getIntent().getStringExtra("EVENT");
        // Connect to database and get event details
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        docRef = eventsRef.document(eventHash);
        docRef.get().addOnSuccessListener(currentEvent -> {
           if (currentEvent.exists()) {
               // Start modifying text values

               title.setText(currentEvent.getString("name"));
               // Placeholder for closing date
               // Placeholder for start/end dates
               String dayTimeText = currentEvent.getString("weekdayString");
               dayTimeText += " ";
               dayTimeText += currentEvent.getString("time");
               dayTime.setText(dayTimeText);
               location.setText(currentEvent.getString("location"));
               String priceText = "$";
               priceText += String.valueOf(currentEvent.get("price"));
               price.setText(priceText);
               description.setText(currentEvent.getString("description"));
               String entrantNumbersText = "Current Entrants: ";
               ArrayList<String> entrants = (ArrayList<String>) currentEvent.get("entrants");
               assert entrants != null;
               entrantNumbersText += String.valueOf(entrants.size());
               entrantNumbersText += "/";
               entrantNumbers.setText(entrantNumbersText);
               // Placeholder for obtaining entrant numbers and limits
               String lotteryWinnersText = "Lottery Winners: ";
               // Placeholder for obtaining number of winners
           }
           else {
               Log.e("Firestore", "Event not found.");
           }
        });

        //listener for register button.
        register_button = findViewById(R.id.button_register);
        register_button.setOnClickListener(view -> {
            docRef.get().addOnSuccessListener(currentEvent -> {
               if (currentEvent.exists()) {
                   // Retrieve array and add email
                   ArrayList<String> entrants = (ArrayList<String>) currentEvent.get("entrants");
                   assert entrants != null;
                   // Check if user has already signed up first
                   boolean userFound = false;
                   for (int i = 0; i < entrants.size(); i++) {
                       if (entrants.get(i).equals(user_email)) {
                           userFound = true;
                       }
                   }
                   if (userFound) {
                       // User must have already registered previously
                       Toast rejection = Toast.makeText(this, "You have already registered for this event.", Toast.LENGTH_LONG);
                       rejection.show();
                   }
                   else {
                       // Placeholder to check against maximum event entrants

                       // Add user to event entrants list
                       entrants.add(user_email);
                       docRef.update("entrants", FieldValue.arrayUnion(user_email));
                       Toast confirmation = Toast.makeText(this, "Successfully registered!", Toast.LENGTH_LONG);
                       confirmation.show();
                   }
               }
            });
        });

        //listener for button to return to homescreen.
        back_event_details_button = findViewById(R.id.button_event_details_back);
        back_event_details_button.setOnClickListener(view -> {
            Intent intent = new Intent(EntEventDetailActivity.this, EntEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }


}
