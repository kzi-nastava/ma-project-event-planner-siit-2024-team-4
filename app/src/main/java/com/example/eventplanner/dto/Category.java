package com.example.eventplanner.dto;

public class Category {
    public Long id;
    public String name;
    public String description;
    public boolean isApprovedByAdmin;
    
    public Category() {}
    
    public Category(Long id, String name, String description, boolean isApprovedByAdmin) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isApprovedByAdmin = isApprovedByAdmin;
    }
    
    // Getters and setters
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
    
    public boolean isApprovedByAdmin() {
        return isApprovedByAdmin;
    }
    
    public void setApprovedByAdmin(boolean isApprovedByAdmin) {
        this.isApprovedByAdmin = isApprovedByAdmin;
    }
}
