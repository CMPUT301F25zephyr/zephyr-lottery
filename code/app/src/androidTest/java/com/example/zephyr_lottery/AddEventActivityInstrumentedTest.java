package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zephyr_lottery.activities.AddEventActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test verifying AddEventActivity saves a complete event
 * (including sampleSize) to Firestore, then cleans it up.
 */
@RunWith(AndroidJUnit4.class)
public class AddEventActivityInstrumentedTest {

    private FirebaseFirestore db;
    private String testEventId;

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    @After
    public void cleanup() {
        if (testEventId != null) {
            db.collection("events").document(testEventId).delete();
        }
    }

    @Test
    public void testAddEventSavesWithSampleSizeAndCleansUp() throws InterruptedException {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AddEventActivity.class);
        intent.putExtra("USER_EMAIL", "test_organizer@example.com");

        try (ActivityScenario<AddEventActivity> scenario = ActivityScenario.launch(intent)) {

            scenario.onActivity(activity -> {

                ((EditText) activity.findViewById(R.id.add_event_name)).setText("JUnit Test Event");
                ((EditText) activity.findViewById(R.id.add_event_times)).setText("6pm");
                ((EditText) activity.findViewById(R.id.add_event_location)).setText("Community Hall");
                ((EditText) activity.findViewById(R.id.add_event_price)).setText("15");
                ((EditText) activity.findViewById(R.id.add_event_description)).setText("Automated Firestore save test.");
                ((EditText) activity.findViewById(R.id.add_event_sample_size)).setText("12");

                // Select a weekday in spinner
                Spinner spinner = activity.findViewById(R.id.weekday_spinner);
                spinner.setSelection(2); // e.g., Wednesday

                // Click save
                activity.findViewById(R.id.button_save_event_add_event).performClick();
            });

            // Wait for async Firestore save
            CountDownLatch latch = new CountDownLatch(1);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                db.collection("events")
                        .whereEqualTo("name", "JUnit Test Event")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                                testEventId = doc.getId();

                                // Verify sampleSize was stored
                                Long sampleSize = doc.getLong("sampleSize");
                                assertNotNull("sampleSize field should exist", sampleSize);
                                assertEquals("sampleSize should match input value",
                                        12, sampleSize.intValue());

                                // Verify other fields as well
                                assertEquals("JUnit Test Event", doc.getString("name"));
                                assertEquals("Community Hall", doc.getString("location"));
                                assertEquals("Automated Firestore save test.", doc.getString("description"));
                            } else {
                                fail("Event not found in Firestore after save");
                            }
                            latch.countDown();
                        })
                        .addOnFailureListener(e -> {
                            fail("Firestore query failed: " + e.getMessage());
                            latch.countDown();
                        });
            }, 3000); // allow 3 s for Firestore write to complete

            assertTrue("Timed out waiting for Firestore verification",
                    latch.await(10, TimeUnit.SECONDS));
        }
    }
}
