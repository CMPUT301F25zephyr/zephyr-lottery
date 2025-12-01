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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    // Tests account creation of organizer (no US, but important)
    @Test
    public void addAccountOrg() throws InterruptedException{
        String unique_email = "sofia" + System.currentTimeMillis() + "@m.com";

        //Type Sofia in the username edittext
        onView(withId(R.id.signup_user)).perform(ViewActions.typeText("Sofia"));

        //Type email in email text
        onView(withId(R.id.signup_email)).perform(ViewActions.typeText(unique_email));

        //Type donjons in password text
        onView(withId(R.id.signup_pass)).perform(ViewActions.typeText("donjons"));

        //select organizer from the spinner
        onView(withId(R.id.account_type_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Organizer"))).perform(click());

        //Click on Confirm
        onView(withId(R.id.button_signup)).perform(click());

        //sign in
        onView(withId(R.id.signin_email)).perform(ViewActions.typeText(unique_email));
        onView(withId(R.id.signin_pass)).perform(ViewActions.typeText("donjons"));
        onView(withId(R.id.button_signin)).perform(click());
        Thread.sleep(3000);

        //check that we are signed into an organizer account
        onView(withId(R.id.textView_orgHome)).check(matches(isDisplayed()));
    }

    // Tests creating an Entrant account (US 01.02.01)
    @Test
    public void addAccountEnt() throws InterruptedException{
        String unique_email = "marielle" + System.currentTimeMillis() + "@m.com";

        //Type marielle in the username edittext
        onView(withId(R.id.signup_user)).perform(ViewActions.typeText("Marielle"));

        //Type email in email text
        onView(withId(R.id.signup_email)).perform(ViewActions.typeText(unique_email));

        //Type donjons in password text
        onView(withId(R.id.signup_pass)).perform(ViewActions.typeText("donjons"));

        //entrant is already selected in spinner

        //Click on Confirm
        onView(withId(R.id.button_signup)).perform(click());

        //sign in
        onView(withId(R.id.signin_email)).perform(ViewActions.typeText(unique_email));
        onView(withId(R.id.signin_pass)).perform(ViewActions.typeText("donjons"));
        onView(withId(R.id.button_signin)).perform(click());
        Thread.sleep(3000);

        //check that we are signed into an entrant account
        onView(withId(R.id.btnLatestEvents)).check(matches(isDisplayed()));
    }
}