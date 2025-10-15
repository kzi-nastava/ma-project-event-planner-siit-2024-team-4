package com.example.eventplanner.dto;

public class ActivityDTO {
    private String startTime;
    private String endTime;
    private String name;
    private String description;
    private String location;

    // Constructors
    public ActivityDTO() {}

    public ActivityDTO(String startTime, String endTime, String name, String description, String location) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.description = description;
        this.location = location;
    }

    // Getters and Setters
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

