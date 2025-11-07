package com.example.zephyr_lottery.models;

import com.google.firebase.Timestamp;

/**
 * This class defines a participant, tracking their status and timestamps for when they were last invited/updated.
 */
public class Participant {
    private String userId;
    private String status; // PENDING, SELECTED, CONFIRMED, CANCELLED, WAITLISTED
    private Timestamp invitedAt;
    private Timestamp updatedAt;

    /**
     * Creates a new participant with an ID, status, and timestamps
     * @param userId
     *  The ID of the user
     * @param status
     *  The current status of the user in the event, can be PENDING, SELECTED, CONFIRMED, CANCELLED, WAITLISTED
     * @param invitedAt
     *  Timestamp of when the user was drawn from the lottery
     * @param updatedAt
     *  Timestamp of when the user's status was last changed
     */
    public Participant(String userId, String status, Timestamp invitedAt, Timestamp updatedAt) {
        this.userId = userId;
        this.status = status;
        this.invitedAt = invitedAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Obtains the ID of the user
     * @return
     * Returns the ID as a String
     */
    public String getUserId() { return userId; }

    /**
     * Obtains the status of the user
     * @return
     * Returns the status as a String
     */
    public String getStatus() { return status; }

    /**
     * Obtains the time when the user was drawn from the lottery
     * @return
     * Returns the time as a Timestamp
     */
    public Timestamp getInvitedAt() { return invitedAt; }

    /**
     * Obtains the time from when the status was last changed
     * @return
     * Returns the time as a Timestamp
     */
    public Timestamp getUpdatedAt() { return updatedAt; }

    /**
     * Sets the userID to the given value
     * @param userId
     *  The new ID to set for the user, as a String
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Sets the status to the given value
     * @param status
     *   The new status to set, as a String
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Sets the time when the user was drawn from the lottery
     * @param invitedAt
     *  The new time, as a Timestamp
     */
    public void setInvitedAt(Timestamp invitedAt) { this.invitedAt = invitedAt; }

    /**
     * Sets the time when the status was last changed
     * @param updatedAt
     *  The new time, as a Timestamp
     */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}