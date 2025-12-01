package com.example.zephyr_lottery.repositories;

import android.util.Log;

import com.example.zephyr_lottery.models.Participant;
import com.example.zephyr_lottery.models.WaitingListEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This class manages the statuses of participants and the accepting/declining of invitations.
 */
public class EventRepository {
    private static final String TAG = "EventRepo";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Changes the status of a specified participant in an event
     * @param eventId
     *  The event ID that the participant is in the lottery for
     * @param userId
     *  The participant's ID
     * @param status
     *  The new status of the participant
     * @return
     *  Returns a Task when completed asynchronously
     */
    // Update participant status (accepted/declined)
    public Task<Void> updateParticipantStatus(String eventId, String userId, String status) {
        DocumentReference participantRef = db.collection("events")
                .document(eventId)
                .collection("participants")
                .document(userId);

        Participant p = new Participant(userId, status, null, Timestamp.now());
        return participantRef.set(p, SetOptions.merge());
    }

    /**
     * Accepts the invitation (called when the accept button from the notification screen is pushed)
     * @param eventId
     *  The ID of the event that the participant is accepting
     * @param userId
     *  The ID of the participant
     * @param onSuccess
     *  Runnable function if the invitation is properly accepted
     * @param onError
     *  Exception if the invitation is not properly accepted
     */
    public void acceptInvitation(String eventId, String userId,
                                 Runnable onSuccess, Consumer<Exception> onError) {
        updateParticipantStatus(eventId, userId, "CONFIRMED")
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Accepted invitation");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Accept failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * Declines the invitation, then invites next participant from the waiting list (called when the decline button from the notification screen is pushed)
     * @param eventId
     *  The ID of the event that the participant is declining
     * @param userId
     *  The ID of the participant
     * @param onSuccess
     *  Runnable function if the invitation is properly declined
     * @param onError
     *  Exception if the invitation is not properly declined
     */
    public void declineInvitation(String eventId, String userId,
                                  Runnable onSuccess, Consumer<Exception> onError) {
        updateParticipantStatus(eventId, userId, "CANCELLED")
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Declined invitation");
                    inviteNextFromWaitingList(eventId, onSuccess, onError);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Decline failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * Invites the next participant from the waiting list, based on the oldest joinedAt
     * @param eventId
     *  The ID of the event
     * @param onSuccess
     *  Runnable function if the invitation is successfully sent
     * @param onError
     *  Exception if the invitation is not properly sent
     */
    public void inviteNextFromWaitingList(String eventId,
                                          Runnable onSuccess,
                                          Consumer<Exception> onError) {
        CollectionReference waitingRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingRef.orderBy("joinedAt", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = query.getDocuments();
                    if (docs.isEmpty()) {
                        Log.d(TAG, "No waiting list entrants");
                        if (onSuccess != null) onSuccess.run();
                        return;
                    }
                    DocumentSnapshot next = docs.get(0);
                    String nextUserId = next.getId(); // waitingList docId == userId (recommended)

                    WriteBatch batch = db.batch();

                    DocumentReference participantRef = db.collection("events")
                            .document(eventId)
                            .collection("participants")
                            .document(nextUserId);

                    Participant invited = new Participant(
                            nextUserId, "SELECTED", Timestamp.now(), Timestamp.now()
                    );

                    batch.set(participantRef, invited, SetOptions.merge());
                    batch.delete(next.getReference()); // remove from waiting list

                    batch.commit()
                            .addOnSuccessListener(bv -> {
                                Log.d(TAG, "Invited next entrant: " + nextUserId);
                                if (onSuccess != null) onSuccess.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed inviting next entrant", e);
                                if (onError != null) onError.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Read waiting list failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    // Listen to participant status for current user to toggle UI

    /**
     * Sets a listener for any changes in status in the database, to update the list in real time
     * @param eventId
     *  The current ID of the event
     * @param userId
     *  The current user ID to track
     * @param onStatus
     *  Collects the status changes
     * @return
     *  Returns a ListenerRegistration that listens for status changes
     */
    public ListenerRegistration listenToParticipantStatus(String eventId, String userId,
                                                          Consumer<String> onStatus) {
        DocumentReference ref = db.collection("events")
                .document(eventId)
                .collection("participants")
                .document(userId);

        return ref.addSnapshotListener((snap, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen error", error);
                return;
            }
            String status = (snap != null && snap.exists()) ? snap.getString("status") : null;
            if (onStatus != null) onStatus.accept(status);
        });
    }

    /**
     * US02.07.01: Notify all waiting list entrants and log notifications.
     * Waiting list entrants are those with status "PENDING" in the participants subcollection.
     */
    public void notifyAllWaitingListEntrants(String eventId,
                                             Consumer<Integer> onComplete,
                                             Consumer<Exception> onError) {

        // Read the event document itself
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "Event " + eventId + " not found when notifying waiting list");
                        if (onComplete != null) onComplete.accept(0);
                        return;
                    }

                    // Waiting list = "entrants" array on the event
                    List<String> entrants = (List<String>) snapshot.get("entrants");
                    if (entrants == null) {
                        entrants = Collections.emptyList();
                    }

                    int count = entrants.size();
                    if (count == 0) {
                        Log.d(TAG, "No entrants to notify for event " + eventId);
                        if (onComplete != null) onComplete.accept(0);
                        return;
                    }

                    // Optional: nice event name in your notification text (for backend use)
                    String eventName = snapshot.getString("name");

                    for (String userId : entrants) {
                        if (userId == null || userId.isEmpty()) continue;

                        // We log a notification for each waiting-list entrant.
                        // Your Cloud Function can watch notificationLogs and send FCM pushes.
                        logNotificationSent(
                                eventId,
                                userId,
                                "WAITING",
                                null,
                                null
                        );
                    }

                    if (onComplete != null) onComplete.accept(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event for waiting list entrants", e);
                });
    }
    /* US02.07.02: Notify all selected entrants and log notifications.
     */
    public void notifyAllSelectedEntrants(String eventId,
                                          Consumer<Integer> onComplete,
                                          Consumer<Exception> onError) {
        CollectionReference participantsRef = db.collection("events")
                .document(eventId)
                .collection("participants");
        participantsRef.whereEqualTo("status", "SELECTED")
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    if (count == 0) {
                        Log.d("EventRepo", "No selected entrants to notify for event " + eventId);
                        if (onComplete != null) onComplete.accept(0);
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getId();
                        sendNotificationToUser(userId,
                                "Congratulations! You have been selected for event " + eventId);
                        // Audit log
                        logNotificationSent(eventId, userId, "SELECTED", null, null);
                    }

                    if (onComplete != null) onComplete.accept(count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Failed to fetch selected entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * US02.07.01: Notify all waiting list entrants and log notifications.
     */
    public void notifyAllWaitingListEntrants(String eventId,
                                             Runnable onSuccess,
                                             Consumer<Exception> onError) {
        CollectionReference participantsRef = db.collection("events")
                .document(eventId)
                .collection("participants");

        participantsRef.whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Log.d("EventRepo", "No waiting list entrants to notify for event " + eventId);
                        if (onSuccess != null) onSuccess.run();
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getId();
                        sendNotificationToUser(userId,
                                "You are on the waiting list for event " + eventId);
                        // Audit log
                        logNotificationSent(eventId, userId, "WAITING", null, null);
                    }

                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Failed to fetch waiting list entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * US02.07.03: Notify all cancelled entrants and log notifications.
     */
    public void notifyAllCancelledEntrants(String eventId,
                                           Runnable onSuccess,
                                           Consumer<Exception> onError) {
        CollectionReference participantsRef = db.collection("events")
                .document(eventId)
                .collection("participants");

        participantsRef.whereEqualTo("status", "CANCELLED")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Log.d("EventRepo", "No cancelled entrants to notify for event " + eventId);
                        if (onSuccess != null) onSuccess.run();
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getId();
                        sendNotificationToUser(userId,
                                "Your participation in event " + eventId + " has been cancelled");
                        // Audit log
                        logNotificationSent(eventId, userId, "CANCELLED", null, null);
                    }

                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Failed to fetch cancelled entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * US03.08.01: Record a notification in an audit trail for admin review.
     * Your Cloud Function can watch this collection and send FCM pushes.
     */
    public void logNotificationSent(String eventId, String userId, String notificationType,
                                    Runnable onSuccess, Consumer<Exception> onError) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("eventId", eventId);
        logEntry.put("userId", userId);
        logEntry.put("notificationType", notificationType);
        logEntry.put("sentAt", Timestamp.now());

        db.collection("notificationLogs")
                .add(logEntry)
                .addOnSuccessListener(docRef -> {
                    Log.d("EventRepo", "Logged notification: " + docRef.getId());
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Failed to log notification", e);
                    if (onError != null) onError.accept(e);
                });
    }


    // Placeholder for actual FCM integration
    private void sendNotificationToUser(String userId, String message) {
        Log.d("EventRepo", "Sending notification to " + userId + ": " + message);
    }

    // Waiting-list location logic  (US 02.02.02 map story)

    /**
     * Add / update an entrant in the waiting list subcollection for an event,
     * including optional latitude/longitude.
     * <p>
     * Path: events/{eventId}/waitingList/{userId}
     */
    public void addEntrantLocationToWaitingList(String eventId,
                                                String entrantId,
                                                Double latitude,
                                                Double longitude) {
        if (eventId == null || entrantId == null) {
            Log.e(TAG, "Null eventId or entrantId");
            return;
        }

        CollectionReference waitingRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", entrantId);
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("joinedAt", Timestamp.now());

        waitingRef.document(entrantId).set(data, SetOptions.merge());
    }

    /**
     * One-shot read of all waiting-list entries (with locations if present)
     * for a given event.
     */
    public void getWaitingListWithLocations(
            String eventId,
            Consumer<List<WaitingListEntry>> onSuccess,
            Consumer<Exception> onError
    ) {
        CollectionReference ref = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        ref.get()
                .addOnSuccessListener(query -> {
                    List<WaitingListEntry> result = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null) {
                            if (entry.getUserId() == null) {
                                entry.setUserId(doc.getId());
                            }
                            result.add(entry);
                        }
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load waiting list", e);
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }
}