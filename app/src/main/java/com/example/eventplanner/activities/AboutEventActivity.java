package com.example.eventplanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.EventService;
import com.example.eventplanner.network.service.FavoriteService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutEventActivity extends BaseActivity implements OnMapReadyCallback {

    private EventDTO event;
    private boolean isLoading = true;
    private boolean isFavorite = false;
    private String userId;
    private FavoriteService favoriteService;

    // UI Components
    private ProgressBar progressBar;
    private TextView eventName, eventDate, eventDescription, maxParticipants, locationText;
    private ImageView favoriteIcon;
    private Button downloadPdfBtn;
    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_about_event, contentFrame, true);

        initViews();
        setupClickListeners();
        
        // Initialize MapView with savedInstanceState
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
        
        // Get event ID from intent
        int eventId = getIntent().getIntExtra("event_id", -1);
        if (eventId != -1) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get current user
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        
        // Initialize FavoriteService
        favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        eventName = findViewById(R.id.eventName);
        eventDate = findViewById(R.id.eventDate);
        eventDescription = findViewById(R.id.eventDescription);
        maxParticipants = findViewById(R.id.maxParticipants);
        locationText = findViewById(R.id.locationText);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        mapView = findViewById(R.id.mapView);
    }

    private void setupClickListeners() {
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        downloadPdfBtn.setOnClickListener(v -> generatePDF());
    }

    private void fetchEventDetails(int eventId) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<EventDTO> call = eventService.getEventById(eventId);

        call.enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    event = response.body();
                    displayEventDetails();
                    
                    if (userId != null) {
                        checkIfFavorite();
                    }
                } else {
                    Toast.makeText(AboutEventActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AboutEventActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayEventDetails() {
        if (event == null) return;

        eventName.setText(event.getName());
        
        // Format date
        String formattedDate = formatDate(event.getStartDate());
        eventDate.setText("Date: " + formattedDate);
        
        eventDescription.setText(event.getDescription());
        maxParticipants.setText("Max. participants: " + event.getParticipants());
        
        if (event.getLocation() != null) {
            locationText.setText("Location: " + event.getLocation().getAddress());
        } else {
            locationText.setText("Location: Not specified");
        }
        
        // Update map if it's already ready
        if (googleMap != null) {
            updateMapLocation();
        }
    }

    private String formatDate(String dateString) {
        try {
            // Simple date formatting - you might want to use SimpleDateFormat for better formatting
            return dateString.split("T")[0]; // Get just the date part
        } catch (Exception e) {
            return dateString;
        }
    }

    private void toggleFavorite() {
        if (userId == null) {
            Toast.makeText(this, "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event == null) {
            Toast.makeText(this, "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toggle favorite status locally first for better UX
        isFavorite = !isFavorite;
        updateFavoriteIcon();

        // Get JWT token
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            isFavorite = !isFavorite; // Revert local change
            updateFavoriteIcon();
            return;
        }

        String authHeader = "Bearer " + token;
        Call<Void> call;

        if (isFavorite) {
            // Add to favorites
            call = favoriteService.addEventToFavorites(userId, event.getId(), authHeader);
        } else {
            // Remove from favorites
            call = favoriteService.removeEventFromFavorites(userId, event.getId(), authHeader);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(AboutEventActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Revert local change if API call failed
                    isFavorite = !isFavorite;
                    updateFavoriteIcon();
                    Toast.makeText(AboutEventActivity.this, "Failed to update favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Revert local change if API call failed
                isFavorite = !isFavorite;
                updateFavoriteIcon();
                Toast.makeText(AboutEventActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon() {
        if (favoriteIcon != null) {
            favoriteIcon.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart_empty);
        }
    }

    private void checkIfFavorite() {
        if (userId == null || event == null || favoriteService == null) {
            updateFavoriteIcon();
            return;
        }

        // Get JWT token
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            updateFavoriteIcon();
            return;
        }

        String authHeader = "Bearer " + token;
        Call<Boolean> call = favoriteService.checkIfEventIsFavorite(userId, event.getId(), authHeader);
        
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body();
                    updateFavoriteIcon();
                } else {
                    isFavorite = false;
                    updateFavoriteIcon();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                isFavorite = false;
                updateFavoriteIcon();
            }
        });
    }

    private void generatePDF() {
        // TODO: Implement PDF generation
        Toast.makeText(this, "PDF generation - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d("AboutEvent", "onMapReady called");
        googleMap = map;
        
        if (event != null && event.getLocation() != null) {
            updateMapLocation();
        }
    }

    private void updateMapLocation() {
        if (googleMap == null) {
            Log.d("AboutEvent", "GoogleMap is null, cannot update location");
            return;
        }

        LatLng location;
        String title;
        String snippet;
        
        if (event != null && event.getLocation() != null) {
            // Use real event location
            Log.d("AboutEvent", "Using real event location: " + event.getLocation().getLatitude() + ", " + event.getLocation().getLongitude());
            location = new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude());
            title = event.getName();
            snippet = event.getLocation().getName() + " - " + event.getLocation().getAddress();
        } else {
            // Use default location (Belgrade) for testing
            Log.d("AboutEvent", "Using default location (Belgrade)");
            location = new LatLng(44.7866, 20.4489); // Belgrade coordinates
            title = "Default Location";
            snippet = "Belgrade, Serbia";
        }
        
        // Add marker for location
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(snippet));
        
        // Move camera to location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        
        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
