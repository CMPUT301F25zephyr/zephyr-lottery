package com.example.zephyr_lottery.repositories;

import android.util.Log;

import com.example.zephyr_lottery.models.Participant;
import com.example.zephyr_lottery.models.WaitingListEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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

    private static final String TAG = "EventRepo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Changes the status of a specified participant in an event
     *
     * @param eventId The event ID that the participant is in the lottery for
     * @param userId  The participant's ID
     * @param status  The new status of the participant
     * @return Returns a Task when completed asynchronously
     */
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
     *
     * @param eventId   The ID of the event that the participant is accepting
     * @param userId    The ID of the participant
     * @param onSuccess Runnable function if the invitation is properly accepted
     * @param onError   Exception if the invitation is not properly accepted
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
     * Declines the invitation, then invites next participant from the waiting list (called when
     * the decline button from the notification screen is pushed)
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
                    String nextUserId = next.getId(); // waitingList docId == userId

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

    /**
     * Sets a listener for any changes in status in the database, to update the list in real time
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
     * US02.07.01: Notify all waiting list entrants and report how many were notified.
     * (Here we just log and return the count; actual FCM send can be wired in later.)
     */
    public void notifyAllWaitingListEntrants(String eventId,
                                             Consumer<Integer> onComplete,
                                             Consumer<Exception> onError) {

        CollectionReference waitingRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingRef.get()
                .addOnSuccessListener(snapshot -> {
                    int count = snapshot.size();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String userId = doc.getId();
                        Log.d(TAG, "Notify waiting entrant: " + userId);
                        // Hook to your FCM / notification system here if needed
                    }
                    if (onComplete != null) onComplete.accept(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch waiting list entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * US 02.06.01
     * Get all chosen entrants (status == "SELECTED") for a given event.
     * Returns a list of userIds (emails) via onSuccess.
     */
    public void getSelectedEntrants(String eventId,
                                    Consumer<List<String>> onSuccess,
                                    Consumer<Exception> onError) {
        db.collection("events")
                .document(eventId)
                .collection("participants")
                .whereEqualTo("status", "SELECTED")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> selectedEntrants = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        selectedEntrants.add(document.getId());
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(selectedEntrants);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting selected entrants", e);
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

    /**
     * US02.07.02: Notify all selected entrants and log notifications.
     */
    public void notifyAllSelectedEntrants(String eventId,
                                          Consumer<Integer> onComplete,
                                          Consumer<Exception> onError) {

        CollectionReference participantsRef = db.collection("events")
                .document(eventId)
                .collection("participants");

        participantsRef.whereEqualTo("status", "SELECTED")
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    int count = snapshot.size();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String userId = doc.getId();
                        Log.d(TAG, "Notify selected entrant: " + userId);
                        // Hook to your FCM / notification system here if needed
                    }
                    if (onComplete != null) onComplete.accept(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch selected entrants", e);
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * Saves the geographical location of a user who has joined an event's waiting list.
     * Path: /events/{eventId}/waitingList/{userEmail}
     */
    public void addEntrantLocationToWaitingList(String eventId,
                                                String userEmail,
                                                Double lat,
                                                Double lng) {
        if (eventId == null || userEmail == null) {
            Log.e(TAG, "addEntrantLocationToWaitingList: eventId or userEmail is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userEmail);
        data.put("joinedAt", Timestamp.now());

        if (lat != null && lng != null) {
            GeoPoint location = new GeoPoint(lat, lng);
            data.put("location", location);
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(userEmail)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Saved waiting list location for " + userEmail)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save waiting list location", e)
                );
    }

    /**
     * Fetch waiting-list entries for this event that have location information.
     * Returns them as a list of WaitingListEntry objects via onSuccess.
     */
    public void getWaitingListWithLocations(String eventId,
                                            Consumer<List<WaitingListEntry>> onSuccess,
                                            Consumer<Exception> onError) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WaitingListEntry> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null) {
                            Double lat = entry.getLatitude();
                            Double lng = entry.getLongitude();
                            if (lat != null && lng != null) {
                                entries.add(entry);
                            }
                        }
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(entries);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch waiting list locations", e);
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }
}
