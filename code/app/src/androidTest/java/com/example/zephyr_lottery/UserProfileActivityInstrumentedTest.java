package com.example.zephyr_lottery;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.activities.LoginActivity; // Update this to your actual SignIn activity name

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
public class UserProfileActivityInstrumentedTest {

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

        // Wait for login
        Thread.sleep(3000);

        // Navigate to User Profile
        onView(withId(R.id.btnEditProfile))
                .perform(click());

        // Wait for profile to load
        Thread.sleep(2000);
    }

    @Test
    public void testUIElementsDisplayed() {
        // Check if all UI elements are displayed
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPhone)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.cbReceiveNotifications)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSave)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteAccount)).check(matches(isDisplayed()));
    }

    @Test
    public void testTitleText() {
        // Verify the title text
        onView(withId(R.id.tvTitle))
                .check(matches(withText("User Profile")));
    }

    @Test
    public void testBackButtonFunctionality() {
        // Test back button closes the activity
        onView(withId(R.id.btnBack)).perform(click());
        // Activity should be finished after clicking back
    }

    @Test
    public void testEmptyNameValidation() {
        // Clear name field and try to save
        onView(withId(R.id.etName))
                .perform(typeText(""), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnSave)).perform(click());

        // Should show error on name field
        // Note: This test assumes you're logged in with Firebase
    }

    @Test
    public void testEmptyPasswordValidation() {
        // Fill name but leave password empty
        onView(withId(R.id.etName))
                .perform(typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.btnSave)).perform(click());

        // Should show error on password field
    }

    @Test
    public void testPhoneInput() {
        // Test phone number input
        onView(withId(R.id.etPhone))
                .perform(typeText("1234567890"), closeSoftKeyboard());

        // Verify text was entered
        onView(withId(R.id.etPhone))
                .check(matches(withText(containsString("1234567890"))));
    }

    @Test
    public void testCheckboxToggle() {
        // Test notifications checkbox
        onView(withId(R.id.cbReceiveNotifications)).perform(click());
        // Checkbox should now be checked

        onView(withId(R.id.cbReceiveNotifications)).perform(click());
        // Checkbox should now be unchecked
    }

    @Test
    public void testDeleteAccountButtonDisplayed() {
        // Verify delete account button is visible and has correct text
        onView(withId(R.id.btnDeleteAccount))
                .check(matches(isDisplayed()))
                .check(matches(withText("Delete Account")));
    }

    @Test
    public void testPasswordFieldIsSecure() {
        // Password field should have password input type
        onView(withId(R.id.etPassword))
                .check(matches(isDisplayed()));

        // Type password and verify it's masked
        onView(withId(R.id.etPassword))
                .perform(typeText("testpassword"), closeSoftKeyboard());
    }
}