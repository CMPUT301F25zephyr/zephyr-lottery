package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdmEventDetailActivity extends AppCompatActivity {

    private Button back_event_details_button;
    private Button button_remove_image;
    private Button button_delete_event;

    private TextView title;
    private TextView closingDate;
    private TextView startEnd;
    private TextView dayTime;
    private TextView location;
    private TextView price;
    private TextView description;
    private TextView entrantNumbers;
    private TextView lotteryWinners;
    private ImageView eventImageView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference eventsRef;
    private DocumentReference docRef;

    private String eventHash;
    private String currentUserEmail;
    private String eventName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.adm_eventdetail_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                currentUserEmail = currentUser.getEmail();
            }
        }

        eventHash = getIntent().getStringExtra("EVENT");

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        docRef = eventsRef.document(eventHash);

        loadEventDetails();
        setupClickListeners();
    }

    private void initializeViews() {
        title = findViewById(R.id.textView_ed_title);
        closingDate = findViewById(R.id.textView_closingdate);
        startEnd = findViewById(R.id.textView_startenddates);
        dayTime = findViewById(R.id.textView_day_time);
        location = findViewById(R.id.textView_location_string);
        price = findViewById(R.id.textView_price_string);
        description = findViewById(R.id.textView_description_string);
        entrantNumbers = findViewById(R.id.textView_currententrants);
        lotteryWinners = findViewById(R.id.textView_lotterywinners);

        back_event_details_button = findViewById(R.id.button_event_details_back);
        button_remove_image = findViewById(R.id.button_remove_image);
        button_delete_event = findViewById(R.id.button_delete_event);
        eventImageView = findViewById(R.id.imageView_adm_eventImage);
    }

    private void setupClickListeners() {
        back_event_details_button.setOnClickListener(view -> {
            Intent intent = new Intent(AdmEventDetailActivity.this, AdmEventsActivity.class);
            intent.putExtra("USER_EMAIL", currentUserEmail);
            startActivity(intent);
            finish();
        });

        button_remove_image.setOnClickListener(view -> {
            showRemoveImageDialog();
        });

        button_delete_event.setOnClickListener(view -> {
            showDeleteEventDialog();
        });
    }

    private void loadEventDetails() {
        docRef.get().addOnSuccessListener(currentEvent -> {
            if (!currentEvent.exists()) {
                Log.e("Firestore", "Event not found.");
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            eventName = currentEvent.getString("name");
            title.setText(eventName);

            closingDate.setText("");
            startEnd.setText("");

            String weekdayString = currentEvent.getString("weekdayString");
            String timeString = currentEvent.getString("time");
            StringBuilder dayTimeText = new StringBuilder();
            if (weekdayString != null) {
                dayTimeText.append(weekdayString).append(" ");
            }
            if (timeString != null) {
                dayTimeText.append(timeString);
            }
            dayTime.setText(dayTimeText.toString());

            location.setText(currentEvent.getString("location"));

            Object priceObj = currentEvent.get("price");
            if (priceObj != null) {
                price.setText("$" + priceObj.toString());
            } else {
                price.setText("$0");
            }

            description.setText(currentEvent.getString("description"));

            List<String> entrants = (List<String>) currentEvent.get("entrants");
            int entrantCount = entrants != null ? entrants.size() : 0;

            Long limitLong = currentEvent.getLong("limit");
            String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";
            entrantNumbers.setText("Current Entrants: " + entrantCount + "/" + limitDisplay + " slots");

            Long sampleSizeLong = currentEvent.getLong("sampleSize");
            int sampleSize = sampleSizeLong != null ? sampleSizeLong.intValue() : 0;
            lotteryWinners.setText("Lottery Winners: " + sampleSize);

            //get image from database, convert to bitmap, display image.
            String image_base64 = currentEvent.getString("posterImage");
            if (image_base64 != null) {
                byte[] decodedBytes = Base64.decode(image_base64, Base64.DEFAULT);
                Bitmap image_bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                eventImageView.setImageBitmap(image_bitmap);
            }

        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error loading event details", e);
            Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * shows confirmation dialog for removing event image
     */
    private void showRemoveImageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Event Image")
                .setMessage("Are you sure you want to remove the image for this event?\n\nEvent: " + eventName)
                .setPositiveButton("Continue", (dialog, which) -> {
                    showPasswordConfirmationDialog(true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * shows confirmation dialog for deleting entire event
     */
    private void showDeleteEventDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("WARNING: This will permanently delete the entire event!\n\nEvent: " + eventName +
                        "\n\nThis action cannot be undone.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    showPasswordConfirmationDialog(false);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * shows password confirmation dialog
     * @param isImageRemoval true if removing image, false if deleting event
     */
    private void showPasswordConfirmationDialog(boolean isImageRemoval) {
        AdmEventDialogActivity dialog = new AdmEventDialogActivity(
                this,
                eventName,
                isImageRemoval,
                password -> verifyPasswordAndExecuteAction(password, isImageRemoval)
        );
        dialog.show();
    }

    /**
     * verifies admin password and executes
     */
    private void verifyPasswordAndExecuteAction(String password, boolean isImageRemoval) {
        FirebaseUser currentAdmin = mAuth.getCurrentUser();

        if (currentAdmin == null) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminEmail = currentAdmin.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(adminEmail, password);

        currentAdmin.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminAction", "Admin password verified");
                    if (isImageRemoval) {
                        removeEventImage();
                    } else {
                        deleteEvent();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminAction", "Password verification failed", e);
                    Toast.makeText(this, "Incorrect admin password", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * removes the event image string from database
     */
    private void removeEventImage() {
        docRef.update("posterImage", null)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminAction", "Event image reference removed");
                    Toast.makeText(this, "Event image removed successfully", Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminAction", "Error removing image", e);
                    Toast.makeText(this, "Failed to remove image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * deletes the entire event from Firestore
     */
    private void deleteEvent() {
        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminAction", "Event deleted successfully");
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();

                    // Navigate back to events list
                    Intent intent = new Intent(AdmEventDetailActivity.this, AdmEventsActivity.class);
                    intent.putExtra("USER_EMAIL", currentUserEmail);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminAction", "Error deleting event", e);
                    Toast.makeText(this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}