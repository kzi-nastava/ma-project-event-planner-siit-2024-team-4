package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

public class ChooseRoleActivity extends AppCompatActivity {

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
                // Intent intent = new Intent(); nemamo homepage
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(ChooseRoleActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(ChooseRoleActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(ChooseRoleActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        getLayoutInflater().inflate(R.layout.activity_choose_role, findViewById(R.id.content_frame), true);

        Spinner spinner = findViewById(R.id.dropdown_menu);
        String[] options = new String[] {"-", "Event organizer", "Service and product provider"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        spinner.setAdapter(adapter);

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseRoleActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        Button nextButton = findViewById(R.id.nextBtn);
        nextButton.setOnClickListener(v -> {
            String selectedRole = spinner.getSelectedItem().toString();
            if (!"-".equals(selectedRole)) {
                Intent intent = new Intent(ChooseRoleActivity.this, RegistrationActivity.class);
                intent.putExtra("ROLE", selectedRole);
                startActivity(intent);
            } else {
                Toast.makeText(ChooseRoleActivity.this, "You have to select a valid role.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
