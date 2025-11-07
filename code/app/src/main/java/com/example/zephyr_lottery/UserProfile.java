package com.example.zephyr_lottery;

public class UserProfile {

    //attributes to be updated when needed
    private String username;
    private String email;
    private String type;
    private String phone;
    private Boolean receivingNotis;

    // empty constructor for firebase
    public UserProfile() {

    }
    public UserProfile(String username, String email, String type) {
        this.username = username;
        this.email = email;
        this.type = type;
        this.receivingNotis = false;
    }

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

    public String getUsername() {return username;}

    public String getEmail() {return email;}
    public String getType() {return type;}
    public String getPhone() {return phone;}
}

