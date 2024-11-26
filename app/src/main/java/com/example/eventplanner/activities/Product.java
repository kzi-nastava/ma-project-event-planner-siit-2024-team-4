package com.example.eventplanner.activities;

public class Product {
    private String name;
    private int imageResId;
    private String category;

    // Constructor
    public Product(String name, int imageResId, String category) {
        this.name = name;
        this.imageResId = imageResId;
        this.category = category;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
