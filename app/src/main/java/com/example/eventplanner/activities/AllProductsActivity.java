package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ProductAdapter;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.ProductService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AllProductsActivity extends BaseActivity implements ProductAdapter.OnProductClickListener {
    
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private EditText etSearch;
    private Spinner spinnerCategory;
    private Spinner spinnerAvailability;
    private Spinner spinnerSort;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Button btnFilter;
    private FloatingActionButton fabAddProduct;
    
    private ProductAdapter productAdapter;
    private List<ProductDTO> allProducts = new ArrayList<>();
    private List<ProductDTO> filteredProducts = new ArrayList<>();
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    
    private String userRole;
    private boolean isMyProducts = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_all_products, findViewById(R.id.content_frame), true);
        
        isMyProducts = getIntent().getBooleanExtra("isMyProducts", false);
        
        initViews();
        
        if (isMyProducts) {
            setTitle("My Products");
        } else {
            setTitle("All Products");
        }
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        loadUserRole();
        loadCategories();
        loadEventTypes();
        loadProducts();
    }
    
    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        etSearch = findViewById(R.id.etSearch);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);
        spinnerSort = findViewById(R.id.spinnerSort);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        btnFilter = findViewById(R.id.btnFilter);
        fabAddProduct = findViewById(R.id.fabAddProduct);
    }
    
    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(filteredProducts, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
    }
    
    private void setupSpinners() {
        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add("All Categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        String[] availabilityOptions = {"All", "Available", "Unavailable"};
        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availabilityOptions);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);
        
        String[] sortOptions = {"Name", "Price (Low to High)", "Price (High to Low)"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
    }
    
    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnFilter.setOnClickListener(v -> applyFilters());
        
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AllProductsActivity.this, AddProductActivity.class);
            startActivityForResult(intent, 200);
        });
    }
    
    private void loadUserRole() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", null);
        
        if ("SPProvider".equals(userRole) || "SERVICE_PROVIDER".equals(userRole)) {
            fabAddProduct.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadCategories() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.getAllApprovedCategories("Bearer " + token).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategorySpinner();
                }
            }
            
            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
            }
        });
    }
    
    private void loadEventTypes() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            return;
        }
        
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                }
            }
            
            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
            }
        });
    }
    
    private void updateCategorySpinner() {
        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add("All Categories");
        for (CategoryDTO category : categories) {
            categoryOptions.add(category.getName());
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }
    
    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        
        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        
        if (isMyProducts) {
            loadMyProducts(productService);
        } else {
            loadAllProducts(productService);
        }
    }
    
    private void loadAllProducts(ProductService productService) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        productService.getAllProducts("Bearer " + token).enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    filteredProducts = new ArrayList<>(allProducts);
                    
                    
                    productAdapter.updateProducts(filteredProducts);
                    updateEmptyState();
                } else {
                    Toast.makeText(AllProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllProductsActivity.this, "Error loading products: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadMyProducts(ProductService productService) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        String userId = prefs.getString("user_id", null);
        
        if (token == null || userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        productService.getMyProducts("Bearer " + token, userId).enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    filteredProducts = new ArrayList<>(allProducts);
                    productAdapter.setShowEditButton(true);
                    productAdapter.updateProducts(filteredProducts);
                    updateEmptyState();
                } else {
                    Toast.makeText(AllProductsActivity.this, "Failed to load your products", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllProductsActivity.this, "Error loading your products: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void performSearch(String searchTerm) {
        if (searchTerm.trim().isEmpty()) {
            filteredProducts = new ArrayList<>(allProducts);
        } else {
            filteredProducts = new ArrayList<>();
            for (ProductDTO product : allProducts) {
                if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))) {
                    filteredProducts.add(product);
                }
            }
        }
        productAdapter.updateProducts(filteredProducts);
        updateEmptyState();
    }
    
    private void applyFilters() {
        filteredProducts = new ArrayList<>(allProducts);
        
        String selectedCategory = (String) spinnerCategory.getSelectedItem();
        if (!"All Categories".equals(selectedCategory)) {
            filteredProducts.removeIf(product -> !selectedCategory.equals(product.getCategoryName()));
        }
        
        String selectedAvailability = (String) spinnerAvailability.getSelectedItem();
        if ("Available".equals(selectedAvailability)) {
            filteredProducts.removeIf(product -> !Boolean.TRUE.equals(product.getAvailable()));
        } else if ("Unavailable".equals(selectedAvailability)) {
            filteredProducts.removeIf(product -> Boolean.TRUE.equals(product.getAvailable()));
        }
        
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();
        
        if (!minPriceStr.isEmpty()) {
            try {
                double minPrice = Double.parseDouble(minPriceStr);
                filteredProducts.removeIf(product -> product.getPrice() == null || product.getPrice() < minPrice);
            } catch (NumberFormatException e) {
            }
        }
        
        if (!maxPriceStr.isEmpty()) {
            try {
                double maxPrice = Double.parseDouble(maxPriceStr);
                filteredProducts.removeIf(product -> product.getPrice() == null || product.getPrice() > maxPrice);
            } catch (NumberFormatException e) {
            }
        }
        
        String selectedSort = (String) spinnerSort.getSelectedItem();
        if ("Price (Low to High)".equals(selectedSort)) {
            filteredProducts.sort((p1, p2) -> {
                if (p1.getPrice() == null && p2.getPrice() == null) return 0;
                if (p1.getPrice() == null) return 1;
                if (p2.getPrice() == null) return -1;
                return Double.compare(p1.getPrice(), p2.getPrice());
            });
        } else if ("Price (High to Low)".equals(selectedSort)) {
            filteredProducts.sort((p1, p2) -> {
                if (p1.getPrice() == null && p2.getPrice() == null) return 0;
                if (p1.getPrice() == null) return 1;
                if (p2.getPrice() == null) return -1;
                return Double.compare(p2.getPrice(), p1.getPrice());
            });
        } else { 
            filteredProducts.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        }
        
        productAdapter.updateProducts(filteredProducts);
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onProductClick(ProductDTO product) {
        Intent intent = new Intent(this, AboutProductActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
    
    @Override
    public void onEditClick(ProductDTO product) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("product", product);
        startActivityForResult(intent, 300);
    }
    
    @Override
    public void onDeleteClick(ProductDTO product) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteProduct(ProductDTO product) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        productService.deleteProduct("Bearer " + token, product.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AllProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    allProducts.remove(product);
                    filteredProducts.remove(product);
                    productAdapter.updateProducts(filteredProducts);
                    updateEmptyState();
                } else {
                    Toast.makeText(AllProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AllProductsActivity.this, "Error deleting product: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 200 && resultCode == RESULT_OK) {
            loadProducts();
        } else if (requestCode == 300 && resultCode == RESULT_OK) {
            loadProducts();
        }
    }
}
