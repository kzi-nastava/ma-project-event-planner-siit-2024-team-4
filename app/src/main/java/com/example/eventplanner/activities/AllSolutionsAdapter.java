package com.example.eventplanner.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.example.eventplanner.network.dto.SolutionDTO;
import java.util.ArrayList;
import java.util.List;

public class AllSolutionsAdapter extends RecyclerView.Adapter<AllSolutionsAdapter.VH> {

    public interface OnClick { void onClick(SolutionDTO item); }
    private final List<SolutionDTO> items = new ArrayList<>();
    @Nullable private final OnClick onClick;

    public AllSolutionsAdapter(@Nullable OnClick onClick) { this.onClick = onClick; }

    public void replaceAll(@Nullable List<SolutionDTO> data){
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void addAll(@Nullable List<SolutionDTO> more){
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_simple_row, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        SolutionDTO s = items.get(i);
        h.title.setText(s.name != null ? s.name : "(Item)");
        String sub = (s.type!=null? s.type : "") + (s.price!=null? " • " + s.price + " RSD" : "");
        h.subtitle.setText(sub.trim());
        h.itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(s); });
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(@NonNull View v){ super(v);
            title = v.findViewById(R.id.tvTitle);
            subtitle = v.findViewById(R.id.tvSubtitle);
        }
    }
}
