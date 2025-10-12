package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupDrawerContent();
    }

    private void setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_service) {
                startActivity(new Intent(this, ServiceActivity.class));
            } else if (id == R.id.nav_login) {
                startActivity(new Intent(this, LogInActivity.class));
            } else if (id == R.id.nav_registration) {
                startActivity(new Intent(this, ChooseRoleActivity.class));
            } else if (id == R.id.nav_profile) {
                openProfile();
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    private void openProfile() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            return;
        }

        startActivity(new Intent(this, ProfileActivity.class));
    }
}
