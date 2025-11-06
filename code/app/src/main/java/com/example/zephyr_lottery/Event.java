package com.example.zephyr_lottery;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class Event {
    //attributes will need to be updated later on.
    private String name;
    private String description;
    private LocalDateTime lott_start_date; //lottery start date
    private LocalDateTime lott_end_date; //lottery end date
    private String times; //string that shows when the actual event happens
    private Date date_created; //use to order the latest events screen
    private String organizer_email; //the email of the organizer. used for finding organizer's events.
    //NEED TO ADD: arrayList of entrants.

    public Event(String name, String times, String organizer_email) {
        this.name = name;
        this.times = times;
        this.organizer_email = organizer_email;
        //add dates and times to constructor
        // -> when we implement organizers creating events
    }

    public String getOrganizer_email() {
        return organizer_email;
    }

    public void setOrganizer_email(String organizer_email) {
        this.organizer_email = organizer_email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name) && Objects.equals(description, event.description) && Objects.equals(lott_start_date, event.lott_start_date) && Objects.equals(lott_end_date, event.lott_end_date) && Objects.equals(times, event.times);
    }

    @Override
    public int hashCode() {
        //add more to hashcode when that is implemented. need description, possibly start times, author.
        return Objects.hash(name, times);
    }
}
