package com.example.zephyr_lottery.models;

import com.google.firebase.Timestamp;

/**
 * Model for notification audit log entries for admins.
 * Stored in the "notificationLogs" collection.
 */
public class NotificationLog {

    // ID of the event this notification is about
    private String eventId;

    // Human-readable event name (optional, for nice UI)
    private String eventName;

    // Organizer who triggered the notification
    private String senderEmail;

    // Recipient user id / email (canonical field)
    private String userId;

    // e.g. "WAITING", "SELECTED", "REMINDER", etc.
    private String notificationType;

    // When the notification was sent/logged
    private Timestamp sentAt;

    // Required empty constructor for Firestore
    public NotificationLog() {
    }

    /**
     * Initializes a NotificationLog with the notification details
     * @param eventId
     *  The ID of the event that the notification applies to
     * @param eventName
     *  The name of the event that the notification applies to
     * @param senderEmail
     *  The email of the creator of the event
     * @param userId
     *  The ID of the notification recipient
     * @param notificationType
     *  The type of the notification
     * @param sentAt
     *  The time the notification was sent at, as a Timestamp
     */
    public NotificationLog(String eventId,
                           String eventName,
                           String senderEmail,
                           String userId,
                           String notificationType,
                           Timestamp sentAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.senderEmail = senderEmail;
        this.userId = userId;
        this.notificationType = notificationType;
        this.sentAt = sentAt;
    }

    // ----- Getters / setters -----

    /**
     * Retrieves the Id of the event of the notification
     * @return
     *  The Id of the event of the notification
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the Id of the event of the notification
     * @param eventId
     *  The new Id of the event of the notification
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Retrieves the name of the event of the notification
     * @return
     *  The name of the event of the notification
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the name of the event of the notification
     * @param eventName
     *  The new name of the event of the notification
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Retrieves the email of the creator of the event
     * @return
     *  The email of the creator of the event
     */
    public String getSenderEmail() {
        return senderEmail;
    }

    /**
     * Sets the email of the creator of the event
     * @param senderEmail
     *  The new email of the creator of the event
     */
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    // Used by NotificationLogArrayAdapter

    /**
     * Retrieves the ID of the recipient
     * @return
     *  The ID of the recipient
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    // ----- Aliases for FCM logging code -----

    // FCM code might use "recipientEmail" wording
    public String getRecipientEmail() {
        return userId;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.userId = recipientEmail;
    }
}
