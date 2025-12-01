package com.example.zephyr_lottery;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zephyr_lottery.activities.EntEventDetailActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class EventJoinLeaveTests {
    private FirebaseFirestore db;

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    // Tests if user can successfully join event (US 01.01.01)
    @Test
    public void testJoinEvent() throws InterruptedException {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntEventDetailActivity.class);
        intent.putExtra("USER_EMAIL", "entrant@gmail.com");
        intent.putExtra("EVENT", "-1691473109"); // Event with many open spots

        ActivityScenario<EntEventDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.button_register).performClick();
        });

        Thread.sleep(3000);

        DocumentReference docRef = db.collection("events").document("-1691473109");
        docRef.get().addOnSuccessListener(currentEvent -> {
            List<String> entrantsList = (List<String>) currentEvent.get("entrants");
            assert(entrantsList.contains("entrant@gmail.com"));
        });
    }

    // Tests if user can successfully leave event (US 01.01.02)
    @Test
    public void testLeaveEvent() throws InterruptedException {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntEventDetailActivity.class);
        intent.putExtra("USER_EMAIL", "entrant@gmail.com");
        intent.putExtra("EVENT", "-1691473109"); // Event with many open spots

        ActivityScenario<EntEventDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.button_register).performClick();
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e){
                Log.e("test", "Interrupted Exception");
            }
            activity.findViewById(R.id.button_leave_waitlist).performClick();
        });

        Thread.sleep(3000);

        DocumentReference docRef = db.collection("events").document("-1691473109");
        docRef.get().addOnSuccessListener(currentEvent -> {
            List<String> entrantsList = (List<String>) currentEvent.get("entrants");
            assert(!entrantsList.contains("entrant@gmail.com"));
        });
    }

    // Tests that app will restrict ability to join event if full (US 02.03.01)
    @Test
    public void testJoinFullEvent() throws InterruptedException {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntEventDetailActivity.class);
        intent.putExtra("USER_EMAIL", "entrant@gmail.com");
        intent.putExtra("EVENT", "1574958102"); // Full event

        ActivityScenario<EntEventDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
           activity.findViewById(R.id.button_register).performClick();
        });

        Thread.sleep(3000);

        DocumentReference docRef = db.collection("events").document("1574958102");
        docRef.get().addOnSuccessListener(currentEvent -> {
            List<String> entrantsList = (List<String>) currentEvent.get("entrants");
            for (int i = 0; i < entrantsList.size(); i++) {
                assert(!(entrantsList.get(i).equals("entrant@gmail.com")));
            }
        });
    }
}
