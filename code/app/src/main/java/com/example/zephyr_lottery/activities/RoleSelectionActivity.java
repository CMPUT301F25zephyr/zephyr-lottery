package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RoleSelectionActivity extends AppCompatActivity {

    private EditText etUsername;
    private RadioGroup rgUserType;
    private RadioButton rbEntrant, rbOrganizer;
    private TextView tvRoleDescription;
    private Button btnContinue;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String firebaseUid;
    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role_selection_activity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get Firebase UID and Android ID from intent
        firebaseUid = getIntent().getStringExtra("FIREBASE_UID");
        androidId = getIntent().getStringExtra("ANDROID_ID");

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        rgUserType = findViewById(R.id.rgUserType);
        rbEntrant = findViewById(R.id.rbEntrant);
        rbOrganizer = findViewById(R.id.rbOrganizer);
        tvRoleDescription = findViewById(R.id.tvRoleDescription);
        btnContinue = findViewById(R.id.btnContinue);

        // Update description when role changes
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            updateRoleDescription(checkedId);
        });

        // Continue button
        btnContinue.setOnClickListener(v -> createProfile());
    }

    private void updateRoleDescription(int checkedId) {
        if (checkedId == R.id.rbEntrant) {
            tvRoleDescription.setText("Entrants can join and participate in lottery events.");
        } else if (checkedId == R.id.rbOrganizer) {
            tvRoleDescription.setText("Organizers can create and manage lottery events.");
        }
    }

    private void createProfile() {
        String username = etUsername.getText().toString().trim();
        String userType = rbEntrant.isChecked() ? "entrant" : "organizer";

        // Validation
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        // Create profile in Firestore
        createFirestoreProfile(username, userType);
    }

    private void createFirestoreProfile(String username, String userType) {
        // Create profile document
        Map<String, Object> profile = new HashMap<>();
        profile.put("uid", firebaseUid);
        profile.put("androidId", androidId);
        profile.put("username", username);
        profile.put("type", userType);
        profile.put("deviceModel", Build.MANUFACTURER + " " + Build.MODEL);
        profile.put("deviceOs", "Android " + Build.VERSION.RELEASE);
        profile.put("createdAt", Timestamp.now());
        profile.put("lastLogin", Timestamp.now());
        profile.put("receivingNotis", false);

        // Use Firebase UID as document ID
        db.collection("accounts")
                .document(firebaseUid)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RoleSelection", "Profile created successfully");
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();

                    // Navigate to appropriate home screen
                    navigateToHome(userType);
                })
                .addOnFailureListener(e -> {
                    Log.e("RoleSelection", "Error creating profile", e);
                    Toast.makeText(this, "Failed to create profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToHome(String userType) {
        Intent intent;

        if ("organizer".equals(userType)) {
            intent = new Intent(this, HomeOrgActivity.class);
        } else {
            intent = new Intent(this, HomeEntActivity.class);
        }

        intent.putExtra("FIREBASE_UID", firebaseUid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}