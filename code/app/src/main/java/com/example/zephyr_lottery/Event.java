package com.example.zephyr_lottery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * This class stores all of the details of an event from the database
 */
public class Event {
    private String name;
    private String description;
    private String organizer_email;
    private float price;
    private String location;
    private String time;
    private int weekday;
    private String period;
    private ArrayList<String> entrants;
    private int limit;
    private Date date_created;
    private LocalDateTime lott_start_date;
    private LocalDateTime lott_end_date;
    private int sampleSize;

    public Event() {
        entrants = new ArrayList<>();
    }
    
    /**
     * Creates a new event with a name, time, and email
     * @param name
     *  Name of the event
     * @param time
     *  The scheduled time of the event every week
     * @param organizer_email
     *  The email of the organizer
     */
    public Event(String name, String time, String organizer_email) {
        this.name = name;
        this.time = time;
        this.organizer_email = organizer_email;
        this.entrants = new ArrayList<>();

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Float.compare(price, event.price) == 0
                && weekday == event.weekday
                && Objects.equals(name, event.name)
                && Objects.equals(description, event.description)
                && Objects.equals(location, event.location)
                && Objects.equals(time, event.time)
                && Objects.equals(date_created, event.date_created)
                && Objects.equals(organizer_email, event.organizer_email)
                && Objects.equals(lott_start_date, event.lott_start_date)
                && Objects.equals(lott_end_date, event.lott_end_date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, price, location, time, weekday,
                date_created, organizer_email, lott_start_date, lott_end_date);
    }

    /**
     * Obtains the email of the organizer
     * @return
     * Returns the email as a String
     */
    public String getOrganizer_email() {
        return organizer_email;
    }

    /**
     * Sets the organizer email for the event
     * @param organizer_email
     * The email to set as the organizer of the event
     */
    public void setOrganizer_email(String organizer_email) {
        this.organizer_email = organizer_email;
    }

    /**
     * Obtains the name of the event
     * @return
     * Returns the name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name of the event
     * @param name
     * The new name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtains the recurring weekday integer on which this event is held
     * @return
     * Returns the weekday as an Integer
     */
    public int getWeekday() {
        return weekday;
    }

    /**
     * Obtains the recurring weekday on which this event is held
     * @return
     * Returns the weekday as a String
     */
    public String getWeekdayString() {
        String[] weekdaysStr = {
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };
        if (weekday < 0 || weekday >= weekdaysStr.length) return "";
        return weekdaysStr[this.weekday];
    }
    
    /**
     * Changes the weekday string on which this event is held
     * @param weekdayStr
     * The new weekday string
     */
    public void setWeekdayString(String weekdayStr) {
        String[] weekdaysStr = {
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };
        for (int i = 0; i < weekdaysStr.length; i++) {
            if (weekdaysStr[i].equals(weekdayStr)) {
                this.weekday = i;
                return;
            }
        }
        this.weekday = 0;
    }

    /**
     * Changes the weekday integer on which this event is held
     * @param weekday
     * The new weekday integer
     */
    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    /**
     * Obtains the current price of the event
     * @return
     * The price as a float
     */
    public float getPrice() {
        return price;
    }

    /**
     * Changes the price of the event
     * @param price
     * The new price of the event
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * Obtains the description of the event
     * @return
     * The description, as a String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description of the event
     * @param description
     * The new description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtains the recurring time at which this event takes place
     * @return
     * The time, as a String
     */
    public String getTime() {
        return time;
    }

    /**
     * Changes the recurring time at which this event takes place
     * @param time
     * The new time of the event
     */
    public void setTime(String time) {
        this.time = time;
    }

    // compatibility if some old code calls getTimes()/setTimes()

    /**
     * (compatibility) Obtains the recurring time at which this event takes place
     * @return
     * The time, as a String
     */
    public String getTimes() {
        return time;
    }

    /**
     * (compatibility) Changes the recurring tim at which this event takes place
     * @param time
     * The new time of the event
     */
    public void setTimes(String time) {
        this.time = time;
    }
    
    /**
     * Obtain the period over which the event runs
     * @return
     * The period, as a String
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Change the period over which the event runs
     * @param period
     * The new period over which the event runs
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * Obtain the location of the event
     * @return
     * The location of the event, as a String
     */
    public String getLocation() {
        return location;
    }

    /**
     * Change the location of the event
     * @param location
     * The new location of the event
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Obtain the date that the event was created
     * @return
     * The date the event was created, as a Date
     */
    public Date getDate_created() {
        return date_created;
    }

    /**
     * Set the date that the event was created
     * @param date_created
     * The date that the event is created
     */
    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    /**
     * Obtain the starting date for the lottery
     * @return
     * The starting date for the lottery
     */
    public LocalDateTime getLott_start_date() {
        return lott_start_date;
    }

    /**
     * Set the starting date for the lottery
     * @param lott_start_date
     * The new starting date for the lottery
     */
    public void setLott_start_date(LocalDateTime lott_start_date) {
        this.lott_start_date = lott_start_date;
    }

    /**
     * Obtain the date at which the lottery will be drawn for the event
     * @return
     * The date at which the lottery will be drawn
     */
    public LocalDateTime getLott_end_date() {
        return lott_end_date;
    }

    /**
     * Set the date at which the lottery will be drawn for the event
     * @param lott_end_date
     * The date at which the lottery will be drawn
     */
    public void setLott_end_date(LocalDateTime lott_end_date) {
        this.lott_end_date = lott_end_date;
    }
    
    /**
     * Obtain the number of users to be drawn from the lottery
     * @return
     * The number of users, as an int
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Change the number of users to be drawn from the lottery
     * @param sampleSize
     * The new number of users to be drawn from the lottery
     */
    public void setSampleSize(int sampleSize) {
        if (sampleSize < 0) {
            this.sampleSize = 0;
        } else {
            this.sampleSize = sampleSize;
        }
    }

    /**
     * Obtain the entrants signed up for the lottery
     * @return
     * The entrants signed up for the lottery, as an ArrayList of Strings
     */
    public ArrayList<String> getEntrants() {
        if (entrants == null) {
            entrants = new ArrayList<>();
        }
        return entrants;
    }

    /**
     * Set a new list of entrants to be signed up for the lottery
     * @param entrants
     * The new list of entrants
     */
    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants != null ? entrants : new ArrayList<>();
    }

    /**
     * Obtain the maximum number of entrants allowed to sign up for the lottery
     * @return
     * The maximum number of entrants, as an int
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Change the maximum number of entrants allowed to sign up for the lottery
     * @param limit
     * The new maximum number of entrants
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }
}
