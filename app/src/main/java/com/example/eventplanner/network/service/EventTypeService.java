package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.CreateEventTypeDTO;
import com.example.eventplanner.dto.UpdateEventTypeDTO;

import java.util.List;

import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventTypeService {

    @GET("event-types")
    Call<List<EventTypeDTO>> getAllEventTypes(@Header("Authorization") String token);

    @GET("event-types/{id}")
    Call<EventTypeDTO> getEventTypeById(
            @Header("Authorization") String token,
            @Path("id") Long id
    );

    @POST("event-types")
    Call<EventTypeDTO> createEventType(
            @Header("Authorization") String token,
            @Body CreateEventTypeDTO dto
    );

    @PUT("event-types/{id}")
    Call<EventTypeDTO> updateEventType(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Body UpdateEventTypeDTO dto
    );

    @PATCH("event-types/{id}/activate")
    Call<Void> activateEventType(
            @Header("Authorization") String token,
            @Path("id") Long id
    );

    @PATCH("event-types/{id}/deactivate")
    Call<Void> deactivateEventType(
            @Header("Authorization") String token,
            @Path("id") Long id
    );
}


