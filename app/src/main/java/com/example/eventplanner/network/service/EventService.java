package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventDTO;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EventService {
    @GET("events")
    Call<List<EventDTO>> getAllEvents();
    
    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") int eventId);
}
