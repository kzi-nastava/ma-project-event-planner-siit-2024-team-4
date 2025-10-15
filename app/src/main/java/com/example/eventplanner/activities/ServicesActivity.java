package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.ServiceService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesActivity extends BaseActivity implements ServiceAdapter.ServiceActionListener {
    
    private RecyclerView recyclerServices;
    private ServiceAdapter serviceAdapter;
    private List<ServiceDTO> services;
    
    private EditText etSearch;
    private Button btnSearch;
    private Button btnFilter;
    private Button btnAddService;
    private LinearLayout filterLayout;
    private Button btnToggleFilters;
    private boolean filtersVisible = false;
    
    // Filter controls
    private EditText etMinPrice, etMaxPrice;
    private Spinner spinnerCategory, spinnerEventType, spinnerAvailability;
    private Button btnApplyFilters;
    
    // Data for spinners
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    
    private boolean isMyServices = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate the services layout into the content frame
        getLayoutInflater().inflate(R.layout.activity_services, findViewById(R.id.content_frame));
        setTitle(R.string.services);
        
        // Check if this is "My Services" mode
        Intent intent = getIntent();
        isMyServices = intent.getBooleanExtra("isMyServices", false);
        
        Log.d("ServicesActivity", "onCreate - isMyServices: " + isMyServices);
        
        if (isMyServices) {
            setTitle("My Services");
        }
        
        initViews();
        setupRecyclerView();
        loadFilterData();
        loadServices();
    }

    private void initViews() {
        recyclerServices = findViewById(R.id.recyclerServices);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddService = findViewById(R.id.btnAddService);
        filterLayout = findViewById(R.id.filterLayout);
        btnToggleFilters = findViewById(R.id.btnToggleFilters);
        
        // Filter controls
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerEventType = findViewById(R.id.spinnerEventType);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        Button btnClearFilters = findViewById(R.id.btnClearFilters);

        // Set up search functionality
        btnSearch.setOnClickListener(v -> searchServices());

        // Set up filter toggle
        btnToggleFilters.setOnClickListener(v -> toggleFilters());
        
        // Set up apply filters
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        
        // Set up clear filters
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Set up add service button (only for service providers in My Services view)
        if (isServiceProvider() && isMyServices) {
            btnAddService.setVisibility(View.VISIBLE);
            btnAddService.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddServiceActivity.class);
                startActivity(intent);
            });
        } else {
            btnAddService.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(services, this, isMyServices);
        recyclerServices.setLayoutManager(new LinearLayoutManager(this));
        recyclerServices.setAdapter(serviceAdapter);
        Log.d("ServicesActivity", "RecyclerView setup complete with " + services.size() + " services");
    }

    private void loadServices() {
        Log.d("ServicesActivity", "loadServices called");
        ServiceService service = ApiClient.getClient(this).create(ServiceService.class);
        
        Call<List<ServiceDTO>> call;
        if (isMyServices) {
            // Load my services - use getServicesByProviderId method
            Long providerId = getCurrentUserId();
            Log.d("ServicesActivity", "Loading my services for provider ID: " + providerId);
            if (providerId == -1L) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            call = service.getServicesByProviderId(getAuthHeader(), providerId);
        } else {
            // Load all services
            Log.d("ServicesActivity", "Loading all services");
            call = service.getAllServices(getAuthHeader());
        }

        call.enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, retrofit2.Response<List<ServiceDTO>> response) {
                Log.d("ServicesActivity", "Response received: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    List<ServiceDTO> allServices = response.body();
                    Log.d("ServicesActivity", "Total services from backend: " + allServices.size());
                    Log.d("ServicesActivity", "isMyServices: " + isMyServices);
                    Log.d("ServicesActivity", "isServiceProvider: " + isServiceProvider());
                    
                    // Debug: Log raw response
                    Log.d("ServicesActivity", "Raw response body: " + response.body().toString());
                    
                    // For now, show ALL services to debug the issue
                    services.addAll(allServices);
                    Log.d("ServicesActivity", "Added " + services.size() + " services to display");
                    
                    for (ServiceDTO service : services) {
                        Long providerId = service.getProvider() != null ? service.getProvider().getId() : service.getProviderId();
                        Log.d("ServicesActivity", "Service: " + service.getName() + 
                              ", Available: " + service.isAvailable() + 
                              ", Price: " + service.getPrice() + 
                              ", Description: " + service.getDescription() +
                              ", Provider ID: " + providerId +
                              ", Provider Name: " + (service.getProvider() != null ? service.getProvider().getName() : "N/A"));
                    }
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ServicesActivity", "Error loading services: " + response.code() + " " + response.message());
                    Toast.makeText(ServicesActivity.this, "Error loading services: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Log.e("ServicesActivity", "Network error loading services: " + t.getMessage(), t);
                Toast.makeText(ServicesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchServices() {
        String searchTerm = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(searchTerm)) {
            loadServices();
            return;
        }

        ServiceService service = ApiClient.getClient(this).create(ServiceService.class);
        service.searchServices(getAuthHeader(), searchTerm).enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, retrofit2.Response<List<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    services.addAll(response.body());
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ServicesActivity.this, "Error searching services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Toast.makeText(ServicesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilterData() {
        // Load categories
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, retrofit2.Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    setupCategorySpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                // Handle error silently
            }
        });

        // Load event types
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, retrofit2.Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                // Handle error silently
            }
        });

        // Setup availability spinner
        setupAvailabilitySpinner();
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        for (CategoryDTO category : categories) {
            if (category.isApprovedByAdmin) {
                categoryNames.add(category.name);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("All Event Types");
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
    }

    private void setupAvailabilitySpinner() {
        String[] availabilityOptions = {"All", "Available Only", "Not Available Only"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availabilityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(adapter);
    }

    private void toggleFilters() {
        filtersVisible = !filtersVisible;
        filterLayout.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
    }

    private void applyFilters() {
        ServiceService service = ApiClient.getClient(this).create(ServiceService.class);
        
        // Get filter values - make them final for inner class
        final Long categoryId;
        if (spinnerCategory.getSelectedItemPosition() > 0) {
            String selectedCategory = (String) spinnerCategory.getSelectedItem();
            Long tempCategoryId = null;
            for (CategoryDTO category : categories) {
                if (category.name.equals(selectedCategory)) {
                    tempCategoryId = category.id;
                    break;
                }
            }
            categoryId = tempCategoryId;
        } else {
            categoryId = null;
        }

        final Long eventTypeId;
        if (spinnerEventType.getSelectedItemPosition() > 0) {
            String selectedEventType = (String) spinnerEventType.getSelectedItem();
            Long tempEventTypeId = null;
            for (EventTypeDTO eventType : eventTypes) {
                if (eventType.getName().equals(selectedEventType)) {
                    tempEventTypeId = eventType.getId();
                    break;
                }
            }
            eventTypeId = tempEventTypeId;
        } else {
            eventTypeId = null;
        }

        Double tempMinPrice = null;
        if (!TextUtils.isEmpty(etMinPrice.getText().toString())) {
            try {
                tempMinPrice = Double.parseDouble(etMinPrice.getText().toString());
            } catch (NumberFormatException e) {
                // Keep null
            }
        }
        final Double minPrice = tempMinPrice;

        Double tempMaxPrice = null;
        if (!TextUtils.isEmpty(etMaxPrice.getText().toString())) {
            try {
                tempMaxPrice = Double.parseDouble(etMaxPrice.getText().toString());
            } catch (NumberFormatException e) {
                // Keep null
            }
        }
        final Double maxPrice = tempMaxPrice;

        final Boolean isAvailable;
        int availabilitySelection = spinnerAvailability.getSelectedItemPosition();
        if (availabilitySelection == 1) {
            isAvailable = true;
        } else if (availabilitySelection == 2) {
            isAvailable = false;
        } else {
            isAvailable = null;
        }

        // Apply filters - client-side filtering
        // First, we need to reload all services to get fresh data
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        Call<List<ServiceDTO>> call;
        if (isMyServices) {
            Long providerId = getCurrentUserId();
            if (providerId == -1L) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            call = serviceAPI.getServicesByProviderId(getAuthHeader(), providerId);
        } else {
            call = serviceAPI.getAllServices(getAuthHeader());
        }
        
        call.enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> allServices = response.body();
                    List<ServiceDTO> filteredServices = new ArrayList<>();
                    
                    for (ServiceDTO serviceItem : allServices) {
                        boolean matches = true;
                        
                        // For non-SPP users, only show available services
                        if (!isMyServices && !isServiceProvider() && !serviceItem.isAvailable()) {
                            matches = false;
                        }
                        
                        // Filter by category
                        if (categoryId != null && (serviceItem.getCategory() == null || !serviceItem.getCategory().id.equals(categoryId))) {
                            matches = false;
                        }
                        
                        // Filter by event type
                        if (eventTypeId != null) {
                            boolean hasEventType = false;
                            if (serviceItem.getEventTypes() != null) {
                                for (EventTypeDTO eventType : serviceItem.getEventTypes()) {
                                    if (eventType.getId().equals(eventTypeId)) {
                                        hasEventType = true;
                                        break;
                                    }
                                }
                            }
                            if (!hasEventType) {
                                matches = false;
                            }
                        }
                        
                        // Filter by price
                        if (minPrice != null && serviceItem.getPrice() < minPrice) {
                            matches = false;
                        }
                        if (maxPrice != null && serviceItem.getPrice() > maxPrice) {
                            matches = false;
                        }
                        
                        // Filter by availability
                        if (isAvailable != null && serviceItem.isAvailable() != isAvailable) {
                            matches = false;
                        }
                        
                        if (matches) {
                            filteredServices.add(serviceItem);
                        }
                    }
                    
                    // Update the displayed services with filtered results
                    services.clear();
                    services.addAll(filteredServices);
                    serviceAdapter.notifyDataSetChanged();
                    
                    // Show results count
                    if (filteredServices.isEmpty()) {
                        Toast.makeText(ServicesActivity.this, "No services found matching your criteria", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ServicesActivity.this, "Found " + filteredServices.size() + " services", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ServicesActivity.this, "Error loading services for filtering", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Toast.makeText(ServicesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearFilters() {
        // Reset all filter controls
        etMinPrice.setText("");
        etMaxPrice.setText("");
        spinnerCategory.setSelection(0);
        spinnerEventType.setSelection(0);
        spinnerAvailability.setSelection(0);
        
        // Reload all services
        loadServices();
        
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    private boolean isServiceProvider() {
        String userRole = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("user_role", null);
        Log.d("ServicesActivity", "Current user role: " + userRole);
        boolean isSPP = "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole);
        Log.d("ServicesActivity", "Is service provider: " + isSPP);
        return isSPP;
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getLong("user_id", -1L);
    }

    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }

    @Override
    public void onEdit(ServiceDTO service) {
        Intent intent = new Intent(this, EditServiceActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(ServiceDTO service) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete " + service.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
                    serviceAPI.deleteService(getAuthHeader(), service.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ServicesActivity.this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                                loadServices();
                            } else {
                                Toast.makeText(ServicesActivity.this, "Error deleting service", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(ServicesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onView(ServiceDTO service) {
        // Open ServiceDetailsActivity
        Intent intent = new Intent(this, ServiceDetailsActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh services when returning from add/edit
        loadServices();
    }
}