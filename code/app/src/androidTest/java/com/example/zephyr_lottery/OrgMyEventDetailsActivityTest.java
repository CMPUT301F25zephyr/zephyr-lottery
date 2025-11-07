package com.example.zephyr_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zephyr_lottery.activities.OrgMyEventDetailsActivity;
import com.example.zephyr_lottery.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrgMyEventDetailsActivityTest {

    @Before
    public void launchActivity() {
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(),
                OrgMyEventDetailsActivity.class
        );
        intent.putExtra("USER_EMAIL", "test@organizer.com");
        intent.putExtra("EVENT_CLICKED_CODE", 12345);
        ActivityScenario.launch(intent);
    }

    @Test
    public void recyclerView_isDisplayed() {
        // Check that the RecyclerView for invited entrants is visible
        onView(withId(R.id.recycler_invited_entrants))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invitedEntrantItem_isDisplayed() {
        // Assuming Firestore has at least one invited entrant for event 12345
        // Check that a participant row with "Status: invited" is shown
        onView(withText("Status: invited"))
                .check(matches(isDisplayed()));
    }
}