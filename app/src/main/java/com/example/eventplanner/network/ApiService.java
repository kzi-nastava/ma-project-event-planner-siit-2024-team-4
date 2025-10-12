package com.example.eventplanner.network;

import com.example.eventplanner.network.dto.EventDTO;
import com.example.eventplanner.network.dto.SolutionDTO;

import java.util.Collection;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/homepage/top-5-events")
    Call<List<EventDTO>> getTop5Events();

    @GET("api/homepage/top-5-products-and-services")
    Call<Collection<SolutionDTO>> getTop5Solutions();
}
