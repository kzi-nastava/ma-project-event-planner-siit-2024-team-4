package com.example.eventplanner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.CreateCategoryDTO;
import com.example.eventplanner.dto.CreateServiceDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.CategoryService;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.ServiceService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddServiceActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;

    private EditText etServiceName, etServiceDescription, etServicePrice, etServiceDiscount;
    private EditText etDuration, etMinEngagement, etMaxEngagement;
    private EditText etReservationDue, etCancellationDue;
    private Spinner spinnerCategory, spinnerEventTypes;
    private RadioGroup rgReservationType;
    private CheckBox cbAvailable, cbVisible;
    private Button btnUploadImages, btnAddEventType, btnSave, btnCancel;
    private TextView tvImageCount, tvSelectedEventTypes;

    private List<CategoryDTO> categories = new ArrayList<>();
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    private List<EventTypeDTO> selectedEventTypes = new ArrayList<>();
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<CheckBox> categoryCheckboxes = new ArrayList<>();
    private CategoryDTO selectedCategory = null;
    boolean isVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);
        setTitle("Add Service");
        
        initViews();
        loadCategories();
        loadEventTypes();
        setupListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServicePrice = findViewById(R.id.etServicePrice);
        etServiceDiscount = findViewById(R.id.etServiceDiscount);
        etDuration = findViewById(R.id.etDuration);
        etMinEngagement = findViewById(R.id.etMinEngagement);
        etMaxEngagement = findViewById(R.id.etMaxEngagement);
        etReservationDue = findViewById(R.id.etReservationDue);
        etCancellationDue = findViewById(R.id.etCancellationDue);
        
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerEventTypes = findViewById(R.id.spinnerEventTypes);
        
        rgReservationType = findViewById(R.id.rgReservationType);
        cbAvailable = findViewById(R.id.cbAvailable);
        cbVisible = findViewById(R.id.cbVisible);
        
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnAddEventType = findViewById(R.id.btnAddEventType);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        
        tvImageCount = findViewById(R.id.tvImageCount);
        tvSelectedEventTypes = findViewById(R.id.tvSelectedEventTypes);
    }

    private void setupListeners() {
        btnUploadImages.setOnClickListener(v -> selectImages());
        btnAddEventType.setOnClickListener(v -> addEventType());
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    setupCategorySpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Toast.makeText(AddServiceActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventTypes() {
        EventTypeService eventTypeService = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeService.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(AddServiceActivity.this, "Error loading event types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category");
        for (CategoryDTO category : categories) {
            if (category.isApprovedByAdmin) {
                categoryNames.add(category.name);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("Select Event Type");
        for (EventTypeDTO eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventTypes.setAdapter(adapter);
    }

    private void addEventType() {
        int position = spinnerEventTypes.getSelectedItemPosition();
        if (position > 0) {
            EventTypeDTO selectedEventType = eventTypes.get(position - 1);
            if (!selectedEventTypes.contains(selectedEventType)) {
                selectedEventTypes.add(selectedEventType);
                updateSelectedEventTypesDisplay();
            }
        }
    }

    private void updateSelectedEventTypesDisplay() {
        if (selectedEventTypes.isEmpty()) {
            tvSelectedEventTypes.setText("Selected: None");
        } else {
            StringBuilder sb = new StringBuilder("Selected: ");
            for (int i = 0; i < selectedEventTypes.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(selectedEventTypes.get(i).getName());
            }
            tvSelectedEventTypes.setText(sb.toString());
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnCancelCategory = dialogView.findViewById(R.id.btnCancel);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAdd);

        AlertDialog dialog = builder.create();

        btnCancelCategory.setOnClickListener(v -> dialog.dismiss());

        btnAddCategory.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();
            String categoryDescription = etCategoryDescription.getText().toString().trim();

            if (TextUtils.isEmpty(categoryName)) {
                etCategoryName.setError("Category name is required");
                return;
            }

            if (TextUtils.isEmpty(categoryDescription)) {
                etCategoryDescription.setError("Category description is required");
                return;
            }

            createNewCategory(categoryName, categoryDescription);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createNewCategory(String name, String description) {
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        
        CreateCategoryDTO newCategory = new CreateCategoryDTO();
        newCategory.name = name;
        newCategory.description = description;
        newCategory.isApprovedByAdmin = false; // New categories need admin approval

        categoryService.createCategory(getAuthHeader(), newCategory).enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryDTO createdCategory = response.body();
                    categories.add(createdCategory);
                    
                    // Update the category spinner adapter
                    ArrayAdapter<CategoryDTO> adapter = (ArrayAdapter<CategoryDTO>) spinnerCategory.getAdapter();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    isVisible = false;
                    Toast.makeText(AddServiceActivity.this, "Category created successfully and added to selected event types", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddServiceActivity.this, "Error creating category", Toast.LENGTH_SHORT).show();
                    isVisible = true;
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                Toast.makeText(AddServiceActivity.this, "Error creating category: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isVisible = true;
            }
        });
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();
            
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            
            tvImageCount.setText(selectedImageUris.size() + " image(s) selected");
        }
    }

    private void saveService() {
        if (!validateForm()) {
            return;
        }

        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setName(etServiceName.getText().toString().trim());
        dto.setDescription(etServiceDescription.getText().toString().trim());
        dto.setPrice(Double.parseDouble(etServicePrice.getText().toString().trim()));
        
        String discountStr = etServiceDiscount.getText().toString().trim();
        dto.setDiscount(TextUtils.isEmpty(discountStr) ? 0 : Double.parseDouble(discountStr));
        
        dto.setAvailable(cbAvailable.isChecked());
        dto.setVisible(isVisible);
        
        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        } else {
            dto.setCategoryId(selectedCategory.id);
        }
        
        List<Long> eventTypeIds = new ArrayList<>();
        for (EventTypeDTO eventType : selectedEventTypes) {
            eventTypeIds.add(eventType.getId());
        }
        dto.setEventTypes(eventTypeIds);
        
        String durationStr = etDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationStr)) {
            dto.setDuration(Integer.parseInt(durationStr));
        }
        
        String minEngStr = etMinEngagement.getText().toString().trim();
        String maxEngStr = etMaxEngagement.getText().toString().trim();
        if (!TextUtils.isEmpty(minEngStr)) {
            dto.setMinEngagement(Integer.parseInt(minEngStr));
        }
        if (!TextUtils.isEmpty(maxEngStr)) {
            dto.setMaxEngagement(Integer.parseInt(maxEngStr));
        }
        
        String resDueStr = etReservationDue.getText().toString().trim();
        String cancelDueStr = etCancellationDue.getText().toString().trim();
        dto.setReservationDue(TextUtils.isEmpty(resDueStr) ? 0 : Integer.parseInt(resDueStr));
        dto.setCancelationDue(TextUtils.isEmpty(cancelDueStr) ? 0 : Integer.parseInt(cancelDueStr));
        
        int selectedId = rgReservationType.getCheckedRadioButtonId();
        dto.setReservationType(selectedId == R.id.rbAutomatic ? "AUTOMATIC" : "MANUAL");
        
        dto.setProviderId(getCurrentUserId());
        
        Gson gson = new Gson();
        String dtoJson = gson.toJson(dto);
        RequestBody dtoBody = RequestBody.create(MediaType.parse("application/json"), dtoJson);
        
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                File file = createTempFileFromUri(uri);
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part part = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
                imageParts.add(part);
            } catch (IOException e) {
                Toast.makeText(AddServiceActivity.this, "Error creating file from URI", Toast.LENGTH_SHORT).show();
            }
        }
        
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        serviceAPI.createService(getAuthHeader(), dtoBody, imageParts).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddServiceActivity.this, "Service created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddServiceActivity.this, "Error creating service: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Toast.makeText(AddServiceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etServiceName.getText())) {
            etServiceName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etServiceDescription.getText())) {
            etServiceDescription.setError("Description is required");
            return false;
        }
        if (TextUtils.isEmpty(etServicePrice.getText())) {
            etServicePrice.setError("Price is required");
            return false;
        }
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outputStream.close();
        
        return tempFile;
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getLong("user_id", -1L);
    }

    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }
}
