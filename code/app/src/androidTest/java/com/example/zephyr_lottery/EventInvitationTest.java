package com.example.zephyr_lottery;

import static org.junit.Assert.*;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zephyr_lottery.activities.EntEventDetailActivity;
import com.example.zephyr_lottery.activities.EventInvitationActivity;
import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventInvitationTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Test ability to accept invitation (US 01.05.02)
    @Test
    public void testAcceptInvitationUpdatesStatus() throws InterruptedException {
        DocumentReference docRef = db.collection("events").document("EventInvitationTest");
        Event event = new Event("EventInvitationTest", "1am", "joshua@gmail.com");
        ArrayList<String> testEntrants = new ArrayList<>();
        testEntrants.add("entrant@gmail.com");
        event.setEntrants_waitlist(testEntrants);
        event.setWinners(testEntrants);
        docRef.set(event);

        Thread.sleep(3000);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventInvitationActivity.class);
        intent.putExtra("USER_EMAIL", "entrant@gmail.com");
        intent.putExtra("EVENT_CODE", "EventInvitationTest"); // Previously created event

        ActivityScenario<EventInvitationActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.button_accept_invitation).performClick();
        });

        Thread.sleep(3000);

        docRef.get().addOnSuccessListener(currentEvent -> {
            List<String> waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            assert(!waitlist.contains("entrant@gmail.com"));
            List<String> winners = (List<String>) currentEvent.get("winners");
            assert(!winners.contains("entrant@gmail.com"));
            List<String> acceptedList = (List<String>) currentEvent.get("accepted_entrants");
            assert(acceptedList.contains("entrant@gmail.com"));
        });

        Thread.sleep(3000);
    }

    // Tests ability to decline invitation (US 01.05.03)
    @Test
    public void testDeclineInvitationUpdatesStatus() throws InterruptedException {
        DocumentReference docRef = db.collection("events").document("EventInvitationTest");
        Event event = new Event("EventInvitationTest", "1am", "joshua@gmail.com");
        ArrayList<String> testEntrants = new ArrayList<>();
        testEntrants.add("entrant@gmail.com");
        event.setEntrants_waitlist(testEntrants);
        event.setWinners(testEntrants);
        docRef.set(event);

        Thread.sleep(3000);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventInvitationActivity.class);
        intent.putExtra("USER_EMAIL", "entrant@gmail.com");
        intent.putExtra("EVENT_CODE", "EventInvitationTest"); // Previously created event

        ActivityScenario<EventInvitationActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.button_reject_invitation).performClick();
        });

        Thread.sleep(3000);

        docRef.get().addOnSuccessListener(currentEvent -> {
            List<String> waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            //assert(!waitlist.contains("entrant@gmail.com"));
            List<String> winners = (List<String>) currentEvent.get("winners");
            //assert(!winners.contains("entrant@gmail.com"));
            List<String> rejectedList = (List<String>) currentEvent.get("rejected_entrants");
            assert(rejectedList.contains("entrant@gmail.com"));
        });

        Thread.sleep(3000);
    }
}