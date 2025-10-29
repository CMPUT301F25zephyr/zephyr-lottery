package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //set spinner contents (dropdown menu)
        Spinner spinner = (Spinner) findViewById(R.id.account_type_spinner);
        //create ArrayAdapter from string array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_type_spinner_array,
                android.R.layout.simple_spinner_item
        );
        //pick default layout for spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter
        spinner.setAdapter(adapter);

        //do the db thing i guess
        db = FirebaseFirestore.getInstance();
        accounts = db.collection("accounts");

        //will update this (from lab 5) with new userprofile class and array adapter
//        citiesRef.addSnapshotListener((value, error) -> {
//            if (error != null) {
//                Log.e("Firestore", error.toString());
//            }
//            if(value != null&& !value.isEmpty()){
//                cityArrayList.clear();
//                for (QueryDocumentSnapshot snapshot : value){
//                    String name = snapshot.getString("name");
//                    String province = snapshot.getString("province");
//
//                    cityArrayList.add(new City(name,province));
//                }
//                cityArrayAdapter.notifyDataSetChanged();
//            }
//        });
    }
}