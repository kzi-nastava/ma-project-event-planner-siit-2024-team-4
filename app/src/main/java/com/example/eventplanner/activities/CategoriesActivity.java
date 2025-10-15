package com.example.eventplanner.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.CategoryActionListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private final List<CategoryDTO> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        recyclerView = findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categories, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> openCreateDialog());

        loadCategories();
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
        service().getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    adapter.notifyDataSetChanged();
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
}


