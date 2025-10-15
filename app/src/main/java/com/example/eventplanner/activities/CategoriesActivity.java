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
import com.example.eventplanner.dto.UpdateCategoryDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_categories, contentFrame, true);

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
        Log.d("CategoriesActivity", "User role: " + userRole);
        boolean isAdmin = userRole != null && ("ADMIN".equals(userRole) || "Admin".equals(userRole));
        Log.d("CategoriesActivity", "Is admin: " + isAdmin);
        return isAdmin;
    }

    private String getAuthHeader() {
        // Expect AuthInterceptor to inject token automatically via ApiClient.getClient(context)
        // Some endpoints also accept explicit token via header, but we rely on interceptor
        return null;
    }

    private CategoryService service() {
        return ApiClient.getClient(this).create(CategoryService.class);
    }

    private void loadCategories() {
        // Load all categories and filter them like in IKS Angular app
        service().getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryDTO> allCategories = response.body();
                    
                    // Filter approved categories (like in IKS: data.filter((c: any) => c.approvedByAdmin))
                    approvedCategories.clear();
                    for (CategoryDTO category : allCategories) {
                        if (category.isApprovedByAdmin) {
                            approvedCategories.add(category);
                        }
                    }
                    approvedAdapter.notifyDataSetChanged();
                    
                    // Filter pending categories (like in IKS: data.filter((c: any) => !c.approvedByAdmin))
                    if (isAdmin()) {
                        pendingCategories.clear();
                        for (CategoryDTO category : allCategories) {
                            if (!category.isApprovedByAdmin) {
                                pendingCategories.add(category);
                            }
                        }
                        pendingAdapter.notifyDataSetChanged();
                        
                        // Show/hide pending section based on whether there are pending categories
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
                dto.isApprovedByAdmin = true; // admin creates approved
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
        // Approve category by updating isApprovedByAdmin to true (like in IKS Angular app)
        UpdateCategoryDTO updateDto = new UpdateCategoryDTO();
        updateDto.id = category.id;
        updateDto.name = category.name;
        updateDto.description = category.description;
        updateDto.isApprovedByAdmin = true;

        service().updateCategory(getAuthHeader(), category.id, updateDto).enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriesActivity.this, R.string.category_approved, Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(CategoriesActivity.this, R.string.error_approve_category, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                Toast.makeText(CategoriesActivity.this, R.string.error_approve_category, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeny(CategoryDTO category) {
        // Deny category by deleting it (like in IKS Angular app)
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


