package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.FavoriteService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventAdapterNoImage extends RecyclerView.Adapter<EventAdapterNoImage.EventViewHolder> {

    private List<EventDTO> events;
    private List<EventDTO> eventsFull;
    private Context context;
    private FavoriteService favoriteService;
    private boolean isFavoriteEventsList;

    public EventAdapterNoImage(List<EventDTO> events, Context context) {
        this.events = events;
        this.context = context;
        eventsFull = new ArrayList<>(events);
        favoriteService = ApiClient.getClient(context).create(FavoriteService.class);
        this.isFavoriteEventsList = false;
    }

    public EventAdapterNoImage(List<EventDTO> events, Context context, boolean isFavoriteEventsList) {
        this.events = events;
        this.context = context;
        eventsFull = new ArrayList<>(events);
        favoriteService = ApiClient.getClient(context).create(FavoriteService.class);
        this.isFavoriteEventsList = isFavoriteEventsList;
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
        
        if (isFavoriteEventsList) {
            holder.heartIcon.setImageResource(R.drawable.heart_filled);
            holder.heartIcon.setTag(true);
        } else {
            checkAndSetFavoriteStatus(holder, event);
        }
        
        holder.heartIcon.setOnClickListener(v -> toggleFavorite(holder, event));
        
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

    public void filter(String query) {
        events.clear();
        if (query.isEmpty()) {
            events.addAll(eventsFull);
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
        ImageView heartIcon;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventType = itemView.findViewById(R.id.eventType);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }

    public void updateEvents(List<EventDTO> newEvents) {
        events.clear();
        events.addAll(newEvents);
        eventsFull.clear();
        eventsFull.addAll(newEvents);
        notifyDataSetChanged();
    }

    private void checkAndSetFavoriteStatus(EventViewHolder holder, EventDTO event) {
        holder.heartIcon.setImageResource(R.drawable.heart_empty);
        holder.heartIcon.setTag(false);
        
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userIdStr = prefs.getString("user_id", null);
        String token = prefs.getString("jwt_token", null);
        
        if (userIdStr == null || token == null) {
            return;
        }
        
        String authHeader = "Bearer " + token;
        
        favoriteService.checkIfEventIsFavorite(userIdStr, event.getId(), authHeader).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFavorite = response.body();
                    
                    holder.itemView.post(() -> {
                        holder.heartIcon.setImageResource(isFavorite ? 
                            R.drawable.heart_filled : R.drawable.heart_empty);
                        holder.heartIcon.setTag(isFavorite);
                    });
                } else {
                    holder.itemView.post(() -> {
                        holder.heartIcon.setImageResource(R.drawable.heart_empty);
                        holder.heartIcon.setTag(false);
                    });
                }
            }
            
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                holder.itemView.post(() -> {
                    holder.heartIcon.setImageResource(R.drawable.heart_empty);
                    holder.heartIcon.setTag(false);
                });
            }
        });
    }


    private void toggleFavorite(EventViewHolder holder, EventDTO event) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userIdStr = prefs.getString("user_id", null);
        
        if (userIdStr == null) {
            Toast.makeText(context, "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String authHeader = "Bearer " + context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("jwt_token", "");
        
        Boolean currentStatus = (Boolean) holder.heartIcon.getTag();
        boolean isFavorite = currentStatus != null ? currentStatus : false;
        
        boolean newStatus = !isFavorite;
        holder.heartIcon.setImageResource(newStatus ? 
            R.drawable.heart_filled : R.drawable.heart_empty);
        holder.heartIcon.setTag(newStatus);
        
        Call<Void> call;
        if (isFavorite) {
            call = favoriteService.removeEventFromFavorites(userIdStr, event.getId(), authHeader);
        } else {
            call = favoriteService.addEventToFavorites(userIdStr, event.getId(), authHeader);
        }
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = newStatus ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    
                    if (isFavoriteEventsList && !newStatus) {
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            events.remove(position);
                            eventsFull.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, events.size());
                        }
                    }
                } else {
                    holder.heartIcon.setImageResource(isFavorite ?
                        R.drawable.heart_filled : R.drawable.heart_empty);
                    holder.heartIcon.setTag(isFavorite);
                    Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                holder.heartIcon.setImageResource(isFavorite ?
                    R.drawable.heart_filled : R.drawable.heart_empty);
                holder.heartIcon.setTag(isFavorite);
                Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
