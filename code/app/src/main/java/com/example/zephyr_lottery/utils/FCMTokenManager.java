package com.example.zephyr_lottery.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * This class manages FCM tokens for access to Firestore.
 */
public class FCMTokenManager {

    private static final String TAG = "FCMTokenManager";

    /**
     * Creates an FCM token and saves it to Firestore
     */
    public static void initializeFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        saveTokenToFirestore(token);
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                    }
                });
    }

    /**
     * Saves an FCM token to Firestore
     * @param token
     */
    private static void saveTokenToFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();

            db.collection("accounts")
                    .document(userEmail)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "FCM token saved successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving FCM token", e);
                    });
        }
    }
}