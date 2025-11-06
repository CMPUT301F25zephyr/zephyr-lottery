package com.example.zephyr_lottery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Event {

    private String name;
    private String description;
    private LocalDateTime lott_start_date;
    private LocalDateTime lott_end_date;
    private String times;
    private Date date_created;
    // Entrants on waiting list (usernames/emails)
    private ArrayList<String> entrants;

    public Event() {
        this.entrants = new ArrayList<>();
    }

    public Event(String name, String times) {
        this.name = name;
        this.times = times;
        this.entrants = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public ArrayList<String> getEntrants() {
        if (entrants == null) {
            entrants = new ArrayList<>();
        }
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = (entrants != null) ? entrants : new ArrayList<>();
    }

    public void addEntrant(String username) {
        if (username == null || username.isEmpty()) return;
        if (entrants == null) entrants = new ArrayList<>();
        if (!entrants.contains(username)) {
            entrants.add(username);
        }
    }

    public void removeEntrant(String username) {
        if (entrants == null || username == null) return;
        entrants.remove(username);
    }

    public int getEntrantsCount() {
        return getEntrants().size();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name)
                && Objects.equals(lott_start_date, event.lott_start_date)
                && Objects.equals(lott_end_date, event.lott_end_date)
                && Objects.equals(times, event.times);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, times);
    }
}
