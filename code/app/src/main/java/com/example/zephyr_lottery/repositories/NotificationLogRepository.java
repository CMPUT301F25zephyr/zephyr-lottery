package com.example.zephyr_lottery.repositories;

import android.util.Log;

import com.example.zephyr_lottery.models.NotificationLog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Repository for interacting with notificationLogs collection.
 */
public class NotificationLogRepository {
    private static final String TAG = "NotificationLogRepo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listen to logs for a specific event (if you only need one event).
     */
    public ListenerRegistration listenToLogsForEvent(
            String eventId,
            Consumer<List<NotificationLog>> onSuccess,
            Consumer<Exception> onError
    ) {
        CollectionReference logsRef = db.collection("notificationLogs");
        return logsRef.whereEqualTo("eventId", eventId)
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        if (onError != null) onError.accept(e);
                        return;
                    }
                    List<NotificationLog> results = new ArrayList<>();
                    if (snapshots != null) {
                        snapshots.forEach(doc -> {
                            NotificationLog log = doc.toObject(NotificationLog.class);
                            results.add(log);
                        });
                    }
                    if (onSuccess != null) onSuccess.accept(results);
                });
    }

    /**
     * Listen to all logs (for admin).
     */
    public ListenerRegistration listenToAllLogs(
            Consumer<List<NotificationLog>> onSuccess,
            Consumer<Exception> onError
    ) {
        CollectionReference logsRef = db.collection("notificationLogs");
        return logsRef.addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
            if (e != null) {
                Log.e(TAG, "Listen failed.", e);
                if (onError != null) onError.accept(e);
                return;
            }
            List<NotificationLog> results = new ArrayList<>();
            if (snapshots != null) {
                snapshots.forEach(doc -> {
                    NotificationLog log = doc.toObject(NotificationLog.class);
                    results.add(log);
                });
            }
            if (onSuccess != null) onSuccess.accept(results);
        });
    }
}
