package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.BudgetPlanDTO;
import com.example.eventplanner.dto.CreateBudgetPlanDTO;
import com.example.eventplanner.dto.UpdateBudgetPlanDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BudgetService {
    
    @POST("budget")
    Call<BudgetPlanDTO> createBudgetPlan(@Header("Authorization") String token, @Body CreateBudgetPlanDTO dto);
    
    @PUT("budget/{id}")
    Call<BudgetPlanDTO> updateBudgetPlan(@Header("Authorization") String token, @Path("id") Long id, @Body UpdateBudgetPlanDTO dto);
    
    @GET("budget/organizer/{organizerId}")
    Call<List<BudgetPlanDTO>> getBudgetPlansByOrganizer(@Header("Authorization") String token, @Path("organizerId") Long organizerId);
    
    @GET("budget/{eventId}")
    Call<BudgetPlanDTO> getBudgetByEventId(@Header("Authorization") String token, @Path("eventId") Long eventId);
}
