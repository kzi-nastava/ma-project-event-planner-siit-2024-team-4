package com.example.eventplanner.dto;

import java.util.List;

public class EventDTO {
    private int id;
    private String name;
    private String description;
    private int participants;
    private boolean isPublic;
    private String startDate;
    private String endDate;
    private LocationDTO location;
    private String eventTypeName;
    private List<ActivityDTO> activities;

    // Constructors
    public EventDTO() {}

    public EventDTO(int id, String name, String description, int participants, boolean isPublic, 
                   String startDate, String endDate, LocationDTO location, String eventTypeName, 
                   List<ActivityDTO> activities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.participants = participants;
        this.isPublic = isPublic;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.eventTypeName = eventTypeName;
        this.activities = activities;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }
}
