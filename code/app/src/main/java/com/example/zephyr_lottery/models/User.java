//package com.example.zephyr_lottery.models;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * User model representing Entrants and Organizers in the Event Lottery System
// * Note: Admins are handled separately in the Admin class
// *
// * User Stories Addressed:
// * - US 01.02.01: Personal information (name, email, phone)
// * - US 01.02.02: Update user information
// * - US 01.02.04: Delete profile
// * - US 01.07.01: Device-based identification
// */
//public class User implements Serializable {
//
//    // Unique identifier based on device ID
//    private String userId;
//
//    // Personal Information
//    private String name;
//    private String email;
//    private String phoneNumber; // Optional
//
//    // Profile
//    private String profileImageUrl;
//    private boolean hasCustomProfileImage;
//
//    // Role: ENTRANT or ORGANIZER only (Admin is separate)
//    private UserRole role;
//
//    // Device Information
//    private String deviceId;
//    private String fcmToken; // For push notifications
//
//    // Notification Preferences (US 01.04.03)
//    private boolean notificationsEnabled;
//    private boolean organizerNotificationsEnabled;
//    private boolean adminNotificationsEnabled;
//
//    // Timestamps
//    private Date createdAt;
//    private Date updatedAt;
//
//    // Event History (US 01.02.03)
//    // For Entrants: events they've joined
//    private List<String> registeredEventIds;
//    private List<String> selectedEventIds;
//
//    // For Organizers: events they've created
//    private List<String> organizedEventIds;
//
//    // Status
//    private boolean isActive;
//    private boolean isDeleted;
//
//    /**
//     * Default constructor required for Firebase
//     */
//    public User() {
//        this.registeredEventIds = new ArrayList<>();
//        this.selectedEventIds = new ArrayList<>();
//        this.organizedEventIds = new ArrayList<>();
//        this.notificationsEnabled = true;
//        this.organizerNotificationsEnabled = true;
//        this.adminNotificationsEnabled = true;
//        this.isActive = true;
//        this.isDeleted = false;
//        this.hasCustomProfileImage = false;
//    }
//
//    /**
//     * Constructor for creating a new user
//     *
//     * @param deviceId Unique device identifier
//     * @param name User's name
//     * @param email User's email
//     */
//    public User(String deviceId, String name, String email) {
//        this();
//        this.userId = deviceId; // Device ID becomes user ID
//        this.deviceId = deviceId;
//        this.name = name;
//        this.email = email;
//        this.role = UserRole.ENTRANT; // Default role
//        this.createdAt = new Date();
//        this.updatedAt = new Date();
//    }
//
//    /**
//     * Full constructor
//     */
//    public User(String userId, String deviceId, String name, String email,
//                String phoneNumber, UserRole role) {
//        this();
//        this.userId = userId;
//        this.deviceId = deviceId;
//        this.name = name;
//        this.email = email;
//        this.phoneNumber = phoneNumber;
//        this.role = role;
//        this.createdAt = new Date();
//        this.updatedAt = new Date();
//    }
//
//    // ==================== Getters and Setters ====================
//
//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//        this.updatedAt = new Date();
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//        this.updatedAt = new Date();
//    }
//
//    public String getPhoneNumber() {
//        return phoneNumber;
//    }
//
//    public void setPhoneNumber(String phoneNumber) {
//        this.phoneNumber = phoneNumber;
//        this.updatedAt = new Date();
//    }
//
//    public String getProfileImageUrl() {
//        return profileImageUrl;
//    }
//
//    public void setProfileImageUrl(String profileImageUrl) {
//        this.profileImageUrl = profileImageUrl;
//        this.hasCustomProfileImage = (profileImageUrl != null && !profileImageUrl.isEmpty());
//        this.updatedAt = new Date();
//    }
//
//    public boolean hasCustomProfileImage() {
//        return hasCustomProfileImage;
//    }
//
//    public void setHasCustomProfileImage(boolean hasCustomProfileImage) {
//        this.hasCustomProfileImage = hasCustomProfileImage;
//    }
//
//    public UserRole getRole() {
//        return role;
//    }
//
//    public void setRole(UserRole role) {
//        // Only ENTRANT or ORGANIZER allowed
//        if (role == UserRole.ENTRANT || role == UserRole.ORGANIZER) {
//            this.role = role;
//            this.updatedAt = new Date();
//        } else {
//            throw new IllegalArgumentException("User can only be ENTRANT or ORGANIZER. Use Admin class for admin users.");
//        }
//    }
//
//    public boolean isOrganizer() {
//        return role == UserRole.ORGANIZER;
//    }
//
//    public boolean isEntrant() {
//        return role == UserRole.ENTRANT;
//    }
//
//    public String getDeviceId() {
//        return deviceId;
//    }
//
//    public void setDeviceId(String deviceId) {
//        this.deviceId = deviceId;
//    }
//
//    public String getFcmToken() {
//        return fcmToken;
//    }
//
//    public void setFcmToken(String fcmToken) {
//        this.fcmToken = fcmToken;
//    }
//
//    public boolean isNotificationsEnabled() {
//        return notificationsEnabled;
//    }
//
//    public void setNotificationsEnabled(boolean notificationsEnabled) {
//        this.notificationsEnabled = notificationsEnabled;
//        this.updatedAt = new Date();
//    }
//
//    public boolean isOrganizerNotificationsEnabled() {
//        return organizerNotificationsEnabled;
//    }
//
//    public void setOrganizerNotificationsEnabled(boolean organizerNotificationsEnabled) {
//        this.organizerNotificationsEnabled = organizerNotificationsEnabled;
//        this.updatedAt = new Date();
//    }
//
//    public boolean isAdminNotificationsEnabled() {
//        return adminNotificationsEnabled;
//    }
//
//    public void setAdminNotificationsEnabled(boolean adminNotificationsEnabled) {
//        this.adminNotificationsEnabled = adminNotificationsEnabled;
//        this.updatedAt = new Date();
//    }
//
//    public Date getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Date createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Date getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(Date updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//    public List<String> getRegisteredEventIds() {
//        return registeredEventIds;
//    }
//
//    public void setRegisteredEventIds(List<String> registeredEventIds) {
//        this.registeredEventIds = registeredEventIds;
//    }
//
//    public void addRegisteredEvent(String eventId) {
//        if (!registeredEventIds.contains(eventId)) {
//            registeredEventIds.add(eventId);
//        }
//    }
//
//    public void removeRegisteredEvent(String eventId) {
//        registeredEventIds.remove(eventId);
//    }
//
//    public List<String> getSelectedEventIds() {
//        return selectedEventIds;
//    }
//
//    public void setSelectedEventIds(List<String> selectedEventIds) {
//        this.selectedEventIds = selectedEventIds;
//    }
//
//    public void addSelectedEvent(String eventId) {
//        if (!selectedEventIds.contains(eventId)) {
//            selectedEventIds.add(eventId);
//        }
//    }
//
//    public List<String> getOrganizedEventIds() {
//        return organizedEventIds;
//    }
//
//    public void setOrganizedEventIds(List<String> organizedEventIds) {
//        this.organizedEventIds = organizedEventIds;
//    }
//
//    public void addOrganizedEvent(String eventId) {
//        if (!organizedEventIds.contains(eventId)) {
//            organizedEventIds.add(eventId);
//        }
//    }
//
//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean active) {
//        isActive = active;
//    }
//
//    public boolean isDeleted() {
//        return isDeleted;
//    }
//
//    public void setDeleted(boolean deleted) {
//        isDeleted = deleted;
//        this.updatedAt = new Date();
//    }
//
//    // ==================== Utility Methods ====================
//
//    /**
//     * Check if user has provided all required information
//     */
//    public boolean isProfileComplete() {
//        return name != null && !name.trim().isEmpty() &&
//                email != null && !email.trim().isEmpty();
//    }
//
//    /**
//     * Check if user can receive notifications
//     */
//    public boolean canReceiveNotifications() {
//        return isActive && !isDeleted && notificationsEnabled;
//    }
//
//    /**
//     * Check if user can receive organizer notifications
//     */
//    public boolean canReceiveOrganizerNotifications() {
//        return canReceiveNotifications() && organizerNotificationsEnabled;
//    }
//
//    /**
//     * Check if user can receive admin notifications
//     */
//    public boolean canReceiveAdminNotifications() {
//        return canReceiveNotifications() && adminNotificationsEnabled;
//    }
//
//    /**
//     * Soft delete the user (US 01.02.04)
//     */
//    public void softDelete() {
//        this.isDeleted = true;
//        this.isActive = false;
//        this.updatedAt = new Date();
//    }
//
//    /**
//     * Convert User object to Map for Firebase
//     */
//    public Map<String, Object> toMap() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId", userId);
//        map.put("deviceId", deviceId);
//        map.put("name", name);
//        map.put("email", email);
//        map.put("phoneNumber", phoneNumber);
//        map.put("profileImageUrl", profileImageUrl);
//        map.put("hasCustomProfileImage", hasCustomProfileImage);
//        map.put("role", role != null ? role.toString() : null);
//        map.put("fcmToken", fcmToken);
//        map.put("notificationsEnabled", notificationsEnabled);
//        map.put("organizerNotificationsEnabled", organizerNotificationsEnabled);
//        map.put("adminNotificationsEnabled", adminNotificationsEnabled);
//        map.put("createdAt", createdAt);
//        map.put("updatedAt", updatedAt);
//        map.put("registeredEventIds", registeredEventIds);
//        map.put("selectedEventIds", selectedEventIds);
//        map.put("organizedEventIds", organizedEventIds);
//        map.put("isActive", isActive);
//        map.put("isDeleted", isDeleted);
//        return map;
//    }
//
//    /**
//     * Create User object from Firebase Map
//     */
//    public static User fromMap(Map<String, Object> map) {
//        User user = new User();
//
//        if (map.get("userId") != null) {
//            user.setUserId((String) map.get("userId"));
//        }
//        if (map.get("deviceId") != null) {
//            user.setDeviceId((String) map.get("deviceId"));
//        }
//        if (map.get("name") != null) {
//            user.setName((String) map.get("name"));
//        }
//        if (map.get("email") != null) {
//            user.setEmail((String) map.get("email"));
//        }
//        if (map.get("phoneNumber") != null) {
//            user.setPhoneNumber((String) map.get("phoneNumber"));
//        }
//        if (map.get("profileImageUrl") != null) {
//            user.setProfileImageUrl((String) map.get("profileImageUrl"));
//        }
//        if (map.get("hasCustomProfileImage") != null) {
//            user.setHasCustomProfileImage((Boolean) map.get("hasCustomProfileImage"));
//        }
//        if (map.get("role") != null) {
//            user.setRole(UserRole.valueOf((String) map.get("role")));
//        }
//        if (map.get("fcmToken") != null) {
//            user.setFcmToken((String) map.get("fcmToken"));
//        }
//        if (map.get("notificationsEnabled") != null) {
//            user.setNotificationsEnabled((Boolean) map.get("notificationsEnabled"));
//        }
//        if (map.get("organizerNotificationsEnabled") != null) {
//            user.setOrganizerNotificationsEnabled((Boolean) map.get("organizerNotificationsEnabled"));
//        }
//        if (map.get("adminNotificationsEnabled") != null) {
//            user.setAdminNotificationsEnabled((Boolean) map.get("adminNotificationsEnabled"));
//        }
//        if (map.get("createdAt") != null) {
//            user.setCreatedAt((Date) map.get("createdAt"));
//        }
//        if (map.get("updatedAt") != null) {
//            user.setUpdatedAt((Date) map.get("updatedAt"));
//        }
//        if (map.get("isActive") != null) {
//            user.setActive((Boolean) map.get("isActive"));
//        }
//        if (map.get("isDeleted") != null) {
//            user.setDeleted((Boolean) map.get("isDeleted"));
//        }
//
//        @SuppressWarnings("unchecked")
//        List<String> registeredEvents = (List<String>) map.get("registeredEventIds");
//        if (registeredEvents != null) {
//            user.setRegisteredEventIds(registeredEvents);
//        }
//
//        @SuppressWarnings("unchecked")
//        List<String> selectedEvents = (List<String>) map.get("selectedEventIds");
//        if (selectedEvents != null) {
//            user.setSelectedEventIds(selectedEvents);
//        }
//
//        @SuppressWarnings("unchecked")
//        List<String> organizedEvents = (List<String>) map.get("organizedEventIds");
//        if (organizedEvents != null) {
//            user.setOrganizedEventIds(organizedEvents);
//        }
//
//        return user;
//    }
//
//    @Override
//    public String toString() {
//        return "User{" +
//                "userId='" + userId + '\'' +
//                ", name='" + name + '\'' +
//                ", email='" + email + '\'' +
//                ", role=" + role +
//                ", isActive=" + isActive +
//                '}';
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        User user = (User) o;
//        return userId != null && userId.equals(user.userId);
//    }
//
//    @Override
//    public int hashCode() {
//        return userId != null ? userId.hashCode() : 0;
//    }
//}