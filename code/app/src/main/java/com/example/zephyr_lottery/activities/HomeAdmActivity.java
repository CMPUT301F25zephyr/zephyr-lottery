package com.example.zephyr_lottery.activities;

import android.app.Activity;
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

public class HomeAdmActivity extends AppCompatActivity {
    private Button events_button;
    private Button profile_button;
    private Button images_button;
    private Button logs_button;

    //database variables
    private FirebaseFirestore db;
    private CollectionReference accountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_adm_activity);
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
            TextView text_home = findViewById(R.id.textView_admin_home);
            String temp_text = "Hello, " + username;
            text_home.setText(temp_text);
        });

        //switch to browse events activity
        events_button = findViewById(R.id.admin_events_button);
        events_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdmActivity.this, AdmEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        // Switch to browse profiles activity
        profile_button = findViewById(R.id.admin_profiles_button);
        profile_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdmActivity.this, AdmProfilesActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        // Browse images (whatever activity your team uses for this)
        images_button = findViewById(R.id.admin_images_button);
        images_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdmActivity.this, AdmBrowseImagesActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        // NEW: Notification logs screen
        logs_button = findViewById(R.id.admin_notification_logs_button);
        logs_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdmActivity.this, AdmNotificationLogsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }
}
