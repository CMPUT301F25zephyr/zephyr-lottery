package com.example.zephyr_lottery.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.activities.HomeEntActivity;
import com.example.zephyr_lottery.models.NotificationLog;
import com.example.zephyr_lottery.repositories.NotificationLogRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "lottery_notifications";

    // NEW: repo for logging
    private final NotificationLogRepository logRepository = new NotificationLogRepository();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if user has notifications enabled
        checkNotificationPreferenceAndShow(remoteMessage);
    }

    private void checkNotificationPreferenceAndShow(RemoteMessage remoteMessage) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();

            db.collection("accounts")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Boolean isReceivingNotis = documentSnapshot.getBoolean("isReceivingNotis");

                        if (isReceivingNotis != null && isReceivingNotis) {
                            // Show OS notification
                            showNotification(remoteMessage);
                            // NEW: write an entry to notification logs
                            logNotification(remoteMessage, userEmail);
                        } else {
                            Log.d(TAG, "User has disabled notifications");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking notification preference", e);
                    });
        }
    }

    /**
     * NEW: Build and save a NotificationLog document.
     * Assumes that FCM data payload may contain:
     *   senderEmail, eventId, eventName, type, title, body
     * but will also fall back to the Notification part if present.
     */
    private void logNotification(RemoteMessage remoteMessage, String recipientEmail) {
        NotificationLog log = new NotificationLog();
        log.setRecipientEmail(recipientEmail);

        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            log.setSenderEmail(data.get("senderEmail"));
            log.setEventId(data.get("eventId"));
            log.setEventName(data.get("eventName"));
            log.setType(data.get("type"));
            if (log.getTitle() == null) log.setTitle(data.get("title"));
            if (log.getBody() == null) log.setBody(data.get("body"));
        }

        RemoteMessage.Notification notif = remoteMessage.getNotification();
        if (notif != null) {
            if (log.getTitle() == null) log.setTitle(notif.getTitle());
            if (log.getBody() == null) log.setBody(notif.getBody());
        }

        log.setTimestamp(Timestamp.now());

        logRepository.logNotification(log);
    }

    private void showNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getTitle() : "New Notification";
        String body = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getBody() : "";

        createNotificationChannel();

        Intent intent = new Intent(this, HomeEntActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

        Log.d(TAG, "Notification shown: " + title);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lottery Notifications";
            String description = "Notifications for lottery events";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        sendTokenToFirestore(token);
    }

    private void sendTokenToFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();

            db.collection("accounts")
                    .document(userEmail)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "FCM token saved to Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving FCM token", e);
                    });
        }
    }
}
