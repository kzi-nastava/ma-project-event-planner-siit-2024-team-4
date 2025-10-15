package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;

public class EventAdapterNoImage extends RecyclerView.Adapter<EventAdapterNoImage.EventViewHolder> {

    private List<EventDTO> events;
    private List<EventDTO> eventsFull;

    public EventAdapterNoImage(List<EventDTO> events) {
        this.events = events;
        eventsFull = new ArrayList<>(events);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card_no_image, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDTO event = events.get(position);
        holder.eventName.setText(event.getName());
        holder.eventType.setText(event.getEventTypeName());
        holder.eventDescription.setText(event.getDescription());
        
        // Set click listener for the entire card
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AboutEventActivity.class);
            intent.putExtra("event_id", event.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // Filter method
    public void filter(String query) {
        events.clear();
        if (query.isEmpty()) {
            events.addAll(eventsFull); // If query is empty, show all events
        } else {
            for (EventDTO event : eventsFull) {
                if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                    events.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;
        TextView eventType;
        TextView eventDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventType = itemView.findViewById(R.id.eventType);
            eventDescription = itemView.findViewById(R.id.eventDescription);
        }
    }

    public void updateEvents(List<EventDTO> newEvents) {
        events.clear();
        events.addAll(newEvents);
        eventsFull.clear();
        eventsFull.addAll(newEvents);
        notifyDataSetChanged();
    }
}
