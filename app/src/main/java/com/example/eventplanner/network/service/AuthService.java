package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.LoginRequest;
import com.example.eventplanner.dto.LoginResponse;
import com.example.eventplanner.dto.RegistrationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AuthService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Multipart
    @POST("auth/register")
    Call<RegistrationResponse> register(
            @Part("dto") RequestBody dto,
            @Part List<MultipartBody.Part> files
    );
}
