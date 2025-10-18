package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventplanner.R;
import com.example.eventplanner.fragments.AllServicesFragment;

public class ServicesActivity extends BaseActivity {
    
    private boolean isMyServices = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        isMyServices = intent.getBooleanExtra("isMyServices", false);
        
        if (isMyServices) {
            setTitle("My Services");
        } else {
            setTitle("Available Services");
        }
        
        Fragment fragment = AllServicesFragment.newInstance(isMyServices);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

}