package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.CreatePurchaseDTO;
import com.example.eventplanner.dto.PurchaseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PurchaseService {
    
    @POST("purchase")
    Call<PurchaseDTO> createPurchase(@Header("Authorization") String token, @Body CreatePurchaseDTO dto);
    
    @GET("purchase/{id}")
    Call<PurchaseDTO> getPurchaseById(@Header("Authorization") String token, @Path("id") Long id);
}
