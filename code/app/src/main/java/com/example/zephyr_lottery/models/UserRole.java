//package com.example.zephyr_lottery.models;
//
///**
// * Enum representing user roles in the Event Lottery System
// * Note: ADMIN is handled separately in the Admin class
// *
// * ENTRANT: Regular users who can join event waiting lists
// * ORGANIZER: Users who can create and manage events (also has all entrant capabilities)
// */
//public enum UserRole {
//    ENTRANT("Entrant"),
//    ORGANIZER("Organizer");
//
//    private final String displayName;
//
//    UserRole(String displayName) {
//        this.displayName = displayName;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    /**
//     * Get role from string value (case-insensitive)
//     */
//    public static UserRole fromString(String roleString) {
//        if (roleString == null) {
//            return ENTRANT; // Default role
//        }
//
//        for (UserRole role : UserRole.values()) {
//            if (role.name().equalsIgnoreCase(roleString) ||
//                    role.displayName.equalsIgnoreCase(roleString)) {
//                return role;
//            }
//        }
//
//        return ENTRANT; // Default if not found
//    }
//
//    /**
//     * Check if this role has organizer privileges
//     */
//    public boolean canOrganizeEvents() {
//        return this == ORGANIZER;
//    }
//
//    /**
//     * Check if this role can join waiting lists
//     */
//    public boolean canJoinWaitingLists() {
//        return true; // Both roles can join events
//    }
//
//    @Override
//    public String toString() {
//        return this.name();
//    }
//}
