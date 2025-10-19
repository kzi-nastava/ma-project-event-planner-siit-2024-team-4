package com.example.eventplanner.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.CreateCategoryDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.UpdateCategoryDTO;
import com.example.eventplanner.dto.UpdateServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.ServiceService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import okhttp3.RequestBody;

public class CategoriesActivity extends BaseActivity implements CategoryAdapter.CategoryActionListener {

    private RecyclerView recyclerApprovedCategories;
    private RecyclerView recyclerPendingCategories;
    private CategoryAdapter approvedAdapter;
    private CategoryAdapter pendingAdapter;
    private final List<CategoryDTO> approvedCategories = new ArrayList<>();
    private final List<CategoryDTO> pendingCategories = new ArrayList<>();
    private TextView tvPendingTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getLayoutInflater().inflate(R.layout.activity_categories, findViewById(R.id.content_frame));
        setTitle(R.string.categories);

        recyclerApprovedCategories = findViewById(R.id.recyclerApprovedCategories);
        recyclerPendingCategories = findViewById(R.id.recyclerPendingCategories);
        tvPendingTitle = findViewById(R.id.tvPendingTitle);
        
        recyclerApprovedCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerPendingCategories.setLayoutManager(new LinearLayoutManager(this));
        
        boolean canManage = isAdmin();
        approvedAdapter = new CategoryAdapter(approvedCategories, this, canManage, false);
        pendingAdapter = new CategoryAdapter(pendingCategories, this, canManage, true);
        
        recyclerApprovedCategories.setAdapter(approvedAdapter);
        recyclerPendingCategories.setAdapter(pendingAdapter);

        View btnAdd = findViewById(R.id.btnAddCategory);
        if (canManage) {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setOnClickListener(v -> openCreateDialog());
        } else {
            btnAdd.setVisibility(View.GONE);
        }

