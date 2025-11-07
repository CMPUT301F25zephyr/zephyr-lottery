package com.example.zephyr_lottery.repositories;

import android.util.Log;

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
                    String nextUserId = next.getId(); // waitingList docId == userId

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

    // NEW: Notify all selected entrants (US02.07.02)
    public void notifyAllSelectedEntrants(String eventId,
                                          Runnable onSuccess,
                                          Consumer<Exception> onError) {
        CollectionReference participantsRef = db.collection("events")
                .document(eventId)
                .collection("participants");

        participantsRef.whereEqualTo("status", "accepted") // filter by accepted/selected
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Log.d("EventRepo", "No selected entrants to notify");
                        if (onSuccess != null) onSuccess.run();
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getId();
                        sendNotificationToUser(userId,
                                "Congratulations! You have been selected for event " + eventId);
                    }

                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepo", "Failed to fetch selected entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    // Placeholder for actual FCM integration
    private void sendNotificationToUser(String userId, String message) {
        Log.d("EventRepo", "Sending notification to " + userId + ": " + message);
    }
}