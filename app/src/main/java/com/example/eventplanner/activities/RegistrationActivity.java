package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

import com.example.eventplanner.activities.fragments.EORegistrationFragment;
import com.example.eventplanner.activities.fragments.SPPRegistrationFragment;

public class RegistrationActivity extends AppCompatActivity {

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

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_registration, contentFrame, true);

        String role = getIntent().getStringExtra("ROLE");
        Fragment selectedFragment;

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                // Intent intent = new Intent(); nemamo homepage
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(RegistrationActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(RegistrationActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(RegistrationActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });


        if ("Event organizer".equals(role)) {
            selectedFragment = new EORegistrationFragment();
        } else if ("Service and product provider".equals(role)) {
            selectedFragment = new SPPRegistrationFragment();
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
    }
}
