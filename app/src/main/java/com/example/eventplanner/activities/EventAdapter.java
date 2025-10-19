package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.eventplanner.R;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private List<Event> eventsFull;

    public EventAdapter(List<Event> events) {
        this.events = events;
        eventsFull = new ArrayList<>(events);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventName.setText(event.getName());
        holder.eventImage.setImageResource(event.getImageResId());
        
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AboutEventActivity.class);
            intent.putExtra("event_id", position + 1);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void filter(String query) {
        events.clear();
        if (query.isEmpty()) {
            events.addAll(eventsFull);
        } else {
            for (Event event : eventsFull) {
                if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                    events.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventImage = itemView.findViewById(R.id.eventImage);
        }
    }

    public void updateEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }
}
