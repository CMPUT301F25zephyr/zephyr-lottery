package com.example.zephyr_lottery;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.activities.LoginActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeEntActivityInstrumentedTest {

    // Test credentials
    private static final String TEST_EMAIL = "test_huy@test.com";
    private static final String TEST_PASSWORD = "12345678";

    @Before
    public void setUp() throws InterruptedException {
        // Launch the sign-in activity
        ActivityScenario.launch(LoginActivity.class);

        // Login before each test
        onView(withId(R.id.signin_email))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());

        onView(withId(R.id.signin_pass))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.button_signin))
                .perform(click());

        // Wait for login to complete and navigate to home
        Thread.sleep(3000);
    }

    @Test
    public void testUIElementsDisplayed() {
        // Check if all main UI elements are displayed
        onView(withId(R.id.tvEntrantGreeting)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLatestEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.btnEditProfile)).check(matches(isDisplayed()));
        onView(withId(R.id.btnScanQR)).check(matches(isDisplayed()));
    }

    @Test
    public void testGreetingTextDisplayed() {
        // Verify greeting text contains "Hello"
        onView(withId(R.id.tvEntrantGreeting))
                .check(matches(withText(containsString("Greetings"))));
    }

    @Test
    public void testLatestEventsButtonDisplayed() {
        // Check if "See Latest Events" button is visible
        onView(withId(R.id.btnLatestEvents))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testHistoryButtonDisplayed() {
        // Check if "See History" button is visible
        onView(withId(R.id.btnHistory))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfileButtonDisplayed() {
        // Check if "Edit Profile" button is visible
        onView(withId(R.id.btnEditProfile))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testScanQRButtonDisplayed() {
        // Check if "Scan QR" button is visible
        onView(withId(R.id.btnScanQR))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLatestEventsButtonClick() {
        // Test clicking Latest Events button
        onView(withId(R.id.btnLatestEvents)).perform(click());
        // Should show toast (you'll see it on the screen)
    }

    @Test
    public void testHistoryButtonClick() {
        // Test clicking History button
        onView(withId(R.id.btnHistory)).perform(click());
        // Should show toast
    }

    @Test
    public void testEditProfileButtonNavigation() {
        // Test clicking Edit Profile button navigates to UserProfileActivity
        onView(withId(R.id.btnEditProfile)).perform(click());

        // After clicking, UserProfileActivity should open
        // We can verify by checking if UserProfileActivity's title is displayed
        onView(withId(R.id.tvTitle))
                .check(matches(isDisplayed()))
                .check(matches(withText("User Profile")));
    }

    @Test
    public void testScanQRButtonClick() {
        // Test clicking Scan QR button
        onView(withId(R.id.btnScanQR)).perform(click());
        // Should show toast
    }

    @Test
    public void testAllButtonsAreClickable() {
        // Verify all buttons can be clicked
        onView(withId(R.id.btnLatestEvents)).perform(click());
        onView(withId(R.id.btnHistory)).perform(click());
        onView(withId(R.id.btnEditProfile)).perform(click());

        // Go back to test scan button
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.btnScanQR)).perform(click());
    }
}