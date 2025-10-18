package com.example.eventplanner.activities;

import static com.example.eventplanner.config.ApiConfig.IMG_URL;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.ServiceDTO;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    
    public interface ServiceActionListener {
        void onEdit(ServiceDTO service);
        void onDelete(ServiceDTO service);
        void onView(ServiceDTO service);
    }

    private final List<ServiceDTO> services;
    private final ServiceActionListener listener;
    private final boolean isMyServices;

    public ServiceAdapter(List<ServiceDTO> services, ServiceActionListener listener, boolean isMyServices) {
        this.services = services;
        this.listener = listener;
        this.isMyServices = isMyServices;
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
        
        holder.tvName.setText(service.getName());
        holder.tvDescription.setText(service.getDescription());
        holder.tvPrice.setText(String.format("%.2f RSD", service.getPrice()));
        
        if (service.getDiscount() > 0) {
            holder.tvDiscount.setText(String.format("%.0f%% OFF", service.getDiscount()));
            holder.tvDiscount.setVisibility(View.VISIBLE);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Load first image if available
        if (service.getImageURLs() != null && !service.getImageURLs().isEmpty()) {
            String imageUrl = service.getImageURLs().get(0);
            // Convert relative path to full URL
            if (imageUrl.startsWith("/uploads/")) {
                imageUrl = IMG_URL + imageUrl;
            }
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }

        // Show provider name
        if (service.getProvider() != null) {
            holder.tvProvider.setText("Provider: " + service.getProvider().getName());
        } else {
            holder.tvProvider.setText("Provider: N/A");
        }

        // Show availability status
        if (service.isAvailable()) {
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setText("Not Available");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        // Show visibility status for my services
        if (isMyServices) {
            if (service.isVisible()) {
                holder.tvVisibility.setText("Visible");
                holder.tvVisibility.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.tvVisibility.setText("Hidden");
                holder.tvVisibility.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            }
            holder.tvVisibility.setVisibility(View.VISIBLE);
        } else {
            holder.tvVisibility.setVisibility(View.GONE);
        }

        // Set up action buttons
        if (isMyServices) {
            // For my services, show edit and delete buttons
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnView.setVisibility(View.GONE);

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
        } else {
            // For all services, hide all buttons (no eye icon)
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnView.setVisibility(View.GONE);
        }

        // Set up item click listener
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
        ImageView ivImage;
        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;
        TextView tvDiscount;
        TextView tvProvider;
        TextView tvStatus;
        TextView tvVisibility;
        ImageButton btnEdit;
        ImageButton btnDelete;
        ImageButton btnView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivServiceImage);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDiscount = itemView.findViewById(R.id.tvServiceDiscount);
            tvProvider = itemView.findViewById(R.id.tvServiceProvider);
            tvStatus = itemView.findViewById(R.id.tvServiceStatus);
            tvVisibility = itemView.findViewById(R.id.tvServiceVisibility);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
            btnView = itemView.findViewById(R.id.btnViewService);
        }
    }
}