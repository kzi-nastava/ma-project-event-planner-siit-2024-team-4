package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventDTO> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(EventDTO event);
    }

    public EventAdapter(List<EventDTO> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card_new, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDTO event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void updateEvents(List<EventDTO> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEventImage;
        private TextView tvEventName;
        private TextView tvEventDate;
        private TextView tvEventLocation;
        private TextView tvEventDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(events.get(getAdapterPosition()));
                }
            });
        }

        public void bind(EventDTO event) {
            // Set event name
            tvEventName.setText(event.getName());

            // Set event date
            if (event.getStartDate() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(event.getStartDate());
                    tvEventDate.setText(outputFormat.format(date));
                } catch (Exception e) {
                    tvEventDate.setText(event.getStartDate());
                }
            } else {
                tvEventDate.setText("Date not available");
            }

            // Set event location
            if (event.getLocation() != null && event.getLocation().getName() != null && !event.getLocation().getName().trim().isEmpty()) {
                tvEventLocation.setText(event.getLocation().getName());
            } else {
                tvEventLocation.setText("Location not available");
            }

            // Set event description
            if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
                tvEventDescription.setText(event.getDescription());
            } else {
                tvEventDescription.setText("No description available");
            }

            // Set event image (EventDTO doesn't have imageURLs, so use default)
            ivEventImage.setImageResource(R.drawable.gallery);
        }
    }
}
