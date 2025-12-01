package com.example.zephyr_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.example.zephyr_lottery.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// Tests event creation blackbox (US 02.02.01)
@RunWith(AndroidJUnit4.class)
public class MakeEventTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    @Test
    public void addEventOrg() throws InterruptedException {
        //sign in
        onView(withId(R.id.signin_email)).perform(ViewActions.typeText("joshua@gmail.com"));
        onView(withId(R.id.signin_pass)).perform(ViewActions.typeText("password"));
        onView(withId(R.id.button_signin)).perform(click());
        Thread.sleep(3000);

        //go to create event screen
        onView(withId(R.id.button_my_events)).perform(click());
        onView(withId(R.id.button_my_event_add_event)).perform(click());
        Thread.sleep(1000);

        //fill in form
        String unique_event = "Test Event " + System.currentTimeMillis();
        onView(withId(R.id.add_event_name)).perform(ViewActions.typeText(unique_event));

        //select from the spinner
        onView(withId(R.id.weekday_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Tuesday"))).perform(click());

        onView(withId(R.id.add_event_times)).perform(ViewActions.typeText("7am"));
        onView(withId(R.id.add_event_location)).perform(ViewActions.typeText("CCIS"));
        onView(withId(R.id.add_event_price)).perform(ViewActions.typeText("89.99"));
        onView(withId(R.id.add_event_description)).perform(ViewActions.typeText("come and do event stuff"));

        //save event
        onView(withId(R.id.button_save_event_add_event)).perform(click());
        Thread.sleep(2000);

        onView(withText(unique_event)).check(matches(isDisplayed()));
    }
}
