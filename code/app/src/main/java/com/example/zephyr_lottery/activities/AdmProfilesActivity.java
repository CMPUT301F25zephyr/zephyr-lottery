package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
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
    private Button filter_profiles_button; // needs implementation
    private Button back_browse_profiles_button;
    private ListView profileListView;
    private ArrayList<UserProfile> profileArrayList;
    private ArrayAdapter<UserProfile> profileArrayAdapter;

    //databases
    private FirebaseFirestore db;
    private CollectionReference profilesRef;

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
        profileArrayAdapter = new ProfileArrayAdapter(this, profileArrayList);
        profileListView.setAdapter(profileArrayAdapter);

        //listener. updates array when created and when database changes.
        profilesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                profileArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("username");
                    String email = snapshot.getString("email");
                    String type = snapshot.getString("type");

                    profileArrayList.add(new UserProfile(name, email, type));
                }
                profileArrayAdapter.notifyDataSetChanged();
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

    }
}
