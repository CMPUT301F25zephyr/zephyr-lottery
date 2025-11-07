package com.example.zephyr_lottery.events;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import com.example.zephyr_lottery.location.LocationService;
import com.example.zephyr_lottery.Event;

public class EventManager {
    private static final String TAG = "EventManager";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface JoinCallback { void onComplete(boolean success, String message); }

    public static void joinWaitingList(final String eventId, final String userId,
                                       final Double userLat, final Double userLng,
                                       final JoinCallback callback) {
        final DocumentReference docRef = db.collection("events").document(eventId);

        docRef.get().addOnSuccessListener(ds -> {
            if (!ds.exists()) { callback.onComplete(false, "Event not found"); return; }
            Event ev = ds.toObject(Event.class);
            if (ev == null) { callback.onComplete(false, "Event parse error"); return; }

            // geofence check
            if (ev.getGeofence() != null) {
                Object latObj = ev.getGeofence().get("lat");
                Object lngObj = ev.getGeofence().get("lng");
                Object radObj = ev.getGeofence().get("radiusMeters");
                if (latObj instanceof Number && lngObj instanceof Number && radObj instanceof Number) {
                    double fenceLat = ((Number) latObj).doubleValue();
                    double fenceLng = ((Number) lngObj).doubleValue();
                    double radius = ((Number) radObj).doubleValue();

                    if (userLat == null || userLng == null) {
                        callback.onComplete(false, "Location required to join this event");
                        return;
                    }
                    boolean inside = LocationService.isWithin(userLat, userLng, fenceLat, fenceLng, radius);
                    if (!inside) { callback.onComplete(false, "You are not within required area"); return; }
                }
            }

            // atomic add using arrayUnion
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("waitingList", FieldValue.arrayUnion(userId));
                        transaction.update(docRef, updates);
                        return null;
                    }).addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "join failed", e);
                        callback.onComplete(false, e.getLocalizedMessage());
                    });

        }).addOnFailureListener(e -> callback.onComplete(false, e.getLocalizedMessage()));
    }
}
