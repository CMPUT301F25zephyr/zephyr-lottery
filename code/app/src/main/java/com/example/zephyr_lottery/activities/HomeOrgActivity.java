package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeOrgActivity extends AppCompatActivity {

    private Button my_events_button;
    private FirebaseFirestore db;
    private CollectionReference accountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_org_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get username from email and add it to the activity title
        //email passed in from the login activity
        //consider making a class for this cause the code is reused.!!!
        db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
        String user_email = getIntent().getStringExtra("USER_EMAIL");
        DocumentReference ref = accountsRef.document(user_email);

        ref.get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("username");
            TextView text_home = findViewById(R.id.textView_orgHome);
            String temp_text = "Hello, " + username;
            text_home.setText(temp_text);
        });

        //switch to my events activity
        my_events_button = findViewById(R.id.button_my_events);
        my_events_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeOrgActivity.this, MyEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });


    }
}
