package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.ProfileDTO;
import com.example.eventplanner.dto.UpdateProfileDTO;
import com.example.eventplanner.dto.ChangePasswordDTO;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileService {
    @GET("profile")
    Call<ProfileDTO> getProfile(@Header("Authorization") String token);
    
    @Multipart
    @PUT("profile")
    Call<ProfileDTO> updateProfile(
            @Header("Authorization") String token,
            @Part("dto") RequestBody dto,
            @Part MultipartBody.Part... files
    );
    
    @POST("profile")
    Call<String> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordDTO changePasswordDTO
    );
    
    @DELETE("profile")
    Call<Void> deactivateAccount(@Header("Authorization") String token);
}
