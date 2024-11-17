package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.eventplanner.R;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Spinner spinner = findViewById(R.id.dropdown_menu);

        String[] options = new String[] {"-", "Event organizer", "Service and product provider"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        int backgroundColor = getResources().getColor(R.color.spinner_background, getTheme());
        spinner.setBackgroundColor(backgroundColor);

        spinner.setAdapter(adapter);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseRoleActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        Button nextButton = findViewById(R.id.nextBtn);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedRole = spinner.getSelectedItem().toString();

                if (!"-".equals(selectedRole)) {
                    Intent intent = new Intent(ChooseRoleActivity.this, RegistrationActivity.class);
                    intent.putExtra("ROLE", selectedRole);
                    startActivity(intent);
                } else {
                    Toast.makeText(ChooseRoleActivity.this, "You have to select a valid role.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}