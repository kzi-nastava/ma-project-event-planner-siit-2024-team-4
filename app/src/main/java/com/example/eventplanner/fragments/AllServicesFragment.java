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
<<<<<<< HEAD
        tvInfo = view.findViewById(R.id.tvInfo);
        
=======

>>>>>>> e554bf1 ([update] services)
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
<<<<<<< HEAD
        if (isMyServices) {
            tvInfo.setText("Showing all your visible services (available and unavailable)");
        } else {
            tvInfo.setText("Showing available and visible services");
        }
=======
>>>>>>> e554bf1 ([update] services)
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(services, this, isMyServices);
        recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerServices.setAdapter(serviceAdapter);
    }

    private void loadServices() {
        Log.d("AllServicesFragment", "loadServices called");
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
                Log.d("AllServicesFragment", "Response received: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    List<ServiceDTO> allServices = response.body();
<<<<<<< HEAD
                                        List<ServiceDTO> filteredServices = filterServicesByVisibility(allServices);
=======
                    Log.d("AllServicesFragment", "Received " + allServices.size() + " services from backend");
                    Log.d("AllServicesFragment", "Current user role: " + getCurrentUserRole());
                    Log.d("AllServicesFragment", "Is admin: " + isAdmin());
                    Log.d("AllServicesFragment", "Is SPP: " + isServiceProvider());
                    
                    List<ServiceDTO> filteredServices = filterServicesByVisibility(allServices);
                    Log.d("AllServicesFragment", "After filtering: " + filteredServices.size() + " services");
                    
>>>>>>> e554bf1 ([update] services)
                    services.addAll(filteredServices);
                    
                    serviceAdapter.notifyDataSetChanged();
                } else {
<<<<<<< HEAD
=======
                    Log.d("AllServicesFragment", "Error response: " + response.code() + ", body: " + response.body());
>>>>>>> e554bf1 ([update] services)
                    Toast.makeText(getContext(), "Error loading services: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
<<<<<<< HEAD
=======
                Log.e("AllServicesFragment", "Failed to load services", t);
>>>>>>> e554bf1 ([update] services)
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchServices() {
        String searchTerm = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(searchTerm)) {
            loadServices();
            return;
        }

        ServiceService service = ApiClient.getClient(getContext()).create(ServiceService.class);
        service.searchServices(getAuthHeader(), searchTerm).enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> filteredServices = filterServicesByVisibility(response.body());
                    services.clear();
                    services.addAll(filteredServices);
                    serviceAdapter.notifyDataSetChanged();
                    if (filteredServices.isEmpty()) {
                        Toast.makeText(getContext(), "No services found matching '" + searchTerm + "'", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Found " + filteredServices.size() + " services matching '" + searchTerm + "'", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error searching services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilterData() {
        CategoryService categoryService = ApiClient.getClient(getContext()).create(CategoryService.class);
        categoryService.getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
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

        EventTypeService eventTypeService = ApiClient.getClient(getContext()).create(EventTypeService.class);
        eventTypeService.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
            }
        });
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("All Event Types");
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
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
        ServiceService service = ApiClient.getClient(getContext()).create(ServiceService.class);
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
        ServiceService serviceAPI = ApiClient.getClient(getContext()).create(ServiceService.class);
        Call<List<ServiceDTO>> call;
        if (isMyServices) {
            Long providerId = getCurrentUserId();
            if (providerId == -1L) {
                Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
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
                    List<ServiceDTO> visibilityFilteredServices = filterServicesByVisibility(allServices);
                    List<ServiceDTO> filteredServices = new ArrayList<>();
                    
                    for (ServiceDTO serviceItem : visibilityFilteredServices) {
                        boolean matches = true;
                        if (categoryId != null && (serviceItem.getCategory() == null || !serviceItem.getCategory().id.equals(categoryId))) {
                            matches = false;
                        }
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
                        if (minPrice != null && serviceItem.getPrice() < minPrice) {
                            matches = false;
                        }
                        if (maxPrice != null && serviceItem.getPrice() > maxPrice) {
                            matches = false;
                        }
                        if (isAvailable != null && serviceItem.isAvailable() != isAvailable) {
                            matches = false;
                        }
                        if (matches) {
                            filteredServices.add(serviceItem);
                        }
                    }
                    services.clear();
                    services.addAll(filteredServices);
                    serviceAdapter.notifyDataSetChanged();
                    
                    // Show results count
                    if (filteredServices.isEmpty()) {
                        Toast.makeText(getContext(), "No services found matching your criteria", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Found " + filteredServices.size() + " services", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error loading services for filtering", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearFilters() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
        spinnerCategory.setSelection(0);
        spinnerEventType.setSelection(0);
        spinnerAvailability.setSelection(0);
        
        loadServices();
        
        Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Filters services based on visibility and availability rules:
<<<<<<< HEAD
     * - Niko ne vidi nevidljive usluge (visible = false)
=======
     * - Admin vidi sve servise koje backend vraća (backend već filtrira vidljive)
     * - Niko ne vidi nevidljive usluge (visible = false) - backend ih ne vraća
>>>>>>> e554bf1 ([update] services)
     * - Svi mogu videti dostupne usluge (available = true)
     * - SPP može videti i svoje nedostupne usluge (available = false, ali samo svoje)
     */
    private List<ServiceDTO> filterServicesByVisibility(List<ServiceDTO> allServices) {
        List<ServiceDTO> filteredServices = new ArrayList<>();
        boolean isSPP = isServiceProvider();
<<<<<<< HEAD
=======
        boolean isAdmin = isAdmin();
>>>>>>> e554bf1 ([update] services)
        
        for (ServiceDTO service : allServices) {
            boolean shouldShow = false;
            
<<<<<<< HEAD
            // Prvo proveri da li je usluga vidljiva - niko ne vidi nevidljive
            if (!service.isVisible()) {
                shouldShow = false;
                Log.d("AllServicesFragment", "Service not visible: " + service.getName());
=======
            if (isAdmin) {
                // Admin vidi sve servise koje backend vraća - ne filtrira se ništa
                shouldShow = true;
                Log.d("AllServicesFragment", "Admin - Service: " + service.getName() + 
                      ", Available: " + service.isAvailable() + ", Visible: " + service.isVisible() + 
                      ", Should show: " + shouldShow);
>>>>>>> e554bf1 ([update] services)
            } else if (isMyServices && isSPP) {
                // SPP user viewing their own services - show all visible services (available and unavailable)
                Long currentUserId = getCurrentUserId();
                Long serviceProviderId = service.getProvider() != null ? service.getProvider().getId() : service.getProviderId();
                shouldShow = currentUserId.equals(serviceProviderId);
                Log.d("AllServicesFragment", "SPP My Services - Service: " + service.getName() + 
                      ", Should show: " + shouldShow + ", Current user: " + currentUserId + 
                      ", Service provider: " + serviceProviderId);
<<<<<<< HEAD
            } else {
                // Svi ostali - samo dostupne usluge
                shouldShow = service.isAvailable();
                Log.d("AllServicesFragment", "All Services - Service: " + service.getName() + 
=======
            } else if (isSPP) {
                // SPP korisnici vide sve servise (i dostupne i nedostupne)
                shouldShow = true;
                Log.d("AllServicesFragment", "SPP All Services - Service: " + service.getName() + 
                      ", Available: " + service.isAvailable() + ", Visible: " + service.isVisible() + 
                      ", Should show: " + shouldShow);
            } else {
                // Ostali korisnici - samo dostupne usluge
                shouldShow = service.isAvailable();
                Log.d("AllServicesFragment", "Regular User - Service: " + service.getName() + 
>>>>>>> e554bf1 ([update] services)
                      ", Available: " + service.isAvailable() + ", Visible: " + service.isVisible() + 
                      ", Should show: " + shouldShow);
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
<<<<<<< HEAD
        boolean isSPP = "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole);
=======
        boolean isSPP = "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole) || "SPProvider".equals(userRole);
>>>>>>> e554bf1 ([update] services)
        Log.d("AllServicesFragment", "Is service provider: " + isSPP);
        return isSPP;
    }

    private boolean isAdmin() {
        String userRole = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("user_role", null);
        Log.d("AllServicesFragment", "Current user role for admin check: " + userRole);
<<<<<<< HEAD
        boolean isAdmin = "ADMIN".equals(userRole) || "admin".equals(userRole);
=======
        boolean isAdmin = "ADMIN".equals(userRole) || "admin".equals(userRole) || "Admin".equals(userRole);
>>>>>>> e554bf1 ([update] services)
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

<<<<<<< HEAD
=======
    private String getCurrentUserRole() {
        return requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE).getString("user_role", null);
    }

>>>>>>> e554bf1 ([update] services)
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
        // Open ServiceDetailsActivity
        Intent intent = new Intent(getActivity(), ServiceDetailsActivity.class);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh services when returning from add/edit
        loadServices();
    }
}
