package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventInvitationTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final EventRepository repo = new EventRepository();

    @Test
    public void testAcceptInvitationUpdatesStatus() throws InterruptedException {
        String eventId = "testEvent123";   // use a test event in Firestore
        String userId = "testUser123";     // use a test user

        CountDownLatch latch = new CountDownLatch(1);

        // Act: accept invitation
        repo.acceptInvitation(eventId, userId,
                () -> {
                    // Verify: check Firestore document
                    db.collection("events").document(eventId)
                            .collection("participants").document(userId)
                            .get()
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

        // Wait for async Firestore operations
        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }
}