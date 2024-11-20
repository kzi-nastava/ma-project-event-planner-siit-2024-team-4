package com.example.eventplanner.activities;

public class Event {
    private String name;
    private int imageResId;
    private String type; // Dodato polje za tip događaja

    public Event(String name, int imageResId, String type) {
        this.name = name;
        this.imageResId = imageResId;
        this.type = type; // Inicijalizacija tipa događaja
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getType() {
        return type; // Getter za tip događaja
    }
}
