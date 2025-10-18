package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.ProductDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ProductService {
    
    @GET("products")
    Call<List<ProductDTO>> getAllProducts(@Header("Authorization") String token);
    
    @GET("products")
    Call<List<ProductDTO>> getMyProducts(
            @Header("Authorization") String token,
            @Query("providerId") String providerId
    );
    
    @GET("products/search")
    Call<List<ProductDTO>> searchProducts(
            @Query("name") String name
    );
    
    @GET("products/filter")
    Call<List<ProductDTO>> filterProducts(
            @Query("categoryId") String categoryId,
            @Query("eventTypeId") String eventTypeId,
            @Query("isAvailable") String isAvailable,
            @Query("minPrice") String minPrice,
            @Query("maxPrice") String maxPrice
    );
    
    @GET("products/{id}")
    Call<ProductDTO> getProductById(@Query("id") Long id);
}
