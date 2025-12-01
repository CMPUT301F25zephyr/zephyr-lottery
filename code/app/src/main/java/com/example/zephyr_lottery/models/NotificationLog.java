package com.example.zephyr_lottery.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

/**
 * Model for a single notification log entry.
 */
public class NotificationLog {

    @Exclude
    private String id;           // Firestore document ID (not stored)

    private String senderEmail;   // organizer
    private String recipientEmail; // entrant
    private String eventId;       // optional
    private String eventName;     // optional
    private String type;          // e.g. "INVITATION", "WAITLIST_NOTIFY", etc.
    private String title;         // notification title
    private String body;          // notification body
    private Timestamp timestamp;  // when it was sent

    // **Required** public no-arg constructor for Firestore
    public NotificationLog() {}

    public NotificationLog(String senderEmail,
                           String recipientEmail,
                           String eventId,
                           String eventName,
                           String type,
                           String title,
                           String body,
                           Timestamp timestamp) {
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.eventId = eventId;
        this.eventName = eventName;
        this.type = type;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
