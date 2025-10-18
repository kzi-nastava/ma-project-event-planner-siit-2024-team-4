package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.CreateEventRequest;
import com.example.eventplanner.dto.CreateLocationRequest;
import com.example.eventplanner.dto.LocationDTO;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Path;

public interface EventService {
    @GET("events")
    Call<List<EventDTO>> getAllEvents();
    
    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") int eventId);
    
    @POST("events")
    Call<EventDTO> createEvent(@Body CreateEventRequest createEventRequest);
    
    @GET("event-types")
    Call<List<EventTypeDTO>> getAllEventTypes();
    
    @POST("locations")
    Call<LocationDTO> createLocation(@Body CreateLocationRequest createLocationRequest);
}
