package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

public class LogInActivity extends AppCompatActivity {

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
                Intent intent = new Intent(LogInActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(LogInActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(LogInActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        getLayoutInflater().inflate(R.layout.activity_login, findViewById(R.id.content_frame), true);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button logInBtn = findViewById(R.id.loginBtn);
        logInBtn.setOnClickListener(v ->
                Toast.makeText(LogInActivity.this, "Login button clicked", Toast.LENGTH_SHORT).show()
        );

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView myTextView = findViewById(R.id.forgotPasswordText);
        myTextView.setOnClickListener(v ->
                Toast.makeText(LogInActivity.this, "Check your email for a link to reset your password.", Toast.LENGTH_SHORT).show()
        );

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, ChooseRoleActivity.class);
            startActivity(intent);
        });
    }
}
