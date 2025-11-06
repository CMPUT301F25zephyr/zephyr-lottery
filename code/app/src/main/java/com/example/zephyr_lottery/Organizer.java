package com.example.zephyr_lottery;

import java.util.ArrayList;

//unused for now.
public class Organizer {


    //private ArrayList<Event> myEvents;
    private UserProfile profile; //organzier's basic information. organizer has a profile?

    public Organizer(UserProfile profile){
        this.profile = profile;
    }

    public UserProfile getProfile() {return profile;}
    public void setProfile(UserProfile profile) {this.profile = profile;}
}
