package com.example.eventplanner.dto;

import java.util.List;

public class UpdateProductDTO {
    
    private Long productId;
    private String name;
    private String description;
    private double price;
    private double discount;
    private List<String> imageURLs;
    private boolean visible;
    private boolean available;
    private Long providerId;
    private Long categoryId;
    private List<Long> eventTypes;
    
    public UpdateProductDTO() {
    }
    
    public UpdateProductDTO(Long productId, String name, String description, double price, double discount, 
                           List<String> imageURLs, boolean visible, boolean available, 
                           Long providerId, Long categoryId, List<Long> eventTypes) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imageURLs = imageURLs;
        this.visible = visible;
        this.available = available;
        this.providerId = providerId;
        this.categoryId = categoryId;
        this.eventTypes = eventTypes;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
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
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public Long getProviderId() {
        return providerId;
    }
    
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public List<Long> getEventTypes() {
        return eventTypes;
    }
    
    public void setEventTypes(List<Long> eventTypes) {
        this.eventTypes = eventTypes;
    }
}
