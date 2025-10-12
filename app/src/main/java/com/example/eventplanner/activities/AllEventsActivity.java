package com.example.eventplanner.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.ApiService;
import com.example.eventplanner.network.dto.EventDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Lista svih događaja sa paginacijom i pretragom (tačka 1.4) */
public class AllEventsActivity extends AppCompatActivity {

    private ApiService api;
    private EventAdapter adapter;
    private ProgressBar progress;

    private int page = 0;
    private final int size = 10;
    private String sort = "date,asc"; // promeni po želji
    private String query = null;

    private com.example.eventplanner.activities.EndlessScrollListener endless;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);

        api = ApiClient.getClient().create(ApiService.class);
        progress = findViewById(R.id.progress);

        RecyclerView rv = findViewById(R.id.rv);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);

        adapter = new EventAdapter(item -> {
            // TODO: otvori EventDetailsActivity kad budeš imala
            Toast.makeText(this, "Event: " + item.name, Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        endless = new EndlessScrollListener(lm) {
            @Override public void onLoadMore() { loadNextPage(); }
        };
        rv.addOnScrollListener(endless);

        // Search
        EditText etQuery = findViewById(R.id.etQuery);
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                resetAndLoad();
            }
        });

        // initial load
        resetAndLoad();
    }

    private void resetAndLoad() {
        page = 0;
        adapter.replaceAll(new ArrayList<>());
        endless.setLastPage(false);
        loadNextPage();
    }

    private void loadNextPage() {
        progress.setVisibility(View.VISIBLE);
        endless.setLoading(true);

        api.getEvents(page, size, sort, (query == null || query.isEmpty()) ? null : query)
                .enqueue(new Callback<ApiService.Page<EventDTO>>() {
                    @Override public void onResponse(Call<ApiService.Page<EventDTO>> call,
                                                     Response<ApiService.Page<EventDTO>> res) {
                        progress.setVisibility(View.GONE);
                        endless.setLoading(false);

                        if (!res.isSuccessful() || res.body() == null) {
                            Toast.makeText(AllEventsActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ApiService.Page<EventDTO> pageRes = res.body();
                        if (page == 0) adapter.replaceAll(pageRes.content);
                        else adapter.addAll(pageRes.content);

                        if (pageRes.last || pageRes.number >= pageRes.totalPages - 1) {
                            endless.setLastPage(true);
                        } else {
                            page++; // spremi se za sledeću
                        }
                    }

                    @Override public void onFailure(Call<ApiService.Page<EventDTO>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        endless.setLoading(false);
                        Toast.makeText(AllEventsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
