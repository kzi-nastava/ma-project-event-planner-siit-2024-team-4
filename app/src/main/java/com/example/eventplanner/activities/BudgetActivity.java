package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.BudgetAdapter;
import com.example.eventplanner.dto.BudgetItemDTO;
import com.example.eventplanner.dto.BudgetPlanDTO;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.CreateBudgetPlanDTO;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.UpdateBudgetPlanDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.BudgetService;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventService;
import com.example.eventplanner.network.service.EventTypeService;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetActivity extends BaseActivity implements BudgetAdapter.OnBudgetItemClickListener {
    
    private TextView tvEventName;
    private TextView tvEventType;
    private RecyclerView rvBudgetItems;
    private MaterialButton btnAddBudgetItem;
    private TextView tvTotalBudget;
    private MaterialButton btnResetBudget;
    private MaterialButton btnConfirmBudget;
    
    private Long eventId;
    private EventDTO event;
    private BudgetPlanDTO budget;
    private List<BudgetItemDTO> budgetItems;
    private List<CategoryDTO> suggestedCategories;
    private List<CategoryDTO> allCategories;
    private BudgetAdapter budgetAdapter;
    
    private BudgetService budgetService;
    private EventService eventService;
    private EventTypeService eventTypeService;
    private CategoryService categoryService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is Event Organizer
        if (!isEventOrganizer()) {
            Toast.makeText(this, getString(R.string.only_event_organizers_budget), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        getLayoutInflater().inflate(R.layout.activity_budget, findViewById(R.id.content_frame), true);
        
        // Get event ID from intent
        eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId == -1) {
            Toast.makeText(this, getString(R.string.event_id_not_provided), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        initializeServices();
        setupRecyclerView();
        loadEventData();
        loadBudgetData();
    }
    
    private void initializeViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventType = findViewById(R.id.tvEventType);
        rvBudgetItems = findViewById(R.id.rvBudgetItems);
        btnAddBudgetItem = findViewById(R.id.btnAddBudgetItem);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        btnResetBudget = findViewById(R.id.btnResetBudget);
        btnConfirmBudget = findViewById(R.id.btnConfirmBudget);
        
        btnAddBudgetItem.setOnClickListener(v -> addBudgetItem());
        btnResetBudget.setOnClickListener(v -> resetBudget());
        btnConfirmBudget.setOnClickListener(v -> confirmBudget());
    }
    
    private void initializeServices() {
        budgetService = ApiClient.getClient(this).create(BudgetService.class);
        eventService = ApiClient.getClient(this).create(EventService.class);
        eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        categoryService = ApiClient.getClient(this).create(CategoryService.class);
    }
    
    private void setupRecyclerView() {
        budgetItems = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(budgetItems, allCategories, this);
        rvBudgetItems.setLayoutManager(new LinearLayoutManager(this));
        rvBudgetItems.setAdapter(budgetAdapter);
    }
    
    private void loadEventData() {
        eventService.getEventById(eventId.intValue()).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    event = response.body();
                    tvEventName.setText(event.getName());
                    tvEventType.setText(event.getEventTypeName());
                    loadSuggestedCategories();
                } else {
                    Toast.makeText(BudgetActivity.this, getString(R.string.failed_to_load_event_data), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, getString(R.string.error_loading_event_data) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadSuggestedCategories() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (EventTypeDTO eventType : response.body()) {
                        if (eventType.getName().equals(event.getEventTypeName())) {
                            suggestedCategories = eventType.getSuggestedCategories();
                            if (suggestedCategories == null) {
                                suggestedCategories = new ArrayList<>();
                            }
                            // Update adapter with suggested categories
                            if (budgetAdapter != null) {
                                budgetAdapter.updateAvailableCategories(suggestedCategories);
                            }
                            break;
                        }
                    }
                    loadAllCategories();
                } else {
                    Toast.makeText(BudgetActivity.this, getString(R.string.failed_to_load_event_types), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, getString(R.string.error_loading_event_types) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadAllCategories() {
        // Use suggested categories instead of loading all categories
        if (suggestedCategories != null) {
            allCategories = new ArrayList<>(suggestedCategories);
            if (budgetAdapter != null) {
                budgetAdapter.updateAvailableCategories(allCategories);
            }
        } else {
            // Fallback: load all categories if suggested categories are not available
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String token = prefs.getString("jwt_token", "");
            categoryService.getAllCategories("Bearer " + token).enqueue(new Callback<List<CategoryDTO>>() {
                @Override
                public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        allCategories = response.body();
                        if (budgetAdapter != null) {
                            budgetAdapter.updateAvailableCategories(allCategories);
                        }
                    } else {
                        Toast.makeText(BudgetActivity.this, getString(R.string.failed_to_load_categories), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                    Toast.makeText(BudgetActivity.this, getString(R.string.error_loading_categories) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadBudgetData() {
        budgetService.getBudgetByEventId(eventId).enqueue(new Callback<BudgetPlanDTO>() {
            @Override
            public void onResponse(Call<BudgetPlanDTO> call, Response<BudgetPlanDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budget = response.body();
                    budgetItems = new ArrayList<>(budget.getItems());
                    budgetAdapter.updateBudgetItems(budgetItems);
                    updateTotalBudget();
                } else {
                    // No budget exists yet, create empty list
                    budget = null;
                    budgetItems = new ArrayList<>();
                    budgetAdapter.updateBudgetItems(budgetItems);
                    updateTotalBudget();
                }
            }
            
            @Override
            public void onFailure(Call<BudgetPlanDTO> call, Throwable t) {
                // No budget exists yet, create empty list
                budget = null;
                budgetItems = new ArrayList<>();
                budgetAdapter.updateBudgetItems(budgetItems);
                updateTotalBudget();
            }
        });
    }
    
    private void addBudgetItem() {
        if (suggestedCategories != null && budgetItems.size() >= suggestedCategories.size()) {
            Toast.makeText(this, getString(R.string.maximum_budget_items), Toast.LENGTH_SHORT).show();
            return;
        }
        
        BudgetItemDTO newItem = new BudgetItemDTO();
        newItem.setCategoryId(null);
        newItem.setAmount(0.0);
        budgetItems.add(newItem);
        budgetAdapter.updateBudgetItems(budgetItems);
        updateTotalBudget();
    }
    
    private void resetBudget() {
        budgetItems.clear();
        budgetAdapter.updateBudgetItems(budgetItems);
        updateTotalBudget();
    }
    
    private void confirmBudget() {
        // Filter valid items
        List<BudgetItemDTO> validItems = new ArrayList<>();
        for (BudgetItemDTO item : budgetItems) {
            if (item.getCategoryId() != null && item.getAmount() != null && item.getAmount() >= 0) {
                validItems.add(item);
            }
        }
        
        if (validItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_valid_budget_item), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (budget != null && budget.getId() != null) {
            // Update existing budget - set budgetPlanId for each item
            for (BudgetItemDTO item : validItems) {
                item.setBudgetPlanId(budget.getId());
            }
            
            // Calculate total budget
            double total = 0.0;
            for (BudgetItemDTO item : validItems) {
                if (item.getAmount() != null) {
                    total += item.getAmount();
                }
            }
            
            UpdateBudgetPlanDTO updateDTO = new UpdateBudgetPlanDTO();
            updateDTO.setId(budget.getId());
            updateDTO.setItemsDTO(validItems);
            updateDTO.setTotal(total);
            
            budgetService.updateBudgetPlan(budget.getId(), updateDTO).enqueue(new Callback<BudgetPlanDTO>() {
                @Override
                public void onResponse(Call<BudgetPlanDTO> call, Response<BudgetPlanDTO> response) {
                    if (response.isSuccessful()) {
                        // Update local budget data
                        budget = response.body();
                        budgetItems = new ArrayList<>(budget.getItems());
                        budgetAdapter.updateBudgetItems(budgetItems);
                        updateTotalBudget();
                        Toast.makeText(BudgetActivity.this, getString(R.string.budget_updated_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BudgetActivity.this, getString(R.string.failed_to_update_budget), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<BudgetPlanDTO> call, Throwable t) {
                    Toast.makeText(BudgetActivity.this, getString(R.string.error_updating_budget) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new budget
            // Calculate total budget
            double total = 0.0;
            for (BudgetItemDTO item : validItems) {
                if (item.getAmount() != null) {
                    total += item.getAmount();
                }
            }
            
            CreateBudgetPlanDTO createDTO = new CreateBudgetPlanDTO();
            createDTO.setEventId(eventId);
            createDTO.setItemsDTO(validItems);
            createDTO.setTotal(total);
            
            budgetService.createBudgetPlan(createDTO).enqueue(new Callback<BudgetPlanDTO>() {
                @Override
                public void onResponse(Call<BudgetPlanDTO> call, Response<BudgetPlanDTO> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(BudgetActivity.this, getString(R.string.budget_created_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BudgetActivity.this, getString(R.string.failed_to_create_budget), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<BudgetPlanDTO> call, Throwable t) {
                    Toast.makeText(BudgetActivity.this, getString(R.string.error_creating_budget) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void updateTotalBudget() {
        double total = 0.0;
        for (BudgetItemDTO item : budgetItems) {
            if (item.getAmount() != null) {
                total += item.getAmount();
            }
        }
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvTotalBudget.setText(df.format(total) + " RSD");
    }
    
    private boolean isEventOrganizer() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", null);
        return "EventOrganizer".equals(userRole);
    }
    
    // BudgetAdapter.OnBudgetItemClickListener implementation
    @Override
    public void onAmountChanged(int position, Double amount) {
        if (position < budgetItems.size()) {
            budgetItems.get(position).setAmount(amount);
            updateTotalBudget();
        }
    }
    
    @Override
    public void onCategoryChanged(int position, Long categoryId) {
        if (position < budgetItems.size()) {
            budgetItems.get(position).setCategoryId(categoryId);
        }
    }
    
    @Override
    public void onRemoveClick(int position) {
        if (position < budgetItems.size()) {
            BudgetItemDTO item = budgetItems.get(position);
            if (item.getPurchaseId() != null || item.getReservationId() != null) {
                Toast.makeText(this, getString(R.string.cannot_remove_item_with_purchases), Toast.LENGTH_SHORT).show();
                return;
            }
            
            budgetItems.remove(position);
            budgetAdapter.updateBudgetItems(budgetItems);
            updateTotalBudget();
        }
    }
}
