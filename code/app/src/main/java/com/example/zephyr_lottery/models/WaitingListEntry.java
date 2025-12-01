package com.example.zephyr_lottery.models;

import com.google.firebase.Timestamp;

/**
 * Stores the info of participants in the waiting list.
 */
public class WaitingListEntry {
    private String userId;
    private Double latitude;
    private Double longitude;
    private Timestamp joinedAt;

    public WaitingListEntry() {
        // Needed for Firestore
    }

    public WaitingListEntry(String userId, Double latitude, Double longitude, Timestamp joinedAt) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.joinedAt = joinedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
