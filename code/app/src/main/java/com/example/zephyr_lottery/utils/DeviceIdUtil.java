//package com.example.zephyr_lottery.utils;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.provider.Settings;
//
//import java.util.UUID;
//
///**
// * Utility class for managing device identification
// *
// * Implements:
// * - US 01.07.01: Device-based identification without username/password
// *
// * This class generates and retrieves a unique device ID that persists
// * across app sessions and is used as the user's primary identifier.
// */
//public class DeviceIdUtil {
//
//    private static final String PREFS_NAME = "ZephyrLotteryPrefs";
//    private static final String KEY_DEVICE_ID = "device_id";
//    private static final String KEY_USER_ID = "user_id";
//
//    /**
//     * Get the unique device ID for this device
//     * If no ID exists, creates a new one
//     *
//     * @param context Application context
//     * @return Unique device identifier
//     */
//    public static String getDeviceId(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//
//        // Check if device ID already exists
//        String deviceId = prefs.getString(KEY_DEVICE_ID, null);
//
//        if (deviceId == null) {
//            // Try to get Android ID first
//            deviceId = getAndroidId(context);
//
//            // If Android ID is not available, generate UUID
//            if (deviceId == null || deviceId.isEmpty()) {
//                deviceId = generateUUID();
//            }
//
//            // Save the device ID for future use
//            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
//        }
//
//        return deviceId;
//    }
//
//    /**
//     * Get Android ID (unique to device and app installation)
//     *
//     * @param context Application context
//     * @return Android ID or null if not available
//     */
//    private static String getAndroidId(Context context) {
//        try {
//            String androidId = Settings.Secure.getString(
//                    context.getContentResolver(),
//                    Settings.Secure.ANDROID_ID
//            );
//
//            // Check if Android ID is valid
//            if (androidId != null && !androidId.isEmpty() &&
//                    !"9774d56d682e549c".equals(androidId)) { // Known bad ID on some emulators
//                return androidId;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * Generate a UUID-based device ID
//     *
//     * @return UUID string
//     */
//    private static String generateUUID() {
//        return UUID.randomUUID().toString();
//    }
//
//    /**
//     * Save user ID to shared preferences
//     *
//     * @param context Application context
//     * @param userId User ID to save
//     */
//    public static void saveUserId(Context context, String userId) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        prefs.edit().putString(KEY_USER_ID, userId).apply();
//    }
//
//    /**
//     * Get saved user ID from shared preferences
//     *
//     * @param context Application context
//     * @return Saved user ID or null if not found
//     */
//    public static String getSavedUserId(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        return prefs.getString(KEY_USER_ID, null);
//    }
//
//    /**
//     * Clear saved user ID (for logout)
//     *
//     * @param context Application context
//     */
//    public static void clearUserId(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        prefs.edit().remove(KEY_USER_ID).apply();
//    }
//
//    /**
//     * Clear all stored data
//     *
//     * @param context Application context
//     */
//    public static void clearAll(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        prefs.edit().clear().apply();
//    }
//
//    /**
//     * Check if user is logged in (has saved user ID)
//     *
//     * @param context Application context
//     * @return true if user ID exists, false otherwise
//     */
//    public static boolean isUserLoggedIn(Context context) {
//        String userId = getSavedUserId(context);
//        return userId != null && !userId.isEmpty();
//    }
//}