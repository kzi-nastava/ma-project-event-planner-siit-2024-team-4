package com.example.eventplanner.dto;

import java.util.List;

public class EventTypeDTO {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<CategoryDTO> suggestedCategories;

    public EventTypeDTO() {}

    public EventTypeDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public EventTypeDTO(Long id, String name, String description, boolean active, List<CategoryDTO> suggestedCategories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.suggestedCategories = suggestedCategories;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<CategoryDTO> getSuggestedCategories() {
        return suggestedCategories;
    }

    public void setSuggestedCategories(List<CategoryDTO> suggestedCategories) {
        this.suggestedCategories = suggestedCategories;
    }
}