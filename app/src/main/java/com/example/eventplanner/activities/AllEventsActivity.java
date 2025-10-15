package com.example.eventplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.EventService;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends BaseActivity {

    private RecyclerView rvAllEvents;
    private EventAdapterNoImage allEventAdapter;
    private List<EventDTO> allEventsList;
    private Spinner spinnerFilter;
    private SearchView searchView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_all_events, contentFrame, true);

        rvAllEvents = findViewById(R.id.rvAllEvents);
        searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        progressBar = findViewById(R.id.progressBar);

        // Initialize events list
        allEventsList = new ArrayList<>();

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvAllEvents.setLayoutManager(linearLayoutManager);
        allEventAdapter = new EventAdapterNoImage(new ArrayList<>());
        rvAllEvents.setAdapter(allEventAdapter);

        // Load events from server
        loadEventsFromServer();

        // Pretraga događaja
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                allEventAdapter.filter(newText);  // Filtrira događaje
                return false;
            }
        });

        // Podesavanje spinnera za filtriranje
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                filterEvents(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterEvents("All");
            }
        });
    }

    private void filterEvents(String filter) {
        List<EventDTO> filteredEvents = new ArrayList<>();
        if (filter.equals("All")) {
            filteredEvents.addAll(allEventsList);
        } else {
            for (EventDTO event : allEventsList) {
                if (event.getEventTypeName().equalsIgnoreCase(filter)) {
                    filteredEvents.add(event);
                }
            }
        }
        allEventAdapter.updateEvents(filteredEvents);
    }

    private void loadEventsFromServer() {
        progressBar.setVisibility(View.VISIBLE);
        
        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<List<EventDTO>> call = eventService.getAllEvents();
        
        call.enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    allEventsList.clear();
                    allEventsList.addAll(response.body());
                    allEventAdapter.updateEvents(allEventsList);
                } else {
                    Toast.makeText(AllEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllEventsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
