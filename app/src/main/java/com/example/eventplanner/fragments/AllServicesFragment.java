package com.example.eventplanner.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.AddServiceActivity;
import com.example.eventplanner.activities.EditServiceActivity;
import com.example.eventplanner.activities.ServiceAdapter;
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

public class AllServicesFragment extends Fragment implements ServiceAdapter.ServiceActionListener {
    
    private boolean needsRefresh = false;
    
    private RecyclerView recyclerServices;
    private EditText etSearch;
    private Button btnSearch;
    private Button btnAddService;
    private LinearLayout filterLayout;
    private Button btnToggleFilters;
    private TextView tvInfo;
    
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Spinner spinnerCategory;
    private Spinner spinnerEventType;
    private Spinner spinnerAvailability;
    private Button btnApplyFilters;
    
    private List<ServiceDTO> services;
    private ServiceAdapter serviceAdapter;
    private boolean isMyServices;
    
    private List<CategoryDTO> categories;
    private List<EventTypeDTO> eventTypes;

    public static AllServicesFragment newInstance(boolean isMyServices) {
        AllServicesFragment fragment = new AllServicesFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMyServices", isMyServices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isMyServices = getArguments().getBoolean("isMyServices", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadCategories();
        loadEventTypes();
        loadServices();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when returning from edit or add activity
        if (needsRefresh) {
            loadServices();
            needsRefresh = false;
        }
    }

