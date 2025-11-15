package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdmProfilesActivity extends AppCompatActivity {
    private Button filter_profiles_button;
    private Button back_browse_profiles_button;
    private ListView profileListView;
    private ArrayList<UserProfile> profileArrayList;
    private ArrayList<UserProfile> allProfilesList; // original unfiltered list
    private ArrayAdapter<UserProfile> profileArrayAdapter;
    private int FILTER_MODE = 0;    // filter modes: 0 = both, 1 = entrants, 2 = organizers

    //databases
    private FirebaseFirestore db;
    private CollectionReference profilesRef;

    // Activity Result Launcher for filter activity
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
        profilesRef = db.collection("accounts");

        //set the list view to be the arraylist of profiles.
        profileListView = findViewById(R.id.ListView_browse_profiles);
        profileArrayList = new ArrayList<>();
        allProfilesList = new ArrayList<>(); // Initialize the full list
        profileArrayAdapter = new ProfileArrayAdapter(this, profileArrayList);
        profileListView.setAdapter(profileArrayAdapter);

        FILTER_MODE = getIntent().getIntExtra("FILTER_MODE", 0);

        // register activity result launcher
        filterActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // get the filter mode from the result
                        FILTER_MODE = result.getData().getIntExtra("FILTER_MODE", 0);
                        // apply the filter
                        applyFilter();
                    }
                }
        );

        //listener. updates array when created and when database changes.
        profilesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                allProfilesList.clear(); // Clear the full list
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("username");
                    String email = snapshot.getString("email");
                    String type = snapshot.getString("type");

                    allProfilesList.add(new UserProfile(name, email, type)); // Add to full list
                }
                applyFilter(); // Apply filter after loading data
            }
        });

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //listener for button to return to homescreen.
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

    // change the list to filter mode
    private void applyFilter() {
        profileArrayList.clear(); // clear the displayed list

        for (UserProfile profile : allProfilesList) { // filter from the full list
            boolean shouldAdd = false;

            switch (FILTER_MODE) {
                case 0: // Show all
                    shouldAdd = true;
                    break;
                case 1: // Entrants only
                    shouldAdd = "entrant".equalsIgnoreCase(profile.getType());
                    break;
                case 2: // Organizers only
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