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
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    

                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("jwt_token", loginResponse.getToken());
                    editor.putString("user_role", loginResponse.getRole());
                    editor.putString("user_id", String.valueOf(loginResponse.getUserId()));
                    editor.apply();
                    

                    Toast.makeText(LogInActivity.this, "Welcome " + loginResponse.getEmail(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LogInActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LogInActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
