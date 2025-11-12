package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {
    // can be omitted later by creating a separate class for the firebase connection
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageButton btnBack;

    // Name currently being the same object as username, will be changed later (or just change username to Name altogether)
    private EditText etName, etPhone, etPassword;

    // Email currently a TextView, since it is being used as a login credential
    private TextView etEmail;
    private CheckBox cbReceiveNotifications;
    private Button btnSave, btnDeleteAccount;

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
        etPassword = findViewById(R.id.etPassword);
        cbReceiveNotifications = findViewById(R.id.cbReceiveNotifications);
        btnSave = findViewById(R.id.btnSave);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

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

                    Boolean receivingNotis = profile.getReceivingNotis();

                    // Load notification preference from Firebase
                    // Default to false if not set
                    if (receivingNotis != null) {
                        cbReceiveNotifications.setChecked(receivingNotis);
                    } else {
                        cbReceiveNotifications.setChecked(false);
                    }
                });

        // Back button functionality
        btnBack.setOnClickListener(v -> {
            finish(); // Close this activity and return to previous screen
        });

        // Save button functionality
        btnSave.setOnClickListener(v -> {
            saveUserProfile();
        });

        // Delete account button functionality
        btnDeleteAccount.setOnClickListener(v -> {
            // TODO: Implement delete account functionality
            Toast.makeText(UserProfileActivity.this,
                    "Delete account functionality coming soon",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserProfile() {
        // Get the edited values
        String newUsername = etName.getText().toString().trim();
        String newPhone = ValidationUtil.sanitize(etPhone.getText().toString());
        String password = etPassword.getText().toString().trim();
        boolean receiveNotifications = cbReceiveNotifications.isChecked();

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

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Password is required to confirm changes");
            etPassword.requestFocus();
            return;
        }

        // Verify password before making changes
        verifyPasswordAndUpdate(newUsername, newPhone, receiveNotifications, password);
    }

    private void verifyPasswordAndUpdate(String newUsername, String newPhone, boolean receiveNotifications, String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        String email = user.getEmail();

        // Create credential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // Re-authenticate the user
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Password is correct, proceed with update
                    updateProfile(newUsername, newPhone, receiveNotifications);
                })
                .addOnFailureListener(e -> {
                    // Password is incorrect
                    etPassword.setError("Incorrect password");
                    etPassword.requestFocus();
                    Toast.makeText(UserProfileActivity.this,
                            "Incorrect password. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Password verification failed", e);
                });
    }

    private void updateProfile(String newUsername, String newPhone, boolean receiveNotifications) {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Update the fields in Firestore
        db.collection("accounts")
                .document(currentUserEmail)
                .update(
                        "username", newUsername,
                        "phone", newPhone,
                        "receivingNotis", receiveNotifications
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Clear password field for security
                    etPassword.setText("");

                    finish(); // Return to previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error updating profile", e);
                });
    }

    private void deleteUserProfile() {
        return ;
    }
}