package com.example.eventplanner.dto;

import java.io.Serializable;
import java.util.List;

public class ProductDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private Boolean available;
    private List<String> imageURLs;
    private Long categoryId;
    private String categoryName;
    private Long serviceProviderId;
    private String serviceProviderName;
    private Long eventTypeId;
    private String eventTypeName;
    
    // Nested objects from JSON
    private CategoryDTO category;
    private List<EventTypeDTO> eventTypes;
    private Long providerId;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String description, Double price, Double discount, 
                     Boolean available, List<String> imageURLs, Long categoryId, String categoryName,
                     Long serviceProviderId, String serviceProviderName, Long eventTypeId, String eventTypeName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.available = available;
        this.imageURLs = imageURLs;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.serviceProviderId = serviceProviderId;
        this.serviceProviderName = serviceProviderName;
        this.eventTypeId = eventTypeId;
        this.eventTypeName = eventTypeName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(Long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    // Getters and setters for nested objects
    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public List<EventTypeDTO> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<EventTypeDTO> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", discount=" + discount +
                ", available=" + available +
                ", imageURLs=" + imageURLs +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", serviceProviderId=" + serviceProviderId +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", eventTypeId=" + eventTypeId +
                ", eventTypeName='" + eventTypeName + '\'' +
                '}';
    }
}
