package com.example.zephyr_lottery.repositories;

import android.util.Log;

import com.example.zephyr_lottery.models.WaitingListEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.example.zephyr_lottery.models.Participant;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.List;
import java.util.function.Consumer;

public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Update participant status (accepted/declined)
    public Task<Void> updateParticipantStatus(String eventId, String userId, String status) {
        DocumentReference participantRef = db.collection("events")
                .document(eventId)
                .collection("participants")
                .document(userId);

        Participant p = new Participant(userId, status, null, Timestamp.now());
        return participantRef.set(p, SetOptions.merge());
    }

    // Accept invitation
    public void acceptInvitation(String eventId, String userId,
                                 Runnable onSuccess, Consumer<Exception> onError) {
        updateParticipantStatus(eventId, userId, "accepted")
                .addOnSuccessListener(v -> {
                    Log.d("EventRepo", "Accepted invitation");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Accept failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    // Decline invitation, then invite next from waiting list
    public void declineInvitation(String eventId, String userId,
                                  Runnable onSuccess, Consumer<Exception> onError) {
        updateParticipantStatus(eventId, userId, "declined")
                .addOnSuccessListener(v -> {
                    Log.d("EventRepo", "Declined invitation");
                    inviteNextFromWaitingList(eventId, onSuccess, onError);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Decline failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    // Invite next waiting list entrant (FIFO by joinedAt)
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
                            nextUserId, "invited", Timestamp.now(), Timestamp.now()
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
    /**
     * Add / update an entrant in the waiting list subcollection for an event,
     * including optional latitude/longitude.
     *
     * Path: events/{eventId}/waitingList/{userId}
     */
    public void addEntrantLocationToWaitingList(String eventId,
                                                String entrantId,
                                                Double latitude,
                                                Double longitude) {
        if (eventId == null || entrantId == null) {
            Log.e("EventRepository",
                    "addEntrantLocationToWaitingList: eventId or entrantId is null. " +
                            "eventId=" + eventId + ", entrantId=" + entrantId);
            return; // avoid crashing Firestore when path is null
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
                            // Ensure userId is set even if not stored explicitly
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
                    Log.e("EventRepo", "Failed to load waiting list", e);
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

}