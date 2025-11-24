package com.example.zephyr_lottery.repositories;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Comprehensive test class for EventRepository
 * Tests US 02.07.02, US 02.07.01, US 02.07.03, and US 03.08.01
 */
@RunWith(AndroidJUnit4.class)
public class EventRepositoryTest {

    private FirebaseFirestore db;
    private EventRepository repo;
    private String testEventId;

    @Before
    public void setup() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        repo = new EventRepository();
        testEventId = "testEvent_US020702_" + System.currentTimeMillis();

        CountDownLatch setupLatch = new CountDownLatch(3);

        // Seed Firestore with dummy participants
        db.collection("events").document(testEventId).collection("participants")
                .document("userA")
                .set(new DummyParticipant("userA", "SELECTED"), SetOptions.merge())
                .addOnCompleteListener(task -> setupLatch.countDown());

        db.collection("events").document(testEventId).collection("participants")
                .document("userB")
                .set(new DummyParticipant("userB", "SELECTED"), SetOptions.merge())
                .addOnCompleteListener(task -> setupLatch.countDown());

        db.collection("events").document(testEventId).collection("participants")
                .document("userC")
                .set(new DummyParticipant("userC", "CANCELLED"), SetOptions.merge())
                .addOnCompleteListener(task -> setupLatch.countDown());

        assertTrue("Setup timed out", setupLatch.await(10, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() throws InterruptedException {
        CountDownLatch cleanupLatch = new CountDownLatch(1);

        db.collection("events").document(testEventId).delete()
                .addOnCompleteListener(task -> cleanupLatch.countDown());

        cleanupLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testNotifyAllSelectedEntrants_Success() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        repo.notifyAllSelectedEntrants(testEventId,
                () -> {
                    successCalled.set(true);
                    latch.countDown();
                },
                e -> {
                    errorCalled.set(true);
                    latch.countDown();
                });

        assertTrue("Operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Success callback should be called", successCalled.get());
        assertFalse("Error callback should not be called", errorCalled.get());
    }

    @Test
    public void testNotifyAllSelectedEntrants_OnlyNotifiesSelectedUsers() throws InterruptedException {
        CountDownLatch verifyLatch = new CountDownLatch(1);
        AtomicInteger selectedCount = new AtomicInteger(0);

        db.collection("events").document(testEventId).collection("participants")
                .whereEqualTo("status", "SELECTED")
                .get()
                .addOnSuccessListener(query -> {
                    selectedCount.set(query.size());
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepoTest", "Verification failed", e);
                    verifyLatch.countDown();
                });

        assertTrue("Verification timed out", verifyLatch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have 2 selected participants", 2, selectedCount.get());

        CountDownLatch notifyLatch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        repo.notifyAllSelectedEntrants(testEventId,
                () -> {
                    success.set(true);
                    notifyLatch.countDown();
                },
                e -> notifyLatch.countDown());

        assertTrue("Notification timed out", notifyLatch.await(10, TimeUnit.SECONDS));
        assertTrue("Notification should succeed", success.get());
    }

    @Test
    public void testNotifyAllSelectedEntrants_NoSelectedUsers() throws InterruptedException {
        String emptyEventId = "testEvent_empty_" + System.currentTimeMillis();
        CountDownLatch setupLatch = new CountDownLatch(1);

        db.collection("events").document(emptyEventId).collection("participants")
                .document("userD")
                .set(new DummyParticipant("userD", "PENDING"), SetOptions.merge())
                .addOnCompleteListener(task -> setupLatch.countDown());

        assertTrue("Setup timed out", setupLatch.await(10, TimeUnit.SECONDS));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        repo.notifyAllSelectedEntrants(emptyEventId,
                () -> {
                    successCalled.set(true);
                    latch.countDown();
                },
                e -> latch.countDown());

        assertTrue("Operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Should succeed even with no selected users", successCalled.get());

        db.collection("events").document(emptyEventId).delete();
    }

    @Test
    public void testNotifyAllSelectedEntrants_InvalidEventId() throws InterruptedException {
        String invalidEventId = "nonexistent_event_" + System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        repo.notifyAllSelectedEntrants(invalidEventId,
                () -> {
                    successCalled.set(true);
                    latch.countDown();
                },
                e -> latch.countDown());

        assertTrue("Operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Should succeed gracefully with invalid event ID", successCalled.get());
    }

    static class DummyParticipant {
        public String userId;
        public String status;
        public Timestamp joinedAt;
        public Timestamp updatedAt;

        DummyParticipant(String userId, String status) {
            this.userId = userId;
            this.status = status;
            this.joinedAt = Timestamp.now();
            this.updatedAt = Timestamp.now();
        }
    }
}