package com.example.eventplanner.activities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface CategoryActionListener {
        void onEdit(CategoryDTO category);
        void onDelete(CategoryDTO category);
        void onApprove(CategoryDTO category);
        void onDeny(CategoryDTO category);
    }

    private final List<CategoryDTO> items;
    private final CategoryActionListener listener;
    private final boolean canManage;
    private final boolean isPendingList;

    public CategoryAdapter(List<CategoryDTO> items, CategoryActionListener listener, boolean canManage, boolean isPendingList) {
        this.items = items;
        this.listener = listener;
        this.canManage = canManage;
        this.isPendingList = isPendingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDTO item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvDescription.setText(item.description);
        
        
        if (canManage) {
            if (isPendingList) {
                // Za pending kategorije prikaži approve/deny dugmad
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnDeny.setVisibility(View.VISIBLE);
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.GONE);
                
                holder.btnApprove.setOnClickListener(v -> listener.onApprove(item));
                holder.btnDeny.setOnClickListener(v -> listener.onDeny(item));
                holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
                holder.btnDelete.setOnClickListener(null);
            } else {
                // Za approved kategorije prikaži edit/delete dugmad
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnDeny.setVisibility(View.GONE);
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                
                holder.btnApprove.setOnClickListener(null);
                holder.btnDeny.setOnClickListener(null);
                holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
                holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
            }
        } else {
            // Za obične korisnike sakrij sva dugmad
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnDeny.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            
            holder.btnApprove.setOnClickListener(null);
            holder.btnDeny.setOnClickListener(null);
            holder.btnEdit.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDescription;
        ImageButton btnEdit;
        ImageButton btnDelete;
        ImageButton btnApprove;
        ImageButton btnDeny;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvCategoryDescription);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            btnApprove = itemView.findViewById(R.id.btnApproveCategory);
            btnDeny = itemView.findViewById(R.id.btnDenyCategory);
        }
    }
}


