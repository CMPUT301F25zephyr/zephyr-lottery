package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEventActivity extends AppCompatActivity {

    private Button save_event_button;
    private Button back_add_event_button;

    //database
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_event_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //set spinner contents (dropdown menu)
        Spinner spinner = (Spinner) findViewById(R.id.weekday_spinner);
        //create ArrayAdapter from string array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.weekday_spinner_array,
                android.R.layout.simple_spinner_item
        );
        //pick the default layout for spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter
        spinner.setAdapter(adapter);

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //get the database collection reference
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        //listener for button to return to my events.
        back_add_event_button = findViewById(R.id.button_back_add_event);
        back_add_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(AddEventActivity.this, MyEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        //save event button listener here!
        save_event_button = findViewById(R.id.button_save_event_add_event);
        save_event_button.setOnClickListener(view -> {
            //get information from create event fields
            String event_name = ((EditText) findViewById(R.id.add_event_name)).getText().toString();
            String event_time = ((EditText) findViewById(R.id.add_event_times)).getText().toString();
            String event_weekday = spinner.getSelectedItem().toString();
            String event_price = ((EditText) findViewById(R.id.add_event_price)).getText().toString();
            String event_location = ((EditText) findViewById(R.id.add_event_location)).getText().toString();
            String event_description = ((EditText) findViewById(R.id.add_event_description)).getText().toString();

            //reject if fields left empty or price is not a float.
            if (event_name.isEmpty()||event_time.isEmpty()||event_price.isEmpty()||
                    event_location.isEmpty() || event_description.isEmpty()) {
                Toast.makeText(AddEventActivity.this, "Incomplete Information.",
                        Toast.LENGTH_SHORT).show();

            } else if (!validFloat(event_price)) {
                Toast.makeText(AddEventActivity.this, "Price must be a number.",
                        Toast.LENGTH_SHORT).show();

            }else { //if successful, add to database of events.
                //get info from all the fields.
                Event event = new Event(event_name, event_time, user_email);
                DocumentReference docRef = eventsRef.document(Integer.toString(event.hashCode()));
                event.setWeekdayString(event_weekday);
                event.setPrice(Float.parseFloat(event_price));
                event.setDescription(event_description);
                event.setLocation(event_location);

                //add to database.
                docRef.set(event);

                //return to my events screen
                Intent intent = new Intent(AddEventActivity.this, MyEventsActivity.class);
                intent.putExtra("USER_EMAIL", user_email);
                startActivity(intent);
            }
        });
    }

    //helper function for checking price. may put into separate file later.
    private boolean validFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
