package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.ChangePasswordDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.ProfileService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends BaseActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnConfirm, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_change_password, findViewById(R.id.content_frame), true);

        initViews();
        setupButtons();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupButtons() {
        btnConfirm.setOnClickListener(v -> changePassword());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(currentPassword, newPassword);
        ProfileService profileService = ApiClient.getClient(this).create(ProfileService.class);

        profileService.changePassword("Bearer " + token, changePasswordDTO).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Backend returns a string message
                    String message = response.body();
                    Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "Failed to change password";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
