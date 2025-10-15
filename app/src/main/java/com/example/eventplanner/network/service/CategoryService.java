package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.CreateCategoryDTO;
import com.example.eventplanner.dto.UpdateCategoryDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryService {

    @GET("categories")
    Call<List<CategoryDTO>> getAllCategories(@Header("Authorization") String token);

    @GET("categories/approved")
    Call<List<CategoryDTO>> getAllApprovedCategories(@Header("Authorization") String token);

    @POST("categories")
    Call<CategoryDTO> createCategory(
            @Header("Authorization") String token,
            @Body CreateCategoryDTO dto
    );

    @PUT("categories/{id}")
    Call<CategoryDTO> updateCategory(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Body UpdateCategoryDTO dto
    );

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(
            @Header("Authorization") String token,
            @Path("id") Long id
    );
}


