package com.example.eventplanner.activities;

import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.eventplanner.R;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvTopEvents;
    private RecyclerView rvAllEvents;
    private EventAdapter eventAdapter;
    private List<Event> topEventsList;
    private List<Event> allEventsList;
    private EventAdapter allEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvTopEvents = findViewById(R.id.rvTopEvents);
        rvAllEvents = findViewById(R.id.rvAllEvents);
        SearchView searchView = findViewById(R.id.searchView);

        topEventsList = new ArrayList<>();
        topEventsList.add(new Event("Concert A", R.drawable.event));
        topEventsList.add(new Event("Festival B", R.drawable.event));
        topEventsList.add(new Event("Event C", R.drawable.event));
        topEventsList.add(new Event("Exhibition D", R.drawable.event));
        topEventsList.add(new Event("Workshop E", R.drawable.event));

        allEventsList = new ArrayList<>();
        allEventsList.add(new Event("Concert A", R.drawable.event));
        allEventsList.add(new Event("Festival B", R.drawable.event));
        allEventsList.add(new Event("Event C", R.drawable.event));
        allEventsList.add(new Event("Exhibition D", R.drawable.event));
        allEventsList.add(new Event("Workshop E", R.drawable.event));
        allEventsList.add(new Event("Music Concert F", R.drawable.event));
        allEventsList.add(new Event("Theater G", R.drawable.event));

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
    }
}
