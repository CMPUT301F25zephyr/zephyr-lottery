package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventRepositoryNotifySelectedTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final EventRepository repo = new EventRepository();

    @Test
    public void testNotifyAllSelectedEntrants() throws InterruptedException {
        String eventId = "testEvent123"; // replace with a real test event ID in Firestore
        CountDownLatch latch = new CountDownLatch(1);

        repo.notifyAllSelectedEntrants(eventId,
                () -> {
                    Log.d("Test", "Notifications sent to selected entrants");
                    latch.countDown();
                },
                e -> {
                    fail("Notification failed: " + e.getMessage());
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }
}