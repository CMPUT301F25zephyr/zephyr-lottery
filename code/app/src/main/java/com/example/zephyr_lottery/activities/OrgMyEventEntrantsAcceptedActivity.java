package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrgMyEventEntrantsAcceptedActivity extends AppCompatActivity {
    private CollectionReference eventsRef;
    private ArrayList<String> waitlist_entrants;
    private ArrayList<String> accepted_entrants;
    private ArrayList<String> rejected_entrants;
    private ArrayList<String> pending_entrants;
    private ArrayList<String> entrantArrayList;
    private FirebaseFirestore db;
    Event event;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_entrants_accepted_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        String path = "events";
        eventsRef = db.collection(path);

        //get arraylists from intent. used for
        waitlist_entrants = getIntent().getStringArrayListExtra("WAITLIST_ENTRANTS");
        accepted_entrants = getIntent().getStringArrayListExtra("ACCEPT_ENTRANTS");
        rejected_entrants = getIntent().getStringArrayListExtra("REJECT_ENTRANTS");
        pending_entrants = getIntent().getStringArrayListExtra("PENDING_ENTRANTS");

        String docId = Integer.toString(getIntent().getIntExtra("EVENT_CLICKED_CODE", -1));
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
                });

        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                //entrantArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){

                    if (snapshot.getId().equals(Integer.toString(getIntent().getIntExtra("EVENT_CLICKED_CODE", -1)))) {
                        System.out.println("Populating list\n\n");
                        ListView nameListView = findViewById(R.id.ListView_entrants_accepted);
                        ArrayAdapter<String> nameArrayAdapter;
                        entrantArrayList = (ArrayList<String>) snapshot.get("accepted_entrants");

                        //add the status of the entrant to their entry in the list view
                        if (entrantArrayList != null && !entrantArrayList.isEmpty()) {
                            for (int i = 0; i <entrantArrayList.size(); i ++) {
                                String type = "";
                                String i_email = entrantArrayList.get(i);
                                if (waitlist_entrants != null && waitlist_entrants.contains(i_email)) {
                                    type = "waitlisted";
                                } else if (accepted_entrants != null && accepted_entrants.contains(i_email)) {
                                    type = "accepted";
                                } else if (rejected_entrants != null && rejected_entrants.contains(i_email)) {
                                    type = "rejected";
                                }  else if (pending_entrants != null && pending_entrants.contains(i_email)) {
                                    type = "invitation pending";
                                } else {
                                    type = "unknown";
                                }

                                entrantArrayList.set(i, i_email + ": " + type);
                            }
                        }

                        System.out.println(entrantArrayList);
                        nameArrayAdapter = new ArrayAdapter<String>(this, R.layout.org_my_event_entrantslist_accepted_activity, R.id.entrants_accepted_item, entrantArrayList);
                        nameListView.setAdapter(nameArrayAdapter);
                    }
                }
            }
        });

        findViewById(R.id.button_noti_accepted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference entDocRef = db.collection("accounts");
                ArrayList<String> waitingList = event.getEntrants();
                for (int i = 0; i < waitingList.size(); i++) {
                    DocumentReference entrant = entDocRef.document(waitingList.get(i));
                    entrant.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                UserProfile profile = document.toObject(UserProfile.class);
                                if (profile != null) {
                                    entrant.update("pendingNotifs", FieldValue.arrayUnion("You've accepted " + event.getName() + "//" + event.getDescription()));
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
