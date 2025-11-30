package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrgMyEventEntrantsActivity extends AppCompatActivity {

    private Button export_csv_button;
    private Button my_entrants_back_button;
    private int eventCode;
    private String userEmail;
    private ArrayList<String> entrantArrayList;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_entrants_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        String path = "events";
        eventsRef = db.collection(path);

        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                //entrantArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){

                    if (snapshot.getId().equals(Integer.toString(getIntent().getIntExtra("EVENT_CLICKED_CODE", -1)))) {
                        System.out.println("Populating list\n\n");
                        ListView nameListView = findViewById(R.id.ListView_entrants);
                        ArrayAdapter<String> nameArrayAdapter;
                        entrantArrayList = (ArrayList<String>) snapshot.get("entrants");
                        System.out.println(entrantArrayList);
                        nameArrayAdapter = new ArrayAdapter<String>(this, R.layout.org_my_event_entrantslist_activity, R.id.entrants_item, entrantArrayList);
                        nameListView.setAdapter(nameArrayAdapter);
                    }
                }
            }
        });

        //buttons initialization
        export_csv_button = findViewById(R.id.button_exportcsv);
        my_entrants_back_button = findViewById(R.id.button_back_my_entrants);
        //get things from intent
        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        //database
        usersRef = db.collection("users");

        //export csv button
//        export_csv_button.setOnClickListener(view -> {
//            if (entrantArrayList == null || entrantArrayList.isEmpty()) {
//                Toast.makeText(this, "No entrants", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            Toast.makeText(this, "cvs loading", Toast.LENGTH_SHORT).show();
//
//            getEntrantInfo(entrantArrayList, userProfiles -> {
//                //this stuff runs after the callback is called (after all profiles have been found).
//                try{
//                    //create file
//                    File file_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    File csv = new File(file_dir, "entrants_event_" + eventCode + ".csv");
//                    FileWriter writer = new FileWriter(csv);
//                    writer.append("email,username\n");
//                    Log.e("this works", "header made");
//
//                    //make data rows
//                    for (UserProfile profile : userProfiles) {
//                        Log.e("that works", "we have profiles");
//                        String email = profile.getEmail();
//                        String username = profile.getUsername();
//                        if (username == null) {
//                            username = "unknown";
//                        }
//
//                        //write to file
//                        writer.append(email).append(",")
//                                .append(username).append("\n");
//                    }
//
//                    writer.close();
//                    Toast.makeText(this, "csv saved to downloads", Toast.LENGTH_SHORT).show();
//
//                } catch (IOException e) {
//                    Toast.makeText(this, "error with csv", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });

        //back button
        my_entrants_back_button.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });
    }

//    //callback for getting entrant usernames
//    interface EntrantDetailsCallback {
//        void onProfilesGet(ArrayList<UserProfile> userProfiles);
//    }

//    private void getEntrantInfo(ArrayList<String> entrantEmails, EntrantDetailsCallback callback) {
//        ArrayList<UserProfile> userProfiles = new ArrayList<>();
//
//        //counter until all users found
//        final int[] user_count = {0};
//        final int num_entrants = entrantEmails.size();
//
//        for (String email : entrantEmails) {
//            usersRef.document(email).get().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    UserProfile profile = document.toObject(UserProfile.class);
//                    if (profile != null) {
//                        userProfiles.add(profile);
//                    }
//                    Log.e("more firestore stuff", "this user" + email + "has been found");
//                } else {
//                    Log.e("firestore stuff", "error getting user: " + email, task.getException());
//                }
//                user_count[0]++;
//
//                //when users found == users total, we call the callback.
//                //because the firestore stuff can happen out of sync.
//                if (user_count[0] == num_entrants) {
//                    Log.e("callback stuff", "all profiles gotten");
//                    callback.onProfilesGet(userProfiles);
//                }
//            });
//        }
//    }
}
