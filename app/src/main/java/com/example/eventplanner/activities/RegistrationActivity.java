package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.*;

import fragments.EORegistrationFragment;
import fragments.SPPRegistrationFragment;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

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