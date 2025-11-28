package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrgMyEventEntrantsAcceptedActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_entrants_accepted_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

                    if (snapshot.getId().equals(Integer.toString(getIntent().getIntExtra("EVENT_CLICKED_CODE", -1)))) {
                        System.out.println("Populating list\n\n");
                        ListView nameListView = findViewById(R.id.ListView_entrants_accepted);
                        ArrayAdapter<String> nameArrayAdapter;
                        ArrayList<String> nameArrayList = (ArrayList<String>) snapshot.get("accepted_entrants");
                        if (nameArrayList == null) {
                            return;
                        }
                        System.out.println(nameArrayList);
                        nameArrayAdapter = new ArrayAdapter<String>(this, R.layout.org_my_event_entrantslist_accepted_activity, R.id.entrants_accepted_item, nameArrayList);
                        nameListView.setAdapter(nameArrayAdapter);
                    }
                }
            }
        });


    }
}
