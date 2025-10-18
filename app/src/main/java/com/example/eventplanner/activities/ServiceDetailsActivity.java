package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.config.ApiConfig;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.ServiceService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailsActivity extends AppCompatActivity {

    private ServiceDTO service;
    private Long serviceId;
    
    // Views
    private ImageView imageViewService;
    private TextView tvName, tvDescription, tvPrice, tvDiscount, tvCategory;
    private TextView tvAvailability, tvVisibility, tvDuration, tvReservationType;
    private TextView tvEventTypes, tvProviderInfo;
    private Button btnEdit, btnDelete, btnBook;
    private RecyclerView recyclerImages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);
        
        // Get service ID from intent
        serviceId = getIntent().getLongExtra("serviceId", -1L);
        if (serviceId == -1L) {
            Toast.makeText(this, "Service ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadServiceDetails();
    }
    
    private void initViews() {
        imageViewService = findViewById(R.id.imageViewServiceDetails);
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
        
        // Set up button listeners
        btnEdit.setOnClickListener(v -> editService());
        btnDelete.setOnClickListener(v -> deleteService());
        btnBook.setOnClickListener(v -> bookService());
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
                    Log.e("ServiceDetails", "Error loading service: " + response.code() + " " + response.message());
                    Toast.makeText(ServiceDetailsActivity.this, "Error loading service details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Log.e("ServiceDetails", "Network error loading service: " + t.getMessage(), t);
                Toast.makeText(ServiceDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void populateViews() {
        if (service == null) return;
        
        // Basic info
        tvName.setText(service.getName());
        tvDescription.setText(service.getDescription());
        tvPrice.setText(String.format("%.2f RSD", service.getPrice()));
        
        // Discount
        if (service.getDiscount() > 0) {
            tvDiscount.setText(String.format("%.0f%% OFF", service.getDiscount()));
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }
        
        // Category
        if (service.getCategory() != null) {
            tvCategory.setText(service.getCategory().name);
        } else {
            tvCategory.setText("No category");
        }
        
        // Availability and visibility
        tvAvailability.setText(service.isAvailable() ? "Available" : "Not Available");
        tvVisibility.setText(service.isVisible() ? "Visible" : "Not Visible");
        
        // Duration info
        if (service.getDuration() != null) {
            tvDuration.setText("Duration: " + service.getDuration() + " minutes");
        } else if (service.getMinEngagement() != null && service.getMaxEngagement() != null) {
            tvDuration.setText("Engagement: " + service.getMinEngagement() + " - " + service.getMaxEngagement() + " minutes");
        } else {
            tvDuration.setText("Duration: Not specified");
        }
        
        // Reservation type
        if (service.getReservationType() != null) {
            tvReservationType.setText("Reservation: " + service.getReservationType());
        } else {
            tvReservationType.setText("Reservation: Not specified");
        }
        
        // Event types
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
        
        // Provider info
        if (service.getProvider() != null && service.getProvider().getName() != null) {
            tvProviderInfo.setText("Provider: " + service.getProvider().getName());
        } else if (service.getProviderId() != null) {
            tvProviderInfo.setText("Provider ID: " + service.getProviderId());
        } else {
            tvProviderInfo.setText("Provider: Not specified");
        }
        
        // Load first image
        if (service.getImageURLs() != null && !service.getImageURLs().isEmpty()) {
            String imageUrl = service.getImageURLs().get(0);
            // Convert relative path to full URL
            if (imageUrl.startsWith("/uploads/")) {
                imageUrl = ApiConfig.IMG_URL + imageUrl;
            }
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageViewService);
        } else {
            imageViewService.setImageResource(R.drawable.placeholder_image);
        }
        
        // Show/hide buttons based on user role and service ownership
        setupButtons();
    }
    
    private void setupButtons() {
        // Check if current user is the service provider
        boolean isServiceProvider = isServiceProvider();
        boolean isOwner = isServiceOwner();
        
        if (isServiceProvider && isOwner) {
            // Service provider viewing their own service
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnBook.setVisibility(View.GONE);
        } else if (isServiceProvider) {
            // Service provider viewing someone else's service
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnBook.setVisibility(View.GONE);
        } else {
            // Regular user viewing service
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnBook.setVisibility(service.isAvailable() ? View.VISIBLE : View.GONE);
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
    
    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }
}
