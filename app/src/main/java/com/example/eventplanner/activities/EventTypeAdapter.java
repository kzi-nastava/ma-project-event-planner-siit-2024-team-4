package com.example.eventplanner.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.EventTypeDTO;

import java.util.List;

public class EventTypeAdapter extends RecyclerView.Adapter<EventTypeAdapter.EventTypeViewHolder> {
    
    private List<EventTypeDTO> eventTypes;
    private List<CategoryDTO> categories;
    private boolean isAdmin;
    private EventTypeManagementActivity activity;
    
    public EventTypeAdapter(List<EventTypeDTO> eventTypes, List<CategoryDTO> categories, 
                           boolean isAdmin, EventTypeManagementActivity activity) {
        this.eventTypes = eventTypes;
        this.categories = categories;
        this.isAdmin = isAdmin;
        this.activity = activity;
    }
    
    @NonNull
    @Override
    public EventTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_type, parent, false);
        return new EventTypeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventTypeViewHolder holder, int position) {
        EventTypeDTO eventType = eventTypes.get(position);
        holder.bind(eventType);
    }
    
    @Override
    public int getItemCount() {
        return eventTypes.size();
    }
    
    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    class EventTypeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventTypeName;
        private TextView tvEventTypeDescription;
        private TextView tvCategories;
        private TextView tvStatus;
        private Button btnToggleStatus;
        private Button btnEdit;
        
        public EventTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTypeName = itemView.findViewById(R.id.tvEventTypeName);
            tvEventTypeDescription = itemView.findViewById(R.id.tvEventTypeDescription);
            tvCategories = itemView.findViewById(R.id.tvCategories);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
        
        public void bind(EventTypeDTO eventType) {
            tvEventTypeName.setText(eventType.getName());
            tvEventTypeDescription.setText(eventType.getDescription());
            
            if (eventType.getSuggestedCategories() != null && !eventType.getSuggestedCategories().isEmpty()) {
                StringBuilder categoriesText = new StringBuilder("Categories: ");
                for (int i = 0; i < eventType.getSuggestedCategories().size(); i++) {
                    categoriesText.append(eventType.getSuggestedCategories().get(i).name);
                    if (i < eventType.getSuggestedCategories().size() - 1) {
                        categoriesText.append(", ");
                    }
                }
                tvCategories.setText(categoriesText.toString());
            } else {
                tvCategories.setText("Categories: None");
            }
            
            tvStatus.setText(eventType.isActive() ? "Status: Active" : "Status: Inactive");
            
            if (isAdmin) {
                btnToggleStatus.setVisibility(View.VISIBLE);
                btnToggleStatus.setText(eventType.isActive() ? "Deactivate" : "Activate");
                btnToggleStatus.setOnClickListener(v -> activity.toggleEventTypeStatus(eventType));
                
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> activity.startEditEventType(eventType));
            } else {
                btnToggleStatus.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }
    }
}
