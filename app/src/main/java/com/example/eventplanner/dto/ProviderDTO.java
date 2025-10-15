package com.example.eventplanner.dto;

import java.util.List;

public class ProviderDTO {
    private Long id;
    private String email;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private List<String> imageURLs;
    private boolean verified;
    private boolean deactivated;

    public ProviderDTO() {}

    public ProviderDTO(Long id, String email, String name, String description, String address, 
                      String phoneNumber, List<String> imageURLs, boolean verified, boolean deactivated) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imageURLs = imageURLs;
        this.verified = verified;
        this.deactivated = deactivated;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }
}
