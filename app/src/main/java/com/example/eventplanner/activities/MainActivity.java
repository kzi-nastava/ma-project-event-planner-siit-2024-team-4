package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.eventplanner.R;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;



public class MainActivity extends AppCompatActivity {

    private LinearLayout topEventsContainer;
    private Button viewAllButton;
    private List<Event> topEventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topEventsContainer = findViewById(R.id.topEventsContainer);
        viewAllButton = findViewById(R.id.viewAllButton);

        // Top 5 Events data
        topEventsList = new ArrayList<>();
        topEventsList.add(new Event("Concert A", R.drawable.event, "Concert"));
        topEventsList.add(new Event("Festival B", R.drawable.event, "Festival"));
        topEventsList.add(new Event("Event C", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Exhibition D", R.drawable.event, "Exhibition"));
        topEventsList.add(new Event("Workshop E", R.drawable.event, "Workshop"));

        // Populate Top 5 Events dynamically
        for (Event event : topEventsList) {
            View eventView = getLayoutInflater().inflate(R.layout.item_event_card, topEventsContainer, false);

            TextView eventName = eventView.findViewById(R.id.eventName);
            ImageView eventImage = eventView.findViewById(R.id.eventImage);

            eventName.setText(event.getName());
            eventImage.setImageResource(event.getImageResId());

            topEventsContainer.addView(eventView);
        }


        viewAllButton.setOnClickListener(v -> {
            Log.d("MainActivity", "View All button clicked");
            Intent intent = new Intent(MainActivity.this, AllEventsActivity.class);
            startActivity(intent);
        });
    }
}
