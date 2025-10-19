package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventTypeDTO;
import com.google.android.material.chip.Chip;

import java.util.List;

public class EventTypeChipAdapter extends RecyclerView.Adapter<EventTypeChipAdapter.EventTypeChipViewHolder> {

    private List<EventTypeDTO> eventTypes;
    private OnEventTypeRemoveListener listener;

    public interface OnEventTypeRemoveListener {
        void onEventTypeRemove(EventTypeDTO eventType);
    }

    public EventTypeChipAdapter(List<EventTypeDTO> eventTypes, OnEventTypeRemoveListener listener) {
        this.eventTypes = eventTypes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventTypeChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_type_chip, parent, false);
        return new EventTypeChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventTypeChipViewHolder holder, int position) {
        EventTypeDTO eventType = eventTypes.get(position);
        holder.bind(eventType);
    }

    @Override
    public int getItemCount() {
        return eventTypes != null ? eventTypes.size() : 0;
    }

    public void updateEventTypes(List<EventTypeDTO> newEventTypes) {
        this.eventTypes = newEventTypes;
        notifyDataSetChanged();
    }

    class EventTypeChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        public EventTypeChipViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = (Chip) itemView;
        }

        public void bind(EventTypeDTO eventType) {
            chip.setText(eventType.getName());
            chip.setOnCloseIconClickListener(v -> {
                if (listener != null) {
                    listener.onEventTypeRemove(eventType);
                }
            });
        }
    }
}
