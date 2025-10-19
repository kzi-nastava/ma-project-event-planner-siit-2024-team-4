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
        if ("SPProvider".equals(userRole)) {
            userRole = "SERVICE_PROVIDER";
        }
        return "SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole);
    }
    
    private boolean isUsingNewCategory(Long categoryId) {
        if (categoryId == null || categories == null) {
            return false;
        }
        
        for (CategoryDTO category : categories) {
            if (category.getId().equals(categoryId)) {
                return !category.isApprovedByAdmin();
            }
        }
        return false;
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        token = prefs.getString("jwt_token", null);
        Long userIdLong = prefs.getLong("user_id", -1L);
        userId = userIdLong != -1L ? userIdLong.toString() : null;
        
        selectedEventTypes = new ArrayList<>();
        selectedImages = new ArrayList<>();
    }

    private void setupAdapters() {
        eventTypeChipAdapter = new EventTypeChipAdapter(selectedEventTypes, eventType -> {
            selectedEventTypes.remove(eventType);
            eventTypeChipAdapter.updateEventTypes(selectedEventTypes);
        });
        rvEventTypes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEventTypes.setAdapter(eventTypeChipAdapter);

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
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    setupCategorySpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventTypes() {
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Failed to load event types", Toast.LENGTH_SHORT).show();
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

        CategoryDTO selectedCategory = null;
        EventTypeDTO selectedEventType = null;
        
        if (categories != null && spinnerCategory.getSelectedItemPosition() >= 0) {
            selectedCategory = categories.get(spinnerCategory.getSelectedItemPosition());
        }
        
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            selectedEventType = selectedEventTypes.get(0);
        }

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

        CreateProductDTO createProductDTO = new CreateProductDTO();
        createProductDTO.setName(product.getName());
        createProductDTO.setDescription(product.getDescription());
        createProductDTO.setPrice(product.getPrice());
        createProductDTO.setDiscount(product.getDiscount());
        createProductDTO.setAvailable(product.getAvailable());
        
        boolean isNewCategory = isUsingNewCategory(product.getCategoryId());
        createProductDTO.setVisible(!isNewCategory);
        
        createProductDTO.setProviderId(Long.parseLong(userId));
        createProductDTO.setCategoryId(product.getCategoryId());
        
        List<Long> eventTypeIds = new ArrayList<>();
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            for (EventTypeDTO eventType : selectedEventTypes) {
                eventTypeIds.add(eventType.getId());
            }
        }
        createProductDTO.setEventTypes(eventTypeIds);
        
        if (eventTypeIds.isEmpty() && eventTypes != null && !eventTypes.isEmpty()) {
            eventTypeIds.add(eventTypes.get(0).getId());
            createProductDTO.setEventTypes(eventTypeIds);
        }
        
        String dtoJson = new Gson().toJson(createProductDTO);
        RequestBody dtoBody = RequestBody.create(
                MediaType.parse("application/json"), dtoJson);
        
        if (isNewCategory) {
            Toast.makeText(this, "Product will be invisible until the new category is approved by admin.", Toast.LENGTH_LONG).show();
        }

        List<MultipartBody.Part> fileParts = new ArrayList<>();
        if (selectedImages != null && !selectedImages.isEmpty()) {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (int i = 0; i < selectedImages.size(); i++) {
                try {
                    Uri imageUri = Uri.parse(selectedImages.get(i));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    bitmaps.add(bitmap);
                } catch (IOException e) {
                    Toast.makeText(AddProductActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
            
            if (!bitmaps.isEmpty()) {
                fileParts = MultipartHelper.createMultipartList(bitmaps);
            }
        }

        String authHeader = "Bearer " + token;
        

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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

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
        createCategoryDTO.isApprovedByAdmin = false;

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

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_event_types, null);
        builder.setView(dialogView);

        RecyclerView rvEventTypes = dialogView.findViewById(R.id.rvEventTypes);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSelect = dialogView.findViewById(R.id.btnSelect);

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
            Uri imageUri = data.getData();
            selectedImages.add(imageUri.toString());
            imageAdapter.updateImages(selectedImages);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
