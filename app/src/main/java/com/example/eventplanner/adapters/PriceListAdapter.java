package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.ServiceDTO;

import java.text.DecimalFormat;
import java.util.List;

public class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.PriceListViewHolder> {

    private List<Object> items;
    private OnItemClickListener listener;
    private DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    public interface OnItemClickListener {
        void onEditClick(Object item, int position);
    }

    public PriceListAdapter(List<Object> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PriceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_list, parent, false);
        return new PriceListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceListViewHolder holder, int position) {
        Object item = items.get(position);
        holder.bind(item, position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PriceListViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemNumber;
        private TextView tvItemName;
        private TextView tvItemType;
        private TextView tvPrice;
        private TextView tvDiscount;
        private TextView tvDiscountedPrice;
        private Button btnEdit;

        public PriceListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemNumber = itemView.findViewById(R.id.tv_item_number);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemType = itemView.findViewById(R.id.tv_item_type);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvDiscountedPrice = itemView.findViewById(R.id.tv_discounted_price);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }

        public void bind(Object item, int position, OnItemClickListener listener) {
            // Set item number (1-based index)
            tvItemNumber.setText(String.valueOf(position + 1));

            double price = 0;
            double discount = 0;
            String name = "";
            String type = "";

            if (item instanceof ServiceDTO) {
                ServiceDTO service = (ServiceDTO) item;
                price = service.getPrice();
                discount = service.getDiscount();
                name = service.getName();
                type = "SERVICE";
            } else if (item instanceof ProductDTO) {
                ProductDTO product = (ProductDTO) item;
                price = product.getPrice() != null ? product.getPrice() : 0;
                discount = product.getDiscount() != null ? product.getDiscount() : 0;
                name = product.getName();
                type = "PRODUCT";
            }

            tvItemName.setText(name);
            tvItemType.setText(type);
            tvPrice.setText(formatPrice(price) + " RSD");
            tvDiscount.setText(formatDiscount(discount) + "%");

            // Calculate discounted price
            double discountedPrice = price * (1 - discount / 100);
            tvDiscountedPrice.setText(formatPrice(discountedPrice) + " RSD");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(item, position);
                }
            });
        }

        private String formatPrice(double price) {
            return String.format("%.2f", price);
        }

        private String formatDiscount(double discount) {
            return String.format("%.0f", discount);
        }
    }
}
