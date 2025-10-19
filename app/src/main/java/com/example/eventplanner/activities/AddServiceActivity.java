package com.example.eventplanner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.LinearLayout;
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

    // Form views
    private EditText etServiceName, etServiceDescription, etServicePrice, etServiceDiscount;
    private EditText etDurationHours, etDurationMinutes, etMinEngagement, etMaxEngagement;
    private EditText etReservationDue, etCancellationDue;
    private Spinner spinnerEventTypes;
    private RadioGroup rgReservationType, rgDurationType;
    private RadioButton rbFixedDuration, rbEngagementRange;
    private LinearLayout layoutFixedDuration, layoutEngagementRange, layoutSelectedEventTypes, layoutCategoryCheckboxes;
    private CheckBox cbAvailable;
    private Button btnUploadImages, btnAddCategory, btnSave, btnCancel;
    private TextView tvImageCount, tvSelectedEventTypes, tvCategoryInfo, tvCategoryInstruction;

    // Data
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<CategoryDTO> suggestedCategories = new ArrayList<>();
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
        etDurationHours = findViewById(R.id.etDurationHours);
        etDurationMinutes = findViewById(R.id.etDurationMinutes);
        etMinEngagement = findViewById(R.id.etMinEngagement);
        etMaxEngagement = findViewById(R.id.etMaxEngagement);
        etReservationDue = findViewById(R.id.etReservationDue);
        etCancellationDue = findViewById(R.id.etCancellationDue);
        
        layoutCategoryCheckboxes = findViewById(R.id.layoutCategoryCheckboxes);
        spinnerEventTypes = findViewById(R.id.spinnerEventTypes);
        
        rgReservationType = findViewById(R.id.rgReservationType);
        rgDurationType = findViewById(R.id.rgDurationType);
        rbFixedDuration = findViewById(R.id.rbFixedDuration);
        rbEngagementRange = findViewById(R.id.rbEngagementRange);
        layoutFixedDuration = findViewById(R.id.layoutFixedDuration);
        layoutEngagementRange = findViewById(R.id.layoutEngagementRange);
        layoutSelectedEventTypes = findViewById(R.id.layoutSelectedEventTypes);
        cbAvailable = findViewById(R.id.cbAvailable);
        
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        
        tvImageCount = findViewById(R.id.tvImageCount);
        tvSelectedEventTypes = findViewById(R.id.tvSelectedEventTypes);
        tvCategoryInfo = findViewById(R.id.tvCategoryInfo);
        tvCategoryInstruction = findViewById(R.id.tvCategoryInstruction);
    }

    private void setupListeners() {
        btnUploadImages.setOnClickListener(v -> selectImages());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> finish());
        
        // Duration type radio button listener
        rgDurationType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbFixedDuration) {
                    layoutFixedDuration.setVisibility(View.VISIBLE);
                    layoutEngagementRange.setVisibility(View.GONE);
                } else if (checkedId == R.id.rbEngagementRange) {
                    layoutFixedDuration.setVisibility(View.GONE);
                    layoutEngagementRange.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadCategories() {
        CategoryService categoryService = ApiClient.getClient(this).create(CategoryService.class);
        categoryService.getAllCategories(getAuthHeader()).enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    setupCategoryCheckboxes();
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

    private void setupCategoryCheckboxes() {
        layoutCategoryCheckboxes.removeAllViews();
        categoryCheckboxes.clear();
        
        for (CategoryDTO category : suggestedCategories) {
            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(category.name);
            checkbox.setTag(category);
            checkbox.setTextSize(16);
            checkbox.setTextColor(getResources().getColor(R.color.text_primary));
            checkbox.setPadding(8, 8, 8, 8);
            
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                CategoryDTO cat = (CategoryDTO) buttonView.getTag();
                if (isChecked) {
                    for (CheckBox otherCheckbox : categoryCheckboxes) {
                        if (otherCheckbox != checkbox) {
                            otherCheckbox.setChecked(false);
                        }
                    }
                    selectedCategory = cat;
                } else {
                    selectedCategory = null;
                }
                updateAddCategoryButtonState();
            });
            
            categoryCheckboxes.add(checkbox);
            layoutCategoryCheckboxes.addView(checkbox);
        }
        
        if (selectedEventTypes.isEmpty()) {
            for (CheckBox checkbox : categoryCheckboxes) {
                checkbox.setEnabled(false);
            }
            btnAddCategory.setEnabled(false);
            tvCategoryInstruction.setText("Please select an event type first to see its categories");
            tvCategoryInstruction.setVisibility(View.VISIBLE);
        } else {
            for (CheckBox checkbox : categoryCheckboxes) {
                checkbox.setEnabled(true);
            }
            updateAddCategoryButtonState();
            tvCategoryInstruction.setVisibility(View.GONE);
        }
        
        updateCategoryInfo();
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
        
        spinnerEventTypes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    EventTypeDTO selectedEventType = eventTypes.get(position - 1);
                    
                    if (!selectedEventTypes.contains(selectedEventType)) {
                        selectedEventTypes.add(selectedEventType);
                        updateSelectedEventTypesDisplay();
                        updateSuggestedCategories();
                        spinnerEventTypes.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }


    private void updateSelectedEventTypesDisplay() {
        layoutSelectedEventTypes.removeAllViews();
        
        if (selectedEventTypes.isEmpty()) {
            tvSelectedEventTypes.setText("No event types selected");
            tvSelectedEventTypes.setVisibility(View.VISIBLE);
        } else {
            tvSelectedEventTypes.setVisibility(View.GONE);
            
            for (EventTypeDTO eventType : selectedEventTypes) {
                // Create a card-like layout for each event type
                LinearLayout eventTypeCard = new LinearLayout(this);
                eventTypeCard.setOrientation(LinearLayout.HORIZONTAL);
                eventTypeCard.setPadding(16, 12, 16, 12);
                eventTypeCard.setBackgroundResource(R.drawable.button_secondary);
                
                // Set full width
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 8);
                eventTypeCard.setLayoutParams(cardParams);
                
                // Event type name
                TextView eventTypeName = new TextView(this);
                eventTypeName.setText(eventType.getName());
                eventTypeName.setTextSize(16);
                eventTypeName.setTextColor(getResources().getColor(R.color.text_primary));
                eventTypeName.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                ));
                
                // Remove button
                Button removeButton = new Button(this);
                removeButton.setText("✕");
                removeButton.setTextSize(18);
                removeButton.setTextColor(getResources().getColor(R.color.text_secondary));
                removeButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                removeButton.setPadding(8, 8, 8, 8);
                removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                
                removeButton.setOnClickListener(v -> removeEventType(eventType));
                
                eventTypeCard.addView(eventTypeName);
                eventTypeCard.addView(removeButton);
                layoutSelectedEventTypes.addView(eventTypeCard);
            }
        }
    }

    private void removeEventType(EventTypeDTO eventType) {
        selectedEventTypes.remove(eventType);
        updateSelectedEventTypesDisplay();
        updateSuggestedCategories();
    }

    private void updateSuggestedCategories() {
        suggestedCategories.clear();
        
        for (EventTypeDTO eventType : selectedEventTypes) {
            if (eventType.getSuggestedCategories() != null) {
                for (CategoryDTO category : eventType.getSuggestedCategories()) {
                    if (!suggestedCategories.contains(category)) {
                        suggestedCategories.add(category);
                    }
                }
            }
        }
        
        setupCategoryCheckboxes();
        updateCategoryInfo();
    }

    private void updateCategoryInfo() {
        if (selectedEventTypes.isEmpty()) {
            tvCategoryInfo.setText("Select event type first");
        } else if (suggestedCategories.isEmpty()) {
            tvCategoryInfo.setText("No categories available for selected event types");
        } else {
            tvCategoryInfo.setText(suggestedCategories.size() + " categories available");
        }
    }

    private void updateAddCategoryButtonState() {
        if (selectedEventTypes.isEmpty()) {
            btnAddCategory.setEnabled(false);
        } else {
            // Only enable "Add Category" button if no category is selected
            btnAddCategory.setEnabled(selectedCategory == null);
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnCancelCategory = dialogView.findViewById(R.id.btnCancelCategory);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

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
                    
                    // Add to suggested categories if event types are selected
                    if (!selectedEventTypes.isEmpty()) {
                        suggestedCategories.add(createdCategory);
                        setupCategoryCheckboxes();
                        
                        for (CheckBox checkbox : categoryCheckboxes) {
                            CategoryDTO cat = (CategoryDTO) checkbox.getTag();
                            if (cat.id.equals(createdCategory.id)) {
                                checkbox.setChecked(true);
                                selectedCategory = createdCategory;
                                break;
                            }
                        }
                        
                        updateAddCategoryButtonState();
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
                Toast.makeText(AddServiceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Single image selected
                selectedImageUris.add(data.getData());
            }
            
            tvImageCount.setText(selectedImageUris.size() + " image(s) selected");
        }
    }

    private void saveService() {
        if (!validateForm()) {
            return;
        }

        // Create service DTO
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
        
        // Event types
        List<Long> eventTypeIds = new ArrayList<>();
        for (EventTypeDTO eventType : selectedEventTypes) {
            eventTypeIds.add(eventType.getId());
        }
        dto.setEventTypes(eventTypeIds);
        
        // Duration and Engagement based on selected type
        if (rbFixedDuration.isChecked()) {
            // Fixed duration
            String hoursStr = etDurationHours.getText().toString().trim();
            String minutesStr = etDurationMinutes.getText().toString().trim();
            if (!TextUtils.isEmpty(hoursStr) || !TextUtils.isEmpty(minutesStr)) {
                int hours = TextUtils.isEmpty(hoursStr) ? 0 : Integer.parseInt(hoursStr);
                int minutes = TextUtils.isEmpty(minutesStr) ? 0 : Integer.parseInt(minutesStr);
                dto.setDuration(hours * 60 + minutes);
            }
            // Set default engagement values for fixed duration (backend expects these)
            dto.setMinEngagement(null);
            dto.setMaxEngagement(null);
        } else if (rbEngagementRange.isChecked()) {
            // Engagement range
            String minEngStr = etMinEngagement.getText().toString().trim();
            String maxEngStr = etMaxEngagement.getText().toString().trim();
            if (!TextUtils.isEmpty(minEngStr)) {
                dto.setMinEngagement(Integer.parseInt(minEngStr));
            } else {
                dto.setMinEngagement(1); // Default minimum engagement
            }
            if (!TextUtils.isEmpty(maxEngStr)) {
                dto.setMaxEngagement(Integer.parseInt(maxEngStr));
            } else {
                dto.setMaxEngagement(5); // Default maximum engagement
            }
            dto.setDuration(null);
        } else {
            dto.setDuration(60);
            dto.setMinEngagement(1);
            dto.setMaxEngagement(1);
        }
        
        // Reservation/Cancellation due
        String resDueStr = etReservationDue.getText().toString().trim();
        String cancelDueStr = etCancellationDue.getText().toString().trim();
        dto.setReservationDue(TextUtils.isEmpty(resDueStr) ? 0 : Integer.parseInt(resDueStr));
        dto.setCancelationDue(TextUtils.isEmpty(cancelDueStr) ? 0 : Integer.parseInt(cancelDueStr));
        
        // Reservation type
        int selectedId = rgReservationType.getCheckedRadioButtonId();
        dto.setReservationType(selectedId == R.id.rbAutomatic ? "AUTOMATIC" : "MANUAL");
        
        // Provider ID
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
                // Handle error silently
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
        if (selectedCategory == null) {
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
