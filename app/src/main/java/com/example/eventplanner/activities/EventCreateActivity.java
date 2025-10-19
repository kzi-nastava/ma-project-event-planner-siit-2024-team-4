package com.example.eventplanner.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ActivityAdapter;
import com.example.eventplanner.dto.CreateActivityRequest;
import com.example.eventplanner.dto.CreateEventRequest;
import com.example.eventplanner.dto.CreateLocationRequest;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.LocationDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.EventService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventCreateActivity extends BaseActivity {
    
    private Spinner spinnerEventType;
    private EditText etEventName;
    private EditText etDescription;
    private EditText etMaxParticipants;
    private RadioButton rbPublic;
    private RadioButton rbPrivate;
    private EditText etLocationName;
    private EditText etLocationAddress;
    private EditText etEventDate;
    private RecyclerView rvActivities;
    private MaterialButton btnAddActivity;
    private MaterialButton btnCreateEvent;
    private ProgressBar progressBar;
    private WebView webViewMap;
    private android.widget.TextView tvMapError;
    
    private String selectedLocationName = "";
    private String selectedLocationAddress = "";
    private double selectedLatitude = 44.7866; // Default: Belgrade
    private double selectedLongitude = 20.4489; // Default: Belgrade
    
    private List<EventTypeDTO> eventTypes;
    private List<CreateActivityRequest> activities;
    private ActivityAdapter activityAdapter;
    private Calendar selectedDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!isEventOrganizer()) {
            Toast.makeText(this, "Only Event Organizers can create events", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Toast.makeText(this, "Event Organizer access granted", Toast.LENGTH_SHORT).show();
        }
        
        getLayoutInflater().inflate(R.layout.activity_event_create, findViewById(R.id.content_frame), true);
        
        initializeViews();
        setupEventListeners();
        setupMap();
        loadEventTypes();
        setupActivitiesRecyclerView();
    }
    
    private void initializeViews() {
        spinnerEventType = findViewById(R.id.spinnerEventType);
        etEventName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etDescription);
        etMaxParticipants = findViewById(R.id.etMaxParticipants);
        rbPublic = findViewById(R.id.rbPublic);
        rbPrivate = findViewById(R.id.rbPrivate);
        etLocationName = findViewById(R.id.etLocationName);
        etLocationAddress = findViewById(R.id.etLocationAddress);
        etEventDate = findViewById(R.id.etEventDate);
        rvActivities = findViewById(R.id.rvActivities);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        progressBar = findViewById(R.id.progressBar);
        webViewMap = findViewById(R.id.webViewMap);
        tvMapError = findViewById(R.id.tvMapError);
        
        activities = new ArrayList<>();
        selectedDate = Calendar.getInstance();
    }
    
    private void setupEventListeners() {
        etEventDate.setOnClickListener(v -> showDatePicker());
        
        btnAddActivity.setOnClickListener(v -> addActivity());
        
        btnCreateEvent.setOnClickListener(v -> createEvent());
    }
    
    private void setupMap() {
        
        if (webViewMap != null) {
            WebSettings webSettings = webViewMap.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            
            webViewMap.addJavascriptInterface(new MapJavaScriptInterface(), "Android");
            
            webViewMap.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    tvMapError.setVisibility(View.GONE);
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    tvMapError.setText("Map loading failed: " + description);
                    tvMapError.setVisibility(View.VISIBLE);
                }
            });
            
            String mapHtml = getInteractiveMapHtml();
            webViewMap.loadDataWithBaseURL("https://unpkg.com/", mapHtml, "text/html", "UTF-8", null);
        } else {
            tvMapError.setText("Map not available");
            tvMapError.setVisibility(View.VISIBLE);
        }
    }
    
    public class MapJavaScriptInterface {
        @JavascriptInterface
        public void onLocationSelected(double lat, double lng) {
            runOnUiThread(() -> {
                selectedLatitude = lat;
                selectedLongitude = lng;
                
                getAddressFromCoordinates(selectedLatitude, selectedLongitude);
            });
        }
    }
    
    private String getInteractiveMapHtml() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />" +
                "<script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: Arial, sans-serif; }" +
                "#map { width: 100%; height: 200px; }" +
                ".coordinates { position: absolute; top: 10px; left: 10px; background: rgba(255,255,255,0.9); padding: 5px; border-radius: 3px; font-size: 11px; z-index: 1000; }" +
                ".instructions { position: absolute; bottom: 10px; left: 10px; background: rgba(0,0,0,0.7); color: white; padding: 5px; border-radius: 3px; font-size: 10px; z-index: 1000; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id=\"map\"></div>" +
                "<div class=\"coordinates\" id=\"coords\">Lat: " + selectedLatitude + ", Lng: " + selectedLongitude + "</div>" +
                "<div class=\"instructions\">Click on map to select location</div>" +
                "<script>" +
                "var map = L.map('map').setView([" + selectedLatitude + ", " + selectedLongitude + "], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "attribution: '© OpenStreetMap contributors'" +
                "}).addTo(map);" +
                "var marker = L.marker([" + selectedLatitude + ", " + selectedLongitude + "]).addTo(map);" +
                "map.on('click', function(e) {" +
                "marker.setLatLng(e.latlng);" +
                "document.getElementById('coords').innerHTML = 'Lat: ' + e.latlng.lat.toFixed(6) + ', Lng: ' + e.latlng.lng.toFixed(6);" +
                "if (typeof Android !== 'undefined') {" +
                "Android.onLocationSelected(e.latlng.lat, e.latlng.lng);" +
                "}" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";
    }
    
    private void getAddressFromCoordinates(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) addressBuilder.append(", ");
                    addressBuilder.append(address.getAddressLine(i));
                }
                
                selectedLocationAddress = addressBuilder.toString();
                etLocationAddress.setText(selectedLocationAddress);
                
                String eventName = etEventName.getText().toString().trim();
                if (!eventName.isEmpty()) {
                    selectedLocationName = eventName;
                } else {
                    selectedLocationName = address.getLocality() != null ? address.getLocality() : "Selected Location";
                }
                etLocationName.setText(selectedLocationName);
            }
        } catch (IOException e) {
        }
    }
    
    private void setupActivitiesRecyclerView() {
        activityAdapter = new ActivityAdapter(activities);
        rvActivities.setLayoutManager(new LinearLayoutManager(this));
        rvActivities.setAdapter(activityAdapter);
    }
    
    private boolean isEventOrganizer() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", null);
        
        
        boolean isEO = "EO".equals(userRole) || "EVENT_ORGANIZER".equals(userRole) || "EventOrganizer".equals(userRole);
        
        return isEO;
    }
    
    private void loadEventTypes() {
        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<List<EventTypeDTO>> call = eventService.getAllEventTypes();
        
        call.enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    setupEventTypeSpinner();
                } else {
                    Toast.makeText(EventCreateActivity.this, "Failed to load event types", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(EventCreateActivity.this, "Error loading event types: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("All"); // Default option
        
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etEventDate.setText(sdf.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    
    private void addActivity() {
        CreateActivityRequest newActivity = new CreateActivityRequest();
        activities.add(newActivity);
        activityAdapter.notifyItemInserted(activities.size() - 1);
    }
    
    private void createEvent() {
        if (!validateInput()) {
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnCreateEvent.setEnabled(false);
        
        createLocation();
    }
    
    private boolean validateInput() {
        if (etEventName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter event name", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etEventDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select event date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void createLocation() {
        String eventName = etEventName.getText().toString().trim();
        String locationName = !eventName.isEmpty() ? eventName : (selectedLocationName.isEmpty() ? "Event Location" : selectedLocationName);
        String locationAddress = selectedLocationAddress.isEmpty() ? "Location coordinates: " + selectedLatitude + ", " + selectedLongitude : selectedLocationAddress;
        double latitude = selectedLatitude;
        double longitude = selectedLongitude;
        
        CreateLocationRequest locationRequest = new CreateLocationRequest(locationName, locationAddress, latitude, longitude);
        
        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<LocationDTO> call = eventService.createLocation(locationRequest);
        
        call.enqueue(new Callback<LocationDTO>() {
            @Override
            public void onResponse(Call<LocationDTO> call, Response<LocationDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    createEventWithLocation(response.body());
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnCreateEvent.setEnabled(true);
                    Toast.makeText(EventCreateActivity.this, "Failed to create location!", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<LocationDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                Toast.makeText(EventCreateActivity.this, "Failed to create location!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createEventWithLocation(LocationDTO location) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long organizerId = prefs.getLong("user_id", -1L);
        
        String eventName = etEventName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        int maxParticipants = 0;
        if (!etMaxParticipants.getText().toString().trim().isEmpty()) {
            maxParticipants = Integer.parseInt(etMaxParticipants.getText().toString().trim());
        }
        boolean isPublic = rbPublic.isChecked();
        String eventDate = etEventDate.getText().toString().trim();
        
        Long eventTypeId = null;
        String selectedEventType = (String) spinnerEventType.getSelectedItem();
        if (!"All".equals(selectedEventType)) {
            for (EventTypeDTO eventType : eventTypes) {
                if (eventType.getName().equals(selectedEventType)) {
                    eventTypeId = eventType.getId();
                    break;
                }
            }
        }
        
        List<CreateActivityRequest> agendaWithDateTime = new ArrayList<>();
        for (CreateActivityRequest activity : activities) {
            CreateActivityRequest activityWithDateTime = new CreateActivityRequest();
            activityWithDateTime.setName(activity.getName());
            activityWithDateTime.setDescription(activity.getDescription());
            activityWithDateTime.setLocation(activity.getLocation());
            
            if (activity.getStartTime() != null && !activity.getStartTime().isEmpty()) {
                activityWithDateTime.setStartTime(eventDate + "T" + activity.getStartTime() + ":00");
            }
            if (activity.getEndTime() != null && !activity.getEndTime().isEmpty()) {
                activityWithDateTime.setEndTime(eventDate + "T" + activity.getEndTime() + ":00");
            }
            
            agendaWithDateTime.add(activityWithDateTime);
        }
        
        CreateEventRequest eventRequest = new CreateEventRequest(
            eventName,
            description,
            maxParticipants,
            isPublic,
            eventDate + "T00:00:00", // startDate
            eventDate + "T23:59:59", // endDate
            location.getId(),
            eventTypeId,
            agendaWithDateTime,
            organizerId
        );
        
        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<com.example.eventplanner.dto.EventDTO> call = eventService.createEvent(eventRequest);
        
        call.enqueue(new Callback<com.example.eventplanner.dto.EventDTO>() {
            @Override
            public void onResponse(Call<com.example.eventplanner.dto.EventDTO> call, Response<com.example.eventplanner.dto.EventDTO> response) {
                progressBar.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EventCreateActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EventCreateActivity.this, "Failed to create event", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.eventplanner.dto.EventDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                Toast.makeText(EventCreateActivity.this, "Error creating event: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
