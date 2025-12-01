package com.example.zephyr_lottery.repositories;

import android.util.Log;

import com.example.zephyr_lottery.models.NotificationLog;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Repository for reading & writing notification log entries.
 */
public class NotificationLogRepository {

    private static final String TAG = "NotificationLogRepo";
    private static final String COLLECTION = "notification_logs";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Write a new log entry.
     */
    public Task<Void> logNotification(NotificationLog log) {
        if (log.getTimestamp() == null) {
            log.setTimestamp(Timestamp.now());
        }

        CollectionReference logsRef = db.collection(COLLECTION);

        return logsRef.add(log)
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Logged notification with id=" + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to write log", e))
                // convert Task<DocumentReference> -> Task<Void>
                .continueWith(task -> null);
    }

    /**
     * Listen to all logs ordered by timestamp (newest first).
     * Used by the admin UI.
     */
    public ListenerRegistration listenToAllLogs(Consumer<List<NotificationLog>> onLogs,
                                                Consumer<Exception> onError) {
        return db.collection(COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen error", e);
                        if (onError != null) onError.accept(e);
                        return;
                    }
                    if (snap == null) {
                        if (onLogs != null) onLogs.accept(Collections.emptyList());
                        return;
                    }

                    List<NotificationLog> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        NotificationLog log = doc.toObject(NotificationLog.class);
                        if (log != null) {
                            log.setId(doc.getId());
                            list.add(log);
                        }
                    }
                    if (onLogs != null) onLogs.accept(list);
                });
    }
}
