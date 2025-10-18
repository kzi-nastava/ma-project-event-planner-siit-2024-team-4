package com.example.eventplanner.dto;

import java.io.Serializable;

public class CategoryDTO implements Serializable {
    public Long id;
    public String name;
    public String description;
    public boolean isApprovedByAdmin;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String description, boolean isApprovedByAdmin) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isApprovedByAdmin = isApprovedByAdmin;
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

    public boolean isApprovedByAdmin() {
        return isApprovedByAdmin;
    }

    public void setApprovedByAdmin(boolean approvedByAdmin) {
        isApprovedByAdmin = approvedByAdmin;
    }
}