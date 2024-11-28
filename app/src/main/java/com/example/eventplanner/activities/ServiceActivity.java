package com.example.eventplanner.activities;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                Intent intent = new Intent(ServiceActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(ServiceActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(ServiceActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(ServiceActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        getLayoutInflater().inflate(R.layout.activity_services_view, findViewById(R.id.content_frame), true);

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