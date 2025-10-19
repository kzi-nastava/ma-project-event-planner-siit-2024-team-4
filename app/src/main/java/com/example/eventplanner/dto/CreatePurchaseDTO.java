package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;

public class CreatePurchaseDTO {
    @SerializedName("productId")
    private Long productId;
    
    @SerializedName("eventOrganizerId")
    private Long eventOrganizerId;
    
    @SerializedName("eventId")
    private Long eventId;
    
    public CreatePurchaseDTO() {}
    
    public CreatePurchaseDTO(Long productId, Long eventOrganizerId, Long eventId) {
        this.productId = productId;
        this.eventOrganizerId = eventOrganizerId;
        this.eventId = eventId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getEventOrganizerId() {
        return eventOrganizerId;
    }
    
    public void setEventOrganizerId(Long eventOrganizerId) {
        this.eventOrganizerId = eventOrganizerId;
    }
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
