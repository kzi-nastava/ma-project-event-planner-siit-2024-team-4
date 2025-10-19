package com.example.eventplanner.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CreateActivityRequest;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private List<CreateActivityRequest> activities;

    public ActivityAdapter(List<CreateActivityRequest> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        CreateActivityRequest activity = activities.get(position);
        
        // Set current values if they exist
        holder.etActivityName.setText(activity.getName() != null ? activity.getName() : "");
        holder.etActivityDescription.setText(activity.getDescription() != null ? activity.getDescription() : "");
        holder.etStartTime.setText(activity.getStartTime() != null ? activity.getStartTime() : "");
        holder.etEndTime.setText(activity.getEndTime() != null ? activity.getEndTime() : "");
        holder.etActivityLocation.setText(activity.getLocation() != null ? activity.getLocation() : "");

        // Set click listeners to update the activity object
        holder.etActivityName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.setName(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        holder.etActivityDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Set up time picker for start time
        holder.etStartTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                holder.itemView.getContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    holder.etStartTime.setText(time);
                    activity.setStartTime(time);
                },
                9, 0, true
            );
            timePickerDialog.show();
        });

        // Set up time picker for end time
        holder.etEndTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                holder.itemView.getContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    holder.etEndTime.setText(time);
                    activity.setEndTime(time);
                },
                17, 0, true
            );
            timePickerDialog.show();
        });

        holder.etActivityLocation.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.setLocation(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        holder.btnRemoveActivity.setOnClickListener(v -> {
            activities.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, activities.size());
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        EditText etActivityName;
        EditText etActivityDescription;
        EditText etStartTime;
        EditText etEndTime;
        EditText etActivityLocation;
        com.google.android.material.button.MaterialButton btnRemoveActivity;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            etActivityName = itemView.findViewById(R.id.etActivityName);
            etActivityDescription = itemView.findViewById(R.id.etActivityDescription);
            etStartTime = itemView.findViewById(R.id.etStartTime);
            etEndTime = itemView.findViewById(R.id.etEndTime);
            etActivityLocation = itemView.findViewById(R.id.etActivityLocation);
            btnRemoveActivity = itemView.findViewById(R.id.btnRemoveActivity);
        }
    }
}
