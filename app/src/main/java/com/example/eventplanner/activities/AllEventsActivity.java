package com.example.eventplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
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
        setContentView(R.layout.activity_all_events);

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
