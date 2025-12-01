package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.example.zephyr_lottery.ProfileArrayAdapter;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdmProfilesActivity extends AppCompatActivity {
    private Button filter_profiles_button;
    private Button back_browse_profiles_button;
    private ListView profileListView;
    private ArrayList<UserProfile> profileArrayList;
    private ArrayList<UserProfile> allProfilesList;
    private ProfileArrayAdapter profileArrayAdapter;
    private int FILTER_MODE = 0;

    //databases
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference profilesRef;

    private ActivityResultLauncher<Intent> filterActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.adm_profiles_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        profilesRef = db.collection("accounts");

        profileListView = findViewById(R.id.ListView_browse_profiles);
        profileArrayList = new ArrayList<>();
        allProfilesList = new ArrayList<>();
        profileArrayAdapter = new ProfileArrayAdapter(this, profileArrayList);
        profileListView.setAdapter(profileArrayAdapter);

        FILTER_MODE = getIntent().getIntExtra("FILTER_MODE", 0);

        // Set up delete button listener
        profileArrayAdapter.setOnDeleteClickListener((profile, position) -> {
            showDeleteConfirmationDialog(profile);
        });

        filterActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        FILTER_MODE = result.getData().getIntExtra("FILTER_MODE", 0);
                        applyFilter();
                    }
                }
        );

        profilesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                allProfilesList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("username");
                    String email = snapshot.getString("email");
                    String type = snapshot.getString("type");

                    allProfilesList.add(new UserProfile(name, email, type));
                }
                applyFilter();
            }
        });

        String user_email = getIntent().getStringExtra("USER_EMAIL");

        back_browse_profiles_button = findViewById(R.id.button_browse_profiles_back);
        back_browse_profiles_button.setOnClickListener(view -> {
            Intent intent = new Intent(AdmProfilesActivity.this, HomeAdmActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        filter_profiles_button = findViewById(R.id.button_profile_filter);
        filter_profiles_button.setOnClickListener(view -> {
            Intent intent = new Intent(AdmProfilesActivity.this, AdmProfilesFilterActivity.class);
            intent.putExtra("FILTER_MODE", FILTER_MODE);
            filterActivityLauncher.launch(intent);
        });
    }

    private void showDeleteConfirmationDialog(UserProfile profile) {
        AdmDeleteProfileDiagActivity dialog = new AdmDeleteProfileDiagActivity(
                this,
                profile,
                (deletedProfile, password) -> verifyAdminPasswordAndDelete(deletedProfile, password)
        );
        dialog.show();
    }

    /**
     * verifies the admin password
     */
    private void verifyAdminPasswordAndDelete(UserProfile profile, String password) {
        FirebaseUser currentAdmin = mAuth.getCurrentUser();

        if (currentAdmin == null) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminEmail = currentAdmin.getEmail();

        // credential with admin email and password
        AuthCredential credential = EmailAuthProvider.getCredential(adminEmail, password);

        // re-authenticate the admin user
        currentAdmin.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminDelete", "Admin password verified successfully");
                    // password is correct, proceed
                    deleteProfileFromFirebase(profile);
                })
                .addOnFailureListener(e -> {
                    // password is incorrect
                    Log.e("AdminDelete", "Admin password verification failed", e);
                    Toast.makeText(this,
                            "Incorrect admin password. Deletion cancelled.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void deleteProfileFromFirebase(UserProfile profile) {
        String email = profile.getEmail();
        String type = profile.getType();

        Log.d("AdminDelete", "Starting deletion for user: " + email + " (type: " + type + ")");

        // check if user is an organizer
        if ("organizer".equalsIgnoreCase(type)) {
            // delete all events organized by this user first
            deleteOrganizerEvents(email, () -> {
                // normal deletion
                removeUserFromAllEvents(email, () -> {
                    deleteUserDocument(email);
                });
            });
        } else {
            // for entrants, just remove from events and delete
            removeUserFromAllEvents(email, () -> {
                deleteUserDocument(email);
            });
        }
    }

    /**
     * Deletes all events organized by the specified organizer
     */
    private void deleteOrganizerEvents(String organizerEmail, Runnable onComplete) {
        Log.d("AdminDelete", "Deleting all events organized by: " + organizerEmail);

        db.collection("events")
                .whereEqualTo("organizer_email", organizerEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events to delete");

                    if (totalEvents == 0) {
                        // No events to delete
                        onComplete.run();
                        return;
                    }

                    // Delete each event
                    int[] deletedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    Log.d("AdminDelete", "Deleted event: " + document.getId());

                                    if (deletedCount[0] == totalEvents) {
                                        // All events deleted
                                        Log.d("AdminDelete", "All organizer events deleted successfully");
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error deleting event: " + document.getId(), e);
                                    deletedCount[0]++;

                                    if (deletedCount[0] == totalEvents) {
                                        // Continue even if some deletions failed
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying organizer events", e);
                    Toast.makeText(this, "Error deleting organizer events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Continue with deletion even if this fails
                    onComplete.run();
                });
    }

    /**
     * Removes user from all events where they are an entrant
     * Then chains to remove from winners, accepted, and rejected arrays
     */
    private void removeUserFromAllEvents(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from entrants in all events");

        db.collection("events")
                .whereArrayContains("entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events with user as entrant");

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
                                    Log.d("AdminDelete", "Removed from entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All entrants processed, move to winners
                                        removeUserFromWinners(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error removing from entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromWinners(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events with user as entrant", e);
                    // Continue to next step even if this fails
                    removeUserFromWinners(email, onComplete);
                });
    }

    /**
     * Removes user from winners arrays in all events
     * Then chains to remove from accepted_entrants
     */
    private void removeUserFromWinners(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from winners in all events");

        db.collection("events")
                .whereArrayContains("winners", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events with user as winner");

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
                                    Log.d("AdminDelete", "Removed from winners in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All winners processed, move to accepted_entrants
                                        removeUserFromAcceptedEntrants(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error removing from winners: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromAcceptedEntrants(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events with user as winner", e);
                    // Continue to next step even if this fails
                    removeUserFromAcceptedEntrants(email, onComplete);
                });
    }

    /**
     * Removes user from accepted_entrants arrays in all events
     * Then chains to remove from rejected_entrants
     */
    private void removeUserFromAcceptedEntrants(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from accepted_entrants in all events");

        db.collection("events")
                .whereArrayContains("accepted_entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events with user as accepted entrant");

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
                                    Log.d("AdminDelete", "Removed from accepted_entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All accepted_entrants processed, move to rejected_entrants
                                        removeUserFromRejectedEntrants(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error removing from accepted_entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromRejectedEntrants(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events with user as accepted entrant", e);
                    // Continue to next step even if this fails
                    removeUserFromRejectedEntrants(email, onComplete);
                });
    }

    /**
     * Removes user from rejected_entrants arrays in all events
     * Then chains to remove from entrants_waitlist
     */
    private void removeUserFromRejectedEntrants(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from rejected_entrants in all events");

        db.collection("events")
                .whereArrayContains("rejected_entrants", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events with user as rejected entrant");

                    if (totalEvents == 0) {
                        // No events, proceed to entrants_waitlist
                        removeUserFromWaitlist(email, onComplete);
                        return;
                    }

                    // Remove user from rejected_entrants array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("rejected_entrants", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("AdminDelete", "Removed from rejected_entrants in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All cleanup for rejected_entrants complete, move to waitlist
                                        removeUserFromWaitlist(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error removing from rejected_entrants: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue to waitlist even if some fail
                                        Log.d("AdminDelete", "Rejected entrants cleanup completed with some errors");
                                        removeUserFromWaitlist(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events with user as rejected entrant", e);
                    // Proceed to waitlist even if this fails
                    Log.d("AdminDelete", "Rejected entrants cleanup completed with errors");
                    removeUserFromWaitlist(email, onComplete);
                });
    }

    /**
     * Removes user from entrants_waitlist arrays in all events
     * Then chains to remove from waitingList subcollection
     */
    private void removeUserFromWaitlist(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from entrants_waitlist in all events");

        db.collection("events")
                .whereArrayContains("entrants_waitlist", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Found " + totalEvents + " events with user in waitlist");

                    if (totalEvents == 0) {
                        // No events, proceed to waitingList subcollection
                        removeUserFromWaitingListSubcollection(email, onComplete);
                        return;
                    }

                    // Remove user from entrants_waitlist array in each event
                    int[] processedCount = {0};
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference()
                                .update("entrants_waitlist", com.google.firebase.firestore.FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid -> {
                                    processedCount[0]++;
                                    Log.d("AdminDelete", "Removed from entrants_waitlist in event: " + document.getId());

                                    if (processedCount[0] == totalEvents) {
                                        // All waitlist processed, move to subcollection
                                        removeUserFromWaitingListSubcollection(email, onComplete);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error removing from entrants_waitlist: " + document.getId(), e);
                                    processedCount[0]++;

                                    if (processedCount[0] == totalEvents) {
                                        // Continue even if some fail
                                        removeUserFromWaitingListSubcollection(email, onComplete);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events with user in waitlist", e);
                    // Continue to next step even if this fails
                    removeUserFromWaitingListSubcollection(email, onComplete);
                });
    }

    /**
     * Removes user documents from waitingList subcollections in all events
     * This is the final cleanup step before deleting the user
     */
    private void removeUserFromWaitingListSubcollection(String email, Runnable onComplete) {
        Log.d("AdminDelete", "Removing user from waitingList subcollections");

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalEvents = querySnapshot.size();
                    Log.d("AdminDelete", "Checking " + totalEvents + " events for waitingList documents");

                    if (totalEvents == 0) {
                        Log.d("AdminDelete", "No events found, cleanup complete");
                        onComplete.run();
                        return;
                    }

                    int[] processedEvents = {0};
                    int[] deletedDocs = {0};

                    for (com.google.firebase.firestore.DocumentSnapshot eventDoc : querySnapshot.getDocuments()) {
                        String eventId = eventDoc.getId();

                        // Check if this event has a waitingList subcollection with this user
                        eventDoc.getReference()
                                .collection("waitingList")
                                .document(email)
                                .get()
                                .addOnSuccessListener(waitlistDoc -> {
                                    if (waitlistDoc.exists()) {
                                        // Delete the document
                                        waitlistDoc.getReference()
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    deletedDocs[0]++;
                                                    Log.d("AdminDelete", "Deleted from waitingList in event: " + eventId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("AdminDelete", "Error deleting from waitingList: " + eventId, e);
                                                });
                                    }

                                    processedEvents[0]++;
                                    if (processedEvents[0] == totalEvents) {
                                        Log.d("AdminDelete", "Removed from " + deletedDocs[0] + " waitingList subcollections");
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminDelete", "Error checking waitingList in event: " + eventId, e);
                                    processedEvents[0]++;

                                    if (processedEvents[0] == totalEvents) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDelete", "Error querying events for waitingList cleanup", e);
                    onComplete.run();
                });
    }

    /**
     * Deletes the user's document from accounts collection
     */
    private void deleteUserDocument(String email) {
        Log.d("AdminDelete", "Deleting user document from accounts collection");

        db.collection("accounts")
                .document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminDelete", "User document deleted successfully");
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                    // The snapshot listener will automatically update the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("AdminDelete", "Error deleting user document", e);
                });
    }

    private void applyFilter() {
        profileArrayList.clear();

        for (UserProfile profile : allProfilesList) {
            boolean shouldAdd = false;

            switch (FILTER_MODE) {
                case 0:
                    shouldAdd = true;
                    break;
                case 1:
                    shouldAdd = "entrant".equalsIgnoreCase(profile.getType());
                    break;
                case 2:
                    shouldAdd = "organizer".equalsIgnoreCase(profile.getType());
                    break;
            }

            if (shouldAdd) {
                profileArrayList.add(profile);
            }
        }

        profileArrayAdapter.notifyDataSetChanged();
    }
}