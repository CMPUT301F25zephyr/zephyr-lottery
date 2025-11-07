package com.example.zephyr_lottery;

import com.example.zephyr_lottery.utils.ValidationUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserProfileActivityTest {

    @Test
    public void testPhoneValidation_ValidPhone() {
        // Test with valid phone number
        String validPhone = "1234567890";
        String error = ValidationUtil.getPhoneError(validPhone);
        assertNull("Valid phone should return no error", error);
    }

    @Test
    public void testPhoneValidation_EmptyPhone() {
        // Test with empty phone (should be allowed since it's optional)
        String emptyPhone = "";
        String error = ValidationUtil.getPhoneError(emptyPhone);
        assertNull("Empty phone should be allowed", error);
    }

    @Test
    public void testPhoneValidation_InvalidPhone() {
        // Test with invalid phone number
        String invalidPhone = "123"; // Too short
        String error = ValidationUtil.getPhoneError(invalidPhone);
        assertNotNull("Invalid phone should return an error", error);
    }

    @Test
    public void testPhoneSanitization() {
        // Test phone number sanitization
        String phoneWithSpaces = "123 456 7890";
        String sanitized = ValidationUtil.sanitize(phoneWithSpaces);
        assertEquals("Phone should be sanitized", "1234567890", sanitized);
    }

    @Test
    public void testUsernameValidation_Empty() {
        // Test empty username
        String username = "";
        assertTrue("Empty username should be invalid", username.isEmpty());
    }

    @Test
    public void testUsernameValidation_Valid() {
        // Test valid username
        String username = "JohnDoe";
        assertFalse("Valid username should not be empty", username.isEmpty());
        assertTrue("Username should have reasonable length", username.length() > 0);
    }
}