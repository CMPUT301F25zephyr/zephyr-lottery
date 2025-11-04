package com.example.zephyr_lottery;

import java.time.LocalDateTime;

public class Event {
    //attributes to be updated when needed
    private String name;
    private String description;
    private LocalDateTime lott_start_date; //lottery start date
    private LocalDateTime lott_end_date; //lottery end date
    private String times; //string(?) that shows when the actual event happens

    //arrayList of entrants

    public Event(String name, String times) {
        this.name = name;
        this.times = times;
        //add dates and times to constructor
        // -> when we implement organizers creating events
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
}
