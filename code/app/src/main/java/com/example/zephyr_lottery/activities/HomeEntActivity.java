package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeEntActivity extends AppCompatActivity {
    private Button events_button;
    private Button profile_button;
    private Button history_button;

    //database variables
    private FirebaseFirestore db;
    private CollectionReference accountsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_ent_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get username from database and set it to the text field
        db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
        String user_email = getIntent().getStringExtra("USER_EMAIL");
        DocumentReference ref = accountsRef.document(user_email);

        ref.get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("username");
            TextView text_home = findViewById(R.id.textView_username);
            String temp_text = "Hello, " + username;
            text_home.setText(temp_text);
        });


        //switch to latest events activity
        events_button = findViewById(R.id.entrant_events_button);
        events_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeEntActivity.this, EntEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }
}
