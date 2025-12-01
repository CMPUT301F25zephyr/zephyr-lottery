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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrgMyEventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private static final String TAG = "OrgMyEventDetails";

    private TextView detailsText;
    private ImageView eventImage;
    private Button backButton;
    private Button entrantsButton;
    private Button generateQrButton;
    private Button buttonDrawLottery;
    private Button editButton;
    private Button viewMapButton;

    private int eventCode;
    private String userEmail;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_details_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        detailsText = findViewById(R.id.text_placeholder);
        backButton = findViewById(R.id.button_org_event_details_back);
        entrantsButton = findViewById(R.id.button_entrants);
        generateQrButton = findViewById(R.id.button_generate_qr);
        viewMapButton = findViewById(R.id.button_view_entrants_map);
        buttonDrawLottery = findViewById(R.id.button_draw_lottery);
        eventImage = findViewById(R.id.imageView_orgEventDetails);
        editButton = findViewById(R.id.button_org_event_details_edit);

        loadEventDetails();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        entrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventEntrantsActivity.class);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("USER_EMAIL", userEmail);

            //add in arraylists of entrants with statuses
            intent.putExtra("WAITLIST_ENTRANTS", event.getEntrants_waitlist());
            intent.putExtra("ACCEPT_ENTRANTS", event.getAccepted_entrants());
            intent.putExtra("REJECT_ENTRANTS", event.getRejected_entrants());
            intent.putExtra("PENDING_ENTRANTS", event.getWinners());

            startActivity(intent);
        });

        // View entrants on map: pass Firestore document ID as a String
        viewMapButton.setOnClickListener(v -> {
            if (eventCode == -1) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventId = Integer.toString(eventCode);

            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgEntrantsMapActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        generateQrButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, QRCodeOrgActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgEditEventActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });

        buttonDrawLottery.setOnClickListener(v -> {
            ArrayList<String> winners = drawLotteryWinners();
            if (winners.isEmpty()) {
                Toast.makeText(this, "No entrants to draw from, or all winners already chosen.", Toast.LENGTH_SHORT).show();
                return;
            }

            //disable button until memory stuff done
            buttonDrawLottery.setEnabled(false);

            //save winners to database, send winners to dialogue
            db.collection("events").document(Integer.toString(eventCode))
                    .update("winners", winners)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Winners saved!", Toast.LENGTH_SHORT).show();

                        //update local version of winners as well
                        event.setWinners(winners);

                        //save the invitation to the events chosen
                        sendInvitations(winners);

                        Intent intent = new Intent(OrgMyEventDetailsActivity.this, DrawWinnersPopupActivity.class);
                        intent.putStringArrayListExtra("WINNERS", winners);
                        startActivity(intent);

                        //enable button after memory stuff done
                        buttonDrawLottery.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save winners", e);
                        Toast.makeText(this, "Failed to save winners", Toast.LENGTH_SHORT).show();
                        buttonDrawLottery.setEnabled(true);
                    });
        });
    }

    /**
     * finds users of the winners and updates their pending invitations
     * @param winner_emails
     * the emails of the lottery winners
     */
    private void sendInvitations(ArrayList<String> winner_emails){
        //reference to users database
        CollectionReference usersRef = db.collection("accounts");

        //go through all emails of winners
        for (String email : winner_emails) {

            //get user and send invitation
            DocumentReference doc_ref = usersRef.document(email);
            doc_ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //user has been found. now we add to their list of invitations
                    DocumentSnapshot document = task.getResult();

                    //check for null (like if account has been deleted perhaps?)
                    if (document.exists()) {
                        UserProfile profile = document.toObject(UserProfile.class);
                        if (profile != null) {

                            //add this invitation to the user's invitation codes if it's not already there
                            doc_ref.update("invitationCodes", FieldValue.arrayUnion(eventCode))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Invitation sent to " + email);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to send invitation to " + email, e);
                                    });

                        } else {
                            Log.e(TAG, "account does not exist for " + email + ":(");
                        }

                        //remove this user from the entrants_waitlist
                        db.collection("events").document(Integer.toString(eventCode))
                                .update("entrants_waitlist", FieldValue.arrayRemove(email))
                                .addOnSuccessListener(aVoid ->{
                                    event.getEntrants_waitlist().remove(email);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "user not removed from waitlist!", e);
                                    Toast.makeText(this, "Failed to remove from waitlist", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.e(TAG, "account not found for: " + email + ":(");
                    }

                } else {
                    Log.e("firestore stuff", "error getting user: " + email + "!!!!!!!!! bad!", task.getException());
                }
            });
        }
    }

    private void loadEventDetails() {
        if (eventCode == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = Integer.toString(eventCode);
        DocumentReference docRef = db.collection("events").document(docId);
        docRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Event e = snapshot.toObject(Event.class);
                    if (e == null) {
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    event = e;

                    String name = e.getName() != null ? e.getName() : "";
                    String time = e.getTime() != null ? e.getTime() : "";
                    String weekday = e.getWeekdayString() != null ? e.getWeekdayString() : "";
                    String location = e.getLocation() != null ? e.getLocation() : "";
                    String description = e.getDescription() != null ? e.getDescription() : "";
                    String period = e.getPeriod() != null ? e.getPeriod() : "";
                    float price = e.getPrice();
                    int limit = e.getLimit();
                    int sampleSize = e.getSampleSize();

                    //get image from class, convert to bitmap, display image.
                    String image_base64 = e.getPosterImage();
                    if (image_base64 != null) {
                        byte[] decodedBytes = Base64.decode(image_base64, Base64.DEFAULT);
                        Bitmap image_bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        eventImage.setImageBitmap(image_bitmap);
                    }

                    String text = ""
                            + "Name: " + name + "\n"
                            + "Time: " + time + " (" + weekday + ")\n"
                            + "Location: " + location + "\n"
                            + "Price: " + price + "\n"
                            + "Registration period: " + period + "\n"
                            + "Entrant limit: " + limit + "\n"
                            + "Sample size (lottery winners): " + sampleSize + "\n"
                            + "\nDescription:\n" + description;

                    detailsText.setText(text);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }

    private ArrayList<String> drawLotteryWinners() {
        ArrayList<String> winners = new ArrayList<>();

        if (event == null) {
            Toast.makeText(this, "Event not loaded yet.", Toast.LENGTH_SHORT).show();
            return winners;
        }

        if (event.getEntrants_waitlist() == null || event.getEntrants_waitlist().isEmpty()) {
            return winners;
        }

        ArrayList<String> entrants = new ArrayList<>(event.getEntrants_waitlist());

        List<String> accepted_list = event.getAccepted_entrants();
        List<String> winners_list = event.getWinners();

        //if accepted list is not null, we subtract it's size from the sample size
        int accepted_size = !(accepted_list == null) ? accepted_list.size() : 0;
        //if pending invitations list is not null, subtract it's size from the sample size
        int pending_size = !(winners_list == null) ? winners_list.size() : 0;

        //calculate sample size: number of participants to roll for.
        int sampleSize = event.getSampleSize() - accepted_size - pending_size;

        if (sampleSize > entrants.size()) {
            sampleSize = entrants.size();
        }

        //if sample size is 0 (or below??) then return.
        if (sampleSize <= 0) {
            Log.d(TAG, "you already have enough winners");
            return winners;
        }

        Collections.shuffle(entrants);
        for (int i = 0; i < sampleSize; i++) {
            winners.add(entrants.get(i));
        }

        //tvWinners.setText("Last draw: " + winners.size() + " selected");

        return winners;
    }
}
