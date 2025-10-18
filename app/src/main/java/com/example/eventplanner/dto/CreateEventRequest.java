package com.example.eventplanner.dto;

import java.util.List;

public class CreateEventRequest {
    private String name;
    private String description;
    private int participants;
    private boolean isPublic;
    private String startDate;
    private String endDate;
    private Long locationId;
    private Long eventTypeId;
    private List<CreateActivityRequest> agenda;
    private Long organizerId;

    public CreateEventRequest() {}

    public CreateEventRequest(String name, String description, int participants, boolean isPublic,
                             String startDate, String endDate, Long locationId, Long eventTypeId,
                             List<CreateActivityRequest> agenda, Long organizerId) {
        this.name = name;
        this.description = description;
        this.participants = participants;
        this.isPublic = isPublic;
        this.startDate = startDate;
        this.endDate = endDate;
        this.locationId = locationId;
        this.eventTypeId = eventTypeId;
        this.agenda = agenda;
        this.organizerId = organizerId;
    }

    // Getters and Setters
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public List<CreateActivityRequest> getAgenda() {
        return agenda;
    }

    public void setAgenda(List<CreateActivityRequest> agenda) {
        this.agenda = agenda;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
}
