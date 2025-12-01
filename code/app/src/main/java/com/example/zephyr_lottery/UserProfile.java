package com.example.zephyr_lottery;

import java.util.ArrayList;

/**
 * This class stores the details of a user
 */
public class UserProfile {

    //attributes to be updated when needed
    private String username;
    private String androidId;  // Changed from email - now the primary identifier
    private String email;      // Optional - kept in case you want to add email later
    private String type;
    private String phone;
    private Boolean receivingNotis;
    private ArrayList<Integer> invitationCodes;
    private String fcmToken;

    // empty constructor for firebase
    public UserProfile() {

    }

    /**
     * Creates a new UserProfile (assume notifications are off)
     * @param username
     *  The username of the profile
     * @param androidId
     *  The Android ID of the device
     * @param type
     *  The type of the profile (entrant, organizer, admin)
     */
    public UserProfile(String username, String androidId, String type) {
        this.username = username;
        this.androidId = androidId;
        this.type = type;
        this.receivingNotis = false;
        this.invitationCodes = new ArrayList<Integer>();
    }

    /**
     * Creates a new UserProfile with phone number
     * @param username
     *  The username of the profile
     * @param androidId
     *  The Android ID of the device
     * @param type
     *  The type of the profile (entrant, organizer, admin)
     * @param phone
     *  The phone number of the profile
     */
    public UserProfile(String username, String androidId, String type, String phone) {
        this.username = username;
        this.androidId = androidId;
        this.type = type;
        this.phone = phone;
        this.receivingNotis = false;
        this.invitationCodes = new ArrayList<Integer>();
    }

    /**
     * Creates a new UserProfile with all fields
     * @param username
     *  The username of the profile
     * @param androidId
     *  The Android ID of the device
     * @param type
     *  The type of the profile (entrant, organizer, admin)
     * @param phone
     *  The phone number of the profile
     * @param receivingNotis
     *  If the user wants to receive notifications
     */
    public UserProfile(String username, String androidId, String type, String phone, Boolean receivingNotis) {
        this.username = username;
        this.androidId = androidId;
        this.type = type;
        this.phone = phone;
        this.receivingNotis = receivingNotis;
        this.invitationCodes = new ArrayList<Integer>();
    }

    /**
     * Obtain the user's notification preference
     * @return
     *  Boolean indicating whether the user enabled notifications
     */
    public Boolean getReceivingNotis() {
        return receivingNotis;
    }

    /**
     * Set the user's notification preference
     * @param receivingNotis
     *  Boolean indicating whether the user wants notifications
     */
    public void setReceivingNotis(Boolean receivingNotis) {
        this.receivingNotis = receivingNotis;
    }

    /**
     * Obtains the username of the profile
     * @return
     * The username of the profile, as a String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the profile
     * @param username
     *  The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtains the Android ID of the device
     * @return
     * The Android ID, as a String
     */
    public String getAndroidId() {
        return androidId;
    }

    /**
     * Sets the Android ID
     * @param androidId
     *  The Android ID to set
     */
    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    /**
     * Obtains the email of the profile (optional field)
     * @return
     * The email of the profile, as a String (may be null)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the profile (optional)
     * @param email
     *  The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtains the type of the profile (entrant, organizer, admin)
     * @return
     * The type of the profile, as a String
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the profile
     * @param type
     *  The type (entrant, organizer, admin)
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Obtains the phone number of the profile
     * @return
     * The phone number of the profile, as a String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number
     * @param phone
     *  The phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Obtains the user's FCM token
     * @return
     *  The user's FCM token
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Sets the user's FCM token
     * @param fcmToken
     *  The FCM token to set
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Get the user's incoming invitations
     * @return
     * ArrayList of hashcodes of the events of any incoming invitations
     */
    public ArrayList<Integer> getInvitationCodes() {
        return invitationCodes;
    }

    /**
     * Set the user's incoming invitations
     * @param invitationCodes
     * ArrayList with hashcodes of the events of any incoming invitations
     */
    public void setInvitationCodes(ArrayList<Integer> invitationCodes) {
        this.invitationCodes = invitationCodes;
    }

    /**
     * Add one invitation to a user's incoming invitations
     * @param new_invitation_code
     * The hashcode of the event with the new invitation
     */
    public void addInvitationCode(int new_invitation_code) {
        if (this.invitationCodes == null) {
            this.invitationCodes = new ArrayList<Integer>();
        }
        this.invitationCodes.add(new_invitation_code);
    }
}