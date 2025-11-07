package com.example.zephyr_lottery;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Event {
    //attributes will need to be updated later on.
    private String name;
    private String description;
    private String organizer_email; //the email of the organizer. used for finding organizer's events.
    private float price;
    private String location;
    private String time; //string that shows when the actual event happens

    //number that corresponds to weekday the event happens on. 0-6 for monday-sunday
    private int weekday;

    private String period; // event period

    private ArrayList<String> entrants;

    //for now unused attributes
    private Date date_created; //use to order the latest events screen
    private LocalDateTime lott_start_date; //lottery start date
    private LocalDateTime lott_end_date; //lottery end date
    //NEED TO ADD: arrayList of entrants.

    public Event(String name, String time, String organizer_email) {
        this.name = name;
        this.time = time;
        this.organizer_email = organizer_email;

        //add dates and times to constructor
        // -> when we implement organizers creating events
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Float.compare(price, event.price) == 0 && weekday == event.weekday && Objects.equals(name, event.name) && Objects.equals(description, event.description) && Objects.equals(location, event.location) && Objects.equals(time, event.time) && Objects.equals(date_created, event.date_created) && Objects.equals(organizer_email, event.organizer_email) && Objects.equals(lott_start_date, event.lott_start_date) && Objects.equals(lott_end_date, event.lott_end_date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, price, location, time, weekday, date_created, organizer_email, lott_start_date, lott_end_date);
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

    public int getWeekday() {
        return weekday;
    }

    public String getWeekdayString(){
        String[] weekdays_str = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return weekdays_str[this.weekday];
    }

    public void setWeekdayString(String weekday_str){
        String[] weekdays_str = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < weekdays_str.length; i ++) {
            if (weekdays_str[i].equals(weekday_str)){
                this.weekday = i;
                return;
            }
        }
    }

    public void setWeekday(int weekdays) {
        this.weekday = weekdays;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }
}