        loadCategories();
    }

    private boolean isAdmin() {
        String userRole = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("user_role", null);
        boolean isAdmin = userRole != null && ("ADMIN".equals(userRole) || "Admin".equals(userRole));
        return isAdmin;
    }

    private String getAuthHeader() {
        return null;
    }

    private CategoryService service() {
        return ApiClient.getClient(this).create(CategoryService.class);
    }

    private void loadCategories() {
        service().getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryDTO> allCategories = response.body();
                    
                    approvedCategories.clear();
                    for (CategoryDTO category : allCategories) {
                        if (category.isApprovedByAdmin) {
                            approvedCategories.add(category);
                        }
                    }
                    approvedAdapter.notifyDataSetChanged();
                    
                    if (isAdmin()) {
                        pendingCategories.clear();
                        for (CategoryDTO category : allCategories) {
                            if (!category.isApprovedByAdmin) {
                                pendingCategories.add(category);
                            }
                        }
                        pendingAdapter.notifyDataSetChanged();
                        
                        if (pendingCategories.isEmpty()) {
                            tvPendingTitle.setVisibility(View.GONE);
                            recyclerPendingCategories.setVisibility(View.GONE);
                        } else {
                            tvPendingTitle.setVisibility(View.VISIBLE);
                            recyclerPendingCategories.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Toast.makeText(CategoriesActivity.this, R.string.error_loading_categories, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Toast.makeText(CategoriesActivity.this, R.string.error_loading_categories, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreateDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null);
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etDescription = view.findViewById(R.id.etCategoryDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_category)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String desc = etDescription.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc)) {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                CreateCategoryDTO dto = new CreateCategoryDTO();
                dto.name = name;
                dto.description = desc;
                dto.isApprovedByAdmin = true;
                service().createCategory(getAuthHeader(), dto).enqueue(new Callback<CategoryDTO>() {
                    @Override
                    public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CategoriesActivity.this, R.string.category_created, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadCategories();
                        } else {
                            Toast.makeText(CategoriesActivity.this, R.string.error_create_category, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CategoryDTO> call, Throwable t) {
                        Toast.makeText(CategoriesActivity.this, R.string.error_create_category, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        dialog.show();
    }

    private void openEditDialog(CategoryDTO category) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null);
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etDescription = view.findViewById(R.id.etCategoryDescription);
        etName.setText(category.name);
        etDescription.setText(category.description);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_category)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String desc = etDescription.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc)) {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                UpdateCategoryDTO dto = new UpdateCategoryDTO();
                dto.id = category.id;
                dto.name = name;
                dto.description = desc;
                dto.isApprovedByAdmin = category.isApprovedByAdmin;
                service().updateCategory(getAuthHeader(), category.id, dto).enqueue(new Callback<CategoryDTO>() {
                    @Override
                    public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CategoriesActivity.this, R.string.category_updated, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadCategories();
                        } else {
                            Toast.makeText(CategoriesActivity.this, R.string.error_update_category, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CategoryDTO> call, Throwable t) {
                        Toast.makeText(CategoriesActivity.this, R.string.error_update_category, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        dialog.show();
    }

    @Override
    public void onEdit(CategoryDTO category) {
        openEditDialog(category);
    }

    @Override
    public void onDelete(CategoryDTO category) {
        service().deleteCategory(getAuthHeader(), category.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriesActivity.this, R.string.category_deleted, Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(CategoriesActivity.this, R.string.error_delete_category, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CategoriesActivity.this, R.string.error_delete_category, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(CategoryDTO category) {
        UpdateCategoryDTO updateDto = new UpdateCategoryDTO();
        updateDto.id = category.id;
        updateDto.name = category.name;
        updateDto.description = category.description;
        updateDto.isApprovedByAdmin = true;

        android.util.Log.d("CategoriesActivity", "Updating category with DTO: " + new com.google.gson.Gson().toJson(updateDto));

        service().updateCategory(getAuthHeader(), category.id, updateDto).enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                android.util.Log.d("CategoriesActivity", "=== updateCategory RESPONSE ===");
                android.util.Log.d("CategoriesActivity", "Response code: " + response.code());
                android.util.Log.d("CategoriesActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful()) {
                    android.util.Log.d("CategoriesActivity", "✅ Category approved successfully, now making services visible...");
                    // After approving category, make all services with this category visible
                    makeServicesVisibleForCategory(category.id);
                    Toast.makeText(CategoriesActivity.this, R.string.category_approved, Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    android.util.Log.e("CategoriesActivity", "❌ Failed to approve category");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("CategoriesActivity", "Error response body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("CategoriesActivity", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(CategoriesActivity.this, R.string.error_approve_category, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                android.util.Log.e("CategoriesActivity", "❌ Error approving category", t);
                Toast.makeText(CategoriesActivity.this, R.string.error_approve_category, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeServicesVisibleForCategory(Long categoryId) {
        android.util.Log.d("CategoriesActivity", "=== STARTING makeServicesVisibleForCategory for category ID: " + categoryId + " ===");
        
        // Get all services and find those with the approved category
        ServiceService serviceService = ApiClient.getClient(this).create(ServiceService.class);
        serviceService.getAllServices(getAuthHeader()).enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                android.util.Log.d("CategoriesActivity", "=== API RESPONSE for getAllServices ===");
                android.util.Log.d("CategoriesActivity", "Response code: " + response.code());
                android.util.Log.d("CategoriesActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> allServices = response.body();
                    android.util.Log.d("CategoriesActivity", "Total services loaded: " + allServices.size());
                    
                    int updatedCount = 0;
                    
                    for (ServiceDTO service : allServices) {
                        android.util.Log.d("CategoriesActivity", "Checking service: " + service.getName() + 
                            ", Category ID: " + (service.getCategory() != null ? service.getCategory().getId() : "null") +
                            ", Visible: " + service.isVisible());
                            
                        // Check if service has the approved category
                        if (service.getCategory() != null && service.getCategory().getId().equals(categoryId)) {
                            android.util.Log.d("CategoriesActivity", "Found service with category ID " + categoryId + ": " + service.getName() + 
                                " (ID: " + service.getId() + "), Visible: " + service.isVisible());
                            
                            if (!service.isVisible()) {
                                android.util.Log.d("CategoriesActivity", "Service is not visible, updating to visible: " + service.getName());
                                // Update service to be visible
                                updateServiceVisibility(service.getId(), true);
                                updatedCount++;
                            } else {
                                android.util.Log.d("CategoriesActivity", "Service is already visible: " + service.getName());
                            }
                        }
                    }
                    
                    android.util.Log.d("CategoriesActivity", "=== FINAL RESULT ===");
                    android.util.Log.d("CategoriesActivity", "Updated " + updatedCount + " services for category ID: " + categoryId);
                    
                    if (updatedCount > 0) {
                        android.util.Log.d("CategoriesActivity", "Made " + updatedCount + " services visible for category ID: " + categoryId);
                    } else {
                        android.util.Log.d("CategoriesActivity", "No services found to update for category ID: " + categoryId);
                    }
                } else {
                    android.util.Log.e("CategoriesActivity", "Failed to load services - Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("CategoriesActivity", "Error response body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("CategoriesActivity", "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                android.util.Log.e("CategoriesActivity", "Error loading services for category approval", t);
            }
        });
    }

    private void updateServiceVisibility(Long serviceId, boolean visible) {
        android.util.Log.d("CategoriesActivity", "=== STARTING updateServiceVisibility for service ID: " + serviceId + ", visible: " + visible + " ===");
        
        ServiceService serviceService = ApiClient.getClient(this).create(ServiceService.class);
        
        // Get the service first to preserve all its data
        serviceService.getServiceById(getAuthHeader(), serviceId).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                android.util.Log.d("CategoriesActivity", "=== getServiceById RESPONSE ===");
                android.util.Log.d("CategoriesActivity", "Response code: " + response.code());
                android.util.Log.d("CategoriesActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ServiceDTO service = response.body();
                    android.util.Log.d("CategoriesActivity", "Service loaded: " + service.getName() + ", current visible: " + service.isVisible());
                    
                    // Create update DTO with all existing data but change visibility
                    UpdateServiceDTO updateDto = new UpdateServiceDTO();
                    updateDto.setName(service.getName());
                    updateDto.setDescription(service.getDescription());
                    updateDto.setPrice(service.getPrice());
                    updateDto.setDiscount(service.getDiscount());
                    updateDto.setDuration(service.getDuration());
                    updateDto.setMinEngagement(service.getMinEngagement());
                    updateDto.setMaxEngagement(service.getMaxEngagement());
                    updateDto.setReservationDue(service.getReservationDue());
                    updateDto.setCancelationDue(service.getCancelationDue());
                    updateDto.setReservationType(service.getReservationType());
                    updateDto.setAvailable(service.isAvailable());
                    updateDto.setVisible(visible); // Set to visible
                    updateDto.setCategoryId(service.getCategory().getId());
                    updateDto.setEventTypeIds(service.getEventTypes().stream().map(et -> et.getId()).collect(java.util.stream.Collectors.toList()));
                    
                    android.util.Log.d("CategoriesActivity", "Update DTO created - setting visible to: " + visible);
                    android.util.Log.d("CategoriesActivity", "Update DTO JSON: " + new com.google.gson.Gson().toJson(updateDto));
                    
                    // Update the service
                    serviceService.updateService(getAuthHeader(), serviceId, 
                        RequestBody.create(okhttp3.MediaType.parse("application/json"), new com.google.gson.Gson().toJson(updateDto)), 
                        new java.util.ArrayList<>()).enqueue(new Callback<ServiceDTO>() {
                        @Override
                        public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                            android.util.Log.d("CategoriesActivity", "=== updateService RESPONSE ===");
                            android.util.Log.d("CategoriesActivity", "Response code: " + response.code());
                            android.util.Log.d("CategoriesActivity", "Response successful: " + response.isSuccessful());
                            
                            if (response.isSuccessful()) {
                                android.util.Log.d("CategoriesActivity", "✅ Service " + serviceId + " successfully made visible");
                            } else {
                                android.util.Log.e("CategoriesActivity", "❌ Failed to update service " + serviceId + " visibility");
                                if (response.errorBody() != null) {
                                    try {
                                        String errorBody = response.errorBody().string();
                                        android.util.Log.e("CategoriesActivity", "Error response body: " + errorBody);
                                    } catch (Exception e) {
                                        android.util.Log.e("CategoriesActivity", "Error reading error body", e);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ServiceDTO> call, Throwable t) {
                            android.util.Log.e("CategoriesActivity", "❌ Error updating service " + serviceId + " visibility", t);
                        }
                    });
                } else {
                    android.util.Log.e("CategoriesActivity", "❌ Failed to get service " + serviceId + " for visibility update");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("CategoriesActivity", "Error response body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("CategoriesActivity", "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                android.util.Log.e("CategoriesActivity", "❌ Error getting service " + serviceId + " for visibility update", t);
            }
        });
    }

    @Override
    public void onDeny(CategoryDTO category) {
        new AlertDialog.Builder(this)
                .setTitle("Deny Category")
                .setMessage("Are you sure you want to deny this category? It will be permanently deleted.")
                .setPositiveButton("Deny", (dialog, which) -> {
                    service().deleteCategory(getAuthHeader(), category.id).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CategoriesActivity.this, R.string.category_denied, Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                Toast.makeText(CategoriesActivity.this, R.string.error_deny_category, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CategoriesActivity.this, R.string.error_deny_category, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}


