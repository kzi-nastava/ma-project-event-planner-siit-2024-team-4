package com.example.eventplanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.FavoriteService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class FavoriteEventsActivity extends BaseActivity {

    private RecyclerView rvFavoriteEvents;
    private EventAdapterNoImage favoriteEventAdapter;
    private List<EventDTO> favoriteEventsList;
    private ProgressBar progressBar;
    private TextView noFavoritesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout into the content frame
        getLayoutInflater().inflate(R.layout.activity_favorite_events, findViewById(R.id.content_frame), true);

        rvFavoriteEvents = findViewById(R.id.rvFavoriteEvents);
        progressBar = findViewById(R.id.progressBar);
        noFavoritesText = findViewById(R.id.noFavoritesText);

        // Initialize events list
        favoriteEventsList = new ArrayList<>();

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvFavoriteEvents.setLayoutManager(linearLayoutManager);
        favoriteEventAdapter = new EventAdapterNoImage(new ArrayList<>(), this, true); // true = favorite events list
        rvFavoriteEvents.setAdapter(favoriteEventAdapter);

        // Load favorite events from server
        loadFavoriteEvents();
    }

    private void loadFavoriteEvents() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userIdStr = prefs.getString("user_id", null);
        
        if (userIdStr == null) {
            Toast.makeText(this, "Please log in to view favorites", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noFavoritesText.setVisibility(View.GONE);
        
        String authHeader = "Bearer " + getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", "");
        FavoriteService favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
        Call<List<EventDTO>> call = favoriteService.getFavoriteEvents(userIdStr, authHeader);
        
        call.enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    favoriteEventsList.clear();
                    favoriteEventsList.addAll(response.body());
                    favoriteEventAdapter.updateEvents(favoriteEventsList);
                    
                    if (favoriteEventsList.isEmpty()) {
                        noFavoritesText.setVisibility(View.VISIBLE);
                    } else {
                        noFavoritesText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(FavoriteEventsActivity.this, "Failed to load favorite events", Toast.LENGTH_SHORT).show();
                    noFavoritesText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoriteEventsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                noFavoritesText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        // This ensures that if user removes favorites from other activities, the list is updated
        if (favoriteEventsList != null) {
            loadFavoriteEvents();
        }
    }
}