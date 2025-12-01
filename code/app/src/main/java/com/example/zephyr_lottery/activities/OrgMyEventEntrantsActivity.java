package com.example.zephyr_lottery.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class OrgMyEventEntrantsActivity extends AppCompatActivity {

    private Button export_csv_button;
    private Button my_entrants_back_button;
    private int eventCode;
    private String userEmail;
    private ArrayList<String> entrantArrayList;
    private ArrayList<String> accepted_entrants_arraylist;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;

    private ArrayList<String> waitlist_entrants;
    private ArrayList<String> accepted_entrants;
    private ArrayList<String> rejected_entrants;
    private ArrayList<String> pending_entrants;

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

        //get arraylists from intent. used for
        waitlist_entrants = getIntent().getStringArrayListExtra("WAITLIST_ENTRANTS");
        accepted_entrants = getIntent().getStringArrayListExtra("ACCEPT_ENTRANTS");
        rejected_entrants = getIntent().getStringArrayListExtra("REJECT_ENTRANTS");
        pending_entrants = getIntent().getStringArrayListExtra("PENDING_ENTRANTS");

        //list view display
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
                        nameArrayAdapter = new ArrayAdapter<String>(this, R.layout.org_my_event_entrantslist_activity, R.id.entrants_item, entrantArrayList);
                        nameListView.setAdapter(nameArrayAdapter);
                    }
                }
            }
        });

        //buttons initialization
        export_csv_button = findViewById(R.id.button_exportcsv);
        my_entrants_back_button = findViewById(R.id.button_back_my_entrants);
        Button accepted_entrants_button = findViewById(R.id.button_list_accepted);
        Button cancelled_entrants_button = findViewById(R.id.button_list_cancelled);
        //get things from intent
        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        //database
        usersRef = db.collection("accounts");

        //disable button until the data has been retreived
        //(so we don't get like a race condition)
        export_csv_button.setEnabled(false);

        //get array of accepted emails
        DocumentReference docRef =eventsRef.document(Integer.toString(eventCode));
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

                    accepted_entrants_arraylist = e.getAccepted_entrants();
                    export_csv_button.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                });


        //export csv button
        export_csv_button.setOnClickListener(view -> {
            if (accepted_entrants_arraylist == null || accepted_entrants_arraylist.isEmpty()) {
                Toast.makeText(this, "No accepted entrants", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "cvs loading", Toast.LENGTH_SHORT).show();

            getEntrantInfo(accepted_entrants_arraylist, userProfiles -> {
                //this stuff runs after the callback is called (after all profiles have been found).
                try{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //new android
                        save_csv_new(userProfiles);
                    } else {
                        //old android
                        save_csv_old(userProfiles);
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "error with csv", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //back button
        my_entrants_back_button.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });

        //accepted entrants list
        accepted_entrants_button.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventEntrantsAcceptedActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("WAITLIST_ENTRANTS", waitlist_entrants);
            intent.putExtra("ACCEPT_ENTRANTS", accepted_entrants);
            intent.putExtra("REJECT_ENTRANTS", rejected_entrants);
            intent.putExtra("PENDING_ENTRANTS", pending_entrants);
            startActivity(intent);
        });

        //cancelled entrants list
        cancelled_entrants_button.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventEntrantsActivity.this, OrgMyEventEntrantsCancelledActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            intent.putExtra("WAITLIST_ENTRANTS", waitlist_entrants);
            intent.putExtra("ACCEPT_ENTRANTS", accepted_entrants);
            intent.putExtra("REJECT_ENTRANTS", rejected_entrants);
            intent.putExtra("PENDING_ENTRANTS", pending_entrants);
            startActivity(intent);
        });
    }

    /**
     * the new version for saving and writing the data into the csv.
     * @param userProfiles
     * @throws java.io.IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void save_csv_new(ArrayList<UserProfile> userProfiles) throws java.io.IOException{
        //content values stuff
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "entrants_event_" + eventCode + ".csv");
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        //create file
        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Failed to create CSV file", Toast.LENGTH_SHORT).show();
            return;
        }

        //write data
        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) {
                Toast.makeText(this, "Failed to write CSV", Toast.LENGTH_SHORT).show();
                return;
            }

            //header
            out.write("email,username\n".getBytes());

            //write data rows
            for (UserProfile profile : userProfiles) {
                Log.e("this works", "we have profiles");
                String email = profile.getEmail();
                String username = profile.getUsername();
                if (username == null) {
                    username = "unknown";
                }
                String row = email + "," + username + "\n";
                out.write(row.getBytes());
            }
            out.flush();
        }

        //finish writing
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        Toast.makeText(this, "CSV saved to Downloads", Toast.LENGTH_SHORT).show();
        Log.e("MediaStore", "CSV saved successfully");
    }

    /**
     * function for writing and saving the data in the csv.
     * old version needs to exist because the new stuff is too new for the minimum API
     * @param userProfiles arraylist of all profiles
     * @throws java.io.IOException
     */
    private void save_csv_old(ArrayList<UserProfile> userProfiles) throws java.io.IOException {
        //create file
        File file_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csv = new File(file_dir, "entrants_event_" + eventCode + ".csv");
        FileWriter writer = new FileWriter(csv);
        writer.append("email,username\n");
        Log.e("this works", "header made");

        //make data rows
        for (UserProfile profile : userProfiles) {
            Log.e("that works", "we have profiles");
            String email = profile.getEmail();
            String username = profile.getUsername();
            if (username == null) {
                username = "unknown";
            }

            //write to file
            writer.append(email).append(",")
                    .append(username).append("\n");
        }

        writer.close();
        Toast.makeText(this, "csv saved to downloads", Toast.LENGTH_SHORT).show();
    }

    /**
     * callback interface. for getting the user profiles arrayList.
     */
    interface EntrantDetailsCallback {
        void onProfilesGet(ArrayList<UserProfile> userProfiles);
    }

    /**
     * gets all the profiles from the entrants arraylist from the database.
     * has a callback so you can use the data when it is done
     * @param entrantEmails list of user emails
     * @param callback the callback
     */
    private void getEntrantInfo(ArrayList<String> entrantEmails, EntrantDetailsCallback callback) {
        ArrayList<UserProfile> userProfiles = new ArrayList<>();

        //counter until all users found
        final int[] user_count = {0};
        final int num_entrants = entrantEmails.size();

        for (String email : entrantEmails) {
            usersRef.document(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    UserProfile profile = document.toObject(UserProfile.class);
                    if (profile != null) {
                        userProfiles.add(profile);
                    }
                    Log.e("more firestore stuff", "this user " + email + " has been found");
                } else {
                    Log.e("firestore stuff", "error getting user: " + email, task.getException());
                }
                user_count[0]++;

                //when users found == users total, we call the callback.
                //because the firestore stuff can happen out of sync.
                if (user_count[0] == num_entrants) {
                    Log.e("callback stuff", "all profiles gotten");
                    callback.onProfilesGet(userProfiles);
                }
            });
        }
    }
}
