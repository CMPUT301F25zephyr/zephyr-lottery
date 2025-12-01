package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageButton btnBack;
    private EditText etName, etPhone, etEmail;
    private TextView tvAndroidId;
    private CheckBox cbReceiveNotifications;
    private Button btnSave, btnDeleteAccount;

    private String firebaseUid;  // Changed from androidId

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
        tvAndroidId = findViewById(R.id.tvAndroidId);
        etPhone = findViewById(R.id.etPhone);
        cbReceiveNotifications = findViewById(R.id.cbReceiveNotifications);
        btnSave = findViewById(R.id.btnSave);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get Firebase UID from current user
        if (mAuth.getCurrentUser() != null) {
            firebaseUid = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display Android ID for reference
        String androidId = getAndroidId();
        if (tvAndroidId != null) {
            String displayId = androidId.length() > 12 ?
                    "Device: " + androidId.substring(0, 12) + "..." :
                    "Device: " + androidId;
            tvAndroidId.setText(displayId);
        }

        // Load profile data
        loadProfile();

        // Back button functionality
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Save button functionality
        btnSave.setOnClickListener(v -> {
            saveUserProfile();
        });

        // Delete account button functionality
        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private String getAndroidId() {
        String id = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (id == null || id.isEmpty()) {
            id = "unknown_" + System.currentTimeMillis();
            Log.w("UserProfile", "Android ID not available, using fallback");
        }

        return id;
    }

    private void loadProfile() {
        // Use Firebase UID as document key
        db.collection("accounts")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load profile data
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        Boolean receivingNotis = documentSnapshot.getBoolean("receivingNotis");

                        // Set data to views
                        if (username != null) {
                            etName.setText(username);
                        }

                        if (email != null) {
                            etEmail.setText(email);
                        }

                        if (phone != null) {
                            etPhone.setText(phone);
                        }

                        // Load notification preference
                        if (receivingNotis != null) {
                            cbReceiveNotifications.setChecked(receivingNotis);
                        } else {
                            cbReceiveNotifications.setChecked(false);
                        }
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error loading profile", e);
                });
    }

    private void saveUserProfile() {
        // Get the edited values
        String newUsername = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = ValidationUtil.sanitize(etPhone.getText().toString());
        boolean receiveNotifications = cbReceiveNotifications.isChecked();

        // Basic validation
        if (newUsername.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (newUsername.length() < 3) {
            etName.setError("Username must be at least 3 characters");
            etName.requestFocus();
            return;
        }

        // Validate email (optional)
        if (!newEmail.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate phone
        String phoneError = ValidationUtil.getPhoneError(newPhone);
        if (phoneError != null) {
            etPhone.setError(phoneError);
            etPhone.requestFocus();
            return;
        }

        // Update profile
        updateProfile(newUsername, newEmail, newPhone, receiveNotifications);
    }

    private void updateProfile(String newUsername, String newEmail, String newPhone, boolean receiveNotifications) {
        // Update the fields in Firestore using Firebase UID
        db.collection("accounts")
                .document(firebaseUid)
                .update(
                        "username", newUsername,
                        "email", newEmail,
                        "phone", newPhone,
                        "receivingNotis", receiveNotifications
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error updating profile", e);
                });
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.\n\n" +
                "Type \"I want to delete my account\" to confirm:");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText confirmationInput = new EditText(this);
        confirmationInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        confirmationInput.setHint("Type: I want to delete my account");
        confirmationInput.setPadding(20, 20, 20, 20);
        layout.addView(confirmationInput);

        builder.setView(layout);

        builder.setPositiveButton("Delete Account", (dialog, which) -> {
            String confirmationText = confirmationInput.getText().toString().trim();

            if (!confirmationText.equals("I want to delete my account")) {
                Toast.makeText(this,
                        "Please type the exact phrase: I want to delete my account",
                        Toast.LENGTH_LONG).show();
                showDeleteAccountDialog();
                return;
            }

            deleteUserProfile();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getResources().getColor(android.R.color.holo_red_dark));
    }

    private void deleteUserProfile() {
        Log.d("UserProfile", "Attempting to delete account for UID: " + firebaseUid);

        // Get Android ID to remove from events
        getAndroidIdFromProfile(() -> {
            deleteFirestoreData();
        });
    }

    private void getAndroidIdFromProfile(Runnable onComplete) {
        db.collection("accounts")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String androidId = documentSnapshot.getString("androidId");
                        if (androidId != null) {
                            // Remove from events using Android ID
                            removeUserFromAllEvents(androidId, onComplete);
                        } else {
                            // No Android ID, just delete document
                            onComplete.run();
                        }
                    } else {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error getting Android ID", e);
                    onComplete.run();
                });
    }

    private void deleteFirestoreData() {
        Log.d("UserProfile", "Starting comprehensive data deletion for UID: " + firebaseUid);
        deleteUserDocument();
    }

    private void removeUserFromAllEvents(String androidId, Runnable onComplete) {
        Log.d("UserProfile", "Removing user from entrants in all events");

        db.collection("events")
                .whereArrayContains("entrants", androidId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("UserProfile", "Found " + totalEvents + " events with user as entrant");

                    if (totalEvents == 0) {
                        removeUserFromWinners(androidId, onComplete);
                        return;
                    }

                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("entrants", com.google.firebase.firestore.FieldValue.arrayRemove(androidId))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWinners(androidId, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWinners(androidId, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error querying events", e);
                    removeUserFromWinners(androidId, onComplete);
                });
    }

    private void removeUserFromWinners(String androidId, Runnable onComplete) {
        db.collection("events")
                .whereArrayContains("winners", androidId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        removeUserFromAcceptedEntrants(androidId, onComplete);
                        return;
                    }

                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("winners", com.google.firebase.firestore.FieldValue.arrayRemove(androidId))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromAcceptedEntrants(androidId, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromAcceptedEntrants(androidId, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    removeUserFromAcceptedEntrants(androidId, onComplete);
                });
    }

    private void removeUserFromAcceptedEntrants(String androidId, Runnable onComplete) {
        db.collection("events")
                .whereArrayContains("accepted_entrants", androidId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        removeUserFromRejectedEntrants(androidId, onComplete);
                        return;
                    }

                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("accepted_entrants", com.google.firebase.firestore.FieldValue.arrayRemove(androidId))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromRejectedEntrants(androidId, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromRejectedEntrants(androidId, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    removeUserFromRejectedEntrants(androidId, onComplete);
                });
    }

    private void removeUserFromRejectedEntrants(String androidId, Runnable onComplete) {
        db.collection("events")
                .whereArrayContains("rejected_entrants", androidId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        removeUserFromWaitlist(androidId, onComplete);
                        return;
                    }

                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("rejected_entrants", com.google.firebase.firestore.FieldValue.arrayRemove(androidId))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWaitlist(androidId, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWaitlist(androidId, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    removeUserFromWaitlist(androidId, onComplete);
                });
    }

    private void removeUserFromWaitlist(String androidId, Runnable onComplete) {
        db.collection("events")
                .whereArrayContains("entrants_waitlist", androidId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        removeUserFromWaitingListSubcollection(androidId, onComplete);
                        return;
                    }

                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("entrants_waitlist", com.google.firebase.firestore.FieldValue.arrayRemove(androidId))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWaitingListSubcollection(androidId, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalEvents) {
                                        removeUserFromWaitingListSubcollection(androidId, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    removeUserFromWaitingListSubcollection(androidId, onComplete);
                });
    }

    private void removeUserFromWaitingListSubcollection(String androidId, Runnable onComplete) {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        onComplete.run();
                        return;
                    }

                    int[] processedEvents = {0};

                    for (com.google.firebase.firestore.DocumentSnapshot eventDoc : querySnapshot.getDocuments()) {
                        eventDoc.getReference()
                                .collection("waitingList")
                                .document(androidId)
                                .get()
                                .addOnSuccessListener(waitlistDoc -> {
                                    if (waitlistDoc.exists()) {
                                        waitlistDoc.getReference().delete();
                                    }

                                    processedEvents[0]++;
                                    if (processedEvents[0] == totalEvents) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedEvents[0]++;
                                    if (processedEvents[0] == totalEvents) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    onComplete.run();
                });
    }

    private void deleteUserDocument() {
        Log.d("UserProfile", "Deleting user document from accounts collection");

        // Delete document using Firebase UID
        db.collection("accounts")
                .document(firebaseUid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserProfile", "Firestore document deleted successfully");

                    // Delete Firebase Auth account
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(this, "Account deleted successfully",
                                            Toast.LENGTH_LONG).show();
                                    navigateToSplash();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfile", "Error deleting auth", e);
                                    mAuth.signOut();
                                    navigateToSplash();
                                });
                    } else {
                        navigateToSplash();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error deleting document", e);
                    Toast.makeText(this, "Failed to delete: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}