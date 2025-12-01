package com.example.zephyr_lottery;

import android.content.Intent;

import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zephyr_lottery.activities.EntEventDetailActivity;
import com.example.zephyr_lottery.activities.OrgMyEventDetailsActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

//this file contains tests for:
//US_02.05.03: draw replacement applicant
//US_02.05.02: specify number of entrants to sample in lottery

@RunWith(AndroidJUnit4.class)
public class LotteryTests {
    private FirebaseFirestore db;
    private String test_event_id;

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    //test the lottery drawing US_02.05.02
    @Test
    public void roll_lottery() throws InterruptedException {
        test_event_id = "-654288639"; //the test event
        int test_event_int = -654288639;
        //manually clear lists and
        //manually add leif, staryune, bean to an event. event is only for instrumented tests
        ArrayList<String> waitlist_email = new ArrayList<>();
        waitlist_email.add("leif@gmail.com");
        waitlist_email.add("bean@gmail.com");
        waitlist_email.add("staryune@gmail.com");

        db.collection("events").document(test_event_id)
                .update("entrants_waitlist", waitlist_email);
        db.collection("events").document(test_event_id)
                .update("entrants", new ArrayList<String>());
        db.collection("events").document(test_event_id)
                .update("accepted_entrants", new ArrayList<String>());
        db.collection("events").document(test_event_id)
                .update("rejected_entrants", new ArrayList<String>());
        db.collection("events").document(test_event_id)
                .update("winners", new ArrayList<String>());

        Thread.sleep(3000); //wait until asynch firestore stuff is done.

        //go to this event signed in as the organizer for it
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), OrgMyEventDetailsActivity.class);
        intent.putExtra("USER_EMAIL", "shu@gmail.com");
        intent.putExtra("EVENT_CLICKED_CODE", test_event_int);
        ActivityScenario<OrgMyEventDetailsActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(3000);

        //click the roll button
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.button_draw_lottery).performClick();
        });
        Thread.sleep(2000);

        boolean[] asserts_ran = {false}; //needs to be an array because of lambda expressions

        //check database to see if the right amount of people got in
        DocumentReference docRef = db.collection("events").document(test_event_id);
        docRef.get().addOnSuccessListener(currentEvent -> {

            //after rolling for two winners out of three entrants, we should have: two winners, one in waitlist.
            List<String> entrants_waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            List<String> winner_list = (List<String>) currentEvent.get("winners");

            assert(entrants_waitlist.contains("leif@gmail.com") || entrants_waitlist.contains("staryune@gmail.com") || entrants_waitlist.contains("bean@gmail.com"));
            assert(entrants_waitlist.size() == 1);

            waitlist_email.removeAll(entrants_waitlist); //remove the non winners from original list

            assert(!winner_list.contains(entrants_waitlist.get(0)) && winner_list.containsAll(waitlist_email));
            assert(winner_list.size() == 2);
            asserts_ran[0] = true;
        });
        Thread.sleep(3000); //wait until asynch firestore stuff is done again.
        assertTrue("not enough time for tests to run",asserts_ran[0]);
    }

    //test for US_02.05.03. rerolling for new applicants
    @Test
    public void reroll_lottery() throws InterruptedException {
        test_event_id = "2111027398";
        int test_event_int = 2111027398;

        //manually clear lists and
        //manually add staryune to rejected list, leif to the waitlist, bean not on any list.
        //so leif and staryune registered, staryune won and rejected.
        ArrayList<String> wait_email = new ArrayList<>();
        wait_email.add("leif@gmail.com");
        ArrayList<String> rejected_email = new ArrayList<>();
        rejected_email.add("staryune@gmail.com");

        db.collection("events").document(test_event_id)
                .update("entrants_waitlist", wait_email);
        db.collection("events").document(test_event_id)
                .update("entrants", new ArrayList<String>());
        db.collection("events").document(test_event_id)
                .update("accepted_entrants", new ArrayList<String>());
        db.collection("events").document(test_event_id)
                .update("rejected_entrants", rejected_email);
        db.collection("events").document(test_event_id)
                .update("winners", new ArrayList<String>());

        Thread.sleep(3000); //wait until asynch firestore stuff is done.

        //go to this event signed in as bean.
        Intent intent_ent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntEventDetailActivity.class);
        intent_ent.putExtra("USER_EMAIL", "bean@gmail.com");
        intent_ent.putExtra("EVENT", test_event_id);
        ActivityScenario<EntEventDetailActivity> scenario_ent = ActivityScenario.launch(intent_ent);

        //click the join waitlist button
        scenario_ent.onActivity(activity -> {
            activity.findViewById(R.id.button_register).performClick();
        });
        Thread.sleep(2000);

        //go to this event signed in as the creator and reroll
        Intent intent_org = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), OrgMyEventDetailsActivity.class);
        intent_org.putExtra("USER_EMAIL", "shu@gmail.com");
        intent_org.putExtra("EVENT_CLICKED_CODE", test_event_int);
        ActivityScenario<OrgMyEventDetailsActivity> scenario_org = ActivityScenario.launch(intent_org);

        Thread.sleep(3000);

        //click the draw lottery button again
        scenario_org.onActivity(activity -> {
            activity.findViewById(R.id.button_draw_lottery).performClick();
        });
        Thread.sleep(2000);

        boolean[] asserts_ran = {false};//needs to be an array because of lambda expressions

        //check database to see if the arraylists are correct
        DocumentReference docRef = db.collection("events").document(test_event_id);
        docRef.get().addOnSuccessListener(currentEvent -> {

            //what should have happened is:
            //one of bean or leif made it on winners list. they are not on the waitlist
            //staryune still rejected
            //the one who lost is still on waitlist.
            List<String> entrants_waitlist = (List<String>) currentEvent.get("entrants_waitlist");
            List<String> winner_list = (List<String>) currentEvent.get("winners");
            List<String> rejectd_list = (List<String>) currentEvent.get("rejected_entrants");

            assertTrue("left rejected list?", rejectd_list.contains("staryune@gmail.com") && rejectd_list.size() == 1);

            //the waitlist contains one loser
            assertTrue("not right loser", entrants_waitlist.contains("leif@gmail.com") || entrants_waitlist.contains("bean@gmail.com"));
            assertTrue("loser size", entrants_waitlist.size() == 1);

            String loser_entrant = entrants_waitlist.get(0);
            ArrayList<String> temp_emails = new ArrayList<String>();
            temp_emails.add("bean@gmail.com");
            temp_emails.add("leif@gmail.com");
            temp_emails.remove(loser_entrant);
            String winner_entrant = temp_emails.get(0);

            assertTrue("winner list problem", !winner_list.contains(loser_entrant) && winner_list.contains(winner_entrant));
            assertTrue("winner list size", winner_list.size() == 1);
            asserts_ran[0] = true;
        });
        Thread.sleep(10000); //wait until asynch firestore stuff is done again.
        assertTrue("not enough time for tests to run",asserts_ran[0]);
    }
}
