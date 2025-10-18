package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteService {
    
    @POST("favorites/events/{userId}/{eventId}")
    Call<Void> addEventToFavorites(
            @Path("userId") String userId,
            @Path("eventId") int eventId,
            @Header("Authorization") String token
    );
    
    @DELETE("favorites/events/{userId}/{eventId}")
    Call<Void> removeEventFromFavorites(
            @Path("userId") String userId,
            @Path("eventId") int eventId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/events/{userId}")
    Call<List<EventDTO>> getFavoriteEvents(
            @Path("userId") String userId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/events/events/{userId}/{eventId}")
    Call<Boolean> checkIfEventIsFavorite(
            @Path("userId") String userId,
            @Path("eventId") int eventId,
            @Header("Authorization") String token
    );
}
