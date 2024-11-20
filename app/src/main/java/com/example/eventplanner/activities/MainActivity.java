package com.example.eventplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvTopEvents;
    private RecyclerView rvAllEvents;
    private EventAdapter eventAdapter;
    private EventAdapter allEventsAdapter;
    private List<Event> topEventsList;
    private List<Event> allEventsList;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvTopEvents = findViewById(R.id.rvTopEvents);
        rvAllEvents = findViewById(R.id.rvAllEvents);
        SearchView searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        topEventsList = new ArrayList<>();
        topEventsList.add(new Event("Concert A", R.drawable.event, "Concert"));
        topEventsList.add(new Event("Festival B", R.drawable.event, "Festival"));
        topEventsList.add(new Event("Event C", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Exhibition D", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Workshop E", R.drawable.event, "Workshop"));

        topEventsList.add(new Event("Concert A", R.drawable.event, "Concert"));
        topEventsList.add(new Event("Festival B", R.drawable.event, "Festival"));
        topEventsList.add(new Event("Event C", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Exhibition D", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Workshop E", R.drawable.event, "Workshop"));
        topEventsList.add(new Event("Theater Show F", R.drawable.event, "Theater"));
        topEventsList.add(new Event("Music Fest G", R.drawable.event, "Festival"));
        topEventsList.add(new Event("Art Gallery H", R.drawable.event, "Exhibition"));

        // Set up RecyclerView for top events
        rvTopEvents.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        eventAdapter = new EventAdapter(topEventsList);
        rvTopEvents.setAdapter(eventAdapter);

        // Set up RecyclerView for all events
        rvAllEvents.setLayoutManager(new LinearLayoutManager(this));
        allEventsAdapter = new EventAdapter(allEventsList);
        rvAllEvents.setAdapter(allEventsAdapter);

        // Set up SearchView for filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                allEventsAdapter.filter(newText); // Filter all events based on search query
                return false;
            }
        });

        // Set up Spinner for filtering event types
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedFilter = parentView.getItemAtPosition(position).toString();
                filterEvents(selectedFilter); // Apply filter based on selected option
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
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
        allEventsAdapter.updateEvents(filteredEvents); // Update adapter with filtered list
    }
}
