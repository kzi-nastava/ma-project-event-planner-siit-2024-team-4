package com.example.eventplanner.activities;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.dto.ProfileDTO;
import com.example.eventplanner.dto.UpdateProfileDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.MultipartHelper;
import com.example.eventplanner.network.service.ProfileService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private EditText etEmail, etNameEO, etLastNameEO, etCompanyName, etDescription, etAddress, etPhone;
    private LinearLayout layoutEO, layoutSPP, layoutEOImage, layoutSPPImages;
    private Button btnEditProfile, btnSaveChanges, btnCancel, btnChangePassword, btnDeactivate;
    private Button btnEOChangeImage, btnAddSPPImage, btnPrevImage, btnNextImage, btnRemoveEOImage, btnRemoveSPPImage;
    private ImageView ivEOProfile, ivSPPProfile;

    private boolean isEditing = false;
    private String userRole;
    
    // Image handling variables
    private List<String> existingImageURLs = new ArrayList<>();
    private List<String> originalImageURLs = new ArrayList<>();
    private List<Bitmap> selectedImages = new ArrayList<>();
    private int currentImageIndex = 0;
    
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isMultipleImages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, findViewById(R.id.content_frame), true);

        initActivityResultLaunchers();
        initViews();
        loadUserProfile();
        setupButtons();
    }

    private void initActivityResultLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission needed to select images", Toast.LENGTH_SHORT).show();
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

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etNameEO = findViewById(R.id.etNameEO);
        etLastNameEO = findViewById(R.id.etLastNameEO);
        etCompanyName = findViewById(R.id.etCompanyName);
        etDescription = findViewById(R.id.etDescription);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);

        layoutEO = findViewById(R.id.layoutEO);
        layoutSPP = findViewById(R.id.layoutSPP);
        layoutEOImage = findViewById(R.id.layoutEOImage);
        layoutSPPImages = findViewById(R.id.layoutSPPImages);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeactivate = findViewById(R.id.btnDeactivate);
        btnEOChangeImage = findViewById(R.id.btnEOChangeImage);
        btnAddSPPImage = findViewById(R.id.btnAddSPPImage);
        btnPrevImage = findViewById(R.id.btnPrevImage);
        btnNextImage = findViewById(R.id.btnNextImage);
        btnRemoveEOImage = findViewById(R.id.btnRemoveEOImage);
        btnRemoveSPPImage = findViewById(R.id.btnRemoveSPPImage);
        
        ivEOProfile = findViewById(R.id.ivEOProfile);
        ivSPPProfile = findViewById(R.id.ivSPPProfile);
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        ProfileService profileService = ApiClient.getClient(this).create(ProfileService.class);

        profileService.getProfile("Bearer " + token).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fillProfileFields(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillProfileFields(ProfileDTO user) {
        etEmail.setText(user.getEmail());
        etNameEO.setText(user.getName());
        etLastNameEO.setText(user.getLastName());
        etCompanyName.setText(user.getName());
        etDescription.setText(user.getDescription());
        etAddress.setText(user.getAddress());
        etPhone.setText(user.getPhoneNumber());

        userRole = user.getRole();

        if (user.getImageURLs() != null) {
            existingImageURLs.clear();
            originalImageURLs.clear();
            Log.d("ProfileActivity", "Loading profile - user.getImageURLs() size: " + user.getImageURLs().size());
            for (String imageUrl : user.getImageURLs()) {
                String fullUrl;
                if (imageUrl.startsWith("http")) {
                    fullUrl = imageUrl;
                } else {
                    if (imageUrl.startsWith("/")) {
                        fullUrl = BASE_URL + imageUrl.substring(1);
                    } else {
                        fullUrl = BASE_URL + imageUrl;
                    }
                }
                existingImageURLs.add(fullUrl);
                originalImageURLs.add(fullUrl);
                Log.d("ProfileActivity", "Added existing image to lists: " + fullUrl);
            }
            Log.d("ProfileActivity", "existingImageURLs size after loading: " + existingImageURLs.size());
        } else {
            Log.d("ProfileActivity", "user.getImageURLs() is null");
        }

        if ("EO".equals(userRole) || "EVENT_ORGANIZER".equals(userRole)) {
            layoutEO.setVisibility(View.VISIBLE);
            layoutEOImage.setVisibility(View.VISIBLE);
            
            if (user.getImageURLs() != null && !user.getImageURLs().isEmpty()) {
                String fullImageUrl = existingImageURLs.get(0);
                
                Glide.with(this)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.gallery)
                        .error(R.drawable.gallery)
                        .into(ivEOProfile);
            } else {
                ivEOProfile.setImageResource(R.drawable.gallery);
            }
        } else if ("SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole)) {
            layoutSPP.setVisibility(View.VISIBLE);
            layoutSPPImages.setVisibility(View.VISIBLE);
            
            currentImageIndex = 0;
            updateSPPImageDisplay();
        }
    }

    private void setupButtons() {
        btnEditProfile.setOnClickListener(v -> toggleEditMode(true));
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        btnCancel.setOnClickListener(v -> cancelEdit());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnDeactivate.setOnClickListener(v ->
                Toast.makeText(this, "Profile deactivated", Toast.LENGTH_SHORT).show()
        );

        btnEOChangeImage.setOnClickListener(v -> selectEOImage());
        btnAddSPPImage.setOnClickListener(v -> selectSPPImages());
        btnPrevImage.setOnClickListener(v -> prevImage());
        btnNextImage.setOnClickListener(v -> nextImage());
        btnRemoveEOImage.setOnClickListener(v -> removeEOImage());
        btnRemoveSPPImage.setOnClickListener(v -> removeSPPImage());
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;

        etNameEO.setEnabled(enable);
        etLastNameEO.setEnabled(enable);
        etDescription.setEnabled(enable);
        etAddress.setEnabled(enable);
        etPhone.setEnabled(enable);

        btnEditProfile.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnSaveChanges.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(enable ? View.VISIBLE : View.GONE);

        if ("EO".equals(userRole) || "EVENT_ORGANIZER".equals(userRole)) {
            btnEOChangeImage.setVisibility(enable ? View.VISIBLE : View.GONE);
            btnRemoveEOImage.setVisibility(enable ? View.VISIBLE : View.GONE);
        } else if ("SPP".equals(userRole) || "SERVICE_PROVIDER".equals(userRole)) {
            btnAddSPPImage.setVisibility(enable ? View.VISIBLE : View.GONE);
            btnRemoveSPPImage.setVisibility(enable ? View.VISIBLE : View.GONE);
            btnPrevImage.setVisibility(View.VISIBLE);
            btnNextImage.setVisibility(View.VISIBLE);
        }
    }

    private void cancelEdit() {
        existingImageURLs.clear();
        existingImageURLs.addAll(originalImageURLs);
        
        selectedImages.clear();
        
        currentImageIndex = 0;
        updateSPPImageDisplay();
        
        toggleEditMode(false);
        
        loadUserProfile();
    }

    private void saveProfileChanges() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileDTO updateDTO = new UpdateProfileDTO();
        updateDTO.setAddress(etAddress.getText().toString().trim());
        updateDTO.setPhoneNumber(etPhone.getText().toString().trim());
        
        if ("EVENT_ORGANIZER".equals(userRole) || "EO".equals(userRole)) {
            updateDTO.setName(etNameEO.getText().toString().trim());
            updateDTO.setLastName(etLastNameEO.getText().toString().trim());
        } else if ("SERVICE_PROVIDER".equals(userRole) || "SPP".equals(userRole)) {
            updateDTO.setName(etCompanyName.getText().toString().trim());
            updateDTO.setDescription(etDescription.getText().toString().trim());
        }

        List<String> cleanedImageURLs = new ArrayList<>();
        Log.d("ProfileActivity", "existingImageURLs size: " + existingImageURLs.size());
        for (String url : existingImageURLs) {
            Log.d("ProfileActivity", "Processing URL: " + url);
            if (url != null && url.startsWith(BASE_URL)) {
                String relativePath = url.replace(BASE_URL, "");
                cleanedImageURLs.add(relativePath);
                Log.d("ProfileActivity", "Added existing image: " + relativePath);
            } else {
                Log.d("ProfileActivity", "Skipped URL (not server URL): " + url);
            }
        }
        Log.d("ProfileActivity", "cleanedImageURLs size: " + cleanedImageURLs.size());
        for (String url : cleanedImageURLs) {
            Log.d("ProfileActivity", "cleanedImageURLs item: " + url);
        }
        Log.d("ProfileActivity", "selectedImages size: " + selectedImages.size());
        updateDTO.setImageURLs(cleanedImageURLs);

        ProfileService profileService = ApiClient.getClient(this).create(ProfileService.class);

        String json = convertToJson(updateDTO);
        Log.d("ProfileActivity", "Sending JSON to backend: " + json);
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json"), json);

        List<okhttp3.MultipartBody.Part> fileParts = new ArrayList<>();
        if (!selectedImages.isEmpty()) {
            fileParts = MultipartHelper.createMultipartList(selectedImages);
            Log.d("ProfileActivity", "Created " + fileParts.size() + " multipart files for new images");
        }

        profileService.updateProfile("Bearer " + token, requestBody, fileParts.toArray(new okhttp3.MultipartBody.Part[0])).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    originalImageURLs.clear();
                    originalImageURLs.addAll(existingImageURLs);
                    
                    selectedImages.clear();
                    toggleEditMode(false);
                    loadUserProfile();
                } else {
                    String errorMessage = "Failed to update profile";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertToJson(UpdateProfileDTO dto) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"address\":\"").append(escapeJson(dto.getAddress())).append("\",");
        json.append("\"phoneNumber\":\"").append(escapeJson(dto.getPhoneNumber())).append("\",");
        json.append("\"name\":\"").append(escapeJson(dto.getName())).append("\",");
        if (dto.getLastName() != null) {
            json.append("\"lastName\":\"").append(escapeJson(dto.getLastName())).append("\",");
        }
        if (dto.getDescription() != null) {
            json.append("\"description\":\"").append(escapeJson(dto.getDescription())).append("\",");
        }
        
        json.append("\"imageURLs\":[");
        if (dto.getImageURLs() != null && !dto.getImageURLs().isEmpty()) {
            for (int i = 0; i < dto.getImageURLs().size(); i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(escapeJson(dto.getImageURLs().get(i))).append("\"");
            }
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void selectEOImage() {
        isMultipleImages = false;
        checkPermissionAndOpenPicker();
    }

    private void selectSPPImages() {
        isMultipleImages = true;
        checkPermissionAndOpenPicker();
    }

    private void checkPermissionAndOpenPicker() {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU 
                ? Manifest.permission.READ_MEDIA_IMAGES 
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (isMultipleImages) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        
        Intent chooserIntent = Intent.createChooser(intent, "Select Image" + (isMultipleImages ? "s" : ""));
        imagePickerLauncher.launch(chooserIntent);
    }

    private void handleImageSelection(Intent data) {
        if (isMultipleImages) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                selectedImages.clear();
                
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        selectedImages.add(bitmap);
                        String previewUrl = imageUri.toString();
                        existingImageURLs.add(previewUrl);
                        Log.d("ProfileActivity", "Added new image to existingImageURLs: " + previewUrl);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image " + (i + 1), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                
                if (!selectedImages.isEmpty()) {
                    updateSPPImageDisplay();
                    Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    selectedImages.clear();
                    selectedImages.add(bitmap);
                    String previewUrl = imageUri.toString();
                    existingImageURLs.add(previewUrl);
                    Log.d("ProfileActivity", "Added single new image to existingImageURLs: " + previewUrl);
                    updateSPPImageDisplay();
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    selectedImages.clear();
                    selectedImages.add(bitmap);
                    ivEOProfile.setImageBitmap(bitmap);
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void prevImage() {
        if (existingImageURLs.size() > 0) {
            currentImageIndex = (currentImageIndex - 1 + existingImageURLs.size()) % existingImageURLs.size();
            updateSPPImageDisplay();
        }
    }

    private void nextImage() {
        if (existingImageURLs.size() > 0) {
            currentImageIndex = (currentImageIndex + 1) % existingImageURLs.size();
            updateSPPImageDisplay();
        }
    }

    private void updateSPPImageDisplay() {
        if (!existingImageURLs.isEmpty()) {
            String imageUrl = existingImageURLs.get(currentImageIndex);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gallery)
                    .error(R.drawable.gallery)
                    .into(ivSPPProfile);
        } else {
            ivSPPProfile.setImageResource(R.drawable.gallery);
        }
    }

    private void removeEOImage() {
        selectedImages.clear();
        existingImageURLs.clear();
        ivEOProfile.setImageResource(R.drawable.gallery);
        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
    }

    private void removeSPPImage() {
        if (!existingImageURLs.isEmpty() && currentImageIndex < existingImageURLs.size()) {
            existingImageURLs.remove(currentImageIndex);
            
            if (existingImageURLs.isEmpty()) {
                currentImageIndex = 0;
            } else if (currentImageIndex >= existingImageURLs.size()) {
                currentImageIndex = existingImageURLs.size() - 1;
            }
            
            updateSPPImageDisplay();
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
        }
    }
}
