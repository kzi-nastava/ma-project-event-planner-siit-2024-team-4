package com.example.eventplanner.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.network.dto.SolutionDTO;

import java.util.ArrayList;
import java.util.List;

public class TopSolutionsAdapter extends RecyclerView.Adapter<TopSolutionsAdapter.VH> {

    public interface OnClick { void onClick(SolutionDTO item); }

    private final List<SolutionDTO> items = new ArrayList<>();
    @Nullable private final OnClick onClick;

    public TopSolutionsAdapter(@Nullable OnClick onClick) {
        this.onClick = onClick;
        setHasStableIds(true); // radi boljeg recikliranja
    }

    // ——— Public helpers ———
    public void replaceAll(@Nullable List<SolutionDTO> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void addAll(@Nullable List<SolutionDTO> more) {
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @Nullable
    public SolutionDTO getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    // ——— RecyclerView.Adapter ———
    @Override
    public long getItemId(int position) {
        SolutionDTO s = items.get(position);
        // Ako nema ID u DTO-u, koristimo poziciju da ne padne
        return (s != null && s.id != null) ? s.id : position;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solution_top, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SolutionDTO s = items.get(position);

        // Title
        String title = (s != null && s.name != null && !s.name.trim().isEmpty())
                ? s.name : "(Item)";
        h.title.setText(title);

        // Image
        String url = (s != null) ? s.imageUrl : null;
        Glide.with(h.img.getContext())
                .load(url)
                .placeholder(R.drawable.ic_placeholder)  // obavezno dodaj neku ikonicu u drawable
                .error(R.drawable.ic_placeholder)        // fallback ako URL ne radi
                .into(h.img);

        // Click
        h.itemView.setOnClickListener(v -> {
            if (onClick == null) return;
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            SolutionDTO clicked = getItem(pos);
            if (clicked != null) onClick.onClick(clicked);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ——— ViewHolder ———
    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView title;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.img);
            title = v.findViewById(R.id.title);
        }
    }
}
