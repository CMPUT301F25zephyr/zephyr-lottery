package com.example.zephyr_lottery;

/**
 * This class stores the details of a user
 */
public class UserProfile {

    //attributes to be updated when needed
    private String username;
    private String email;
    private String type;
    private String phone;
    private Boolean receivingNotis;
    private String fcmToken;

    // empty constructor for firebase
    public UserProfile() {

    }

    /**
     * Creates a new UserProfile
     * @param username
     *  The username of the profile
     * @param email
     *  The email of the profile
     * @param type
     *  The type of the profile (entrant, organizer, admin)
     */
    public UserProfile(String username, String email, String type) {
        this.username = username;
        this.email = email;
        this.type = type;
        this.receivingNotis = false;
    }

    /**
     * Creates a new UserProfile
     * @param username
     *  The username of the profile
     * @param email
     *  The email of the profile
     * @param type
     *  The type of the profile (entrant, organizer, admin)
     * @param phone
     *  The phone number of the profile
     */
    public UserProfile(String username, String email, String type, String phone) {
        this.username = username;
        this.email = email;
        this.type = type;
        this.phone = phone;
        this.receivingNotis = false;
    }

    public UserProfile(String username, String email, String type, String phone, Boolean receivingNotis) {
        this.username = username;
        this.email = email;
        this.type = type;
        this.phone = phone;
        this.receivingNotis = receivingNotis;
    }

    public Boolean getReceivingNotis() {
        return receivingNotis;
    }

    /**
     * Obtains the username of the profile
     * @return
     * The username of the profile, as a String
     */
    public String getUsername() {return username;}

    /**
     * Obtains the email of the profile
     * @return
     * The email of the profile, as a String
     */
    public String getEmail() {return email;}

    /**
     * Obtains the type of the profile (entrant, organizer, admin)
     * @return
     * The type of the profile, as a String
     */
    public String getType() {return type;}

    /**
     * Obtains the phone number of the profile
     * @return
     * The phone number of the profile, as a String
     */
    public String getPhone() {return phone;}
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

