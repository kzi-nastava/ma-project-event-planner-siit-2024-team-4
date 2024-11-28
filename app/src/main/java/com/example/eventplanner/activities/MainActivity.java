package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout topEventsContainer;
    private LinearLayout topProductsContainer;
    private Button viewAllButton;
    private Button viewAllProductsButton;
    private List<Event> topEventsList;
    private List<Event> topProductsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(MainActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main, contentFrame, true);

        topEventsContainer = findViewById(R.id.topEventsContainer);
        topProductsContainer = findViewById(R.id.topProductsContainer);
        viewAllButton = findViewById(R.id.viewAllButton);
        viewAllProductsButton = findViewById(R.id.viewAllProductsButton);

        // Top 5 Events data
        topEventsList = new ArrayList<>();
        topEventsList.add(new Event("Concert A", R.drawable.event, "Concert"));
        topEventsList.add(new Event("Festival B", R.drawable.event, "Festival"));
        topEventsList.add(new Event("Event C", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Exhibition D", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Workshop E", R.drawable.event, "Workshop"));

        // Populate Top 5 Events dynamically
        for (Event event : topEventsList) {
            View eventView = getLayoutInflater().inflate(R.layout.item_event_card, topEventsContainer, false);

            TextView eventName = eventView.findViewById(R.id.eventName);
            ImageView eventImage = eventView.findViewById(R.id.eventImage);

            eventName.setText(event.getName());
            eventImage.setImageResource(event.getImageResId());

            topEventsContainer.addView(eventView);
        }

        // Top 5 Products and Services data
        topProductsList = new ArrayList<>();
        topProductsList.add(new Event("Product A", R.drawable.service_band, "Product"));
        topProductsList.add(new Event("Service B", R.drawable.service_band, "Service"));
        topProductsList.add(new Event("Product C", R.drawable.service_band, "Product"));
        topProductsList.add(new Event("Service D", R.drawable.service_band, "Service"));
        topProductsList.add(new Event("Product E", R.drawable.service_band, "Product"));

        // Populate Top 5 Products and Services dynamically
        for (Event product : topProductsList) {
            View productView = getLayoutInflater().inflate(R.layout.item_event_card, topProductsContainer, false);

            TextView productName = productView.findViewById(R.id.eventName);
            ImageView productImage = productView.findViewById(R.id.eventImage);

            productName.setText(product.getName());
            productImage.setImageResource(product.getImageResId());

            topProductsContainer.addView(productView);
        }

        // OnClickListener za "View All Events" button
        viewAllButton.setOnClickListener(v -> {
            Log.d("MainActivity", "View All button clicked");
            Intent intent = new Intent(MainActivity.this, AllEventsActivity.class);
            startActivity(intent);
        });

        // OnClickListener za "View All Products and Services" button
        viewAllProductsButton.setOnClickListener(v -> {
            Log.d("MainActivity", "View All Products button clicked");

            Intent intent = new Intent(MainActivity.this, AllProductsAndServicesActivity.class);
            startActivity(intent);
        });
    }
}
