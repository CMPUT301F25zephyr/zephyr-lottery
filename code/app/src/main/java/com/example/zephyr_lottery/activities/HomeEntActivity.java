package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;

public class HomeEntActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private CardView viewEventsButton, viewHistoryButton, editProfileButton, scanQRButton;
    private TextView textViewGreeting;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userEmail;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_ent_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request notification permission for Android 13+
        requestNotificationPermission();

        textViewGreeting = findViewById(R.id.tvEntrantGreeting);
        viewEventsButton = findViewById(R.id.btnLatestEvents);
        viewHistoryButton = findViewById(R.id.btnHistory);
        editProfileButton = findViewById(R.id.btnEditProfile);
        scanQRButton = findViewById(R.id.btnScanQR);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load username from Firebase
        loadUsername();

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        viewHistoryButton.setOnClickListener(v -> {
            // TODO: add view history view
            Intent intent = new Intent(HomeEntActivity.this, EntEventHistoryActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, EntEventsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        //activity launcher for scanning qr code.
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String event_code = result.getContents();

                //switch to details activity and pass in the hashcode of the QR code
                Intent intent = new Intent(HomeEntActivity.this, EntEventDetailActivity.class);
                intent.putExtra("USER_EMAIL", mAuth.getCurrentUser().getEmail());
                intent.putExtra("EVENT", event_code);
                intent.putExtra("FROM_ACTIVITY", "HOME_ENT");
                startActivity(intent);
            } else {
                Toast.makeText(HomeEntActivity.this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        scanQRButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("scan an event QR code");
            options.setCameraId(0);  //rear camera
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(false);
            options.setOrientationLocked(false);

            scanLauncher.launch(options);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload username when returning from UserProfileActivity
        loadUsername();
    }

    private void loadUsername() {
        userEmail = mAuth.getCurrentUser().getEmail();

        db.collection("accounts")
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);

                    if (profile != null) {
                        // Use the profile data
                        String username = profile.getUsername();
                        String userEmail = profile.getEmail();
                        textViewGreeting.setText("Greetings, " + username);
                    } else {
                        textViewGreeting.setText("Hello, User");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error - show default greeting
                    textViewGreeting.setText("Hello, User");
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeEntActivity", "Notification permission granted");
            } else {
                Log.d("HomeEntActivity", "Notification permission denied");
            }
        }
    }
}