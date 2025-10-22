package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.UpdateServiceDTO;
import com.example.eventplanner.network.ApiClient;
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

public class EditServiceActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;

    private Long serviceId;
    private ServiceDTO currentService;

    private EditText etServiceName, etServiceDescription, etServicePrice, etServiceDiscount;
    private EditText etDurationHours, etDurationMinutes, etMinEngagement, etMaxEngagement;
    private EditText etReservationDue, etCancellationDue;
    private TextView tvCategoryName;
    private RadioGroup rgReservationType, rgDurationType;
    private CheckBox cbAvailable;
    private Button btnUploadImages, btnSave, btnCancel;
    private TextView tvImageCount, tvCurrentImages;
    private View layoutDuration, layoutEngagement;

    private List<Uri> newImageUris = new ArrayList<>();

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
        
        tvCategoryName = findViewById(R.id.tvCategoryName);
        
        rgReservationType = findViewById(R.id.rgReservationType);
        rgDurationType = findViewById(R.id.rgDurationType);
        layoutDuration = findViewById(R.id.layoutDuration);
        layoutEngagement = findViewById(R.id.layoutEngagement);
        
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
        
        // Toggle between Duration and Engagement fields
        rgDurationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDuration) {
                layoutDuration.setVisibility(View.VISIBLE);
                layoutEngagement.setVisibility(View.GONE);
                // Clear engagement fields
                etMinEngagement.setText("");
                etMaxEngagement.setText("");
            } else if (checkedId == R.id.rbEngagement) {
                layoutDuration.setVisibility(View.GONE);
                layoutEngagement.setVisibility(View.VISIBLE);
                // Clear duration fields
                etDurationHours.setText("");
                etDurationMinutes.setText("");
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
        
        // Check if using Duration or Engagement
        if (currentService.getDuration() != null && currentService.getDuration() > 0) {
            // Using Duration - convert minutes to hours and minutes
            rgDurationType.check(R.id.rbDuration);
            layoutDuration.setVisibility(View.VISIBLE);
            layoutEngagement.setVisibility(View.GONE);
            
            int totalMinutes = currentService.getDuration();
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            
            if (hours > 0) {
                etDurationHours.setText(String.valueOf(hours));
            }
            if (minutes > 0) {
                etDurationMinutes.setText(String.valueOf(minutes));
            }
        } else if (currentService.getMinEngagement() != null && currentService.getMaxEngagement() != null) {
            // Using Engagement
            rgDurationType.check(R.id.rbEngagement);
            layoutDuration.setVisibility(View.GONE);
            layoutEngagement.setVisibility(View.VISIBLE);
            etMinEngagement.setText(String.valueOf(currentService.getMinEngagement()));
            etMaxEngagement.setText(String.valueOf(currentService.getMaxEngagement()));
        }
        
        etReservationDue.setText(String.valueOf(currentService.getReservationDue()));
        etCancellationDue.setText(String.valueOf(currentService.getCancelationDue()));
        
        cbAvailable.setChecked(currentService.isAvailable());
        
        if ("AUTOMATIC".equals(currentService.getReservationType())) {
            rgReservationType.check(R.id.rbAutomatic);
        } else {
            rgReservationType.check(R.id.rbManual);
        }
        
        // Display category name (readonly)
        if (currentService.getCategory() != null) {
            tvCategoryName.setText(currentService.getCategory().name);
        } else {
            tvCategoryName.setText("No category");
        }
        
        if (currentService.getImageURLs() != null && !currentService.getImageURLs().isEmpty()) {
            tvCurrentImages.setText(currentService.getImageURLs().size() + " image(s)");
        } else {
            tvCurrentImages.setText("No images");
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
            newImageUris.clear();
            
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    newImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                newImageUris.add(data.getData());
            }
            
            tvImageCount.setText(newImageUris.size() + " new image(s) selected");
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
        dto.setVisible(currentService.isVisible()); // Keep original visibility
        
        // Keep original category (cannot be changed)
        if (currentService.getCategory() != null) {
            dto.setCategoryId(currentService.getCategory().id);
        }
        
        // Set eventTypes as empty list (not required anymore, but backend expects non-null)
        dto.setEventTypeIds(new ArrayList<>());
        
        // Check if Duration or Engagement is selected
        int durationTypeId = rgDurationType.getCheckedRadioButtonId();
        if (durationTypeId == R.id.rbDuration) {
            // Using Duration - convert hours and minutes to total minutes
            String hoursStr = etDurationHours.getText().toString().trim();
            String minutesStr = etDurationMinutes.getText().toString().trim();
            
            int hours = TextUtils.isEmpty(hoursStr) ? 0 : Integer.parseInt(hoursStr);
            int minutes = TextUtils.isEmpty(minutesStr) ? 0 : Integer.parseInt(minutesStr);
            
            if (hours == 0 && minutes == 0) {
                Toast.makeText(this, "Please enter duration (hours and/or minutes)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int totalMinutes = (hours * 60) + minutes;
            dto.setDuration(totalMinutes);
            // Clear engagement fields
            dto.setMinEngagement(null);
            dto.setMaxEngagement(null);
        } else {
            // Using Engagement Range
            String minEngStr = etMinEngagement.getText().toString().trim();
            String maxEngStr = etMaxEngagement.getText().toString().trim();
            if (!TextUtils.isEmpty(minEngStr) && !TextUtils.isEmpty(maxEngStr)) {
                dto.setMinEngagement(Integer.parseInt(minEngStr));
                dto.setMaxEngagement(Integer.parseInt(maxEngStr));
                // Clear duration field
                dto.setDuration(null);
            } else {
                Toast.makeText(this, "Please enter both min and max engagement", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        String resDueStr = etReservationDue.getText().toString().trim();
        String cancelDueStr = etCancellationDue.getText().toString().trim();
        dto.setReservationDue(TextUtils.isEmpty(resDueStr) ? 0 : Integer.parseInt(resDueStr));
        dto.setCancelationDue(TextUtils.isEmpty(cancelDueStr) ? 0 : Integer.parseInt(cancelDueStr));
        
        int selectedId = rgReservationType.getCheckedRadioButtonId();
        dto.setReservationType(selectedId == R.id.rbAutomatic ? "AUTOMATIC" : "MANUAL");
        
        if (newImageUris.isEmpty() && currentService.getImageURLs() != null) {
            dto.setImageURLs(currentService.getImageURLs());
        }
        
        Gson gson = new Gson();
        String dtoJson = gson.toJson(dto);
        RequestBody dtoBody = RequestBody.create(MediaType.parse("application/json"), dtoJson);
        
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : newImageUris) {
            try {
                File file = createTempFileFromUri(uri);
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part part = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
                imageParts.add(part);
            } catch (IOException e) {
                Toast.makeText(EditServiceActivity.this, "Error creating file from URI", Toast.LENGTH_SHORT).show();
            }
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

    private String getAuthHeader() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("jwt_token", null);
        return token != null ? "Bearer " + token : "";
    }
}
