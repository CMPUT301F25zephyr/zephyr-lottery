//package com.example.zephyr_lottery.models;
//
///**
// * Enum representing different admin privilege levels
// * Allows for hierarchical admin permissions
// */
//public enum AdminLevel {
//    SUPER_ADMIN("Super Admin"),      // Full system access, can manage other admins
//    MODERATOR("Moderator"),          // Can remove content and users
//    VIEWER("Viewer");                // Read-only access for auditing
//
//    private final String displayName;
//
//    AdminLevel(String displayName) {
//        this.displayName = displayName;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    /**
//     * Get level from string value
//     */
//    public static AdminLevel fromString(String levelString) {
//        if (levelString == null) {
//            return MODERATOR; // Default level
//        }
//
//        for (AdminLevel level : AdminLevel.values()) {
//            if (level.name().equalsIgnoreCase(levelString) ||
//                    level.displayName.equalsIgnoreCase(levelString)) {
//                return level;
//            }
//        }
//
//        return MODERATOR;
//    }
//
//    /**
//     * Check if this level has full system access
//     */
//    public boolean isSuperAdmin() {
//        return this == SUPER_ADMIN;
//    }
//
//    /**
//     * Check if this level can modify data
//     */
//    public boolean canModify() {
//        return this == SUPER_ADMIN || this == MODERATOR;
//    }
//
//    /**
//     * Check if this level is read-only
//     */
//    public boolean isReadOnly() {
//        return this == VIEWER;
//    }
//
//    @Override
//    public String toString() {
//        return this.name();
//    }
//}