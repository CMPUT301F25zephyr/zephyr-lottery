package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.example.zephyr_lottery.utils.ValidationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {
    // can be omitted later by creating a separate class for the firebase connection
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageButton btnBack;

    // Name currently being the same object as username, will be changed later (or just change username to Name altogether)
    private EditText etName, etPhone;

    // Email currently a TextView, since it is being used as a login credential
    private TextView etEmail;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        db.collection("accounts")
                .document(currentUserEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);

                    // Use the profile data
                    String username = profile.getUsername();
                    String email = profile.getEmail();
                    String phone = profile.getPhone();

                    etName.setText(username);
                    etEmail.setText(email);

                    if (phone != null) {
                        etPhone.setText(phone);
                    }

                });

        // Back button functionality
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity and return to previous screen
            }
        });

        // Save button functionality
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void saveUserProfile() {
        // Get the edited values
        String newUsername = etName.getText().toString().trim();
        String newPhone = ValidationUtil.sanitize(etPhone.getText().toString());
        // String email = etEmail.getText().toString(); // Email doesn't change

        // Basic validation
        if (newUsername.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        // Validate phone
        String phoneError = ValidationUtil.getPhoneError(newPhone);
        if (phoneError != null) {
            etPhone.setError(phoneError);
            etPhone.requestFocus();
            return;
        }

        // Get current user email
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Update only the fields that changed
        db.collection("accounts")
                .document(currentUserEmail)
                .update(
                        "username", newUsername,
                        "phone", newPhone
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error updating profile", e);
                });
    }
}