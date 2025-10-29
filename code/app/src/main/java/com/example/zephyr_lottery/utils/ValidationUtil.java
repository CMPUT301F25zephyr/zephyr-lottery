package com.example.zephyr_lottery.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Utility class for validating user input
 * Provides validation methods for email, phone, names, etc.
 */
public class ValidationUtil {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;

    // Phone number patterns (supports various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,13}$"
    );

    // Name pattern (allows letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s'-]{2,50}$"
    );

    /**
     * Validate email address
     *
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number
     * Accepts formats like: 1234567890, +11234567890, etc.
     *
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }

        // Remove spaces, dashes, parentheses
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate name
     * Must be 2-50 characters, letters only with spaces, hyphens, apostrophes
     *
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate that a string is not empty
     *
     * @param value String to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate string length
     *
     * @param value String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if length is within bounds, false otherwise
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Get email validation error message
     *
     * @param email Email to validate
     * @return Error message or null if valid
     */
    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        return null;
    }

    /**
     * Get name validation error message
     *
     * @param name Name to validate
     * @return Error message or null if valid
     */
    public static String getNameError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (name.trim().length() < 2) {
            return "Name must be at least 2 characters";
        }
        if (name.trim().length() > 50) {
            return "Name must be less than 50 characters";
        }
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }
        return null;
    }

    /**
     * Get phone validation error message
     *
     * @param phone Phone to validate
     * @return Error message or null if valid
     */
    public static String getPhoneError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Phone is optional
        }

        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return "Please enter a valid phone number (10-13 digits)";
        }
        return null;
    }

    /**
     * Validate all user profile fields
     *
     * @param name User's name
     * @param email User's email
     * @param phone User's phone (optional)
     * @return Error message or null if all valid
     */
    public static String validateUserProfile(String name, String email, String phone) {
        String nameError = getNameError(name);
        if (nameError != null) {
            return nameError;
        }

        String emailError = getEmailError(email);
        if (emailError != null) {
            return emailError;
        }

        String phoneError = getPhoneError(phone);
        if (phoneError != null) {
            return phoneError;
        }

        return null; // All valid
    }

    /**
     * Format phone number for display
     * Converts 1234567890 to (123) 456-7890
     *
     * @param phone Raw phone number
     * @return Formatted phone number
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }

        // Remove all non-digit characters
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        // Format based on length
        if (cleanPhone.length() == 10) {
            return String.format("(%s) %s-%s",
                    cleanPhone.substring(0, 3),
                    cleanPhone.substring(3, 6),
                    cleanPhone.substring(6, 10));
        } else if (cleanPhone.length() == 11 && cleanPhone.startsWith("1")) {
            return String.format("+1 (%s) %s-%s",
                    cleanPhone.substring(1, 4),
                    cleanPhone.substring(4, 7),
                    cleanPhone.substring(7, 11));
        }

        return phone; // Return original if can't format
    }

    /**
     * Sanitize input string (remove leading/trailing spaces)
     *
     * @param input Input string
     * @return Sanitized string
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}