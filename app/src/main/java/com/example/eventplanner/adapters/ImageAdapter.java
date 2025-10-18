package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.eventplanner.R;

import java.util.List;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imagePaths;
    private OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(List<String> imagePaths, OnImageRemoveListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        holder.bind(imagePath, position);
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    public void updateImages(List<String> newImagePaths) {
        this.imagePaths = newImagePaths;
        notifyDataSetChanged();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private ImageButton btnRemoveImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnRemoveImage = itemView.findViewById(R.id.btnRemoveImage);
        }

        public void bind(String imagePath, int position) {
            // Check if it's a URL (starts with /uploads/) or a local URI
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                String fullUrl;
                if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                    // Local URI
                    fullUrl = imagePath;
                } else {
                    // Server URL - add BASE_URL
                    fullUrl = BASE_URL + imagePath;
                    // Fix double slashes in URL
                    fullUrl = fullUrl.replace("//", "/").replace(":/", "://");
                }
                
                Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.gallery)
                        .error(R.drawable.gallery)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.gallery);
            }

            btnRemoveImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageRemove(position);
                }
            });
        }
    }
}
