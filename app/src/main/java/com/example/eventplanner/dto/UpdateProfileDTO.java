package com.example.eventplanner.dto;

import java.util.List;

public class UpdateProfileDTO {
    private String address;
    private String phoneNumber;
    private List<String> imageURLs;
    private String name;
    private String description;
    private String lastName;

    public UpdateProfileDTO() {}

    public UpdateProfileDTO(String address, String phoneNumber, List<String> imageURLs, String name, String description, String lastName) {
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imageURLs = imageURLs;
        this.name = name;
        this.description = description;
        this.lastName = lastName;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}