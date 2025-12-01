package com.example.zephyr_lottery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

import java.io.IOException;

public class HomeEntActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private CardView viewEventsButton, viewHistoryButton, editProfileButton, scanQRButton;
    private TextView textViewGreeting;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String androidId;  // Changed from userEmail
    private ArrayList<Integer> incoming_invitations;
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

        // Get Android ID - either from intent or get it directly
        androidId = getIntent().getStringExtra("ANDROID_ID");
        if (androidId == null || androidId.isEmpty()) {
            androidId = getAndroidId();
        }

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, UserProfileActivity.class);
            intent.putExtra("ANDROID_ID", androidId);
            startActivity(intent);
        });

        viewHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, EntEventHistoryActivity.class);
            intent.putExtra("ANDROID_ID", androidId);
            startActivity(intent);
        });

        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeEntActivity.this, EntEventsActivity.class);
            intent.putExtra("ANDROID_ID", androidId);
            startActivity(intent);
        });

        //activity launcher for scanning qr code.
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String event_code = result.getContents();

                //switch to details activity and pass in the hashcode of the QR code
                Intent intent = new Intent(HomeEntActivity.this, EntEventDetailActivity.class);
                intent.putExtra("ANDROID_ID", androidId);
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
        loadUsername_checkAccount();
    }

    private String getAndroidId() {
        String id = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (id == null || id.isEmpty()) {
            id = "unknown_" + System.currentTimeMillis();
            Log.w("HomeEntActivity", "Android ID not available, using fallback");
        }

        return id;
    }

    private void loadUsername_checkAccount() {
        // Get Firebase UID instead of email
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();

        // Query using Firebase UID as document key
        db.collection("accounts")
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get username
                        String username = documentSnapshot.getString("username");

                        if (username != null && !username.isEmpty()) {
                            textViewGreeting.setText("Greetings, " + username);
                        } else {
                            textViewGreeting.setText("Hello, User");
                        }

                        // Get Android ID from profile
                        String profileAndroidId = documentSnapshot.getString("androidId");

                        // Get any event invitations
                        incoming_invitations = (ArrayList<Integer>) documentSnapshot.get("invitationCodes");

                        if (incoming_invitations != null && !incoming_invitations.isEmpty()) {
                            Intent intent = new Intent(HomeEntActivity.this, EventInvitationActivity.class);
                            intent.putExtra("FIREBASE_UID", firebaseUid);

                            int invitation_code = incoming_invitations.get(incoming_invitations.size() - 1);
                            intent.putExtra("EVENT_CODE", Integer.toString(invitation_code));

                            // Remove invitation using Firebase UID
                            db.collection("accounts").document(firebaseUid)
                                    .update("invitationCodes", FieldValue.arrayRemove(invitation_code))
                                    .addOnSuccessListener(aVoid -> {
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(HomeEntActivity.this,
                                                "Invitation not removed from database",
                                                Toast.LENGTH_SHORT).show();
                                        Log.e("HomeEntActivity", "Error removing invitation", e);
                                    });
                        }
                    } else {
                        textViewGreeting.setText("Hello, User");
                        Log.w("HomeEntActivity", "Profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    textViewGreeting.setText("Hello, User");
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e("HomeEntActivity", "Error loading profile", e);
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