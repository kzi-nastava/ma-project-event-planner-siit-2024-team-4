package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends AppCompatActivity {

    private RecyclerView rvAllEvents;
    private EventAdapter allEventAdapter;
    private List<Event> allEventsList;
    private Spinner spinnerFilter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_all_events);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                Intent intent = new Intent(AllEventsActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(AllEventsActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(AllEventsActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(AllEventsActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_all_events, contentFrame, true);

        rvAllEvents = findViewById(R.id.rvAllEvents);
        searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        // Popunjavanje liste događaja
        allEventsList = new ArrayList<>();
        allEventsList.add(new Event("Concert A", R.drawable.event, "Concert"));
        allEventsList.add(new Event("Festival B", R.drawable.event, "Festival"));
        allEventsList.add(new Event("Exhibition C", R.drawable.event, "Exhibition"));
        allEventsList.add(new Event("Workshop D", R.drawable.event, "Workshop"));
        allEventsList.add(new Event("Theater Show E", R.drawable.event, "Theater"));
        allEventsList.add(new Event("Music Fest F", R.drawable.event, "Festival"));
        allEventsList.add(new Event("Art Gallery G", R.drawable.event, "Exhibition"));

        // Podesavanje RecyclerView
        rvAllEvents.setLayoutManager(new LinearLayoutManager(this));
        allEventAdapter = new EventAdapter(allEventsList);
        rvAllEvents.setAdapter(allEventAdapter);

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
        List<Event> filteredEvents = new ArrayList<>();
        if (filter.equals("All")) {
            filteredEvents.addAll(allEventsList);
        } else {
            for (Event event : allEventsList) {
                if (event.getType().equalsIgnoreCase(filter)) {
                    filteredEvents.add(event);
                }
            }
        }
        allEventAdapter.updateEvents(filteredEvents);
    }
}
