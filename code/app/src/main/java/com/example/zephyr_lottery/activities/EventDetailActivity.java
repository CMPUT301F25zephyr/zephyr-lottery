package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.events.EventManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 101;
    private String eventId;
    private Event event;
    private TextView tvName, tvTimes, tvDesc;
    private Button btnJoin;
    private FusedLocationProviderClient fusedLocationClient;

    public static Intent createIntent(AppCompatActivity ctx, String eventId) {
        Intent i = new Intent(ctx, EventDetailActivity.class);
        i.putExtra("EVENT_ID", eventId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail); // create this layout (name can vary)

        tvName = findViewById(R.id.tvEventName);
        tvTimes = findViewById(R.id.tvEventTimes);
        tvDesc = findViewById(R.id.tvEventDesc);
        btnJoin = findViewById(R.id.btnJoin);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        eventId = getIntent().getStringExtra("EVENT_ID");
        loadEvent();

        btnJoin.setOnClickListener(v -> attemptJoin());
    }

    private void loadEvent() {
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .get().addOnSuccessListener(doc -> {
                    event = doc.toObject(Event.class);
                    if (event != null) {
                        event.setId(doc.getId());
                        tvName.setText(event.getName());
                        tvTimes.setText(event.getTimes() == null ? "" : event.getTimes());
                        tvDesc.setText(event.getDescription() == null ? "" : event.getDescription());
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show());
    }

    private void attemptJoin() {
        // check if event requires geofence
        if (event != null && event.getGeofence() != null) {
            // need location permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    callJoin(location);
                } else {
                    Toast.makeText(this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Location error", Toast.LENGTH_SHORT).show());
        } else {
            // no geofence -> pass null coords
            callJoin(null);
        }
    }

    private void callJoin(Location location) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "You must be logged in to join", Toast.LENGTH_SHORT).show();
            return;
        }
        Double lat = location != null ? location.getLatitude() : null;
        Double lng = location != null ? location.getLongitude() : null;

        EventManager.joinWaitingList(eventId, uid, lat, lng, (success, message) -> runOnUiThread(() -> {
            if (success) {
                Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Join failed: " + message, Toast.LENGTH_LONG).show();
            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOCATION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            attemptJoin();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
