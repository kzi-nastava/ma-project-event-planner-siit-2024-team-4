package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.button.MaterialButton;
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
    private MaterialButton btnCreateEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_all_events, contentFrame, true);

        rvAllEvents = findViewById(R.id.rvAllEvents);
        searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        progressBar = findViewById(R.id.progressBar);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        allEventsList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvAllEvents.setLayoutManager(linearLayoutManager);
        allEventAdapter = new EventAdapterNoImage(new ArrayList<>(), this);
        rvAllEvents.setAdapter(allEventAdapter);

        loadEventsFromServer();
        
        setupCreateEventButton();

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
    
    private void setupCreateEventButton() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", null);
        
        if ("EventOrganizer".equals(userRole)) {
            btnCreateEvent.setVisibility(View.VISIBLE);
            btnCreateEvent.setOnClickListener(v -> {
                Intent intent = new Intent(AllEventsActivity.this, EventCreateActivity.class);
                startActivityForResult(intent, 100); // Request code 100 for event creation
            });
        } else {
            // Hide button for non-Event Organizers
            btnCreateEvent.setVisibility(View.GONE);
        }
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadEventsFromServer();
        }
    }

}