    private void initViews(View view) {
        recyclerServices = view.findViewById(R.id.recyclerServices);
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnAddService = view.findViewById(R.id.btnAddService);
        filterLayout = view.findViewById(R.id.filterLayout);
        btnToggleFilters = view.findViewById(R.id.btnToggleFilters);
        tvInfo = view.findViewById(R.id.tvInfo);
        
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerEventType = view.findViewById(R.id.spinnerEventType);
        spinnerAvailability = view.findViewById(R.id.spinnerAvailability);
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        Button btnClearFilters = view.findViewById(R.id.btnClearFilters);
        
        // Ensure filter button is visible for both All Services and My Services
        btnToggleFilters.setVisibility(View.VISIBLE);
        
        // Setup availability spinner
        setupAvailabilitySpinner();

        btnSearch.setOnClickListener(v -> searchServices());
        btnToggleFilters.setOnClickListener(v -> toggleFilters());
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Auto-apply filters when spinners change
        spinnerAvailability.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        spinnerEventType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        if (isMyServices) {
            btnAddService.setVisibility(View.VISIBLE);
            btnAddService.setOnClickListener(v -> {
                needsRefresh = true; // Set flag to refresh when returning
                Intent intent = new Intent(getActivity(), AddServiceActivity.class);
                startActivity(intent);
            });
        } else {
            btnAddService.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(services, this, isMyServices);
        recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerServices.setAdapter(serviceAdapter);
    }

    private void loadServices() {
        Log.d("AllServicesFragment", "=== loadServices START ===");
        Log.d("AllServicesFragment", "isMyServices: " + isMyServices);
        
        ServiceService service = ApiClient.getClient(getContext()).create(ServiceService.class);
        
        Call<List<ServiceDTO>> call;
        if (isMyServices) {
            Long providerId = getCurrentUserId();
            Log.d("AllServicesFragment", "Loading MY services for providerId: " + providerId);
            call = service.getMyServices(getAuthHeader(), providerId);
        } else {
            Log.d("AllServicesFragment", "Loading ALL services");
            call = service.getAllServices(getAuthHeader());
        }
        
        Log.d("AllServicesFragment", "Auth header: " + getAuthHeader());

        call.enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                Log.d("AllServicesFragment", "=== API RESPONSE ===");
                Log.d("AllServicesFragment", "Response code: " + response.code());
                Log.d("AllServicesFragment", "Response message: " + response.message());
                
                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("AllServicesFragment", "Error response body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("AllServicesFragment", "Error reading error body", e);
                    }
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    services.addAll(response.body());
                    serviceAdapter.notifyDataSetChanged();
                    
                    Log.d("AllServicesFragment", "Loaded " + services.size() + " services");
                    
                    if (services.isEmpty()) {
                        tvInfo.setText("No services found");
                    } else {
                        tvInfo.setText("Showing " + services.size() + " services");
                    }
                } else {
                    Log.e("AllServicesFragment", "Response not successful - Code: " + response.code());
                    Toast.makeText(getContext(), "Error loading services: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Log.e("AllServicesFragment", "Network error loading services", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        CategoryService categoryService = ApiClient.getClient(getContext()).create(CategoryService.class);
        categoryService.getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    setupCategorySpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                // Handle error silently
            }
        });
    }

    private void loadEventTypes() {
        EventTypeService eventTypeService = ApiClient.getClient(getContext()).create(EventTypeService.class);
        eventTypeService.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                // Handle error silently
            }
        });
    }

    private void setupCategorySpinner() {
        if (categories == null) return;
        
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        for (CategoryDTO category : categories) {
                categoryNames.add(category.name);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupEventTypeSpinner() {
        if (eventTypes == null) return;
        
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("All Event Types");
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
    }

    private void searchServices() {
        String searchTerm = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(searchTerm)) {
            loadServices();
            return;
        }
        
        // Filter services by search term
        List<ServiceDTO> filteredServices = new ArrayList<>();
        for (ServiceDTO service : services) {
            if (service.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                service.getDescription().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredServices.add(service);
            }
        }
        
        services.clear();
        services.addAll(filteredServices);
        serviceAdapter.notifyDataSetChanged();
        
        tvInfo.setText("Found " + filteredServices.size() + " services matching '" + searchTerm + "'");
    }

    private void toggleFilters() {
        if (filterLayout.getVisibility() == View.GONE) {
            filterLayout.setVisibility(View.VISIBLE);
            btnToggleFilters.setText("Hide Filters");
        } else {
            filterLayout.setVisibility(View.GONE);
            btnToggleFilters.setText("Show Filters");
        }
    }

    private void applyFilters() {
        // Apply price filter
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();
        
        double minPrice = TextUtils.isEmpty(minPriceStr) ? 0 : Double.parseDouble(minPriceStr);
        double maxPrice = TextUtils.isEmpty(maxPriceStr) ? Double.MAX_VALUE : Double.parseDouble(maxPriceStr);
        
        // Apply category filter
        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        Long categoryId = (categoryPosition > 0 && categories != null) ? 
                categories.get(categoryPosition - 1).id : null;
        
        // Apply event type filter
        int eventTypePosition = spinnerEventType.getSelectedItemPosition();
        Long eventTypeId = (eventTypePosition > 0 && eventTypes != null) ? 
                eventTypes.get(eventTypePosition - 1).getId() : null;
        
        // Apply availability filter
        int availabilityPosition = spinnerAvailability.getSelectedItemPosition();
        Boolean available = null;
        if (availabilityPosition == 1) available = true;
        else if (availabilityPosition == 2) available = false;
        
        // Filter services
        List<ServiceDTO> filteredServices = new ArrayList<>();
        for (ServiceDTO service : services) {
            boolean matchesPrice = service.getPrice() >= minPrice && service.getPrice() <= maxPrice;
            boolean matchesCategory = categoryId == null || service.getCategory().getId().equals(categoryId);
            boolean matchesEventType = eventTypeId == null || 
                    service.getEventTypes().stream().anyMatch(et -> et.getId().equals(eventTypeId));
            boolean matchesAvailability = available == null || service.isAvailable() == available;
            
            if (matchesPrice && matchesCategory && matchesEventType && matchesAvailability) {
                filteredServices.add(service);
            }
        }
        
                    services.clear();
                    services.addAll(filteredServices);
                    serviceAdapter.notifyDataSetChanged();
                    
        tvInfo.setText("Showing " + filteredServices.size() + " filtered services");
    }
    
    private void clearFilters() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
        spinnerCategory.setSelection(0);
        spinnerEventType.setSelection(0);
        spinnerAvailability.setSelection(0);
        loadServices();
    }

    @Override
    public void onEdit(ServiceDTO service) {
        needsRefresh = true; // Set flag to refresh when returning
        Intent intent = new Intent(getActivity(), EditServiceActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(ServiceDTO service) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete '" + service.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteService(service);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onView(ServiceDTO service) {
        // For now, just show a simple message - could be expanded to show service details
        Toast.makeText(getContext(), "Viewing: " + service.getName(), Toast.LENGTH_SHORT).show();
    }

    private void deleteService(ServiceDTO service) {
                    ServiceService serviceAPI = ApiClient.getClient(getContext()).create(ServiceService.class);
                    serviceAPI.deleteService(getAuthHeader(), service.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                    // Remove from local list and refresh
                    services.remove(service);
                    serviceAdapter.notifyDataSetChanged();
                    updateInfoText();
                            } else {
                    Toast.makeText(getContext(), "Error deleting service: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void updateInfoText() {
        if (services.isEmpty()) {
            tvInfo.setText("No services found");
        } else {
            tvInfo.setText("Showing " + services.size() + " services");
        }
    }

    private void setupAvailabilitySpinner() {
        String[] availabilityOptions = {"All", "Available", "Not Available"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, availabilityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(adapter);
    }

    private String getAuthHeader() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", 0);
        String token = prefs.getString("jwt_token", "");
        return "Bearer " + token;
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", 0);
        return prefs.getLong("user_id", 0);
    }

    private boolean isServiceProvider() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", 0);
        String role = prefs.getString("user_role", "");
        return "SPProvider".equals(role) || "SERVICE_PROVIDER".equals(role);
    }
}
