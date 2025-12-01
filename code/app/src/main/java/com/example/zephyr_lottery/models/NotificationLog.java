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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    // Used by NotificationLogArrayAdapter
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
