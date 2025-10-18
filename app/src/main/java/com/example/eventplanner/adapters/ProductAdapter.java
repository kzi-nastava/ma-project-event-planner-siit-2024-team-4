package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.ProductDTO;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<ProductDTO> products;
    private OnProductClickListener listener;
    private boolean showEditButton = false;
    
    public interface OnProductClickListener {
        void onProductClick(ProductDTO product);
        void onEditClick(ProductDTO product);
    }
    
    public ProductAdapter(List<ProductDTO> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }
    
    public void setShowEditButton(boolean showEditButton) {
        this.showEditButton = showEditButton;
    }
    
    public void updateProducts(List<ProductDTO> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = products.get(position);
        holder.bind(product);
    }
    
    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }
    
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ViewPager2 viewPagerImages;
        private LinearLayout layoutIndicators;
        private TextView tvProductName;
        private TextView tvProductDescription;
        private TextView tvPrice;
        private TextView tvDiscount;
        private TextView tvAvailability;
        private Button btnViewDetails;
        private ImageButton btnEdit;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            layoutIndicators = itemView.findViewById(R.id.layoutIndicators);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(products.get(getAdapterPosition()));
                }
            });
            
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(products.get(getAdapterPosition()));
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(products.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(ProductDTO product) {
            // Set product name
            tvProductName.setText(product.getName());
            
            // Set product description
            tvProductDescription.setText(product.getDescription());
            
            // Set price
            if (product.getPrice() != null) {
                tvPrice.setText("Price: " + product.getPrice().intValue() + " RSD");
            } else {
                tvPrice.setText("Price: N/A");
            }
            
            // Set discount
            if (product.getDiscount() != null && product.getDiscount() > 0) {
                tvDiscount.setText("Discount: " + product.getDiscount().intValue() + "%");
                tvDiscount.setVisibility(View.VISIBLE);
            } else {
                tvDiscount.setVisibility(View.GONE);
            }
            
            // Set availability
            if (product.getAvailable() != null && product.getAvailable()) {
                tvAvailability.setText("Available");
                tvAvailability.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                tvAvailability.setBackgroundResource(R.drawable.availability_background);
            } else {
                tvAvailability.setText("Unavailable");
                tvAvailability.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                tvAvailability.setBackgroundResource(R.drawable.unavailable_background);
            }
            
            
            
            // Set product images
            if (product.getImageURLs() != null && !product.getImageURLs().isEmpty()) {
                ProductImageAdapter imageAdapter = new ProductImageAdapter(product.getImageURLs());
                viewPagerImages.setAdapter(imageAdapter);
                viewPagerImages.setVisibility(View.VISIBLE);
                
                // Setup indicators if more than one image
                if (product.getImageURLs().size() > 1) {
                    setupIndicators(product.getImageURLs().size());
                    layoutIndicators.setVisibility(View.VISIBLE);
                } else {
                    layoutIndicators.setVisibility(View.GONE);
                }
            } else {
                // Show default image if no images
                viewPagerImages.setVisibility(View.GONE);
                layoutIndicators.setVisibility(View.GONE);
            }
            
            // Show/hide edit button
            btnEdit.setVisibility(showEditButton ? View.VISIBLE : View.GONE);
        }
        
        private void setupIndicators(int count) {
            layoutIndicators.removeAllViews();
            
            for (int i = 0; i < count; i++) {
                View indicator = new View(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    itemView.getContext().getResources().getDimensionPixelSize(R.dimen.indicator_size),
                    itemView.getContext().getResources().getDimensionPixelSize(R.dimen.indicator_size)
                );
                params.setMargins(4, 0, 4, 0);
                indicator.setLayoutParams(params);
                indicator.setBackgroundResource(R.drawable.indicator_unselected);
                layoutIndicators.addView(indicator);
            }
            
            // Set first indicator as selected
            if (count > 0) {
                layoutIndicators.getChildAt(0).setBackgroundResource(R.drawable.indicator_selected);
            }
            
            // Update indicators when page changes
            viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
                        View indicator = layoutIndicators.getChildAt(i);
                        if (i == position) {
                            indicator.setBackgroundResource(R.drawable.indicator_selected);
                        } else {
                            indicator.setBackgroundResource(R.drawable.indicator_unselected);
                        }
                    }
                }
            });
        }
    }
}
