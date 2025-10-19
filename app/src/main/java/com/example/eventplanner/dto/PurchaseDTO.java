package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;

public class PurchaseDTO {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("eventOrganizerId")
    private Long eventOrganizerId;
    
    public PurchaseDTO() {}
    
    public PurchaseDTO(Long id, Long eventOrganizerId) {
        this.id = id;
        this.eventOrganizerId = eventOrganizerId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEventOrganizerId() {
        return eventOrganizerId;
    }
    
    public void setEventOrganizerId(Long eventOrganizerId) {
        this.eventOrganizerId = eventOrganizerId;
    }
}
