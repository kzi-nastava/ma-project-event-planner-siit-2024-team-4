package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.AuthService;
import com.example.eventplanner.network.dto.LoginRequest;
import com.example.eventplanner.network.dto.LoginResponse;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button logInBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        // Toolbar + navigation meni
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                startActivity(new Intent(LogInActivity.this, MainActivity.class));
            } else if (id == R.id.nav_service) {
                startActivity(new Intent(LogInActivity.this, ServiceActivity.class));
            } else if (id == R.id.nav_login) {
                startActivity(new Intent(LogInActivity.this, LogInActivity.class));
            } else if (id == R.id.nav_registration) {
                startActivity(new Intent(LogInActivity.this, ChooseRoleActivity.class));
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        // Učitaj login layout unutar base_layout
        getLayoutInflater().inflate(R.layout.activity_login, findViewById(R.id.content_frame), true);

        // Povezivanje sa XML elementima
        emailInput = findViewById(R.id.enterEmailText);
        passwordInput = findViewById(R.id.enterPasswordText);
        logInBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        logInBtn.setOnClickListener(v -> doLogin());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView myTextView = findViewById(R.id.forgotPasswordText);
        myTextView.setOnClickListener(v ->
                Toast.makeText(LogInActivity.this, "Check your email for a reset link.", Toast.LENGTH_SHORT).show()
        );

        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(LogInActivity.this, ChooseRoleActivity.class));
        });
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
                    Toast.makeText(LogInActivity.this, "Welcome " + response.body().getEmail(), Toast.LENGTH_SHORT).show();

                    // TODO: ovde sačuvaj token ako ga backend šalje
                    startActivity(new Intent(LogInActivity.this, MainActivity.class));
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
