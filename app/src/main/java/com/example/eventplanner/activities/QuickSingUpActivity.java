package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickSingUpActivity extends AppCompatActivity {

    private static final String PREFS = "eventplanner_prefs";
    private static final String KEY_TOKEN = "jwt";

    private ApiService api;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_sing_up);

        api = ApiClient.getClient().create(ApiService.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        // 1) Parsiraj deeplink parametre
        prefillFromDeeplink(getIntent() != null ? getIntent().getData() : null);

        // 2) Submit
        btnConfirm.setOnClickListener(v -> doQuickSignup());
    }

    private void prefillFromDeeplink(@Nullable Uri data) {
        if (data == null) return;
        // podržava: ep://quick-signup?email=...&token=...   ili   https://example.com/quick-signup?email=...&token=...
        String email = data.getQueryParameter("email");
        String token = data.getQueryParameter("token");

        if (email != null) etEmail.setText(email);

        // Ako nam backend već šalje token u linku → odmah ga sačuvamo i idemo na Home
        if (token != null && !token.trim().isEmpty()) {
            saveTokenAndGoHome(token);
        }
    }

    private void doQuickSignup() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString(); // može i prazno

        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.QuickSignupRequest body = new ApiService.QuickSignupRequest();
        body.email = email;
        body.password = password.isEmpty() ? null : password;

        api.quickSignup(body).enqueue(new Callback<ApiService.TokenDTO>() {
            @Override public void onResponse(Call<ApiService.TokenDTO> call, Response<ApiService.TokenDTO> res) {
                if (!res.isSuccessful() || res.body() == null || res.body().token == null) {
                    Toast.makeText(QuickSingUpActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveTokenAndGoHome(res.body().token);
            }

            @Override public void onFailure(Call<ApiService.TokenDTO> call, Throwable t) {
                Toast.makeText(QuickSingUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTokenAndGoHome(String token) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();

        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity(); // očisti back stack
    }
}
