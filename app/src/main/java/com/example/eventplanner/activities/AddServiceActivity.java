package com.example.eventplanner.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eventplanner.R;

public class AddServiceActivity extends AppCompatActivity {

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
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
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

}
