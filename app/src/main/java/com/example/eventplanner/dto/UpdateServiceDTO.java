package com.example.eventplanner.dto;

import java.util.List;

public class UpdateServiceDTO {
    private Long serviceId;
    private String name;
    private String description;
    private double price;
    private double discount;
    private List<String> imageURLs;
    private boolean isVisible;
    private boolean isAvailable;
    private List<Long> eventTypeIds;
    private Integer duration;
    private Integer minEngagement;
    private Integer maxEngagement;
    private int reservationDue;
    private int cancelationDue;
    private String reservationType; // AUTOMATIC or MANUAL

    public UpdateServiceDTO() {}

    public UpdateServiceDTO(Long serviceId, String name, String description, double price, double discount, 
                           List<String> imageURLs, boolean isVisible, boolean isAvailable, 
                           List<Long> eventTypeIds, Integer duration, Integer minEngagement, Integer maxEngagement, 
                           int reservationDue, int cancelationDue, String reservationType) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imageURLs = imageURLs;
        this.isVisible = isVisible;
        this.isAvailable = isAvailable;
        this.eventTypeIds = eventTypeIds;
        this.duration = duration;
        this.minEngagement = minEngagement;
        this.maxEngagement = maxEngagement;
        this.reservationDue = reservationDue;
        this.cancelationDue = cancelationDue;
        this.reservationType = reservationType;
    }

    // Getters and Setters
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(List<Long> eventTypeIds) {
        this.eventTypeIds = eventTypeIds;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getMinEngagement() {
        return minEngagement;
    }

    public void setMinEngagement(Integer minEngagement) {
        this.minEngagement = minEngagement;
    }

    public Integer getMaxEngagement() {
        return maxEngagement;
    }

    public void setMaxEngagement(Integer maxEngagement) {
        this.maxEngagement = maxEngagement;
    }

    public int getReservationDue() {
        return reservationDue;
    }

    public void setReservationDue(int reservationDue) {
        this.reservationDue = reservationDue;
    }

    public int getCancelationDue() {
        return cancelationDue;
    }

    public void setCancelationDue(int cancelationDue) {
        this.cancelationDue = cancelationDue;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }
}
