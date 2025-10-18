package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.eventplanner.R;

import java.util.List;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {
    
    private List<String> imageUrls;
    
    public ProductImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (imageUrls != null && position < imageUrls.size()) {
            String imageUrl = imageUrls.get(position);
            holder.bind(imageUrl);
        }
    }
    
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
        
        public void bind(String imageUrl) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String fullUrl = BASE_URL + imageUrl;
                // Fix double slashes in URL
                fullUrl = fullUrl.replace("//", "/").replace(":/", "://");
                
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.gallery)
                        .error(R.drawable.gallery)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.gallery);
            }
        }
    }
}
