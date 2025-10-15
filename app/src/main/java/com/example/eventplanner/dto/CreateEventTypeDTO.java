package com.example.eventplanner.dto;

import java.util.List;

public class CreateEventTypeDTO {
    public String name;
    public String description;
    public List<Category> suggestedCategories;
    
    public CreateEventTypeDTO() {}
    
    public CreateEventTypeDTO(String name, String description, List<Category> suggestedCategories) {
        this.name = name;
        this.description = description;
        this.suggestedCategories = suggestedCategories;
    }
    
    // Getters and setters
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
    
    public List<Category> getSuggestedCategories() {
        return suggestedCategories;
    }
    
    public void setSuggestedCategories(List<Category> suggestedCategories) {
        this.suggestedCategories = suggestedCategories;
    }
}
