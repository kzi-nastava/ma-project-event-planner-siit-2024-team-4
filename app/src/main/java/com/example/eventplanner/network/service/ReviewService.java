package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.CreateReviewDTO;
import com.example.eventplanner.dto.ReviewDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewService {
    
    @POST("reviews")
    Call<ReviewDTO> createReview(@Header("Authorization") String token, @Body CreateReviewDTO dto);
    
    @GET("reviews/{solutionId}")
    Call<List<ReviewDTO>> getReviewsForSolution(@Header("Authorization") String token, @Path("solutionId") Long solutionId);
}
