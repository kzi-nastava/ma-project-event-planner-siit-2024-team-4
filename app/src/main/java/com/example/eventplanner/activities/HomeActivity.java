package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.ApiService;
import com.example.eventplanner.network.dto.EventDTO;
import com.example.eventplanner.network.dto.SolutionDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * HomeActivity – Početna (Student 2 – tačka 1.4)
 * - prikaz Top 5 Events (horizontalno)
 * - prikaz Top 5 Products & Services (horizontalno)
 * - dugmad za prelaz na AllEvents i AllProductsAndServices ekrane
 */
public class HomeActivity extends AppCompatActivity {

    private ApiService api;

    // Adapteri za top liste
    private TopEventsAdapter eventsAdapter;
    private TopSolutionsAdapter solutionsAdapter;

    // UI
    private RecyclerView rvTopEvents;
    private RecyclerView rvTopSolutions;
    private Button btnAllEvents;
    private Button btnAllSolutions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // obavezno da layout ima ID-jeve iz koda

        // 1) API klijent
        api = ApiClient.getClient().create(ApiService.class);

        // 2) UI reference
        rvTopEvents    = findViewById(R.id.rvTopEvents);
        rvTopSolutions = findViewById(R.id.rvTopSolutions);
        btnAllEvents   = findViewById(R.id.btnViewAllEvents);
        btnAllSolutions= findViewById(R.id.btnViewAllSolutions);

        // 3) LayoutManager-i (horizontalni karuseli)
        rvTopEvents.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvTopSolutions.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        // 4) Adapteri (sa klikovima)
        eventsAdapter = new TopEventsAdapter(item -> {
            // Kasnije možeš otvoriti detalje event-a sa ID-em;
            // za sada idemo na listu svih događaja:
            startActivity(new Intent(this, AllEventsActivity.class));
        });

        solutionsAdapter = new TopSolutionsAdapter(item -> {
            // Kasnije možeš otvoriti ServiceActivity sa prosleđenim ID-em:
            // Intent i = new Intent(this, ServiceActivity.class);
            // i.putExtra("serviceId", item.id);
            // startActivity(i);
            startActivity(new Intent(this, AllProductsAndServicesActivity.class));
        });

        rvTopEvents.setAdapter(eventsAdapter);
        rvTopSolutions.setAdapter(solutionsAdapter);

        // 5) Dugmad “View All …”
        btnAllEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AllEventsActivity.class)));

        btnAllSolutions.setOnClickListener(v ->
                startActivity(new Intent(this, AllProductsAndServicesActivity.class)));

        // 6) Učitavanje podataka
        loadTop5Events();
        loadTop5Solutions();
    }

    // ======================
    //   NETWORK POZIVI
    // ======================

    private void loadTop5Events() {
        api.getTop5Events().enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(HomeActivity.this, "No events", Toast.LENGTH_SHORT).show();
                    eventsAdapter.replaceAll(new ArrayList<>()); // prazan fallback
                    return;
                }
                eventsAdapter.replaceAll(res.body());
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                eventsAdapter.replaceAll(new ArrayList<>());
            }
        });
    }

    private void loadTop5Solutions() {
        api.getTop5Solutions().enqueue(new Callback<java.util.Collection<SolutionDTO>>() {
            @Override
            public void onResponse(Call<java.util.Collection<SolutionDTO>> call,
                                   Response<java.util.Collection<SolutionDTO>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(HomeActivity.this, "No products/services", Toast.LENGTH_SHORT).show();
                    solutionsAdapter.replaceAll(new ArrayList<>()); // prazan fallback
                    return;
                }
                // pretvaramo Collection -> List
                solutionsAdapter.replaceAll(new ArrayList<>(res.body()));
            }

            @Override
            public void onFailure(Call<java.util.Collection<SolutionDTO>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                solutionsAdapter.replaceAll(new ArrayList<>());
            }
        });
    }
}
