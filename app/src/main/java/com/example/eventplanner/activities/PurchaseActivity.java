package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventAdapterNoImage;
import com.example.eventplanner.dto.CreatePurchaseDTO;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.PurchaseDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.EventService;
import com.example.eventplanner.network.service.PurchaseService;
import com.example.eventplanner.network.service.BudgetService;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.dto.BudgetPlanDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.CategoryDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseActivity extends BaseActivity implements EventAdapterNoImage.OnEventClickListener {
    
    private ProductDTO product;
    private Long eventOrganizerId;
    private List<EventDTO> eventsWithBudget;
    private Long selectedEventId;
    private Long productCategoryId;
    private List<EventTypeDTO> eventTypes;
    private List<CategoryDTO> allCategories;
    
    private TextView tvProductName;
    private TextView tvProductPrice;
    private Spinner spinnerEvents;
    private Button btnPurchase;
    private ProgressBar progressBar;
    
    private EventService eventService;
    private PurchaseService purchaseService;
    private BudgetService budgetService;
    private EventTypeService eventTypeService;
    private CategoryService categoryService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PurchaseActivity", "onCreate started");
        
        getLayoutInflater().inflate(R.layout.activity_purchase, findViewById(R.id.content_frame), true);
        Log.d("PurchaseActivity", "Layout inflated successfully");
        
        // Check if user is Event Organizer
        if (!isEventOrganizer()) {
            Log.d("PurchaseActivity", "User is not Event Organizer - finishing activity");
            Toast.makeText(this, "DEBUG: User is not Event Organizer", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("PurchaseActivity", "User is Event Organizer - continuing");
        
        product = (ProductDTO) getIntent().getSerializableExtra("product");
        if (product == null) {
            Log.d("PurchaseActivity", "Product not found in intent - finishing activity");
            Toast.makeText(this, "DEBUG: Product not found in intent", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("PurchaseActivity", "Product found: " + product.getName());
        
        // Check if product is available
        if (product.getAvailable() == null || !product.getAvailable()) {
            Log.d("PurchaseActivity", "Product not available - finishing activity. Available: " + product.getAvailable());
            Toast.makeText(this, "DEBUG: Product not available - available=" + product.getAvailable(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("PurchaseActivity", "Product is available - continuing");
        
        initializeViews();
        Log.d("PurchaseActivity", "Views initialized");
        initializeServices();
        Log.d("PurchaseActivity", "Services initialized");
        loadUserInfo();
        Log.d("PurchaseActivity", "User info loaded");
        
        // Get product category ID
        if (product.getCategory() != null) {
            productCategoryId = product.getCategory().getId();
            Log.d("PurchaseActivity", "Product category from category object: " + productCategoryId);
        } else if (product.getCategoryId() != null) {
            productCategoryId = product.getCategoryId();
            Log.d("PurchaseActivity", "Product category from categoryId: " + productCategoryId);
        } else {
            Log.d("PurchaseActivity", "Product category not found - finishing activity");
            Toast.makeText(this, "DEBUG: Product category not found - category=" + product.getCategory() + ", categoryId=" + product.getCategoryId(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        Log.d("PurchaseActivity", "PurchaseActivity started successfully - loading event types and categories");
        Toast.makeText(this, "DEBUG: PurchaseActivity started successfully", Toast.LENGTH_SHORT).show();
        loadEventTypesAndCategories();
    }
    
    private void initializeViews() {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        spinnerEvents = findViewById(R.id.spinnerEvents);
        btnPurchase = findViewById(R.id.btnPurchase);
        progressBar = findViewById(R.id.progressBar);
        
        tvProductName.setText(product.getName());
        if (product.getPrice() != null) {
            tvProductPrice.setText("Price: " + product.getPrice().intValue() + " RSD");
        } else {
            tvProductPrice.setText("Price: N/A");
        }
        
        btnPurchase.setOnClickListener(v -> createPurchase());
    }
    
    private void initializeServices() {
        eventService = ApiClient.getClient(this).create(EventService.class);
        purchaseService = ApiClient.getClient(this).create(PurchaseService.class);
        budgetService = ApiClient.getClient(this).create(BudgetService.class);
        eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        categoryService = ApiClient.getClient(this).create(CategoryService.class);
    }
    
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        Long organizerId = prefs.getLong("organizer_id", -1L);
        // Use organizer_id if available, otherwise use user_id
        eventOrganizerId = organizerId != -1L ? organizerId : (userId != -1L ? userId : null);
    }
    
    private void loadEventTypesAndCategories() {
        Log.d("PurchaseActivity", "loadEventTypesAndCategories started");
        progressBar.setVisibility(View.VISIBLE);
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null || eventOrganizerId == null) {
            Log.d("PurchaseActivity", "Token or eventOrganizerId is null - finishing activity");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d("PurchaseActivity", "Loading event types with token: " + (token != null ? "present" : "null"));
        // Load event types and categories first
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                Log.d("PurchaseActivity", "Event types API response - Success: " + response.isSuccessful() + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    Log.d("PurchaseActivity", "Loaded " + eventTypes.size() + " event types");
                    for (EventTypeDTO eventType : eventTypes) {
                        Log.d("PurchaseActivity", "Event type: " + eventType.getName() + " - Suggested categories: " + (eventType.getSuggestedCategories() != null ? eventType.getSuggestedCategories().size() : 0));
                    }
                    loadAllCategories();
                } else {
                    Log.d("PurchaseActivity", "Failed to load event types");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PurchaseActivity.this, "Failed to load event types", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Log.d("PurchaseActivity", "Event types API failed: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PurchaseActivity.this, "Error loading event types: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadAllCategories() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        categoryService.getAllCategories("Bearer " + token).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories = response.body();
                    loadEventsWithBudget();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PurchaseActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PurchaseActivity.this, "Error loading categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadEventsWithBudget() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        eventService.getMyEvents("Bearer " + token, eventOrganizerId.toString()).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventDTO> allEvents = response.body();
                    filterEventsWithBudget(allEvents);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PurchaseActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PurchaseActivity.this, "Error loading events: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void filterEventsWithBudget(List<EventDTO> allEvents) {
        Log.d("PurchaseActivity", "filterEventsWithBudget started with " + allEvents.size() + " events");
        eventsWithBudget = new ArrayList<>();
        
        // Debug logging
        Toast.makeText(this, "Filtering " + allEvents.size() + " events for category " + productCategoryId, 
                Toast.LENGTH_LONG).show();
        
        // Filter events that have budget for this product category
        for (EventDTO event : allEvents) {
            if (isEventEligibleForPurchase(event)) {
                eventsWithBudget.add(event);
                Log.d("PurchaseActivity", "Event eligible: " + event.getName());
            } else {
                Log.d("PurchaseActivity", "Event not eligible: " + event.getName());
            }
        }
        
        progressBar.setVisibility(View.GONE);
        
        if (eventsWithBudget.isEmpty()) {
            Log.d("PurchaseActivity", "No events available with budget - finishing activity");
            Toast.makeText(this, "No events available with budget for this product category. " +
                    "Total events: " + allEvents.size() + ", Product category: " + productCategoryId, 
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        Log.d("PurchaseActivity", "Found " + eventsWithBudget.size() + " eligible events");
        Toast.makeText(this, "Found " + eventsWithBudget.size() + " eligible events", Toast.LENGTH_SHORT).show();
        setupEventsSpinner();
    }
    
    private boolean isEventEligibleForPurchase(EventDTO event) {
        Log.d("PurchaseActivity", "Checking eligibility for event: " + event.getName() + " (Type: " + event.getEventTypeName() + ")");
        
        // Check if event type has suggested categories that include product category (like IKS)
        boolean hasMatchingCategory = false;
        
        if (eventTypes != null) {
            Log.d("PurchaseActivity", "Event types available: " + eventTypes.size());
            for (EventTypeDTO eventType : eventTypes) {
                Log.d("PurchaseActivity", "Checking event type: " + eventType.getName() + " vs " + event.getEventTypeName());
                if (eventType.getName().equals(event.getEventTypeName())) {
                    Log.d("PurchaseActivity", "Found matching event type: " + eventType.getName());
                    if (eventType.getSuggestedCategories() != null) {
                        Log.d("PurchaseActivity", "Suggested categories: " + eventType.getSuggestedCategories().size());
                        for (CategoryDTO category : eventType.getSuggestedCategories()) {
                            Log.d("PurchaseActivity", "Category ID: " + category.getId() + " vs Product Category ID: " + productCategoryId);
                            if (category.getId().equals(productCategoryId)) {
                                hasMatchingCategory = true;
                                Log.d("PurchaseActivity", "Found matching category!");
                                break;
                            }
                        }
                    } else {
                        Log.d("PurchaseActivity", "No suggested categories for event type");
                    }
                    break;
                }
            }
        } else {
            Log.d("PurchaseActivity", "No event types available");
        }
        
        if (!hasMatchingCategory) {
            Log.d("PurchaseActivity", "Event " + event.getName() + " does not have matching category");
            return false;
        }
        
        Log.d("PurchaseActivity", "Event " + event.getName() + " has matching category, checking budget...");
        // Check if event has budget with items for this category (like IKS)
        boolean hasBudget = checkEventBudget((long)event.getId());
        Log.d("PurchaseActivity", "Event " + event.getName() + " budget check result: " + hasBudget);
        return hasBudget;
    }
    
    private boolean checkEventBudget(Long eventId) {
        Log.d("PurchaseActivity", "Checking budget for event ID: " + eventId + " for category: " + productCategoryId);
        
        // For now, let's assume all events have budget for testing purposes
        // This will help us see if the issue is with budget checking or something else
        Log.d("PurchaseActivity", "TEMPORARY: Assuming event " + eventId + " has budget for category " + productCategoryId);
        return true;
    }
    
    private void setupEventsSpinner() {
        if (eventsWithBudget == null || eventsWithBudget.isEmpty()) {
            Toast.makeText(this, "No events available for purchase", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        List<String> eventNames = new ArrayList<>();
        for (EventDTO event : eventsWithBudget) {
            eventNames.add(event.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvents.setAdapter(adapter);
        
        // Set first event as selected by default
        if (!eventsWithBudget.isEmpty()) {
            selectedEventId = (long) eventsWithBudget.get(0).getId();
        }
        
        spinnerEvents.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedEventId = (long) eventsWithBudget.get(position).getId();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    
    private void createPurchase() {
        Log.d("PurchaseActivity", "createPurchase started");
        if (selectedEventId == null || eventOrganizerId == null) {
            Log.d("PurchaseActivity", "selectedEventId or eventOrganizerId is null - selectedEventId: " + selectedEventId + ", eventOrganizerId: " + eventOrganizerId);
            Toast.makeText(this, "Please select an event", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("PurchaseActivity", "Creating purchase - selectedEventId: " + selectedEventId + ", eventOrganizerId: " + eventOrganizerId);
        progressBar.setVisibility(View.VISIBLE);
        
        CreatePurchaseDTO dto = new CreatePurchaseDTO();
        dto.setProductId(product.getId());
        dto.setEventOrganizerId(eventOrganizerId);
        dto.setEventId(selectedEventId);
        
        Log.d("PurchaseActivity", "Purchase DTO created - ProductID: " + product.getId() + ", EventID: " + selectedEventId + ", EventOrganizerID: " + eventOrganizerId);
        
        // Debug logging
        Toast.makeText(this, "Creating purchase: ProductId=" + product.getId() + 
                ", OrganizerId=" + eventOrganizerId + ", EventId=" + selectedEventId, 
                Toast.LENGTH_LONG).show();
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        Log.d("PurchaseActivity", "Making purchase request with token: " + (token != null ? "present" : "null"));
        purchaseService.createPurchase("Bearer " + token, dto).enqueue(new Callback<PurchaseDTO>() {
            @Override
            public void onResponse(Call<PurchaseDTO> call, Response<PurchaseDTO> response) {
                Log.d("PurchaseActivity", "Purchase response received - Success: " + response.isSuccessful() + ", Code: " + response.code());
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Log.d("PurchaseActivity", "Purchase successful - navigating to review activity");
                    Toast.makeText(PurchaseActivity.this, "Purchase successful!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to review activity
                    Intent intent = new Intent(PurchaseActivity.this, ReviewActivity.class);
                    intent.putExtra("product", product);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "Purchase failed";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                            Log.d("PurchaseActivity", "Purchase error response: " + errorMessage);
                        } catch (Exception e) {
                            errorMessage += " (Error code: " + response.code() + ")";
                            Log.d("PurchaseActivity", "Error reading error response: " + e.getMessage());
                        }
                    } else {
                        errorMessage += " (Error code: " + response.code() + ")";
                    }
                    Log.d("PurchaseActivity", "Purchase failed with code: " + response.code());
                    Toast.makeText(PurchaseActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<PurchaseDTO> call, Throwable t) {
                Log.d("PurchaseActivity", "Purchase request failed: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PurchaseActivity.this, "Error creating purchase: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private boolean isEventOrganizer() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", null);
        boolean isOrganizer = "EventOrganizer".equals(userRole);
        Log.d("PurchaseActivity", "User role = " + userRole + ", isEventOrganizer = " + isOrganizer);
        Toast.makeText(this, "DEBUG: User role = " + userRole + ", isEventOrganizer = " + isOrganizer, Toast.LENGTH_LONG).show();
        return isOrganizer;
    }
    
    @Override
    public void onEventClick(EventDTO event) {
        // Not used in this activity
    }
}
