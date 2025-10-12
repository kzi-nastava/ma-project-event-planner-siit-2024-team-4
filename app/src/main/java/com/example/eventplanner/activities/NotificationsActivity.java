package com.example.eventplanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.ApiService;
import com.example.eventplanner.network.dto.NotificationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private static final String PREFS = "eventplanner_prefs";
    private static final String KEY_MUTE = "mute_notifications";

    private ApiService api;
    private NotificationsAdapter adapter;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        api = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        Switch switchMute = findViewById(R.id.switchMute);
        RecyclerView rv = findViewById(R.id.rvNotifications);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter();
        rv.setAdapter(adapter);

        // učitaj mute state
        boolean isMuted = prefs.getBoolean(KEY_MUTE, false);
        switchMute.setChecked(isMuted);

        switchMute.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_MUTE, checked).apply();
            Toast.makeText(this,
                    checked ? "Notifications muted" : "Notifications enabled",
                    Toast.LENGTH_SHORT).show();
        });

        // ako nisu mutovana, učitaj sa backenda
        if (!isMuted) loadNotifications();
    }

    private void loadNotifications() {
        api.getNotifications().enqueue(new Callback<List<NotificationDTO>>() {
            @Override public void onResponse(Call<List<NotificationDTO>> call, Response<List<NotificationDTO>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(NotificationsActivity.this, "No notifications", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.replaceAll(res.body());
            }

            @Override public void onFailure(Call<List<NotificationDTO>> call, Throwable t) {
                Toast.makeText(NotificationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
