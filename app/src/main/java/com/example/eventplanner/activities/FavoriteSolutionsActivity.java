package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.FavoriteSolutionsAdapter;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.FavoriteSolutionDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.FavoriteService;
import com.example.eventplanner.network.service.ServiceService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteSolutionsActivity extends BaseActivity implements FavoriteSolutionsAdapter.OnSolutionClickListener {
    
    private RecyclerView recyclerView;
    private FavoriteSolutionsAdapter adapter;
    private List<ServiceDTO> favoriteServices;
    private FavoriteService favoriteService;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate the content into the base layout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_favorite_solutions, contentFrame, true);
        
        initViews();
        loadFavoriteSolutions();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewFavoriteSolutions);
        favoriteServices = new ArrayList<>();
        adapter = new FavoriteSolutionsAdapter(favoriteServices, this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
    }
    
    private void loadFavoriteSolutions() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String token = prefs.getString("jwt_token", null);
        
        if (userId == -1L || token == null) {
            Toast.makeText(this, "Please log in to view favorites", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String authHeader = "Bearer " + token;
        String userIdStr = userId.toString();
        
        // Load favorite solutions (both services and products use the same endpoint)
        favoriteService.getFavoriteServices(userIdStr, authHeader).enqueue(new Callback<List<FavoriteSolutionDTO>>() {
            @Override
            public void onResponse(Call<List<FavoriteSolutionDTO>> call, Response<List<FavoriteSolutionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteServices.clear();
                    
                    // Extract ServiceDTO from FavoriteSolutionDTO
                    for (FavoriteSolutionDTO favoriteSolution : response.body()) {
                        if (favoriteSolution.getSolution() != null) {
                            ServiceDTO service = favoriteSolution.getSolution();
                            Log.d("FavoriteSolutionsActivity", "Service: " + service.getName() + 
                                ", providerId: " + service.getProviderId() + 
                                ", provider: " + (service.getProvider() != null ? service.getProvider().getName() : "null"));
                            favoriteServices.add(service);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (favoriteServices.isEmpty()) {
                        Toast.makeText(FavoriteSolutionsActivity.this, "No favorite solutions yet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FavoriteSolutionsActivity.this, "Failed to load favorite solutions", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<FavoriteSolutionDTO>> call, Throwable t) {
                Toast.makeText(FavoriteSolutionsActivity.this, "Error loading favorite solutions", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onSolutionClick(ServiceDTO service) {
        // Try to load as service first, if it fails, it's a product
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        String authHeader = "Bearer " + getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", "");
        
        serviceAPI.getServiceById(authHeader, service.getId()).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // It's a service, navigate to ServiceDetailsActivity
                    Intent intent = new Intent(FavoriteSolutionsActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("serviceId", service.getId());
                    intent.putExtra("isFromFavorites", true);
                    startActivity(intent);
                } else {
                    // It's a product, navigate to AboutProductActivity
                    // We need to create a ProductDTO from the ServiceDTO
                    ProductDTO product = convertServiceToProduct(service);
                    Intent intent = new Intent(FavoriteSolutionsActivity.this, AboutProductActivity.class);
                    intent.putExtra("product", product);
                    intent.putExtra("isFromFavorites", true);
                    startActivity(intent);
                }
            }
            
            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                // It's a product, navigate to AboutProductActivity
                ProductDTO product = convertServiceToProduct(service);
                Intent intent = new Intent(FavoriteSolutionsActivity.this, AboutProductActivity.class);
                intent.putExtra("product", product);
                intent.putExtra("isFromFavorites", true);
                startActivity(intent);
            }
        });
    }
    
    private ProductDTO convertServiceToProduct(ServiceDTO service) {
        Log.d("FavoriteSolutionsActivity", "Converting service to product: " + service.getName() + 
            ", providerId: " + service.getProviderId() + 
            ", provider: " + (service.getProvider() != null ? service.getProvider().getName() : "null"));
            
        ProductDTO product = new ProductDTO();
        product.setId(service.getId());
        product.setName(service.getName());
        product.setDescription(service.getDescription());
        product.setPrice(service.getPrice());
        product.setDiscount(service.getDiscount());
        product.setAvailable(service.isAvailable());
        product.setImageURLs(service.getImageURLs());
        // Use provider ID from provider object if available, otherwise use providerId field
        Long providerId = service.getProvider() != null ? service.getProvider().getId() : service.getProviderId();
        Log.d("FavoriteSolutionsActivity", "Setting providerId: " + providerId + " for product: " + product.getName());
        product.setProviderId(providerId);
        product.setServiceProviderId(providerId);
        
        // Copy category information
        if (service.getCategory() != null) {
            product.setCategory(service.getCategory());
            product.setCategoryId(service.getCategory().getId());
            product.setCategoryName(service.getCategory().getName());
        }
        
        // Copy event types
        if (service.getEventTypes() != null && !service.getEventTypes().isEmpty()) {
            product.setEventTypes(service.getEventTypes());
            product.setEventTypeId(service.getEventTypes().get(0).getId());
            product.setEventTypeName(service.getEventTypes().get(0).getName());
        }
        
        // Copy provider information
        if (service.getProvider() != null) {
            product.setServiceProviderName(service.getProvider().getName());
        }
        
        return product;
    }
    
    @Override
    public void onRemoveFavorite(ServiceDTO service) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String token = prefs.getString("jwt_token", null);
        
        if (userId == -1L || token == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String authHeader = "Bearer " + token;
        String userIdStr = userId.toString();
        
        // Try to remove as service first, then as product
        favoriteService.removeServiceFromFavorites(userIdStr, service.getId(), authHeader).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    favoriteServices.remove(service);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FavoriteSolutionsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    // If service removal failed, try product removal
                    favoriteService.removeProductFromFavorites(userIdStr, service.getId(), authHeader).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                favoriteServices.remove(service);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(FavoriteSolutionsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FavoriteSolutionsActivity.this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(FavoriteSolutionsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FavoriteSolutionsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
