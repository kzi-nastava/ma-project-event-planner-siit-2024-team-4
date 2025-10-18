package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.AuthService;
import com.example.eventplanner.dto.LoginRequest;
import com.example.eventplanner.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends BaseActivity {

    private EditText emailInput, passwordInput;
    private Button logInBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_login, findViewById(R.id.content_frame), true);

        emailInput = findViewById(R.id.enterEmailText);
        passwordInput = findViewById(R.id.enterPasswordText);
        logInBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        logInBtn.setOnClickListener(v -> doLogin());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView myTextView = findViewById(R.id.forgotPasswordText);
        myTextView.setOnClickListener(v ->
                Toast.makeText(LogInActivity.this, "Check your email for a reset link.", Toast.LENGTH_SHORT).show()
        );

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(LogInActivity.this, ChooseRoleActivity.class))
        );
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthService service = ApiClient.getClient().create(AuthService.class);
        LoginRequest request = new LoginRequest(email, password);

        service.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("LogInActivity", "Response code: " + response.code());
                Log.d("LogInActivity", "Response message: " + response.message());
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    Log.d("LogInActivity", "Login response - Role: " + loginResponse.getRole());
                    Log.d("LogInActivity", "Login response - Email: " + loginResponse.getEmail());
                    Log.d("LogInActivity", "Login response - UserId: " + loginResponse.getUserId());
                    Log.d("LogInActivity", "Login response - Token: " + (loginResponse.getToken() != null ? "Present" : "Null"));

                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("jwt_token", loginResponse.getToken());
                    editor.putString("user_role", loginResponse.getRole());
                    editor.putLong("user_id", loginResponse.getUserId());
                    editor.apply();
                    
                    Log.d("LogInActivity", "Saved role to SharedPreferences: " + loginResponse.getRole());

                    Toast.makeText(LogInActivity.this, "Welcome " + loginResponse.getEmail(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("LogInActivity", "Login failed - Code: " + response.code() + ", Message: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("LogInActivity", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("LogInActivity", "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(LogInActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LogInActivity", "Network error: " + t.getMessage(), t);
                Toast.makeText(LogInActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
