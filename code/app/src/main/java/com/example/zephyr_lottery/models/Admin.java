//package com.example.zephyr_lottery.models;
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Admin model for system administrators
// * Separate from User class as admins have different responsibilities
// *
// * User Stories Addressed:
// * - US 03.01.01: Remove events
// * - US 03.02.01: Remove profiles
// * - US 03.03.01: Remove images
// * - US 03.04.01: Browse events
// * - US 03.05.01: Browse profiles
// * - US 03.06.01: Browse images
// * - US 03.07.01: Remove organizers
// * - US 03.08.01: Review notification logs
// */
//public class Admin implements Serializable {
//
//    // Unique identifier
//    private String adminId;
//
//    // Basic info (minimal - admins are system accounts)
//    private String username;
//    private String email;
//
//    // Authentication (can use device ID for convenience)
//    private String deviceId;
//
//    // Admin metadata
//    private AdminLevel level; // SUPER_ADMIN, MODERATOR, VIEWER
//    private boolean isActive;
//
//    // Audit trail
//    private Date createdAt;
//    private Date lastLoginAt;
//    private int totalActionsPerformed;
//
//    /**
//     * Default constructor for Firebase
//     */
//    public Admin() {
//        this.isActive = true;
//        this.totalActionsPerformed = 0;
//    }
//
//    /**
//     * Constructor for creating new admin
//     */
//    public Admin(String adminId, String username, String email) {
//        this();
//        this.adminId = adminId;
//        this.username = username;
//        this.email = email;
//        this.level = AdminLevel.MODERATOR; // Default level
//        this.createdAt = new Date();
//        this.lastLoginAt = new Date();
//    }
//
//    /**
//     * Full constructor
//     */
//    public Admin(String adminId, String username, String email, String deviceId, AdminLevel level) {
//        this();
//        this.adminId = adminId;
//        this.username = username;
//        this.email = email;
//        this.deviceId = deviceId;
//        this.level = level;
//        this.createdAt = new Date();
//        this.lastLoginAt = new Date();
//    }
//
//    // ==================== Getters and Setters ====================
//
//    public String getAdminId() {
//        return adminId;
//    }
//
//    public void setAdminId(String adminId) {
//        this.adminId = adminId;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
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
//    public AdminLevel getLevel() {
//        return level;
//    }
//
//    public void setLevel(AdminLevel level) {
//        this.level = level;
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
//    public Date getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Date createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Date getLastLoginAt() {
//        return lastLoginAt;
//    }
//
//    public void setLastLoginAt(Date lastLoginAt) {
//        this.lastLoginAt = lastLoginAt;
//    }
//
//    public int getTotalActionsPerformed() {
//        return totalActionsPerformed;
//    }
//
//    public void setTotalActionsPerformed(int totalActionsPerformed) {
//        this.totalActionsPerformed = totalActionsPerformed;
//    }
//
//    public void incrementActionsPerformed() {
//        this.totalActionsPerformed++;
//    }
//
//    // ==================== Permission Checks ====================
//
//    /**
//     * Check if admin can remove users (US 03.02.01)
//     */
//    public boolean canRemoveUsers() {
//        return isActive && (level == AdminLevel.SUPER_ADMIN || level == AdminLevel.MODERATOR);
//    }
//
//    /**
//     * Check if admin can remove events (US 03.01.01)
//     */
//    public boolean canRemoveEvents() {
//        return isActive && (level == AdminLevel.SUPER_ADMIN || level == AdminLevel.MODERATOR);
//    }
//
//    /**
//     * Check if admin can remove images (US 03.03.01)
//     */
//    public boolean canRemoveImages() {
//        return isActive && (level == AdminLevel.SUPER_ADMIN || level == AdminLevel.MODERATOR);
//    }
//
//    /**
//     * Check if admin can browse all data (US 03.04.01, 03.05.01, 03.06.01)
//     */
//    public boolean canBrowseData() {
//        return isActive;
//    }
//
//    /**
//     * Check if admin can review notification logs (US 03.08.01)
//     */
//    public boolean canReviewLogs() {
//        return isActive && (level == AdminLevel.SUPER_ADMIN || level == AdminLevel.MODERATOR);
//    }
//
//    /**
//     * Check if admin can remove organizers (US 03.07.01)
//     */
//    public boolean canRemoveOrganizers() {
//        return isActive && level == AdminLevel.SUPER_ADMIN;
//    }
//
//    /**
//     * Check if admin can manage other admins
//     */
//    public boolean canManageAdmins() {
//        return isActive && level == AdminLevel.SUPER_ADMIN;
//    }
//
//    // ==================== Utility Methods ====================
//
//    /**
//     * Record admin login
//     */
//    public void recordLogin() {
//        this.lastLoginAt = new Date();
//    }
//
//    /**
//     * Check if admin account is valid
//     */
//    public boolean isValid() {
//        return adminId != null && !adminId.isEmpty() &&
//                username != null && !username.isEmpty() &&
//                email != null && !email.isEmpty() &&
//                isActive;
//    }
//
//    /**
//     * Convert Admin object to Map for Firebase
//     */
//    public Map<String, Object> toMap() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("adminId", adminId);
//        map.put("username", username);
//        map.put("email", email);
//        map.put("deviceId", deviceId);
//        map.put("level", level != null ? level.toString() : null);
//        map.put("isActive", isActive);
//        map.put("createdAt", createdAt);
//        map.put("lastLoginAt", lastLoginAt);
//        map.put("totalActionsPerformed", totalActionsPerformed);
//        return map;
//    }
//
//    /**
//     * Create Admin object from Firebase Map
//     */
//    public static Admin fromMap(Map<String, Object> map) {
//        Admin admin = new Admin();
//
//        if (map.get("adminId") != null) {
//            admin.setAdminId((String) map.get("adminId"));
//        }
//        if (map.get("username") != null) {
//            admin.setUsername((String) map.get("username"));
//        }
//        if (map.get("email") != null) {
//            admin.setEmail((String) map.get("email"));
//        }
//        if (map.get("deviceId") != null) {
//            admin.setDeviceId((String) map.get("deviceId"));
//        }
//        if (map.get("level") != null) {
//            admin.setLevel(AdminLevel.valueOf((String) map.get("level")));
//        }
//        if (map.get("isActive") != null) {
//            admin.setActive((Boolean) map.get("isActive"));
//        }
//        if (map.get("createdAt") != null) {
//            admin.setCreatedAt((Date) map.get("createdAt"));
//        }
//        if (map.get("lastLoginAt") != null) {
//            admin.setLastLoginAt((Date) map.get("lastLoginAt"));
//        }
//        if (map.get("totalActionsPerformed") != null) {
//            Number actions = (Number) map.get("totalActionsPerformed");
//            admin.setTotalActionsPerformed(actions.intValue());
//        }
//
//        return admin;
//    }
//
//    @Override
//    public String toString() {
//        return "Admin{" +
//                "adminId='" + adminId + '\'' +
//                ", username='" + username + '\'' +
//                ", level=" + level +
//                ", isActive=" + isActive +
//                '}';
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Admin admin = (Admin) o;
//        return adminId != null && adminId.equals(admin.adminId);
//    }
//
//    @Override
//    public int hashCode() {
//        return adminId != null ? adminId.hashCode() : 0;
//    }
//}