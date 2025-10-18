package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventTypeDTO;

import java.util.List;

public class EventTypeCheckboxAdapter extends RecyclerView.Adapter<EventTypeCheckboxAdapter.EventTypeCheckboxViewHolder> {

    private List<EventTypeDTO> eventTypes;
    private List<EventTypeDTO> selectedEventTypes;

    public EventTypeCheckboxAdapter(List<EventTypeDTO> eventTypes, List<EventTypeDTO> selectedEventTypes) {
        this.eventTypes = eventTypes;
        this.selectedEventTypes = selectedEventTypes;
    }

    @NonNull
    @Override
    public EventTypeCheckboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_type_checkbox, parent, false);
        return new EventTypeCheckboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventTypeCheckboxViewHolder holder, int position) {
        EventTypeDTO eventType = eventTypes.get(position);
        holder.bind(eventType);
    }

    @Override
    public int getItemCount() {
        return eventTypes != null ? eventTypes.size() : 0;
    }

    class EventTypeCheckboxViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbEventType;
        private TextView tvEventTypeName;

        public EventTypeCheckboxViewHolder(@NonNull View itemView) {
            super(itemView);
            cbEventType = itemView.findViewById(R.id.cbEventType);
            tvEventTypeName = itemView.findViewById(R.id.tvEventTypeName);
        }

        public void bind(EventTypeDTO eventType) {
            tvEventTypeName.setText(eventType.getName());
            
            // Check if this event type is already selected
            boolean isSelected = selectedEventTypes.contains(eventType);
            cbEventType.setChecked(isSelected);
            
            cbEventType.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedEventTypes.contains(eventType)) {
                        selectedEventTypes.add(eventType);
                    }
                } else {
                    selectedEventTypes.remove(eventType);
                }
            });
        }
    }
}
