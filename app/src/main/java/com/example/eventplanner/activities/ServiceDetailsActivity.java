package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ServiceImageAdapter;
import com.example.eventplanner.config.ApiConfig;
import com.example.eventplanner.dto.FavoriteSolutionDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.FavoriteService;
import com.example.eventplanner.network.service.ServiceService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailsActivity extends AppCompatActivity {

    private ServiceDTO service;
    private Long serviceId;
    private boolean isFavorite = false;
    private boolean isFromFavorites = false;
    private FavoriteService favoriteService;
    
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicators;
    private ImageView favoriteIcon;
    private TextView tvName, tvDescription, tvPrice, tvDiscount, tvCategory;
    private TextView tvAvailability, tvVisibility, tvDuration, tvReservationType;
    private TextView tvEventTypes, tvProviderInfo;
    private Button btnEdit, btnDelete, btnBook, btnProviderProfile, btnChatWithProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);
        
        serviceId = getIntent().getLongExtra("serviceId", -1L);
        isFromFavorites = getIntent().getBooleanExtra("isFromFavorites", false);
        
        if (serviceId == -1L) {
            Toast.makeText(this, "Service ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadServiceDetails();
    }
    
    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        tvName = findViewById(R.id.tvServiceNameDetails);
        tvDescription = findViewById(R.id.tvServiceDescriptionDetails);
        tvPrice = findViewById(R.id.tvServicePriceDetails);
        tvDiscount = findViewById(R.id.tvServiceDiscountDetails);
        tvCategory = findViewById(R.id.tvServiceCategoryDetails);
        tvAvailability = findViewById(R.id.tvServiceAvailabilityDetails);
        tvVisibility = findViewById(R.id.tvServiceVisibilityDetails);
        tvDuration = findViewById(R.id.tvServiceDurationDetails);
        tvReservationType = findViewById(R.id.tvServiceReservationTypeDetails);
        tvEventTypes = findViewById(R.id.tvServiceEventTypesDetails);
        tvProviderInfo = findViewById(R.id.tvServiceProviderInfoDetails);
        
        btnEdit = findViewById(R.id.btnEditServiceDetails);
        btnDelete = findViewById(R.id.btnDeleteServiceDetails);
        btnBook = findViewById(R.id.btnBookServiceDetails);
        btnProviderProfile = findViewById(R.id.btnProviderProfileDetails);
        btnChatWithProvider = findViewById(R.id.btnChatWithProviderDetails);
        
        btnEdit.setOnClickListener(v -> editService());
        btnDelete.setOnClickListener(v -> deleteService());
        btnBook.setOnClickListener(v -> bookService());
        btnProviderProfile.setOnClickListener(v -> viewProviderProfile());
        btnChatWithProvider.setOnClickListener(v -> chatWithProvider());
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        
        // Initialize favorite service
        favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
    }
    
    private void loadServiceDetails() {
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        
        serviceAPI.getServiceById(getAuthHeader(), serviceId).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    service = response.body();
                    populateViews();
                } else {
                    Toast.makeText(ServiceDetailsActivity.this, "Failed to load service", Toast.LENGTH_SHORT).show();
                    Toast.makeText(ServiceDetailsActivity.this, "Error loading service details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Toast.makeText(ServiceDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void populateViews() {
        if (service == null) return;
        
        tvName.setText(service.getName());
        tvDescription.setText(service.getDescription());
        tvPrice.setText(String.format("%.2f RSD", service.getPrice()));
        
        if (service.getDiscount() > 0) {
            tvDiscount.setText(String.format("%.0f%% OFF", service.getDiscount()));
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }
        
        if (service.getCategory() != null) {
            tvCategory.setText(service.getCategory().name);
        } else {
            tvCategory.setText("No category");
        }
        
        tvAvailability.setText(service.isAvailable() ? "Available" : "Not Available");
        tvVisibility.setText(service.isVisible() ? "Visible" : "Not Visible");
        
        if (service.getDuration() != null) {
            tvDuration.setText("Duration: " + service.getDuration() + " minutes");
        } else if (service.getMinEngagement() != null && service.getMaxEngagement() != null) {
            tvDuration.setText("Engagement: " + service.getMinEngagement() + " - " + service.getMaxEngagement() + " minutes");
        } else {
            tvDuration.setText("Duration: Not specified");
        }
        
        if (service.getReservationType() != null) {
            tvReservationType.setText("Reservation: " + service.getReservationType());
        } else {
            tvReservationType.setText("Reservation: Not specified");
        }
        
        if (service.getEventTypes() != null && !service.getEventTypes().isEmpty()) {
            StringBuilder eventTypesText = new StringBuilder("Event Types: ");
            for (int i = 0; i < service.getEventTypes().size(); i++) {
                if (i > 0) eventTypesText.append(", ");
                eventTypesText.append(service.getEventTypes().get(i).getName());
            }
            tvEventTypes.setText(eventTypesText.toString());
        } else {
            tvEventTypes.setText("Event Types: Not specified");
        }
        
        if (service.getProvider() != null && service.getProvider().getName() != null) {
            tvProviderInfo.setText("Provider: " + service.getProvider().getName());
        } else if (service.getProviderId() != null) {
            tvProviderInfo.setText("Provider ID: " + service.getProviderId());
        } else {
            tvProviderInfo.setText("Provider: Not specified");
        }
        
        if (service.getImageURLs() != null && !service.getImageURLs().isEmpty()) {
            ServiceImageAdapter imageAdapter = new ServiceImageAdapter(service.getImageURLs());
            viewPagerImages.setAdapter(imageAdapter);
            viewPagerImages.setVisibility(View.VISIBLE);
            
            if (service.getImageURLs().size() > 1) {
                setupIndicators(service.getImageURLs().size());
                layoutIndicators.setVisibility(View.VISIBLE);
            } else {
                layoutIndicators.setVisibility(View.GONE);
            }
        } else {
            viewPagerImages.setVisibility(View.GONE);
            layoutIndicators.setVisibility(View.GONE);
        }
        
        setupButtons();
    }
    
    private void setupButtons() {
        boolean isServiceProvider = isServiceProvider();
        boolean isOwner = isServiceOwner();
        boolean isAdmin = isAdmin();
        boolean isEventOrganizer = isEventOrganizer();
        
        if (isServiceProvider && isOwner) {
            // My Services - show edit/delete buttons
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnBook.setVisibility(View.GONE);
            btnProviderProfile.setVisibility(View.GONE);
            btnChatWithProvider.setVisibility(View.GONE);
            favoriteIcon.setVisibility(View.GONE);
        } else if (isServiceProvider) {
            // Other SPP's services - show nothing
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnBook.setVisibility(View.GONE);
            btnProviderProfile.setVisibility(View.GONE);
            btnChatWithProvider.setVisibility(View.GONE);
            favoriteIcon.setVisibility(View.GONE);
        } else {
            // EO/Admin viewing services - show profile, chat, and favorite
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnBook.setVisibility(service.isAvailable() ? View.VISIBLE : View.GONE);
            btnProviderProfile.setVisibility(View.VISIBLE);
            btnChatWithProvider.setVisibility(View.VISIBLE);
            favoriteIcon.setVisibility(View.VISIBLE);
            
            favoriteIcon.setImageResource(R.drawable.heart_empty); // Set initial state
            // Always check if service is favorite
            checkIfFavorite();
        }
    }
    
    private void editService() {
        Intent intent = new Intent(this, EditServiceActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }
    
    private void deleteService() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete " + service.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
                    serviceAPI.deleteService(getAuthHeader(), service.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ServiceDetailsActivity.this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ServiceDetailsActivity.this, "Error deleting service", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(ServiceDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void bookService() {
        // TODO: Implement booking functionality
        Toast.makeText(this, "Booking functionality not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    private boolean isServiceProvider() {
        String userRole = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("user_role", null);
        return "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole);
    }
    
    private boolean isServiceOwner() {
        if (service == null) return false;
        Long currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("user_id", -1L);
        return service.getProviderId() != null && service.getProviderId().equals(currentUserId);
    }
    
    private void viewProviderProfile() {
        if (service != null && service.getProvider() != null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", service.getProvider().getId());
            startActivity(intent);
        }
    }
    
    private void chatWithProvider() {
        if (service != null && service.getProvider() != null) {
            // TODO: Implement ChatActivity
            Toast.makeText(this, "Chat functionality not implemented yet", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        
        if (userId == -1L) {
            Toast.makeText(this, "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userIdStr = userId.toString();
        
        if (service == null) {
            Toast.makeText(this, "Service not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavorite = !isFavorite;
        updateFavoriteIcon();

        String authHeader = getAuthHeader();
        Call<Void> call;

        if (isFavorite) {
            call = favoriteService.addServiceToFavorites(userIdStr, service.getId(), authHeader);
        } else {
            call = favoriteService.removeServiceFromFavorites(userIdStr, service.getId(), authHeader);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(ServiceDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    isFavorite = !isFavorite;
                    updateFavoriteIcon();
                    Toast.makeText(ServiceDetailsActivity.this, "Failed to update favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isFavorite = !isFavorite;
                updateFavoriteIcon();
                Toast.makeText(ServiceDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateFavoriteIcon() {
        if (favoriteIcon != null) {
            favoriteIcon.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart_empty);
        }
    }
    
    private void checkIfFavorite() {
        // Only check if favorite icon is visible (user is not SPP)
        if (favoriteIcon == null || favoriteIcon.getVisibility() != View.VISIBLE) {
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        
        if (userId == -1L || service == null || favoriteService == null) {
            updateFavoriteIcon();
            return;
        }
        
        String userIdStr = userId.toString();

        String authHeader = getAuthHeader();
        // Instead of checkIfServiceIsFavorite, get all favorites and check if this service is in the list
        favoriteService.getFavoriteServices(userIdStr, authHeader).enqueue(new Callback<List<FavoriteSolutionDTO>>() {
            @Override
            public void onResponse(Call<List<FavoriteSolutionDTO>> call, Response<List<FavoriteSolutionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Check if this service is in the favorites list
                    isFavorite = false;
                    for (FavoriteSolutionDTO favoriteSolution : response.body()) {
                        if (favoriteSolution.getSolution() != null && favoriteSolution.getSolution().getId().equals(service.getId())) {
                            isFavorite = true;
                            break;
                        }
                    }
                } else {
                    isFavorite = false;
                }
                updateFavoriteIcon();
            }

            @Override
            public void onFailure(Call<List<FavoriteSolutionDTO>> call, Throwable t) {
                isFavorite = false;
                updateFavoriteIcon();
            }
        });
    }
    
    private boolean isAdmin() {
        String userRole = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("user_role", null);
        return "ADMIN".equals(userRole);
    }
    
    private boolean isEventOrganizer() {
        String userRole = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("user_role", null);
        return "EO".equals(userRole) || "EVENT_ORGANIZER".equals(userRole);
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setImageResource(R.drawable.indicator_unselected);
            indicator.setPadding(8, 0, 8, 0);
            layoutIndicators.addView(indicator);
        }
        
        // Set first indicator as selected
        if (count > 0) {
            ((ImageView) layoutIndicators.getChildAt(0)).setImageResource(R.drawable.indicator_selected);
        }
        
        // Update indicators when page changes
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
                    ImageView indicator = (ImageView) layoutIndicators.getChildAt(i);
                    indicator.setImageResource(i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
                }
            }
        });
    }

    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }
}
