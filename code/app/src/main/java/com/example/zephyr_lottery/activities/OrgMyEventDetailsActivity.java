package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.EventArrayAdapter;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrgMyEventDetailsActivity extends AppCompatActivity {

    private Button button_generateQR;

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

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //we don't have to worry about the default value though
        int event_code = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);

        //listener for button to go to qr code screen.
        button_generateQR = findViewById(R.id.button_generate_qr);
        button_generateQR.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, QRCodeActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            intent.putExtra("EVENT_CLICKED_CODE", event_code);
            startActivity(intent);
        });

        Button button_entrants = findViewById(R.id.button_entrants);
        button_entrants.setOnClickListener(v -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventEntrantsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            intent.putExtra("EVENT_CLICKED_CODE", event_code);
            startActivity(intent);
        });

        //listener for button to go to back.
        button_generateQR = findViewById(R.id.button_org_event_details_back);
        button_generateQR.setOnClickListener(view -> {
            Intent intent = new Intent(OrgMyEventDetailsActivity.this, OrgMyEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }
}
