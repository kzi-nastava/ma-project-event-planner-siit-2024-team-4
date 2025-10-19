package com.example.eventplanner.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.example.eventplanner.R;
import com.example.eventplanner.fragments.*;

public class RegistrationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_registration, findViewById(R.id.content_frame), true);

        String role = getIntent().getStringExtra("ROLE");

        Fragment selectedFragment;
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

