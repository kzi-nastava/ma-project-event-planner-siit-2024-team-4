package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventTypeDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface EventTypeService {

    @GET("event-types")
    Call<List<EventTypeDTO>> getAllEventTypes(@Header("Authorization") String token);
}
