package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeEntActivity extends AppCompatActivity {
    private CardView viewEventsButton, viewHistoryButton, editProfileButton, scanQRButton;
    private TextView textViewGreeting;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userEmail;

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

        textViewGreeting = findViewById(R.id.tvEntrantGreeting);
        viewEventsButton = findViewById(R.id.btnLatestEvents);
        viewHistoryButton = findViewById(R.id.btnHistory);
        editProfileButton = findViewById(R.id.btnEditProfile);
        scanQRButton = findViewById(R.id.btnScanQR);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load username from Firebase
        loadUsername();

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        viewHistoryButton.setOnClickListener(v -> {
            // TODO: add view history view
        });

        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, EntEventsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        scanQRButton.setOnClickListener(v -> {
            // TODO: add scan qr view
            // this is not correct for now
            Intent intent = new Intent(HomeEntActivity.this, QRCodeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload username when returning from UserProfileActivity
        loadUsername();
    }

    private void loadUsername() {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        db.collection("accounts")
                .document(currentUserEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);

                    if (profile != null) {
                        // Use the profile data
                        String username = profile.getUsername();
                        String userEmail = profile.getEmail();
                        textViewGreeting.setText("Greetings, " + username);
                    } else {
                        textViewGreeting.setText("Hello, User");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error - show default greeting
                    textViewGreeting.setText("Hello, User");
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });

    }
}