package com.example.eventplanner.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventTypeCheckboxAdapter;
import com.example.eventplanner.adapters.EventTypeChipAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.CreateCategoryDTO;
import com.example.eventplanner.dto.CreateProductDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.MultipartHelper;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.ProductService;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends BaseActivity {

    private EditText etProductName;
    private EditText etProductDescription;
    private EditText etProductPrice;
    private EditText etProductDiscount;
    private CheckBox cbProductAvailable;
    private Spinner spinnerCategory;
    private Button btnAddCategory;
    private RecyclerView rvEventTypes;
    private Button btnAddEventType;
    private RecyclerView rvImages;
    private Button btnAddImage;
    private Button btnAddProduct;
    private ProgressBar progressBar;

    private List<CategoryDTO> categories;
    private List<EventTypeDTO> eventTypes;
    private List<EventTypeDTO> selectedEventTypes;
    private List<String> selectedImages;
    private EventTypeChipAdapter eventTypeChipAdapter;
    private ImageAdapter imageAdapter;
    private String token;
    private String userId;
    private String userRole;

    // Image picker variables
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_add_product, contentFrame, true);

        initViews();
        initImagePicker();
        loadUserInfo();
        
        // Check if user has SPP role
        if (!isSPPUser()) {
            Toast.makeText(this, "Only Service Providers can add products", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        loadCategories();
        loadEventTypes();
        setupAdapters();
        setupButtons();
    }

    private void initViews() {
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductDiscount = findViewById(R.id.etProductDiscount);
        cbProductAvailable = findViewById(R.id.cbProductAvailable);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        rvEventTypes = findViewById(R.id.rvEventTypes);
        btnAddEventType = findViewById(R.id.btnAddEventType);
        rvImages = findViewById(R.id.rvImages);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initImagePicker() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData());
                    }
                }
        );
    }

    private boolean isSPPUser() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", null);
        // Map SPProvider to SERVICE_PROVIDER for server compatibility
        if ("SPProvider".equals(userRole)) {
            userRole = "SERVICE_PROVIDER";
        }
        return "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole);
    }
    
    private boolean isUsingNewCategory(Long categoryId) {
        if (categoryId == null || categories == null) {
            return false;
        }
        
        // Check if the category is newly created (not approved by admin)
        for (CategoryDTO category : categories) {
            if (category.getId().equals(categoryId)) {
                // If category doesn't have approval status or is not approved, it's new
                return !category.isApprovedByAdmin();
            }
        }
        return false;
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        token = prefs.getString("jwt_token", null);
        userId = prefs.getString("user_id", null);
        
        // Initialize lists
        selectedEventTypes = new ArrayList<>();
        selectedImages = new ArrayList<>();
    }

    private void setupAdapters() {
        // Setup Event Types RecyclerView
        eventTypeChipAdapter = new EventTypeChipAdapter(selectedEventTypes, eventType -> {
            selectedEventTypes.remove(eventType);
            eventTypeChipAdapter.updateEventTypes(selectedEventTypes);
        });
        rvEventTypes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEventTypes.setAdapter(eventTypeChipAdapter);

        // Setup Images RecyclerView
        imageAdapter = new ImageAdapter(selectedImages, position -> {
            selectedImages.remove(position);
            imageAdapter.updateImages(selectedImages);
        });
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void loadCategories() {
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.getAllApprovedCategories("Bearer " + token).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                android.util.Log.d("AddProduct", "Categories response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    setupCategorySpinner();
                    android.util.Log.d("AddProduct", "Categories loaded successfully: " + categories.size());
                } else {
                    android.util.Log.e("AddProduct", "Failed to load categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                android.util.Log.e("AddProduct", "Categories network error: " + t.getMessage());
            }
        });
    }

    private void loadEventTypes() {
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                android.util.Log.d("AddProduct", "EventTypes response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    setupEventTypeSpinner();
                    android.util.Log.d("AddProduct", "EventTypes loaded successfully: " + eventTypes.size());
                } else {
                    android.util.Log.e("AddProduct", "Failed to load event types: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                android.util.Log.e("AddProduct", "EventTypes network error: " + t.getMessage());
            }
        });
    }

    private void setupCategorySpinner() {
        if (categories != null && !categories.isEmpty()) {
            List<String> categoryNames = new ArrayList<>();
            for (CategoryDTO category : categories) {
                categoryNames.add(category.getName());
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categoryNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
        }
    }

    private void setupEventTypeSpinner() {
        if (eventTypes != null && !eventTypes.isEmpty()) {
            List<String> eventTypeNames = new ArrayList<>();
            for (EventTypeDTO eventType : eventTypes) {
                eventTypeNames.add(eventType.getName());
            }
            
            // Event types are now handled by the RecyclerView with chips
            // No need to set up spinner adapter
        }
    }

    private void setupAddButton() {
        btnAddProduct.setOnClickListener(v -> addProduct());
    }

    private void addProduct() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);

        // Get selected category and event type
        CategoryDTO selectedCategory = null;
        EventTypeDTO selectedEventType = null;
        
        if (categories != null && spinnerCategory.getSelectedItemPosition() >= 0) {
            selectedCategory = categories.get(spinnerCategory.getSelectedItemPosition());
        }
        
        // Use selected event types from the new system
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            // For now, use the first selected event type
            selectedEventType = selectedEventTypes.get(0);
        }

        // Create product
        ProductDTO product = new ProductDTO();
        product.setName(etProductName.getText().toString().trim());
        product.setDescription(etProductDescription.getText().toString().trim());
        product.setPrice(Double.parseDouble(etProductPrice.getText().toString().trim()));
        
        String discountText = etProductDiscount.getText().toString().trim();
        if (!discountText.isEmpty()) {
            product.setDiscount(Double.parseDouble(discountText));
        } else {
            product.setDiscount(0.0);
        }
        
        product.setAvailable(cbProductAvailable.isChecked());
        product.setCategoryId(selectedCategory != null ? selectedCategory.getId() : null);
        product.setEventTypeId(selectedEventType != null ? selectedEventType.getId() : null);
        product.setServiceProviderId(Long.parseLong(userId));
        
        // Don't set imageURLs in the product DTO for multipart request
        // Images will be sent as separate parts

        // Debug log
        android.util.Log.d("AddProduct", "Creating product: " + product.toString());

        // Create CreateProductDTO like Angular does
        CreateProductDTO createProductDTO = new CreateProductDTO();
        createProductDTO.setName(product.getName());
        createProductDTO.setDescription(product.getDescription());
        createProductDTO.setPrice(product.getPrice());
        createProductDTO.setDiscount(product.getDiscount());
        createProductDTO.setAvailable(product.getAvailable());
        
        // Check if we're using a new category (pending approval)
        boolean isNewCategory = isUsingNewCategory(product.getCategoryId());
        createProductDTO.setVisible(!isNewCategory); // Invisible if using new category like Angular
        
        createProductDTO.setProviderId(Long.parseLong(userId));
        createProductDTO.setCategoryId(product.getCategoryId());
        
        // Convert event types to list of Long IDs
        List<Long> eventTypeIds = new ArrayList<>();
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            for (EventTypeDTO eventType : selectedEventTypes) {
                eventTypeIds.add(eventType.getId());
            }
        }
        createProductDTO.setEventTypes(eventTypeIds);
        
        // Ensure we have at least one event type (required by backend)
        if (eventTypeIds.isEmpty() && eventTypes != null && !eventTypes.isEmpty()) {
            // If no event types selected, use the first available one
            eventTypeIds.add(eventTypes.get(0).getId());
            createProductDTO.setEventTypes(eventTypeIds);
        }
        
        // Convert to JSON like Angular does
        String dtoJson = new Gson().toJson(createProductDTO);
        RequestBody dtoBody = RequestBody.create(
                MediaType.parse("application/json"), dtoJson);
        
        // Debug log
        android.util.Log.d("AddProduct", "CreateProductDTO: " + dtoJson);
        
        // Show warning if using new category
        if (isNewCategory) {
            Toast.makeText(this, "Product will be invisible until the new category is approved by admin.", Toast.LENGTH_LONG).show();
        }

        // Create multipart files from selected images - using ProfileActivity approach
        List<MultipartBody.Part> fileParts = new ArrayList<>();
        if (selectedImages != null && !selectedImages.isEmpty()) {
            // Convert URI strings to Bitmaps and create multipart parts
            List<Bitmap> bitmaps = new ArrayList<>();
            for (int i = 0; i < selectedImages.size(); i++) {
                try {
                    Uri imageUri = Uri.parse(selectedImages.get(i));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    bitmaps.add(bitmap);
                } catch (IOException e) {
                    android.util.Log.e("AddProduct", "Error loading image: " + e.getMessage());
                }
            }
            
            // Use the same approach as ProfileActivity
            if (!bitmaps.isEmpty()) {
                fileParts = MultipartHelper.createMultipartList(bitmaps);
            }
        }

        // Debug: Log the authorization header
        String authHeader = "Bearer " + token;
        android.util.Log.d("AddProduct", "Authorization header: " + authHeader);
        android.util.Log.d("AddProduct", "Token length: " + (token != null ? token.length() : "null"));
        android.util.Log.d("AddProduct", "User ID: " + userId);
        android.util.Log.d("AddProduct", "User Role: " + userRole);
        
        // Debug: Try to decode JWT token to see what role it contains
        if (token != null && token.contains(".")) {
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    // Decode the payload (second part)
                    String payload = parts[1];
                    // Add padding if needed
                    while (payload.length() % 4 != 0) {
                        payload += "=";
                    }
                    byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                    String decodedPayload = new String(decodedBytes);
                    android.util.Log.d("AddProduct", "JWT Payload: " + decodedPayload);
                }
            } catch (Exception e) {
                android.util.Log.e("AddProduct", "Error decoding JWT: " + e.getMessage());
            }
        }

        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        productService.createProduct(authHeader, dtoBody,
                fileParts.toArray(new MultipartBody.Part[0])).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to add product";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += " (Error code: " + response.code() + ")";
                        }
                    }
                    Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddProductActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        if (etProductName.getText().toString().trim().isEmpty()) {
            etProductName.setError("Product name is required");
            return false;
        }
        
        if (etProductDescription.getText().toString().trim().isEmpty()) {
            etProductDescription.setError("Product description is required");
            return false;
        }
        
        if (etProductPrice.getText().toString().trim().isEmpty()) {
            etProductPrice.setError("Product price is required");
            return false;
        }
        
        try {
            double price = Double.parseDouble(etProductPrice.getText().toString().trim());
            if (price < 0) {
                etProductPrice.setError("Price must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            etProductPrice.setError("Invalid price format");
            return false;
        }
        
        String discountText = etProductDiscount.getText().toString().trim();
        if (!discountText.isEmpty()) {
            try {
                double discount = Double.parseDouble(discountText);
                if (discount < 0 || discount > 100) {
                    etProductDiscount.setError("Discount must be between 0 and 100");
                    return false;
                }
            } catch (NumberFormatException e) {
                etProductDiscount.setError("Invalid discount format");
                return false;
            }
        }
        
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddProduct.setEnabled(!show);
    }

    private void setupButtons() {
        btnAddProduct.setOnClickListener(v -> {
            if (validateInput()) {
                addProduct();
            }
        });

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        btnAddEventType.setOnClickListener(v -> showEventTypeSelectionDialog());
        btnAddImage.setOnClickListener(v -> selectImage());
    }

    private void showAddCategoryDialog() {
        // Create dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        // Get views
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            String description = etCategoryDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etCategoryName.setError("Category name is required");
                return;
            }

            if (description.isEmpty()) {
                etCategoryDescription.setError("Category description is required");
                return;
            }

            createCategory(name, description);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createCategory(String name, String description) {
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO();
        createCategoryDTO.name = name;
        createCategoryDTO.description = description;
        createCategoryDTO.isApprovedByAdmin = false; // Will be pending approval

        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.createCategory("Bearer " + token, createCategoryDTO).enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    CategoryDTO newCategory = response.body();
                    categories.add(newCategory);
                    updateCategorySpinner();
                    Toast.makeText(AddProductActivity.this, "Category created successfully! It will be pending admin approval. Products with this category will be invisible until approved.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddProductActivity.this, "Failed to create category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddProductActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner() {
        if (categories != null && !categories.isEmpty()) {
            List<String> categoryNames = new ArrayList<>();
            for (CategoryDTO category : categories) {
                categoryNames.add(category.getName());
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categoryNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
        }
    }

    private void showEventTypeSelectionDialog() {
        if (eventTypes == null || eventTypes.isEmpty()) {
            Toast.makeText(this, "No event types available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_event_types, null);
        builder.setView(dialogView);

        // Setup RecyclerView
        RecyclerView rvEventTypes = dialogView.findViewById(R.id.rvEventTypes);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSelect = dialogView.findViewById(R.id.btnSelect);

        // Create a copy of selected event types for the dialog
        List<EventTypeDTO> tempSelectedEventTypes = new ArrayList<>(selectedEventTypes);
        EventTypeCheckboxAdapter adapter = new EventTypeCheckboxAdapter(eventTypes, tempSelectedEventTypes);
        rvEventTypes.setLayoutManager(new LinearLayoutManager(this));
        rvEventTypes.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSelect.setOnClickListener(v -> {
            selectedEventTypes.clear();
            selectedEventTypes.addAll(tempSelectedEventTypes);
            eventTypeChipAdapter.updateEventTypes(selectedEventTypes);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void selectImage() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        
        Intent chooserIntent = Intent.createChooser(intent, "Select Images");
        imagePickerLauncher.launch(chooserIntent);
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images selected
            int count = data.getClipData().getItemCount();
            
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                selectedImages.add(imageUri.toString());
            }
            
            if (!selectedImages.isEmpty()) {
                imageAdapter.updateImages(selectedImages);
                Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            selectedImages.add(imageUri.toString());
            imageAdapter.updateImages(selectedImages);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
