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

/**
 * This class manages the statuses of participants and the accepting/declining of invitations.
 */
public class EventRepository {

    private static final String TAG = "EventRepository";

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
                    Log.d("EventRepo", "Accepted invitation");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Accept failed", e);
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
                    Log.d("EventRepo", "Declined invitation");
                    inviteNextFromWaitingList(eventId, onSuccess, onError);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Decline failed", e);
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
                        Log.d("EventRepo", "No waiting list entrants");
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
                                Log.d("EventRepo", "Invited next entrant: " + nextUserId);
                                if (onSuccess != null) onSuccess.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("EventRepo", "Failed inviting next entrant", e);
                                if (onError != null) onError.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Read waiting list failed", e);
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
                Log.e("EventRepo", "Listen error", error);
                return;
            }
            String status = (snap != null && snap.exists()) ? snap.getString("status") : null;
            if (onStatus != null) onStatus.accept(status);
        });
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