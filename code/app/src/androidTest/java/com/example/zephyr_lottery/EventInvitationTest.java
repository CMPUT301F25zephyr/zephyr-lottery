package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventInvitationTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final EventRepository repo = new EventRepository();

    @Test
    public void testAcceptInvitationUpdatesStatus() throws InterruptedException {
        String eventId = "testEvent_" + System.currentTimeMillis();
        String userId  = "testUser_" + System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(1);

        // Arrange: create a minimal event / participant doc so Firestore path exists
        DocumentReference participantRef = db.collection("events")
                .document(eventId)
                .collection("participants")
                .document(userId);

        Map<String, Object> initialData = new HashMap<>();
        initialData.put("status", "SELECTED");  // before accepting

        participantRef.set(initialData)
                .addOnSuccessListener(v -> {
                    // Act: accept invitation
                    repo.acceptInvitation(eventId, userId,
                            () -> {
                                // Verify: check Firestore document
                                participantRef.get()
                                        .addOnSuccessListener(snapshot -> {
                                            String status = snapshot.getString("status");
                                            assertEquals("accepted", status);
                                            latch.countDown();
                                        })
                                        .addOnFailureListener(e -> {
                                            fail("Firestore read failed: " + e.getMessage());
                                            latch.countDown();
                                        });
                            },
                            e -> {
                                fail("Accept failed: " + e.getMessage());
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    fail("Failed to set up test data: " + e.getMessage());
                    latch.countDown();
                });

        // Wait for async Firestore operations
        assertTrue("Test timed out", latch.await(15, TimeUnit.SECONDS));
    }
}