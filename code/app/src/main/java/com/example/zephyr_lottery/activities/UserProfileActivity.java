package com.example.zephyr_lottery.activities;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
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
            showDeleteAccountDialog();
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

    private void showDeleteAccountDialog() {
        // create an AlertDialog with password input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.\n\n" +
                "Type \"I want to delete my account\" to confirm:");

        // create a LinearLayout to hold both input fields
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // create EditText for confirmation phrase
        final EditText confirmationInput = new EditText(this);
        confirmationInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        confirmationInput.setHint("Type: I want to delete my account");
        confirmationInput.setPadding(20, 20, 20, 20);
        layout.addView(confirmationInput);

        // add spacing between fields
        android.widget.Space space = new android.widget.Space(this);
        space.setMinimumHeight(30);
        layout.addView(space);

        // create EditText for password input
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");
        passwordInput.setPadding(20, 20, 20, 20);
        layout.addView(passwordInput);

        builder.setView(layout);

        // delete button
        builder.setPositiveButton("Delete Account", (dialog, which) -> {
            String confirmationText = confirmationInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // validate confirmation phrase
            if (!confirmationText.equals("I want to delete my account")) {
                Toast.makeText(this,
                        "Please type the exact phrase: I want to delete my account",
                        Toast.LENGTH_LONG).show();
                // reshow the dialog
                showDeleteAccountDialog();
                return;
            }

            // validate password
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                // reshow the dialog
                showDeleteAccountDialog();
                return;
            }

            // both validations passed, proceed with deletion
            deleteUserProfile(password);
        });

        // cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        // show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // color delete button red
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    /**
     * Deletes the user account from Firebase Auth and Firestore
     * Requires password verification for security
     *
     * @param password User's password for verification
     */
    private void deleteUserProfile(String password) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        Log.d("UserProfile", "Attempting to delete account for: " + email);

        // create credential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // re-authenticate the user before deletion (required by Firebase)
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserProfile", "Re-authentication successful, proceeding with deletion");

                    // Step 1: delete Firestore document
                    deleteFirestoreData(email, user);
                })
                .addOnFailureListener(e -> {
                    // password is incorrect
                    Log.e("UserProfile", "Re-authentication failed", e);
                    Toast.makeText(this,
                            "Incorrect password. Account not deleted.",
                            Toast.LENGTH_LONG).show();
                    showDeleteAccountDialog();
                });
    }

    /**
     * deletes user data from Firestore and removes user from all events
     * then proceeds to delete Firebase Auth account
     */
    private void deleteFirestoreData(String email, FirebaseUser user) {
        Log.d("UserProfile", "Starting comprehensive data deletion for: " + email);

        // step 1: remove user from all events (both entrants and selectedEntrants)
        removeUserFromAllEvents(email, () -> {
            // step 2: delete user's Firestore document
            deleteUserDocument(email, user);
        });
    }

    /**
     * Removes user from all events where they are an entrant
     * Then chains to remove from winners, accepted, and rejected arrays
     */
    private void removeUserFromAllEvents(String email, Runnable onComplete) {
        Log.d("UserProfile", "Removing user from entrants in all events");

        db.collection("events")
                .whereArrayContains("entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("UserProfile", "Found " + totalEvents + " events with user as entrant");

                    if (totalEvents == 0) {
                        // No events, move to next step
                        removeUserFromWinners(email, onComplete);
                        return;
                    }

                    // Remove user from entrants array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("entrants", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("UserProfile", "Removed from entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All entrants processed, move to winners
                                        removeUserFromWinners(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfile", "Error removing from entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromWinners(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error querying events with user as entrant", e);
                    // Continue to next step even if this fails
                    removeUserFromWinners(email, onComplete);
                });
    }

    /**
     * Removes user from winners arrays in all events
     * Then chains to remove from accepted_entrants
     */
    private void removeUserFromWinners(String email, Runnable onComplete) {
        Log.d("UserProfile", "Removing user from winners in all events");

        db.collection("events")
                .whereArrayContains("winners", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("UserProfile", "Found " + totalEvents + " events with user as winner");

                    if (totalEvents == 0) {
                        // No events, proceed to accepted_entrants
                        removeUserFromAcceptedEntrants(email, onComplete);
                        return;
                    }

                    // Remove user from winners array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("winners", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("UserProfile", "Removed from winners in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All winners processed, move to accepted_entrants
                                        removeUserFromAcceptedEntrants(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfile", "Error removing from winners: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromAcceptedEntrants(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error querying events with user as winner", e);
                    // Continue to next step even if this fails
                    removeUserFromAcceptedEntrants(email, onComplete);
                });
    }

    /**
     * Removes user from accepted_entrants arrays in all events
     * Then chains to remove from rejected_entrants
     */
    private void removeUserFromAcceptedEntrants(String email, Runnable onComplete) {
        Log.d("UserProfile", "Removing user from accepted_entrants in all events");

        db.collection("events")
                .whereArrayContains("accepted_entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("UserProfile", "Found " + totalEvents + " events with user as accepted entrant");

                    if (totalEvents == 0) {
                        // No events, proceed to rejected_entrants
                        removeUserFromRejectedEntrants(email, onComplete);
                        return;
                    }

                    // Remove user from accepted_entrants array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("accepted_entrants", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("UserProfile", "Removed from accepted_entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All accepted_entrants processed, move to rejected_entrants
                                        removeUserFromRejectedEntrants(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfile", "Error removing from accepted_entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromRejectedEntrants(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error querying events with user as accepted entrant", e);
                    // Continue to next step even if this fails
                    removeUserFromRejectedEntrants(email, onComplete);
                });
    }

    /**
     * Removes user from rejected_entrants arrays in all events
     * This is the final step before calling onComplete
     */
    private void removeUserFromRejectedEntrants(String email, Runnable onComplete) {
        Log.d("UserProfile", "Removing user from rejected_entrants in all events");

        db.collection("events")
                .whereArrayContains("rejected_entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("UserProfile", "Found " + totalEvents + " events with user as rejected entrant");

                    if (totalEvents == 0) {
                        // No events, all cleanup complete
                        Log.d("UserProfile", "All event cleanups complete");
                        onComplete.run();
                        return;
                    }

                    // Remove user from rejected_entrants array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("rejected_entrants", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("UserProfile", "Removed from rejected_entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All cleanup complete
                                        Log.d("UserProfile", "All event cleanups complete");
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfile", "Error removing from rejected_entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue to deletion even if some fail
                                        Log.d("UserProfile", "Event cleanup completed with some errors");
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error querying events with user as rejected entrant", e);
                    // Proceed to deletion even if this fails
                    Log.d("UserProfile", "Event cleanup completed with errors");
                    onComplete.run();
                });
    }

    /**
     * deletes the user's document from accounts collection
     */
    private void deleteUserDocument(String email, FirebaseUser user) {
        Log.d("UserProfile", "Deleting user document from accounts collection");

        db.collection("accounts")
                .document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserProfile", "Firestore document deleted successfully");

                    // delete Firebase Auth account
                    deleteFirebaseAuthAccount(user);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error deleting Firestore document", e);
                    Toast.makeText(this,
                            "Failed to delete user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Even if Firestore deletion fails, try to delete Auth account
                    deleteFirebaseAuthAccount(user);
                });
    }

    /**
     * deletes the Firebase Auth account
     * then navigates back to login screen
     */
    private void deleteFirebaseAuthAccount(FirebaseUser user) {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserProfile", "Firebase Auth account deleted successfully");
                    Toast.makeText(this,
                            "Account deleted successfully",
                            Toast.LENGTH_LONG).show();

                    // navigate to login/signup screen
                    navigateToLogin();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfile", "Error deleting Firebase Auth account", e);
                    Toast.makeText(this,
                            "Failed to delete account: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * navigates to the login screen after account deletion
     * clears the activity stack so user can't go back
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}