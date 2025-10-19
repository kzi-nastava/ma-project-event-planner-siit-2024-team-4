package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.ChatMessageDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatService {
    
    @GET("chats")
    Call<List<ChatMessageDTO>> getAllChats(@Header("Authorization") String token);
    
    @GET("chats/{chatId}")
    Call<ChatMessageDTO> getChatById(@Header("Authorization") String token, @Path("chatId") String chatId);
    
    @POST("chats")
    Call<ChatMessageDTO> createChat(@Header("Authorization") String token, @Body ChatMessageDTO chatMessage);
}
