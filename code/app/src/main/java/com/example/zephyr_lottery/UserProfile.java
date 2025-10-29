package com.example.zephyr_lottery;

public class UserProfile {

    //attributes to be updated when needed
    private String username;
    private String email;
    private String type;

    public UserProfile(String username, String email, String type) {
        this.username = username;
        this.email = email;
        this.type = type;
    }

    public String getUsername() {return username;}

    public String getEmail() {return email;}
    public String getType() {return type;}
}

