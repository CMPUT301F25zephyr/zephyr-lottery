package com.example.zephyr_lottery;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class Event {
    //attributes will need to be updated later on.
    private String id;
    private String name;
    private String description;
    private LocalDateTime lott_start_date; //lottery start date
    private LocalDateTime lott_end_date; //lottery end date
    private String times; //string that shows when the actual event happens
    private Date date_created; //use to order the latest events screen
    //NEED TO ADD: arrayList of entrants.
    private Integer capacity;
    private List<String> waitingList;
    private List<String> attendees;
    private Map<String, Object> geofence; // keys: "lat" (Double), "lng" (Double), "radiusMeters" (Double)
    private String posterUrl;

    // default constructor required by Firestore
    public Event() {}
    public Event(String name, String times) {
        this.id = id;
        this.name = name;
        this.times = times;
        //add dates and times to constructor
        // -> when we implement organizers creating events
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate_created() { return date_created; }
    public void setDate_created(Date date_created) { this.date_created = date_created; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public List<String> getWaitingList() { return waitingList; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }

    public List<String> getAttendees() { return attendees; }
    public void setAttendees(List<String> attendees) { this.attendees = attendees; }

    public Map<String, Object> getGeofence() { return geofence; }
    public void setGeofence(Map<String, Object> geofence) { this.geofence = geofence; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name) && Objects.equals(description, event.description) && Objects.equals(lott_start_date, event.lott_start_date) && Objects.equals(lott_end_date, event.lott_end_date) && Objects.equals(times, event.times);
    }

    @Override
    public int hashCode() {
        //add more to hashcode when that is implemented. need description, possibly start times, author.
        int n = name == null ? 0 : name.hashCode();
        int t = times == null ? 0 : times.hashCode();
        return n ^ t;
    }
}
