package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.Category;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventTypeService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTypeManagementActivity extends BaseActivity {
    
    private static final String TAG = "EventTypeManagement";
    
    private RecyclerView recyclerViewEventTypes;
    private EventTypeAdapter adapter;
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    private List<CategoryDTO> categories = new ArrayList<>();
    
    private ScrollView addFormLayout;
    private LinearLayout addFormContent;
    private LinearLayout categoriesContainer;
    private TextView tvFormTitle;
    private EditText etEventTypeName, etEventTypeDescription;
    private Button btnAddEventType, btnCancelAdd, btnShowAddForm;
    private List<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private List<Long> selectedCategoryIds = new ArrayList<>();
    
    // Edit mode variables
    private EventTypeDTO editingEventType = null;
    private boolean isEditMode = false;
    
    private String userRole;
    private boolean isAdmin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_event_type_management, contentFrame, true);
        
        // Get user role
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "");
        isAdmin = "Admin".equals(userRole);
        
        // Keep title empty like other activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        initViews();
        setupRecyclerView();
        loadEventTypes();
        loadCategories();
        
        if (!isAdmin) {
            hideAddForm();
            btnShowAddForm.setVisibility(View.GONE);
        } else {
            btnShowAddForm.setVisibility(View.VISIBLE);
        }
    }
    
    private void initViews() {
        recyclerViewEventTypes = findViewById(R.id.recyclerViewEventTypes);
        addFormLayout = findViewById(R.id.addFormLayout);
        addFormContent = findViewById(R.id.addFormContent);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etEventTypeName = findViewById(R.id.etEventTypeName);
        etEventTypeDescription = findViewById(R.id.etEventTypeDescription);
        btnAddEventType = findViewById(R.id.btnAddEventType);
        btnCancelAdd = findViewById(R.id.btnCancelAdd);
        btnShowAddForm = findViewById(R.id.btnShowAddForm);
        
        btnAddEventType.setOnClickListener(v -> addEventType());
        btnCancelAdd.setOnClickListener(v -> cancelAdd());
        btnShowAddForm.setOnClickListener(v -> showAddForm());
    }
    
    private void setupRecyclerView() {
        adapter = new EventTypeAdapter(eventTypes, categories, isAdmin, this);
        recyclerViewEventTypes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEventTypes.setAdapter(adapter);
    }
    
    private void loadEventTypes() {
        String token = getToken();
        if (token == null) return;
        
        EventTypeService service = ApiClient.getClient(this).create(EventTypeService.class);
        service.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EventTypeManagementActivity.this, "Failed to load event types", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(EventTypeManagementActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading event types", t);
            }
        });
    }
    
    private void loadCategories() {
        String token = getToken();
        if (token == null) return;
        
        CategoryService service = ApiClient.getClient(this).create(CategoryService.class);
        service.getAllApprovedCategories("Bearer " + token).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    adapter.setCategories(categories);
                    createCategoryCheckBoxes();
                } else {
                    Toast.makeText(EventTypeManagementActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Log.e(TAG, "Error loading categories", t);
            }
        });
    }
    
    private void createCategoryCheckBoxes() {
        categoriesContainer.removeAllViews();
        categoryCheckBoxes.clear();
        
        for (CategoryDTO category : categories) {
            View categoryView = getLayoutInflater().inflate(R.layout.item_category_checkbox, categoriesContainer, false);
            
            CheckBox checkBox = categoryView.findViewById(R.id.cbCategory);
            TextView textView = categoryView.findViewById(R.id.tvCategoryName);
            
            textView.setText(category.name);
            checkBox.setTag(category.id);
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Long categoryId = (Long) buttonView.getTag();
                if (isChecked) {
                    if (!selectedCategoryIds.contains(categoryId)) {
                        selectedCategoryIds.add(categoryId);
                    }
                } else {
                    selectedCategoryIds.remove(categoryId);
                }
            });
            
            categoryCheckBoxes.add(checkBox);
            categoriesContainer.addView(categoryView);
        }
    }
    
    private void addEventType() {
        if (!isAdmin) {
            Toast.makeText(this, "Only administrators can manage event types", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String name = etEventTypeName.getText().toString().trim();
        String description = etEventTypeDescription.getText().toString().trim();
        
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isEditMode && editingEventType != null) {
            updateEventType(name, description);
        } else {
            createEventType(name, description);
        }
    }
    
    private void createEventType(String name, String description) {
        
        // Get selected categories and convert to Category objects
        List<Category> selectedCategories = new ArrayList<>();
        for (Long categoryId : selectedCategoryIds) {
            for (CategoryDTO categoryDTO : categories) {
                if (categoryDTO.id.equals(categoryId)) {
                    Category category = new Category();
                    category.setId(categoryDTO.id);
                    category.setName(categoryDTO.name);
                    category.setDescription(categoryDTO.description);
                    category.setApprovedByAdmin(categoryDTO.isApprovedByAdmin);
                    selectedCategories.add(category);
                    break;
                }
            }
        }
        
        for (Category cat : selectedCategories) {
        }
        
        com.example.eventplanner.dto.CreateEventTypeDTO dto = 
            new com.example.eventplanner.dto.CreateEventTypeDTO(name, description, selectedCategories);
        
        String token = getToken();
        if (token == null) return;
        
        for (Category cat : dto.getSuggestedCategories()) {
        }
        
        EventTypeService service = ApiClient.getClient(this).create(EventTypeService.class);
        service.createEventType("Bearer " + token, dto).enqueue(new Callback<EventTypeDTO>() {
            @Override
            public void onResponse(Call<EventTypeDTO> call, Response<EventTypeDTO> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                    }
                    Toast.makeText(EventTypeManagementActivity.this, "Event type created successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadEventTypes();
                } else {
                    Log.e(TAG, "Failed to create event type: " + response.code() + " - " + response.message());
                    try {
                        Log.e(TAG, "Response body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                    Toast.makeText(EventTypeManagementActivity.this, "Failed to create event type: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<EventTypeDTO> call, Throwable t) {
                Toast.makeText(EventTypeManagementActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error creating event type", t);
            }
        });
    }
    
    private void updateEventType(String name, String description) {
        // Get selected categories and convert to Category objects
        List<Category> selectedCategories = new ArrayList<>();
        for (Long categoryId : selectedCategoryIds) {
            for (CategoryDTO categoryDTO : categories) {
                if (categoryDTO.id.equals(categoryId)) {
                    Category category = new Category();
                    category.setId(categoryDTO.id);
                    category.setName(categoryDTO.name);
                    category.setDescription(categoryDTO.description);
                    category.setApprovedByAdmin(categoryDTO.isApprovedByAdmin);
                    selectedCategories.add(category);
                    break;
                }
            }
        }
        
        
        com.example.eventplanner.dto.UpdateEventTypeDTO dto = 
            new com.example.eventplanner.dto.UpdateEventTypeDTO(name, description, selectedCategories);
        
        String token = getToken();
        if (token == null) return;
        
        EventTypeService service = ApiClient.getClient(this).create(EventTypeService.class);
        service.updateEventType("Bearer " + token, editingEventType.getId(), dto).enqueue(new Callback<EventTypeDTO>() {
            @Override
            public void onResponse(Call<EventTypeDTO> call, Response<EventTypeDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventTypeManagementActivity.this, "Event type updated successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadEventTypes();
                } else {
                    Log.e(TAG, "Failed to update event type: " + response.code() + " - " + response.message());
                    try {
                        Log.e(TAG, "Response body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                    Toast.makeText(EventTypeManagementActivity.this, "Failed to update event type: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<EventTypeDTO> call, Throwable t) {
                Toast.makeText(EventTypeManagementActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating event type", t);
            }
        });
    }
    
    private void cancelAdd() {
        clearForm();
    }
    
    private void clearForm() {
        etEventTypeName.setText("");
        etEventTypeDescription.setText("");
        selectedCategoryIds.clear();
        
        // Uncheck all category checkboxes
        for (CheckBox checkBox : categoryCheckBoxes) {
            checkBox.setChecked(false);
        }
        
        // Reset edit mode
        isEditMode = false;
        editingEventType = null;
        
        addFormLayout.setVisibility(View.GONE);
    }
    
    private void hideAddForm() {
        addFormLayout.setVisibility(View.GONE);
    }
    
    public void toggleEventTypeStatus(EventTypeDTO eventType) {
        String token = getToken();
        if (token == null) return;
        
        EventTypeService service = ApiClient.getClient(this).create(EventTypeService.class);
        Call<Void> call = eventType.isActive() ? 
            service.deactivateEventType("Bearer " + token, eventType.getId()) :
            service.activateEventType("Bearer " + token, eventType.getId());
            
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventTypeManagementActivity.this, 
                        "Event type " + (eventType.isActive() ? "deactivated" : "activated"), 
                        Toast.LENGTH_SHORT).show();
                    loadEventTypes();
                } else {
                    Toast.makeText(EventTypeManagementActivity.this, "Failed to update event type", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventTypeManagementActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error toggling event type status", t);
            }
        });
    }
    
    public void showAddForm() {
        if (isAdmin) {
            isEditMode = false;
            editingEventType = null;
            clearForm();
            updateFormUI();
            addFormLayout.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Only administrators can add event types", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void startEditEventType(EventTypeDTO eventType) {
        if (isAdmin) {
            isEditMode = true;
            editingEventType = eventType;
            
            // Fill form with existing data
            etEventTypeName.setText(eventType.getName());
            etEventTypeDescription.setText(eventType.getDescription());
            
            // Clear and set selected categories
            selectedCategoryIds.clear();
            if (eventType.getSuggestedCategories() != null) {
                for (CategoryDTO cat : eventType.getSuggestedCategories()) {
                    selectedCategoryIds.add(cat.id);
                }
            }
            
            // Update checkboxes
            for (CheckBox checkBox : categoryCheckBoxes) {
                Long categoryId = (Long) checkBox.getTag();
                checkBox.setChecked(selectedCategoryIds.contains(categoryId));
            }
            
            updateFormUI();
            addFormLayout.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Only administrators can edit event types", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateFormUI() {
        if (isEditMode) {
            tvFormTitle.setText("Edit Event Type");
            btnAddEventType.setText("Update");
        } else {
            tvFormTitle.setText("Add New Event Type");
            btnAddEventType.setText("Add");
        }
    }
    
    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        }
        
        return token;
    }
}
