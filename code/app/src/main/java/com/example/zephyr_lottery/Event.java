package com.example.zephyr_lottery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

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

    public String getWeekdayString() {
        String[] weekdaysStr = {
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };
        if (weekday < 0 || weekday >= weekdaysStr.length) return "";
        return weekdaysStr[this.weekday];
    }

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

    public void setWeekday(int weekday) {
        this.weekday = weekday;
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

    // compatibility if some old code calls getTimes()/setTimes()
    public String getTimes() {
        return time;
    }

    public void setTimes(String time) {
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

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public LocalDateTime getLott_start_date() {
        return lott_start_date;
    }

    public void setLott_start_date(LocalDateTime lott_start_date) {
        this.lott_start_date = lott_start_date;
    }

    public LocalDateTime getLott_end_date() {
        return lott_end_date;
    }

    public void setLott_end_date(LocalDateTime lott_end_date) {
        this.lott_end_date = lott_end_date;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        if (sampleSize < 0) {
            this.sampleSize = 0;
        } else {
            this.sampleSize = sampleSize;
        }
    }

    public ArrayList<String> getEntrants() {
        if (entrants == null) {
            entrants = new ArrayList<>();
        }
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants != null ? entrants : new ArrayList<>();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
