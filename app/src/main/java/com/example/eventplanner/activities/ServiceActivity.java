package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnFilters = findViewById(R.id.filterBtn);
        btnFilters.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.FullScreenBottomSheetDialog);
            View dialogView = getLayoutInflater().inflate(R.layout.service_filter, null);
            bottomSheetDialog.setContentView(dialogView);
            bottomSheetDialog.show();
        });

        FloatingActionButton btnAddNew = findViewById(R.id.addNewService);
        btnAddNew.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceActivity.this, AddServiceActivity.class);
            startActivity(intent);
        });

        RelativeLayout card1 = findViewById(R.id.card1);
        RelativeLayout card2 = findViewById(R.id.card2);
        RelativeLayout card3 = findViewById(R.id.card3);
        RelativeLayout card4 = findViewById(R.id.card4);
        RelativeLayout card5 = findViewById(R.id.card5);

        card1.setOnClickListener(v -> openEditServiceActivity());
        card2.setOnClickListener(v -> openEditServiceActivity());
        card3.setOnClickListener(v -> openEditServiceActivity());
        card4.setOnClickListener(v -> openEditServiceActivity());
        card5.setOnClickListener(v -> openEditServiceActivity());
    }

    private void openEditServiceActivity() {
        Intent intent = new Intent(ServiceActivity.this, EditServiceActivity.class);
        startActivity(intent);
    }
}