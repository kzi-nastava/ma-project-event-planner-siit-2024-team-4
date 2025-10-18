package com.example.eventplanner.dto;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private double discount;
    private List<String> imageURLs;
    
    @SerializedName("visible")
    private boolean isVisible;
    
    @SerializedName("available")
    private boolean isAvailable;
    
    private CategoryDTO category;
    private Long providerId;
    private ProviderDTO provider;
    private List<EventTypeDTO> eventTypes;
    private double rating;
    private Integer duration;
    private Integer minEngagement;
    private Integer maxEngagement;
    private int reservationDue;
    private int cancelationDue;
    private String reservationType; // AUTOMATIC or MANUAL

    public ServiceDTO() {}

    public ServiceDTO(Long id, String name, String description, double price, double discount, 
                     List<String> imageURLs, boolean isVisible, boolean isAvailable, 
                     CategoryDTO category, Long providerId, List<EventTypeDTO> eventTypes, 
                     double rating, Integer duration, Integer minEngagement, Integer maxEngagement, 
                     int reservationDue, int cancelationDue, String reservationType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imageURLs = imageURLs;
        this.isVisible = isVisible;
        this.isAvailable = isAvailable;
        this.category = category;
        this.providerId = providerId;
        this.eventTypes = eventTypes;
        this.rating = rating;
        this.duration = duration;
        this.minEngagement = minEngagement;
        this.maxEngagement = maxEngagement;
        this.reservationDue = reservationDue;
        this.cancelationDue = cancelationDue;
        this.reservationType = reservationType;
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

    @SerializedName("visible")
    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @SerializedName("available")
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public ProviderDTO getProvider() {
        return provider;
    }

    public void setProvider(ProviderDTO provider) {
        this.provider = provider;
    }

    public List<EventTypeDTO> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<EventTypeDTO> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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
