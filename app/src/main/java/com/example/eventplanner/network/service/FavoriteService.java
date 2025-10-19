package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.FavoriteSolutionDTO;
import com.example.eventplanner.dto.ProductDTO;
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
    
    // Service favorites
    @POST("favorites/solutions/{userId}/{serviceId}")
    Call<Void> addServiceToFavorites(
            @Path("userId") String userId,
            @Path("serviceId") Long serviceId,
            @Header("Authorization") String token
    );
    
    @DELETE("favorites/solutions/{userId}/{serviceId}")
    Call<Void> removeServiceFromFavorites(
            @Path("userId") String userId,
            @Path("serviceId") Long serviceId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/solutions/{userId}")
    Call<List<FavoriteSolutionDTO>> getFavoriteServices(
            @Path("userId") String userId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/solutions/{userId}/{serviceId}")
    Call<Boolean> checkIfServiceIsFavorite(
            @Path("userId") String userId,
            @Path("serviceId") Long serviceId,
            @Header("Authorization") String token
    );
    
    // Product favorites (products also use solutions endpoint)
    @POST("favorites/solutions/{userId}/{productId}")
    Call<Void> addProductToFavorites(
            @Path("userId") String userId,
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
    
    @DELETE("favorites/solutions/{userId}/{productId}")
    Call<Void> removeProductFromFavorites(
            @Path("userId") String userId,
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/solutions/{userId}")
    Call<List<FavoriteSolutionDTO>> getFavoriteProducts(
            @Path("userId") String userId,
            @Header("Authorization") String token
    );
    
    @GET("favorites/solutions/{userId}/{productId}")
    Call<Boolean> checkIfProductIsFavorite(
            @Path("userId") String userId,
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
}
