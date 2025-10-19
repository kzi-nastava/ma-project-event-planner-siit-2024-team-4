package com.example.eventplanner.activities;

import static com.example.eventplanner.config.ApiConfig.IMG_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ServiceImageAdapter;
import com.example.eventplanner.dto.FavoriteSolutionDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.FavoriteService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    
    public interface ServiceActionListener {
        void onEdit(ServiceDTO service);
        void onDelete(ServiceDTO service);
        void onView(ServiceDTO service);
        void onProviderProfile(ServiceDTO service);
        void onChatWithProvider(ServiceDTO service);
    }

    private final List<ServiceDTO> services;
    private final ServiceActionListener listener;
    private final boolean isMyServices;
    private final Context context;
    private final FavoriteService favoriteService;

    public ServiceAdapter(List<ServiceDTO> services, ServiceActionListener listener, boolean isMyServices, Context context) {
        this.services = services;
        this.listener = listener;
        this.isMyServices = isMyServices;
        this.context = context;
        this.favoriteService = ApiClient.getClient(context).create(FavoriteService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceDTO service = services.get(position);
        
        // Set service name
        holder.tvName.setText(service.getName());
        
        // Set service description
        holder.tvDescription.setText(service.getDescription());
        
        // Set price
        holder.tvPrice.setText("Price: " + (int)service.getPrice() + " RSD");
        
        // Set discount
        if (service.getDiscount() > 0) {
            holder.tvDiscount.setText("Discount: " + (int)service.getDiscount() + "%");
            holder.tvDiscount.setVisibility(View.VISIBLE);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Set availability
        if (service.isAvailable()) {
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundResource(R.drawable.availability_background);
        } else {
            holder.tvStatus.setText("Unavailable");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundResource(R.drawable.unavailable_background);
        }

        // Set provider info
        if (service.getProvider() != null) {
            holder.tvProvider.setText("Provider: " + service.getProvider().getName());
        } else {
            holder.tvProvider.setText("Provider: N/A");
        }

        // Set service images
        if (service.getImageURLs() != null && !service.getImageURLs().isEmpty()) {
            ServiceImageAdapter imageAdapter = new ServiceImageAdapter(service.getImageURLs());
            holder.viewPagerImages.setAdapter(imageAdapter);
            holder.viewPagerImages.setVisibility(View.VISIBLE);
            
            // Setup indicators if more than one image
            if (service.getImageURLs().size() > 1) {
                setupIndicators(holder, service.getImageURLs().size());
                holder.layoutIndicators.setVisibility(View.VISIBLE);
            } else {
                holder.layoutIndicators.setVisibility(View.GONE);
            }
        } else {
            // Show default image if no images
            holder.viewPagerImages.setVisibility(View.GONE);
            holder.layoutIndicators.setVisibility(View.GONE);
        }
        
        // Setup favorite heart for non-SPP users
        if (!isMyServices) {
            setupFavoriteHeart(holder, service);
        } else {
            holder.heartIcon.setVisibility(View.GONE);
        }

        // Setup action buttons
        if (isMyServices) {
            // My Services - show edit/delete buttons
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnProviderProfile.setVisibility(View.GONE);
            holder.btnChatWithProvider.setVisibility(View.GONE);

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
        } else {
            // All Services - hide all action buttons (like in Angular project)
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnProviderProfile.setVisibility(View.GONE);
            holder.btnChatWithProvider.setVisibility(View.GONE);
        }

        // Set click listener for the entire card
        holder.itemView.setOnClickListener(v -> {
            if (isMyServices) {
                listener.onEdit(service);
            } else {
                listener.onView(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPagerImages;
        LinearLayout layoutIndicators;
        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;
        TextView tvDiscount;
        TextView tvProvider;
        TextView tvStatus;
        ImageView heartIcon;
        ImageButton btnEdit;
        ImageButton btnDelete;
        Button btnProviderProfile;
        Button btnChatWithProvider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            layoutIndicators = itemView.findViewById(R.id.layoutIndicators);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDiscount = itemView.findViewById(R.id.tvServiceDiscount);
            tvProvider = itemView.findViewById(R.id.tvServiceProvider);
            tvStatus = itemView.findViewById(R.id.tvServiceStatus);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
            btnProviderProfile = itemView.findViewById(R.id.btnProviderProfile);
            btnChatWithProvider = itemView.findViewById(R.id.btnChatWithProvider);
        }
    }
    
    private void setupIndicators(ViewHolder holder, int count) {
        holder.layoutIndicators.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View indicator = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                context.getResources().getDimensionPixelSize(R.dimen.indicator_size),
                context.getResources().getDimensionPixelSize(R.dimen.indicator_size)
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_unselected);
            holder.layoutIndicators.addView(indicator);
        }
        
        // Set first indicator as selected
        if (count > 0) {
            holder.layoutIndicators.getChildAt(0).setBackgroundResource(R.drawable.indicator_selected);
        }
        
        // Update indicators when page changes
        holder.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < holder.layoutIndicators.getChildCount(); i++) {
                    View indicator = holder.layoutIndicators.getChildAt(i);
                    if (i == position) {
                        indicator.setBackgroundResource(R.drawable.indicator_selected);
                    } else {
                        indicator.setBackgroundResource(R.drawable.indicator_unselected);
                    }
                }
            }
        });
    }
    
    private void setupFavoriteHeart(ViewHolder holder, ServiceDTO service) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String token = prefs.getString("jwt_token", null);
        
        if (userId == -1L || token == null) {
            holder.heartIcon.setVisibility(View.GONE);
            return;
        }
        
        String userIdStr = userId.toString();
        
        String authHeader = "Bearer " + token;
        
        // Get all favorites and check if this service is in the list
        favoriteService.getFavoriteServices(userIdStr, authHeader).enqueue(new Callback<List<FavoriteSolutionDTO>>() {
            @Override
            public void onResponse(Call<List<FavoriteSolutionDTO>> call, Response<List<FavoriteSolutionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Check if this service is in the favorites list
                    final boolean isFavorite = response.body().stream()
                        .anyMatch(favoriteSolution -> 
                            favoriteSolution.getSolution() != null && 
                            favoriteSolution.getSolution().getId().equals(service.getId()));
                    
                    Log.d("ServiceAdapter", "Service " + service.getName() + " (ID: " + service.getId() + ") is favorite: " + isFavorite);
                    
                    holder.itemView.post(() -> {
                        holder.heartIcon.setImageResource(isFavorite ? 
                            R.drawable.heart_filled : R.drawable.heart_empty);
                        holder.heartIcon.setTag(isFavorite);
                        holder.heartIcon.setVisibility(View.VISIBLE);
                    });
                } else {
                    Log.d("ServiceAdapter", "Failed to get favorites for service " + service.getName() + ": " + response.code());
                    holder.itemView.post(() -> {
                        holder.heartIcon.setImageResource(R.drawable.heart_empty);
                        holder.heartIcon.setTag(false);
                        holder.heartIcon.setVisibility(View.VISIBLE);
                    });
                }
            }
            
            @Override
            public void onFailure(Call<List<FavoriteSolutionDTO>> call, Throwable t) {
                holder.itemView.post(() -> {
                    holder.heartIcon.setImageResource(R.drawable.heart_empty);
                    holder.heartIcon.setTag(false);
                    holder.heartIcon.setVisibility(View.VISIBLE);
                });
            }
        });
        
        // Set click listener for heart
        holder.heartIcon.setOnClickListener(v -> toggleFavorite(holder, service));
    }
    
    private void toggleFavorite(ViewHolder holder, ServiceDTO service) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        
        if (userId == -1L) {
            Toast.makeText(context, "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userIdStr = userId.toString();
        
        String authHeader = "Bearer " + context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("jwt_token", "");
        
        Boolean currentStatus = (Boolean) holder.heartIcon.getTag();
        boolean isFavorite = currentStatus != null ? currentStatus : false;
        
        boolean newStatus = !isFavorite;
        holder.heartIcon.setImageResource(newStatus ? 
            R.drawable.heart_filled : R.drawable.heart_empty);
        holder.heartIcon.setTag(newStatus);
        
        Call<Void> call;
        if (isFavorite) {
            call = favoriteService.removeServiceFromFavorites(userIdStr, service.getId(), authHeader);
        } else {
            call = favoriteService.addServiceToFavorites(userIdStr, service.getId(), authHeader);
        }
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = newStatus ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    holder.heartIcon.setImageResource(isFavorite ?
                        R.drawable.heart_filled : R.drawable.heart_empty);
                    holder.heartIcon.setTag(isFavorite);
                    Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                holder.heartIcon.setImageResource(isFavorite ?
                    R.drawable.heart_filled : R.drawable.heart_empty);
                holder.heartIcon.setTag(isFavorite);
                Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}