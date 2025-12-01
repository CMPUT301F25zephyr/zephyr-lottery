package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check device status after short delay
        new Handler().postDelayed(() -> {
            checkDeviceAccount();
        }, 1500);
    }

    private void checkDeviceAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Device already has anonymous account
            String uid = currentUser.getUid();
            Log.d(TAG, "Found existing Firebase UID: " + uid);

            // Check if profile exists in Firestore
            checkProfileExists(uid);
        } else {
            // No account on this device, create anonymous account
            Log.d(TAG, "No Firebase account found, creating anonymous account");
            createAnonymousAccount();
        }
    }

    private void createAnonymousAccount() {
        mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    String androidId = getAndroidId();

                    Log.d(TAG, "Anonymous account created. UID: " + uid);
                    Log.d(TAG, "Android ID: " + androidId);

                    // Navigate to role selection
                    navigateToRoleSelection(uid, androidId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create anonymous account", e);
                    Toast.makeText(this,
                            "Failed to initialize account. Please check your internet connection.",
                            Toast.LENGTH_LONG).show();

                    // Retry after delay
                    new Handler().postDelayed(() -> createAnonymousAccount(), 3000);
                });
    }

    private void checkProfileExists(String uid) {
        db.collection("accounts")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Profile exists, get user type and navigate
                        String userType = documentSnapshot.getString("type");

                        // Update last login
                        documentSnapshot.getReference()
                                .update("lastLogin", com.google.firebase.Timestamp.now());

                        Log.d(TAG, "Profile found. User type: " + userType);
                        navigateToHome(uid, userType);
                    } else {
                        // Firebase account exists but no profile
                        // This might happen if profile creation failed
                        Log.d(TAG, "Firebase account exists but no profile found");
                        String androidId = getAndroidId();
                        navigateToRoleSelection(uid, androidId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking profile", e);
                    Toast.makeText(this,
                            "Failed to load profile. Please check your internet connection.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String getAndroidId() {
        String androidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (androidId == null || androidId.isEmpty()) {
            // Fallback if Android ID is not available (very rare)
            androidId = "unknown_" + System.currentTimeMillis();
            Log.w(TAG, "Android ID not available, using fallback");
        }

        return androidId;
    }

    private void navigateToRoleSelection(String uid, String androidId) {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.putExtra("FIREBASE_UID", uid);
        intent.putExtra("ANDROID_ID", androidId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHome(String uid, String userType) {
        Intent intent;

        if ("admin".equalsIgnoreCase(userType)) {
            intent = new Intent(this, HomeAdmActivity.class);
        } else if ("organizer".equalsIgnoreCase(userType)) {
            intent = new Intent(this, HomeOrgActivity.class);
        } else {
            intent = new Intent(this, HomeEntActivity.class);
        }

        intent.putExtra("FIREBASE_UID", uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}