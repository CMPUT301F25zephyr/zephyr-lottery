package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgMyEventChosenEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "OrgChosenEntrants";

    private ListView listView;
    private Button sendButton;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> chosenEntrants = new ArrayList<>();

    private FirebaseFirestore db;
    private String eventId;   // string version of EVENT_CLICKED_CODE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_chosen_entrants_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        int eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        if (eventCode == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        eventId = Integer.toString(eventCode);

        TextView title = findViewById(R.id.textView_latest_event);
        title.setText("Chosen Entrants");

        listView = findViewById(R.id.ListView_entrants);
        sendButton = findViewById(R.id.button_send_notifications);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                chosenEntrants
        );
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadChosenEntrants();

        sendButton.setOnClickListener(v -> sendNotificationsToSelected());
    }

    private void loadChosenEntrants() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::onEventLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event for chosen entrants", e);
                    Toast.makeText(this,
                            "Failed to load chosen entrants.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void onEventLoaded(DocumentSnapshot snapshot) {
        if (!snapshot.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> winners = (List<String>) snapshot.get("winners");

        chosenEntrants.clear();
        if (winners != null) {
            chosenEntrants.addAll(winners);
        }
        adapter.notifyDataSetChanged();

        if (chosenEntrants.isEmpty()) {
            Toast.makeText(this,
                    "No chosen entrants for this event yet.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationsToSelected() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<String> selected = new ArrayList<>();

        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                String email = adapter.getItem(i);
                if (email != null) {
                    selected.add(email);
                }
            }
        }

        if (selected.isEmpty()) {
            Toast.makeText(this,
                    "Please select at least one entrant.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Log notification requests to Firestore.
        // A Cloud Function or backend can watch this collection and send real pushes.
        CollectionReference requestsRef = db.collection("notificationRequests");
        Timestamp now = Timestamp.now();

        for (String email : selected) {
            Map<String, Object> data = new HashMap<>();
            data.put("eventId", eventId);
            data.put("userId", email);
            data.put("type", "CHOSEN_INVITE");
            data.put("createdAt", now);

            requestsRef.add(data)
                    .addOnFailureListener(e -> Log.e(TAG,
                            "Failed to create notification request for " + email, e));
        }

        Toast.makeText(this,
                "Notifications queued for " + selected.size() + " entrant(s).",
                Toast.LENGTH_SHORT).show();
    }
}
