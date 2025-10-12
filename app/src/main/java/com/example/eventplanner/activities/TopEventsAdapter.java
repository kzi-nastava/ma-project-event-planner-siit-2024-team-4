package com.example.eventplanner.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.network.dto.EventDTO;
import java.util.ArrayList;
import java.util.List;

public class TopEventsAdapter extends RecyclerView.Adapter<TopEventsAdapter.VH> {
    public interface OnClick { void onClick(EventDTO item); }
    private final List<EventDTO> items = new ArrayList<>();
    private final OnClick onClick;

    public TopEventsAdapter(OnClick onClick){ this.onClick = onClick; }

    public void replaceAll(List<EventDTO> data){
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_top, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        EventDTO e = items.get(i);
        h.title.setText(e.name != null ? e.name : "(Event)");
        Glide.with(h.img.getContext())
                .load(e.imageUrl) // može biti null; Glide to hendluje
//                .placeholder(R.drawable.ic_placeholder) // dodaj neku ikonicu u drawable
                .into(h.img);
        h.itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(e); });
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView title;
        VH(@NonNull View v){ super(v); img=v.findViewById(R.id.img); title=v.findViewById(R.id.title); }
    }
}
