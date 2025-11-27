package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrgMyEventEntrantsActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_entrants_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int EVENT_CLICKED_CODE = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);

        findViewById(R.id.button_noti_accepted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventEntrantsCancelledActivity.class);
                intent.putExtra("EVENT_CLICKED_CODE", EVENT_CLICKED_CODE);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_list_accepted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventEntrantsAcceptedActivity.class);
                intent.putExtra("EVENT_CLICKED_CODE", EVENT_CLICKED_CODE);
                startActivity(intent);
            }
        });

        ListView nameListView = findViewById(R.id.ListView_entrants);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String path = "events";
        CollectionReference eventsRef = db.collection(path);
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                //nameArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){

                    if (snapshot.getId().equals(Integer.toString(EVENT_CLICKED_CODE))) {
                        System.out.println("Populating list\n\n");
                        ArrayAdapter<String> nameArrayAdapter;
                        ArrayList<String> nameArrayList = (ArrayList<String>) snapshot.get("entrants");
                        if (nameArrayList == null) {
                            return;
                        }
                        System.out.println(nameArrayList);
                        nameArrayAdapter = new ArrayAdapter<String>(this, R.layout.org_my_event_entrantslist_activity, R.id.entrants_item, nameArrayList);
                        nameListView.setAdapter(nameArrayAdapter);

                        nameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                DocumentReference docRef = eventsRef.document(Integer.toString(EVENT_CLICKED_CODE));
                                docRef.get().addOnSuccessListener(currentEvent -> {
                                    String finalUserEmail = nameArrayList.get(position);
                                    if (!currentEvent.exists()) {
                                        Toast.makeText(
                                                OrgMyEventEntrantsActivity.this,
                                                "Event not found.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return;
                                    }

                                    List<String> entrantsList = (List<String>) currentEvent.get("entrants");
                                    if (entrantsList == null) {
                                        entrantsList = new ArrayList<>();
                                    } else {
                                        entrantsList = new ArrayList<>(entrantsList);
                                        entrantsList.remove(null);
                                    }

                                    if (!entrantsList.contains(finalUserEmail)) {
                                        Toast.makeText(
                                                OrgMyEventEntrantsActivity.this,
                                                "The selected user is not on the waiting list.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        return;
                                    }

                                    List<String> cancelledEntrantsList = (List<String>) currentEvent.get("cancelled_entrants");
                                    if (cancelledEntrantsList == null) {
                                        cancelledEntrantsList = new ArrayList<>();
                                    } else {
                                        cancelledEntrantsList = new ArrayList<>(cancelledEntrantsList);
                                        cancelledEntrantsList.remove(null);
                                    }

                                    Long limitLong = currentEvent.getLong("limit");
                                    String limitDisplay = limitLong != null ? String.valueOf(limitLong) : "?";
                                    int currentSize = entrantsList.size();

                                    docRef.update("entrants", FieldValue.arrayRemove(finalUserEmail))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(
                                                        OrgMyEventEntrantsActivity.this,
                                                        "The user has been removed from the waiting list.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                docRef.update("cancelled_entrants", FieldValue.arrayUnion(finalUserEmail));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("EntEventDetail", "Error removing entrant", e);
                                                Toast.makeText(
                                                        OrgMyEventEntrantsActivity.this,
                                                        "Failed to remove. Please try again.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            });
                                });

                            }
                        });
                    }
                }
            }
        });


    }
}
