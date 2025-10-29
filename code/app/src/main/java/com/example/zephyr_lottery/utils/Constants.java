package com.example.zephyr_lottery.utils;

/**
 * Constants class for application-wide constants
 * Centralizes all constant values in one place
 */
public class Constants {

    // ==================== Firebase Collections ====================

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ADMINS = "admins";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_WAITING_LISTS = "waitingLists";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_IMAGES = "images";
    public static final String COLLECTION_LOGS = "logs";

    // ==================== Firebase Storage Paths ====================

    public static final String STORAGE_PROFILE_IMAGES = "profile_images/";
    public static final String STORAGE_EVENT_POSTERS = "event_posters/";

    // ==================== Shared Preferences Keys ====================

    public static final String PREFS_NAME = "ZephyrLotteryPrefs";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_ADMIN_ID = "admin_id";
    public static final String KEY_IS_ADMIN = "is_admin";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_FCM_TOKEN = "fcm_token";

    // ==================== Intent Keys ====================

    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ADMIN_ID = "admin_id";
    public static final String INTENT_EVENT_ID = "event_id";
    public static final String INTENT_IS_ADMIN = "is_admin";
    public static final String INTENT_USER_ROLE = "user_role";

    // ==================== Request Codes ====================

    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_PROFILE = 1002;
    public static final int REQUEST_CODE_EVENT = 1003;
    public static final int REQUEST_CODE_QR_SCAN = 1004;
    public static final int REQUEST_CODE_IMAGE_PICK = 1005;
    public static final int REQUEST_CODE_CAMERA = 1006;
    public static final int REQUEST_CODE_LOCATION = 1007;

    // ==================== Result Codes ====================

    public static final int RESULT_USER_CREATED = 2001;
    public static final int RESULT_USER_UPDATED = 2002;
    public static final int RESULT_USER_DELETED = 2003;
    public static final int RESULT_EVENT_CREATED = 2004;
    public static final int RESULT_EVENT_UPDATED = 2005;

    // ==================== Validation Constants ====================

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_EVENT_NAME_LENGTH = 3;
    public static final int MAX_EVENT_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MAX_PHONE_LENGTH = 13;

    // ==================== File Size Limits ====================

    public static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final long MAX_EVENT_POSTER_SIZE = 10 * 1024 * 1024; // 10MB

    // ==================== Pagination ====================

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int USERS_PER_PAGE = 20;
    public static final int EVENTS_PER_PAGE = 15;

    // ==================== Time Constants ====================

    public static final long CACHE_EXPIRATION = 5 * 60 * 1000; // 5 minutes
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    public static final long NOTIFICATION_DELAY = 1000; // 1 second

    // ==================== Event Status ====================

    public static final String EVENT_STATUS_UPCOMING = "UPCOMING";
    public static final String EVENT_STATUS_REGISTRATION_OPEN = "REGISTRATION_OPEN";
    public static final String EVENT_STATUS_REGISTRATION_CLOSED = "REGISTRATION_CLOSED";
    public static final String EVENT_STATUS_DRAW_COMPLETE = "DRAW_COMPLETE";
    public static final String EVENT_STATUS_COMPLETED = "COMPLETED";
    public static final String EVENT_STATUS_CANCELLED = "CANCELLED";

    // ==================== Entrant Status ====================

    public static final String ENTRANT_STATUS_WAITING = "WAITING";
    public static final String ENTRANT_STATUS_SELECTED = "SELECTED";
    public static final String ENTRANT_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ENTRANT_STATUS_DECLINED = "DECLINED";
    public static final String ENTRANT_STATUS_CANCELLED = "CANCELLED";

    // ==================== Notification Types ====================

    public static final String NOTIFICATION_TYPE_SELECTED = "SELECTED";
    public static final String NOTIFICATION_TYPE_NOT_SELECTED = "NOT_SELECTED";
    public static final String NOTIFICATION_TYPE_EVENT_UPDATE = "EVENT_UPDATE";
    public static final String NOTIFICATION_TYPE_EVENT_CANCELLED = "EVENT_CANCELLED";
    public static final String NOTIFICATION_TYPE_REMINDER = "REMINDER";
    public static final String NOTIFICATION_TYPE_ADMIN = "ADMIN";

    // ==================== Error Messages ====================

    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_UNKNOWN = "An unknown error occurred. Please try again.";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied.";
    public static final String ERROR_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_ADMIN_NOT_FOUND = "Admin not found.";
    public static final String ERROR_EVENT_NOT_FOUND = "Event not found.";
    public static final String ERROR_INVALID_INPUT = "Invalid input. Please check your information.";
    public static final String ERROR_DEVICE_ID_MISSING = "Device ID is required.";

    // ==================== Success Messages ====================

    public static final String SUCCESS_PROFILE_CREATED = "Profile created successfully.";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully.";
    public static final String SUCCESS_PROFILE_DELETED = "Profile deleted successfully.";
    public static final String SUCCESS_EVENT_CREATED = "Event created successfully.";
    public static final String SUCCESS_EVENT_UPDATED = "Event updated successfully.";
    public static final String SUCCESS_JOINED_WAITING_LIST = "Joined waiting list successfully.";
    public static final String SUCCESS_LEFT_WAITING_LIST = "Left waiting list successfully.";

    // ==================== Date Formats ====================

    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_FULL = "MMMM dd, yyyy 'at' hh:mm a";
    public static final String DATE_FORMAT_TIME = "hh:mm a";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    // ==================== QR Code ====================

    public static final int QR_CODE_SIZE = 512;
    public static final String QR_CODE_PREFIX = "zephyr://event/";

    // ==================== Admin Actions ====================

    public static final String ADMIN_ACTION_REMOVE_USER = "REMOVE_USER";
    public static final String ADMIN_ACTION_REMOVE_EVENT = "REMOVE_EVENT";
    public static final String ADMIN_ACTION_REMOVE_IMAGE = "REMOVE_IMAGE";
    public static final String ADMIN_ACTION_REMOVE_ORGANIZER = "REMOVE_ORGANIZER";
    public static final String ADMIN_ACTION_VIEW_LOGS = "VIEW_LOGS";

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}