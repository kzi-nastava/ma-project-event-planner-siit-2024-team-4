package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CategoryDTO;
import com.example.eventplanner.dto.EventTypeDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.UpdateServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.MultipartHelper;
import com.example.eventplanner.network.service.EventTypeService;
import com.example.eventplanner.network.service.ServiceService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditServiceActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;

    private Long serviceId;
    private ServiceDTO currentService;

    private EditText etServiceName, etServiceDescription, etServicePrice, etServiceDiscount;
    private EditText etDurationHours, etDurationMinutes, etMinEngagement, etMaxEngagement;
    private EditText etReservationDue, etCancellationDue;
    private TextView tvCategory;
    private Spinner spinnerEventTypes;
    private RadioGroup rgDurationType;
    private RadioButton rbFixedDuration;
    private RadioButton rbEngagementRange;
    private LinearLayout layoutFixedDuration;
    private LinearLayout layoutEngagementRange;
    private CheckBox cbAvailable;
    private Button btnUploadImages, btnSave, btnCancel;
    private TextView tvImageCount, tvCurrentImages;

    // Data
    private List<EventTypeDTO> eventTypes = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();
    private List<Bitmap> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);
        setTitle("Edit Service");
        
        serviceId = getIntent().getLongExtra("serviceId", -1L);
        if (serviceId == -1L) {
            Toast.makeText(this, "Service ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadServiceData();
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
        
        tvCategory = findViewById(R.id.tvCategory);
        spinnerEventTypes = findViewById(R.id.spinnerEventTypes);
        rgDurationType = findViewById(R.id.rgDurationType);
        rbFixedDuration = findViewById(R.id.rbFixedDuration);
        rbEngagementRange = findViewById(R.id.rbEngagementRange);
        layoutFixedDuration = findViewById(R.id.layoutFixedDuration);
        layoutEngagementRange = findViewById(R.id.layoutEngagementRange);
        cbAvailable = findViewById(R.id.cbAvailable);
        
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        
        tvImageCount = findViewById(R.id.tvImageCount);
        tvCurrentImages = findViewById(R.id.tvCurrentImages);
    }

    private void setupListeners() {
        btnUploadImages.setOnClickListener(v -> selectImages());
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

    private void loadEventTypesForCategory() {
        if (currentService == null || currentService.getCategory() == null) {
            return;
        }
        
        EventTypeService eventTypeAPI = ApiClient.getClient(this).create(EventTypeService.class);
        eventTypeAPI.getAllEventTypes(getAuthHeader()).enqueue(new Callback<List<EventTypeDTO>>() {
            @Override
            public void onResponse(Call<List<EventTypeDTO>> call, Response<List<EventTypeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventTypeDTO> allEventTypes = response.body();
                    
                    eventTypes.clear();
                    Long currentCategoryId = currentService.getCategory().getId();
                    
                    for (EventTypeDTO eventType : allEventTypes) {
                        if (eventType.getSuggestedCategories() != null) {
                            for (CategoryDTO category : eventType.getSuggestedCategories()) {
                                if (category.getId().equals(currentCategoryId)) {
                                    eventTypes.add(eventType);
                                    break;
                                }
                            }
                        }
                    }
                    
                    List<String> eventTypeNames = new ArrayList<>();
                    eventTypeNames.add("Select Event Type");
                    for (EventTypeDTO eventType : eventTypes) {
                        eventTypeNames.add(eventType.getName());
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditServiceActivity.this, 
                            android.R.layout.simple_spinner_item, eventTypeNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEventTypes.setAdapter(adapter);
                    
                    if (currentService.getEventTypes() != null && !currentService.getEventTypes().isEmpty()) {
                        EventTypeDTO currentEventType = currentService.getEventTypes().get(0);
                        for (int i = 0; i < eventTypes.size(); i++) {
                            if (eventTypes.get(i).getId().equals(currentEventType.getId())) {
                                spinnerEventTypes.setSelection(i + 1);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventTypeDTO>> call, Throwable t) {
                Toast.makeText(EditServiceActivity.this, "Error loading event types", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadServiceData() {
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        serviceAPI.getServiceById(getAuthHeader(), serviceId).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentService = response.body();
                    populateForm();
                } else {
                    Toast.makeText(EditServiceActivity.this, "Error loading service", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Toast.makeText(EditServiceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateForm() {
        if (currentService == null) return;

        etServiceName.setText(currentService.getName());
        etServiceDescription.setText(currentService.getDescription());
        etServicePrice.setText(String.valueOf(currentService.getPrice()));
        etServiceDiscount.setText(String.valueOf(currentService.getDiscount()));
        
        // Duration - determine type and populate accordingly
        if (currentService.getDuration() != null && currentService.getDuration() > 0) {
            // Fixed duration
            rbFixedDuration.setChecked(true);
            layoutFixedDuration.setVisibility(View.VISIBLE);
            layoutEngagementRange.setVisibility(View.GONE);
            
            int totalMinutes = currentService.getDuration();
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            etDurationHours.setText(String.valueOf(hours));
            etDurationMinutes.setText(String.valueOf(minutes));
        } else if (currentService.getMinEngagement() != null || currentService.getMaxEngagement() != null) {
            // Engagement range
            rbEngagementRange.setChecked(true);
            layoutFixedDuration.setVisibility(View.GONE);
            layoutEngagementRange.setVisibility(View.VISIBLE);
            
        if (currentService.getMinEngagement() != null) {
            etMinEngagement.setText(String.valueOf(currentService.getMinEngagement()));
        }
        if (currentService.getMaxEngagement() != null) {
            etMaxEngagement.setText(String.valueOf(currentService.getMaxEngagement()));
        }
        } else {
            // Default to fixed duration
            rbFixedDuration.setChecked(true);
            layoutFixedDuration.setVisibility(View.VISIBLE);
            layoutEngagementRange.setVisibility(View.GONE);
        }
        
        
        etReservationDue.setText(String.valueOf(currentService.getReservationDue()));
        etCancellationDue.setText(String.valueOf(currentService.getCancelationDue()));
        
        cbAvailable.setChecked(currentService.isAvailable());
        
        // Category (read-only)
        if (currentService.getCategory() != null) {
            tvCategory.setText(currentService.getCategory().getName());
        }
        
        // Load event types for this category
        loadEventTypesForCategory();
        
        if (currentService.getImageURLs() != null && !currentService.getImageURLs().isEmpty()) {
            tvCurrentImages.setText(currentService.getImageURLs().size() + " image(s)");
        } else {
            tvCurrentImages.setText("No images");
        }
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
                Toast.makeText(EditServiceActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EditServiceActivity.this, "Error loading event types", Toast.LENGTH_SHORT).show();
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
        
        if (currentService != null && currentService.getCategory() != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id.equals(currentService.getCategory().id)) {
                    spinnerCategory.setSelection(i + 1);
                    break;
                }
            }
        }
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
            handleImageSelection(data);
        }
    }

    private void handleImageSelection(Intent data) {
            newImageUris.clear();
        selectedImages.clear();
            
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    newImageUris.add(imageUri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    selectedImages.add(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Error loading image " + (i + 1), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            tvImageCount.setText(count + " images selected");
            Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
            } else if (data.getData() != null) {
                // Single image selected
            Uri imageUri = data.getData();
            newImageUris.add(imageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                selectedImages.add(bitmap);
                tvImageCount.setText("1 image selected");
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void saveService() {
        if (!validateForm()) {
            return;
        }

        UpdateServiceDTO dto = new UpdateServiceDTO();
        dto.setServiceId(serviceId);
        dto.setName(etServiceName.getText().toString().trim());
        dto.setDescription(etServiceDescription.getText().toString().trim());
        dto.setPrice(Double.parseDouble(etServicePrice.getText().toString().trim()));
        
        String discountStr = etServiceDiscount.getText().toString().trim();
        dto.setDiscount(TextUtils.isEmpty(discountStr) ? 0 : Double.parseDouble(discountStr));
        
        dto.setAvailable(cbAvailable.isChecked());
        dto.setVisible(true);
        
        List<Long> eventTypeIds = new ArrayList<>();
        int selectedPosition = spinnerEventTypes.getSelectedItemPosition();
        if (selectedPosition > 0) {
            EventTypeDTO selectedEventType = eventTypes.get(selectedPosition - 1);
            eventTypeIds.add(selectedEventType.getId());
        }
        dto.setEventTypeIds(eventTypeIds);
        
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
            dto.setMinEngagement(0);
            dto.setMaxEngagement(0);
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
            // Clear duration for engagement range
            dto.setDuration(null);
        } else {
            // Default case - set both duration and engagement
            dto.setDuration(60); // Default 1 hour
            dto.setMinEngagement(1);
            dto.setMaxEngagement(1);
        }
        
        String resDueStr = etReservationDue.getText().toString().trim();
        String cancelDueStr = etCancellationDue.getText().toString().trim();
        dto.setReservationDue(TextUtils.isEmpty(resDueStr) ? 0 : Integer.parseInt(resDueStr));
        dto.setCancelationDue(TextUtils.isEmpty(cancelDueStr) ? 0 : Integer.parseInt(cancelDueStr));
        
        // Reservation type - always MANUAL
        dto.setReservationType("MANUAL");
        
        // Category - keep original category
        if (currentService.getCategory() != null) {
            dto.setCategoryId(currentService.getCategory().getId());
        }
        
        if (newImageUris.isEmpty() && currentService.getImageURLs() != null) {
            dto.setImageURLs(currentService.getImageURLs());
        }
        
        Gson gson = new Gson();
        String dtoJson = gson.toJson(dto);
        RequestBody dtoBody = RequestBody.create(MediaType.parse("application/json"), dtoJson);
        
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        if (!selectedImages.isEmpty()) {
            imageParts = MultipartHelper.createMultipartList(selectedImages);
        }
        
        ServiceService serviceAPI = ApiClient.getClient(this).create(ServiceService.class);
        serviceAPI.updateService(getAuthHeader(), serviceId, dtoBody, imageParts).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditServiceActivity.this, "Service updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditServiceActivity.this, "Error updating service: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Toast.makeText(EditServiceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        return true;
    }


    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }
}
