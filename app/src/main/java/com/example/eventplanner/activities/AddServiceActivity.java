package com.example.eventplanner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eventplanner.R;

public class AddServiceActivity extends AppCompatActivity {

    private View[] eventTypes;
    private boolean[] selectedStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_service);

        View category1 = findViewById(R.id.category1);
        View category2 = findViewById(R.id.category2);
        View category3 = findViewById(R.id.category3);
        View category4 = findViewById(R.id.category4);

        View[] categories = {category1, category2, category3, category4};

        for (View category : categories) {
            category.setOnClickListener(v -> selectCategory(v, categories));
        }
        ImageButton addNewCategoryBtn = findViewById(R.id.addNewCategoryButton);
        addNewCategoryBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_new_category, null);
            builder.setView(dialogView);

            EditText nameCategoryText = dialogView.findViewById(R.id.name_of_category);
            EditText descriptionCategoryText = dialogView.findViewById(R.id.description_of_category);

            builder.setPositiveButton("Submit", (dialog, which) -> {
                String name = nameCategoryText.getText().toString();
                String description = descriptionCategoryText.getText().toString();
                // catch name and description
                Toast.makeText(AddServiceActivity.this, "Category successfully added!", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        View eventType1 = findViewById(R.id.eventType1);
        View eventType2 = findViewById(R.id.eventType2);
        View eventType3 = findViewById(R.id.eventType3);
        View eventType4 = findViewById(R.id.eventType4);
        View eventType5 = findViewById(R.id.eventType5);

        eventTypes = new View[]{eventType1, eventType2, eventType3, eventType4, eventType5};
        selectedStates = new boolean[eventTypes.length];

        for (int i = 0; i < eventTypes.length; i++) {
            final int index = i;
            eventTypes[i].setOnClickListener(v -> toggleSelection(index));
        }

        Button btnAddNew = findViewById(R.id.add);
        btnAddNew.setOnClickListener(v -> {
            Toast.makeText(AddServiceActivity.this, "Service successfully added!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddServiceActivity.this, ServiceActivity.class);
            startActivity(intent);
        });

        Button btnCancel = findViewById(R.id.cancel);
        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(AddServiceActivity.this, ServiceActivity.class);

            startActivity(intent);
        });
    }

    private void selectCategory(View selected, View[] categories) {
        for (View category : categories) {
            category.setBackgroundResource(R.drawable.border_background_black);
            TextView serviceName = category.findViewById(R.id.service_name);
            serviceName.setTextColor(ContextCompat.getColor(this, R.color.black));
            TextView serviceDescription = category.findViewById(R.id.service_description);
            serviceDescription.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
        selected.setBackgroundResource(R.drawable.border_background_purple);
        TextView selectedServiceName = selected.findViewById(R.id.service_name);
        selectedServiceName.setTextColor(ContextCompat.getColor(this, R.color.white));
        TextView selectedServiceDescription = selected.findViewById(R.id.service_description);
        selectedServiceDescription.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void toggleSelection(int index) {
        selectedStates[index] = !selectedStates[index];
        if (selectedStates[index]) {
            eventTypes[index].setBackgroundResource(R.drawable.border_background_purple);
            TextView eventTypeName = eventTypes[index].findViewById(R.id.name_of_event_type);
            eventTypeName.setTextColor(ContextCompat.getColor(this, R.color.white));
            TextView eventTypeDescription = eventTypes[index].findViewById(R.id.event_type_description);
            eventTypeDescription.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            eventTypes[index].setBackgroundResource(R.drawable.border_background_black);
            TextView eventTypeName = eventTypes[index].findViewById(R.id.name_of_event_type);
            eventTypeName.setTextColor(ContextCompat.getColor(this, R.color.black));
            TextView eventTypeDescription = eventTypes[index].findViewById(R.id.event_type_description);
            eventTypeDescription.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }
}
