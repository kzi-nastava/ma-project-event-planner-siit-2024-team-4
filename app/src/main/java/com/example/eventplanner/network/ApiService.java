package com.example.eventplanner.network;

import com.example.eventplanner.network.dto.EventDTO;
import com.example.eventplanner.network.dto.NotificationDTO;
import com.example.eventplanner.network.dto.SolutionDTO;

import java.util.Collection;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/homepage/top-5-events")
    Call<List<EventDTO>> getTop5Events();

    @GET("api/homepage/top-5-products-and-services")
    Call<Collection<SolutionDTO>> getTop5Solutions();
    // PAGEABLE EVENTS LIST
    @GET("api/events")
    Call<ApiService.Page<EventDTO>> getEvents(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort,   // npr. "date,asc" ili "name,desc"
            @Query("q") String q          // pretraga (može biti null)
    );

    // PAGEABLE SOLUTIONS LIST
    @GET("api/solutions")
    Call<ApiService.Page<SolutionDTO>> getSolutions(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort,   // npr. "price,asc" ili "name,asc"
            @Query("q") String q          // pretraga (može biti null)
    );

    // Minimalni "Page" wrapper – uskladi sa backendom ako već postoji drugačiji oblik
    class Page<T> {
        public List<T> content;
        public int number;           // trenutna strana
        public int size;             // veličina strane
        public long totalElements;   // ukupan broj elemenata
        public int totalPages;       // ukupan broj strana
        public boolean last;         // da li je poslednja
    }
    @GET("api/notifications")
    Call<List<NotificationDTO>> getNotifications();

    // QUICK SIGNUP – prilagodi path imenu tvog kontrolera
    @POST("api/auth/quick-signup")
    Call<TokenDTO> quickSignup(@Body QuickSignupRequest body);

    class QuickSignupRequest {
        public String email;
        public String password; // opcionalno
    }

    class TokenDTO {
        public String token;     // očekujemo JWT iz backenda
    }


}
