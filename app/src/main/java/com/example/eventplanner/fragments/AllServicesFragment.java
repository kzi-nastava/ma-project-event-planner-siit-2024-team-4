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
import com.example.eventplanner.activities.ServiceDetailsActivity;
import com.example.eventplanner.activities.ServiceAdapter;
import com.example.eventplanner.activities.ProfileActivity;
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
    
    private RecyclerView recyclerServices;
    private ServiceAdapter serviceAdapter;
    private List<ServiceDTO> services;
    private List<ServiceDTO> allLoadedServices = new ArrayList<>(); // Keep original unfiltered list
    
    private EditText etSearch;
    private Button btnSearch;
    private Button btnAddService;
    private LinearLayout filterLayout;
    private Button btnToggleFilters;
    private TextView tvInfo;
    private boolean filtersVisible = false;
    
    private EditText etMinPrice, etMaxPrice;
    private Spinner spinnerCategory, spinnerEventType, spinnerAvailability;
    private Button btnApplyFilters;
    
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    
    private boolean isMyServices = false;

    public AllServicesFragment() {
    }

    public static AllServicesFragment newInstance(boolean isMyServices) {
        AllServicesFragment fragment = new AllServicesFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMyServices", isMyServices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isMyServices = getArguments().getBoolean("isMyServices", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_services, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadFilterData();
        loadServices();
        
        return view;
    }

    private void initViews(View view) {
        recyclerServices = view.findViewById(R.id.recyclerServices);
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnAddService = view.findViewById(R.id.btnAddService);
        filterLayout = view.findViewById(R.id.filterLayout);
        btnToggleFilters = view.findViewById(R.id.btnToggleFilters);
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerEventType = view.findViewById(R.id.spinnerEventType);
        spinnerAvailability = view.findViewById(R.id.spinnerAvailability);
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        Button btnClearFilters = view.findViewById(R.id.btnClearFilters);

        btnSearch.setOnClickListener(v -> searchServices());

        btnToggleFilters.setOnClickListener(v -> toggleFilters());
        
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        
        btnClearFilters.setOnClickListener(v -> clearFilters());

        if (isServiceProvider() && isMyServices) {
            btnAddService.setVisibility(View.VISIBLE);
            btnAddService.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddServiceActivity.class);
                startActivity(intent);
            });
        } else {
            btnAddService.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(services, this, isMyServices, getContext());
        recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerServices.setAdapter(serviceAdapter);
    }

    private void loadServices() {
        ServiceService service = ApiClient.getClient(getContext()).create(ServiceService.class);
        
        Call<List<ServiceDTO>> call;
        if (isMyServices) {
            Long providerId = getCurrentUserId();
            if (providerId == -1L) {
                Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            call = service.getServicesByProviderId(getAuthHeader(), providerId);
        } else {
            call = service.getAllServices(getAuthHeader());
        }

        call.enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> allServices = response.body(); 
                                       
                    List<ServiceDTO> filteredServices = filterServicesByVisibility(allServices);
                    Log.d("AllServicesFragment", "After filtering: " + filteredServices.size() + " services");
                    
                    // Store the unfiltered list for later filtering
                    allLoadedServices.clear();
                    allLoadedServices.addAll(filteredServices);
                    
                    // Display the services
                    services.clear();
                    services.addAll(filteredServices);
                    
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error loading services: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchServices() {
        // Use local filtering instead of API call
        filterAndSearch();
    }

    private void loadFilterData() {
        CategoryService categoryService = ApiClient.getClient(getContext()).create(CategoryService.class);
        // Use getAllApprovedCategories to get only approved categories
        categoryService.getAllApprovedCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    Log.d("AllServicesFragment", "Loaded " + categories.size() + " approved categories");
                    setupCategorySpinner();
                } else {
                    Log.e("AllServicesFragment", "Error loading approved categories: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Log.e("AllServicesFragment", "Failed to load approved categories", t);
            }
        });

        EventTypeService eventTypeService = ApiClient.getClient(getContext()).create(EventTypeService.class);
        eventTypeService.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    Log.d("AllServicesFragment", "Loaded " + eventTypes.size() + " event types");
                    setupEventTypeSpinner();
                } else {
                    Log.e("AllServicesFragment", "Error loading event types: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Log.e("AllServicesFragment", "Failed to load event types", t);
            }
        });
        setupAvailabilitySpinner();
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        // Since we're using getAllApprovedCategories, all categories are already approved
        for (CategoryDTO category : categories) {
            categoryNames.add(category.name);
        }
        if (getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        Log.d("AllServicesFragment", "Category spinner setup with " + categoryNames.size() + " items");
    }

    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("All Event Types");
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        if (getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
        Log.d("AllServicesFragment", "Event type spinner setup with " + eventTypeNames.size() + " items");
    }

    private void setupAvailabilitySpinner() {
        String[] availabilityOptions = {"All", "Available Only", "Not Available Only"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, availabilityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(adapter);
    }

    private void toggleFilters() {
        filtersVisible = !filtersVisible;
        filterLayout.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
    }

    private void applyFilters() {
        // Apply filters locally like in Angular version
        filterAndSearch();
        Toast.makeText(getContext(), "Filters applied", Toast.LENGTH_SHORT).show();
    }
    
    private void filterAndSearch() {
        // Get filter values
        String searchTerm = etSearch.getText().toString().trim();
        
        Long categoryId = null;
        if (spinnerCategory.getSelectedItemPosition() > 0) {
            String selectedCategory = (String) spinnerCategory.getSelectedItem();
            for (CategoryDTO category : categories) {
                if (category.name.equals(selectedCategory)) {
                    categoryId = category.id;
                    break;
                }
            }
        }
        
        Long eventTypeId = null;
        if (spinnerEventType.getSelectedItemPosition() > 0) {
            String selectedEventType = (String) spinnerEventType.getSelectedItem();
            for (EventTypeDTO eventType : eventTypes) {
                if (eventType.getName().equals(selectedEventType)) {
                    eventTypeId = eventType.getId();
                    break;
                }
            }
        }
        
        Double minPrice = null;
        if (!TextUtils.isEmpty(etMinPrice.getText().toString())) {
            try {
                minPrice = Double.parseDouble(etMinPrice.getText().toString());
            } catch (NumberFormatException e) {
                // Keep null
            }
        }
        
        Double maxPrice = null;
        if (!TextUtils.isEmpty(etMaxPrice.getText().toString())) {
            try {
                maxPrice = Double.parseDouble(etMaxPrice.getText().toString());
            } catch (NumberFormatException e) {
                // Keep null
            }
        }
        
        Boolean isAvailable = null;
        int availabilitySelection = spinnerAvailability.getSelectedItemPosition();
        if (availabilitySelection == 1) {
            isAvailable = true;
        } else if (availabilitySelection == 2) {
            isAvailable = false;
        }
        
        // Check if any filters are active
        boolean hasActiveFilters = !TextUtils.isEmpty(searchTerm) || 
                                    categoryId != null || 
                                    eventTypeId != null || 
                                    minPrice != null || 
                                    maxPrice != null || 
                                    isAvailable != null;
        
        // If no filters are active, show all loaded services
        if (!hasActiveFilters) {
            services.clear();
            services.addAll(allLoadedServices);
            serviceAdapter.notifyDataSetChanged();
            return;
        }
        
        // Filter from the original unfiltered list
        services.clear();
        
        // Apply filters like in Angular
        for (ServiceDTO service : allLoadedServices) {
            boolean shouldInclude = true;
            
            // Search filter
            if (!TextUtils.isEmpty(searchTerm) && 
                !service.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                shouldInclude = false;
            }
            
            // Category filter
            if (categoryId != null && (service.getCategory() == null || !service.getCategory().id.equals(categoryId))) {
                shouldInclude = false;
            }
            
            // Event type filter
            if (eventTypeId != null) {
                boolean hasEventType = false;
                if (service.getEventTypes() != null) {
                    for (EventTypeDTO eventType : service.getEventTypes()) {
                        if (eventType.getId().equals(eventTypeId)) {
                            hasEventType = true;
                            break;
                        }
                    }
                }
                if (!hasEventType) {
                    shouldInclude = false;
                }
            }
            
            // Price filter
            if (minPrice != null && service.getPrice() < minPrice) {
                shouldInclude = false;
            }
            if (maxPrice != null && service.getPrice() > maxPrice) {
                shouldInclude = false;
            }
            
            // Availability filter
            if (isAvailable != null && service.isAvailable() != isAvailable) {
                shouldInclude = false;
            }
            
            if (shouldInclude) {
                services.add(service);
            }
        }
        
        serviceAdapter.notifyDataSetChanged();
    }
    
    private void clearFilters() {
        etSearch.setText("");
        etMinPrice.setText("");
        etMaxPrice.setText("");
        spinnerCategory.setSelection(0);
        spinnerEventType.setSelection(0);
        spinnerAvailability.setSelection(0);
        
        // Reload all services and apply filters
        loadServices();
        
        Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    private List<ServiceDTO> filterServicesByVisibility(List<ServiceDTO> allServices) {
        List<ServiceDTO> filteredServices = new ArrayList<>();
        boolean isSPP = isServiceProvider();
        boolean isAdmin = isAdmin();
        
        for (ServiceDTO service : allServices) {
            boolean shouldShow = false;
            
            if (isAdmin) {
                // Admin vidi sve servise koje backend vraća - ne filtrira se ništa
                shouldShow = true;
                Log.d("AllServicesFragment", "Admin - Service: " + service.getName() + 
                      ", Available: " + service.isAvailable() + ", Visible: " + service.isVisible() + 
                      ", Should show: " + shouldShow);
            } else if (isMyServices && isSPP) {
                // SPP user viewing their own services - show all their services
                Long currentUserId = getCurrentUserId();
                Long serviceProviderId = service.getProvider() != null ? service.getProvider().getId() : service.getProviderId();
                shouldShow = currentUserId.equals(serviceProviderId);
                Log.d("AllServicesFragment", "SPP My Services - Service: " + service.getName() + 
                      ", Should show: " + shouldShow + ", Current user: " + currentUserId + 
                      ", Service provider: " + serviceProviderId);
            } else {
                // Svi ostali korisnici vide sve servise (kao u Angular verziji)
                // Filtriranje se radi kroz UI filtere, ne ovde
                shouldShow = true;
            }
            
            if (shouldShow) {
                filteredServices.add(service);
            }
        }
        
        return filteredServices;
    }

    private boolean isServiceProvider() {
        String userRole = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("user_role", null);
        Log.d("AllServicesFragment", "Current user role: " + userRole);
        boolean isSPP = "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole) || "SPProvider".equals(userRole);
        Log.d("AllServicesFragment", "Is service provider: " + isSPP);
        return isSPP;
    }

    private boolean isAdmin() {
        String userRole = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("user_role", null);
        Log.d("AllServicesFragment", "Current user role for admin check: " + userRole);
        boolean isAdmin = "ADMIN".equals(userRole) || "admin".equals(userRole) || "Admin".equals(userRole);
        Log.d("AllServicesFragment", "Is admin: " + isAdmin);
        return isAdmin;
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        return prefs.getLong("user_id", -1L);
    }

    private String getAuthHeader() {
        String token = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }

    private String getCurrentUserRole() {
        return requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("user_role", null);
    }
    @Override
    public void onEdit(ServiceDTO service) {
        Intent intent = new Intent(getActivity(), EditServiceActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(ServiceDTO service) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete " + service.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ServiceService serviceAPI = ApiClient.getClient(getContext()).create(ServiceService.class);
                    serviceAPI.deleteService(getAuthHeader(), service.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Service deleted successfully", Toast.LENGTH_SHORT).show();
                                loadServices();
                            } else {
                                Toast.makeText(getContext(), "Error deleting service", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onView(ServiceDTO service) {
        Intent intent = new Intent(getActivity(), ServiceDetailsActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    public void onProviderProfile(ServiceDTO service) {
        // Navigate to provider profile
        Log.d("AllServicesFragment", "onProviderProfile called for service: " + service.getName());
        Log.d("AllServicesFragment", "Service provider: " + service.getProvider());
        Log.d("AllServicesFragment", "Service providerId: " + service.getProviderId());
        
        if (service.getProvider() != null) {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("userId", service.getProvider().getId());
            Log.d("AllServicesFragment", "Navigating to profile with userId: " + service.getProvider().getId());
            startActivity(intent);
        } else if (service.getProviderId() != null) {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("userId", service.getProviderId());
            Log.d("AllServicesFragment", "Navigating to profile with providerId: " + service.getProviderId());
            startActivity(intent);
        } else {
            Log.e("AllServicesFragment", "No provider information available for service: " + service.getName());
            Toast.makeText(getContext(), "Provider information not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChatWithProvider(ServiceDTO service) {
        // Navigate to chat with provider
        if (service.getProvider() != null) {
            // TODO: Implement ChatActivity
            Toast.makeText(getContext(), "Chat functionality not implemented yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh services when returning from add/edit
        loadServices();
    }
}
