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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventTypeCheckboxAdapter;
import com.example.eventplanner.adapters.EventTypeChipAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.UpdateProductDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.MultipartHelper;
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

public class EditProductActivity extends BaseActivity {

    private EditText etProductName;
    private EditText etProductDescription;
    private EditText etProductPrice;
    private EditText etProductDiscount;
    private CheckBox cbProductAvailable;
    private RecyclerView rvEventTypes;
    private Button btnAddEventType;
    private RecyclerView rvCurrentImages;
    private RecyclerView rvNewImages;
    private Button btnAddImage;
    private Button btnCancel;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    private ProductDTO product;
    private List<EventTypeDTO> eventTypes;
    private List<EventTypeDTO> selectedEventTypes;
    private List<String> currentImageURLs;
    private List<String> removedImageURLs;
    private List<String> newImageURIs;
    private EventTypeChipAdapter eventTypeChipAdapter;
    private ImageAdapter currentImageAdapter;
    private ImageAdapter newImageAdapter;
    private String token;
    private String userId;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_edit_product, contentFrame, true);

        product = (ProductDTO) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initImagePicker();
        loadUserInfo();
        loadEventTypes();
        setupAdapters();
        setupButtons();
        populateFields();
    }

    private void initViews() {
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductDiscount = findViewById(R.id.etProductDiscount);
        cbProductAvailable = findViewById(R.id.cbProductAvailable);
        rvEventTypes = findViewById(R.id.rvEventTypes);
        btnAddEventType = findViewById(R.id.btnAddEventType);
        rvCurrentImages = findViewById(R.id.rvCurrentImages);
        rvNewImages = findViewById(R.id.rvNewImages);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
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

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        token = prefs.getString("jwt_token", null);
        userId = prefs.getString("user_id", null);
        
        selectedEventTypes = new ArrayList<>();
        currentImageURLs = new ArrayList<>();
        removedImageURLs = new ArrayList<>();
        newImageURIs = new ArrayList<>();
        
        if (product.getImageURLs() != null) {
            currentImageURLs.addAll(product.getImageURLs());
        }
    }

    private void setupAdapters() {
        eventTypeChipAdapter = new EventTypeChipAdapter(selectedEventTypes, eventType -> {
            selectedEventTypes.remove(eventType);
            eventTypeChipAdapter.updateEventTypes(selectedEventTypes);
        });
        rvEventTypes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEventTypes.setAdapter(eventTypeChipAdapter);

        currentImageAdapter = new ImageAdapter(currentImageURLs, position -> {
            String removedURL = currentImageURLs.remove(position);
            removedImageURLs.add(removedURL);
            currentImageAdapter.updateImages(currentImageURLs);
        });
        rvCurrentImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCurrentImages.setAdapter(currentImageAdapter);

        newImageAdapter = new ImageAdapter(newImageURIs, position -> {
            newImageURIs.remove(position);
            newImageAdapter.updateImages(newImageURIs);
        });
        rvNewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNewImages.setAdapter(newImageAdapter);
    }

    private void loadEventTypes() {
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes("Bearer " + token).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    if (product.getEventTypes() != null) {
                        for (EventTypeDTO productEventType : product.getEventTypes()) {
                            for (EventTypeDTO availableEventType : eventTypes) {
                                if (productEventType.getId().equals(availableEventType.getId())) {
                                    selectedEventTypes.add(availableEventType);
                                    break;
                                }
                            }
                        }
                    }
                    eventTypeChipAdapter.updateEventTypes(selectedEventTypes);
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(EditProductActivity.this, "Failed to load event types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        etProductName.setText(product.getName());
        etProductDescription.setText(product.getDescription());
        if (product.getPrice() != null) {
            etProductPrice.setText(product.getPrice().toString());
        }
        if (product.getDiscount() != null) {
            etProductDiscount.setText(product.getDiscount().toString());
        }
        cbProductAvailable.setChecked(product.getAvailable() != null ? product.getAvailable() : true);
        
        currentImageAdapter.updateImages(currentImageURLs);
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> finish());
        
        btnSaveChanges.setOnClickListener(v -> {
            if (validateInput()) {
                saveChanges();
            }
        });

        btnAddEventType.setOnClickListener(v -> showEventTypeSelectionDialog());
        btnAddImage.setOnClickListener(v -> selectImage());
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
                newImageURIs.add(imageUri.toString());
            }
            
            if (!newImageURIs.isEmpty()) {
                newImageAdapter.updateImages(newImageURIs);
                Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
            }
        } else if (data.getData() != null) {
            Uri imageUri = data.getData();
            newImageURIs.add(imageUri.toString());
            newImageAdapter.updateImages(newImageURIs);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
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

    private void saveChanges() {
        showLoading(true);

        UpdateProductDTO updateProductDTO = new UpdateProductDTO();
        updateProductDTO.setProductId(product.getId());
        updateProductDTO.setName(etProductName.getText().toString().trim());
        updateProductDTO.setDescription(etProductDescription.getText().toString().trim());
        updateProductDTO.setPrice(Double.parseDouble(etProductPrice.getText().toString().trim()));
        
        String discountText = etProductDiscount.getText().toString().trim();
        if (!discountText.isEmpty()) {
            updateProductDTO.setDiscount(Double.parseDouble(discountText));
        } else {
            updateProductDTO.setDiscount(0.0);
        }
        
        updateProductDTO.setAvailable(cbProductAvailable.isChecked());
        updateProductDTO.setVisible(true);
        updateProductDTO.setProviderId(Long.parseLong(userId));
        updateProductDTO.setCategoryId(product.getCategoryId());
        
        List<Long> eventTypeIds = new ArrayList<>();
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            for (EventTypeDTO eventType : selectedEventTypes) {
                eventTypeIds.add(eventType.getId());
            }
        }
        updateProductDTO.setEventTypes(eventTypeIds);
        
        List<String> finalImageURLs = new ArrayList<>();
        for (String url : currentImageURLs) {
            if (!removedImageURLs.contains(url)) {
                finalImageURLs.add(url);
            }
        }
        updateProductDTO.setImageURLs(finalImageURLs);
        
        String dtoJson = new Gson().toJson(updateProductDTO);
        RequestBody dtoBody = RequestBody.create(
                MediaType.parse("application/json"), dtoJson);
        

        List<MultipartBody.Part> fileParts = new ArrayList<>();
        if (newImageURIs != null && !newImageURIs.isEmpty()) {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (int i = 0; i < newImageURIs.size(); i++) {
                try {
                    Uri imageUri = Uri.parse(newImageURIs.get(i));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    bitmaps.add(bitmap);
                } catch (IOException e) {
                    Toast.makeText(EditProductActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
            
            if (!bitmaps.isEmpty()) {
                fileParts = MultipartHelper.createMultipartList(bitmaps);
            }
        }

        String authHeader = "Bearer " + token;
        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        productService.updateProduct(authHeader, product.getId(), dtoBody,
                fileParts.toArray(new MultipartBody.Part[0])).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(EditProductActivity.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to update product";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += " (Error code: " + response.code() + ")";
                        }
                    }
                    Toast.makeText(EditProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveChanges.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }
}
