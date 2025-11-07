package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventRepositoryNotifyTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final EventRepository repo = new EventRepository();

    @Test
    public void testNotifyAllWaitingList() throws InterruptedException {
        String eventId = "testEvent123"; // use a test event in Firestore
        String userId = "testUser123";   // dummy user
        CountDownLatch latch = new CountDownLatch(1);

        // Seed Firestore with a waiting list entrant
        Map<String, Object> waitingEntrant = new HashMap<>();
        waitingEntrant.put("joinedAt", Timestamp.now());

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(waitingEntrant)
                .addOnSuccessListener(v -> {
                    // Act: call notifyAllWaitingList
                    repo.notifyAllWaitingList(eventId,
                            () -> {
                                Log.d("Test", "Notifications sent successfully");
                                latch.countDown();
                            },
                            e -> {
                                fail("Notification failed: " + e.getMessage());
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    fail("Failed to seed waiting list: " + e.getMessage());
                    latch.countDown();
                });

        // Wait for async Firestore operations
        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }
}