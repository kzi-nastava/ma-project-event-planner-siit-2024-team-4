package com.example.eventplanner.activities;

public class Event {
    private String name;
    private int imageResId;

    public Event(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
