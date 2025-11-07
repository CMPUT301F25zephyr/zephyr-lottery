package com.example.zephyr_lottery.models;

import com.google.firebase.Timestamp;

public class Participant {
    private String userId;
    private String status; // invited | accepted | declined
    private Timestamp invitedAt;
    private Timestamp updatedAt;

    public Participant() {}

    public Participant(String userId, String status, Timestamp invitedAt, Timestamp updatedAt) {
        this.userId = userId;
        this.status = status;
        this.invitedAt = invitedAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public Timestamp getInvitedAt() { return invitedAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setInvitedAt(Timestamp invitedAt) { this.invitedAt = invitedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}