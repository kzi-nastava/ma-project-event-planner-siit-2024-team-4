package com.example.eventplanner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.ServiceDTO;

import java.util.List;

import static com.example.eventplanner.config.ApiConfig.IMG_URL;

public class FavoriteSolutionsAdapter extends RecyclerView.Adapter<FavoriteSolutionsAdapter.ViewHolder> {
    
    public interface OnSolutionClickListener {
        void onSolutionClick(ServiceDTO service);
        void onRemoveFavorite(ServiceDTO service);
    }
    
    private final List<ServiceDTO> services;
    private final OnSolutionClickListener listener;
    
    public FavoriteSolutionsAdapter(List<ServiceDTO> services, OnSolutionClickListener listener) {
        this.services = services;
        this.listener = listener;
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
        
        Log.d("FavoriteSolutionsAdapter", "Binding service at position " + position + ": " + service.getName());
        Log.d("FavoriteSolutionsAdapter", "Service price: " + service.getPrice());
        Log.d("FavoriteSolutionsAdapter", "Service provider: " + service.getProvider());
        Log.d("FavoriteSolutionsAdapter", "Service available: " + service.isAvailable());
        
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
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundResource(R.drawable.availability_background);
        } else {
            holder.tvStatus.setText("Unavailable");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
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
        
        // Show heart icon (filled) and hide remove button
        holder.heartIcon.setVisibility(View.VISIBLE);
        holder.heartIcon.setImageResource(R.drawable.heart_filled);
        holder.heartIcon.setTag(true); // Mark as favorite
        holder.btnRemoveFavorite.setVisibility(View.GONE);
        
        // Hide other buttons
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
        holder.btnProviderProfile.setVisibility(View.GONE);
        holder.btnChatWithProvider.setVisibility(View.GONE);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onSolutionClick(service));
        holder.heartIcon.setOnClickListener(v -> listener.onRemoveFavorite(service));
    }
    
    @Override
    public int getItemCount() {
        return services.size();
    }
    
    private void setupIndicators(ViewHolder holder, int count) {
        holder.layoutIndicators.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View indicator = new View(holder.itemView.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.indicator_size),
                holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.indicator_size)
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
        ImageView btnEdit;
        ImageView btnDelete;
        Button btnProviderProfile;
        Button btnChatWithProvider;
        Button btnRemoveFavorite;
        
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
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
