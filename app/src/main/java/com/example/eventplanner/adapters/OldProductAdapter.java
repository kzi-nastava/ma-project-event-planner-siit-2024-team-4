package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.Product;

import java.util.ArrayList;
import java.util.List;

public class OldProductAdapter extends RecyclerView.Adapter<OldProductAdapter.ProductViewHolder> {
    
    private List<Product> products;
    private List<Product> filteredProducts;
    
    public OldProductAdapter(List<Product> products) {
        this.products = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
    }
    
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredProducts.get(position);
        holder.bind(product);
    }
    
    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }
    
    public void filter(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(products);
        } else {
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void updateProducts(List<Product> newProducts) {
        this.products = new ArrayList<>(newProducts);
        this.filteredProducts = new ArrayList<>(newProducts);
        notifyDataSetChanged();
    }
    
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.eventImage);
            productName = itemView.findViewById(R.id.eventName);
        }
        
        public void bind(Product product) {
            productName.setText(product.getName());
            productImage.setImageResource(product.getImageResId());
        }
    }
}
