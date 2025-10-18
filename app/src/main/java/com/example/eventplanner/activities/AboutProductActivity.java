package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ProductImageAdapter;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.ProfileDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.GlideAuthModule;
import com.example.eventplanner.network.service.ProfileService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

public class AboutProductActivity extends AppCompatActivity {

    private ProductDTO product;
    private String userRole;
    private String currentUserId;

    // Views
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicators;
    private TextView tvProductName;
    private TextView tvPrice;
    private TextView tvDiscount;
    private TextView tvAvailability;
    private TextView tvDescription;
    private TextView tvProviderName;
    private TextView tvProviderEmail;
    private TextView tvCategory;
    private TextView tvEventType;
    private Button btnContactProvider;
    private Button btnEditProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_product);

        // Get product from intent
        product = (ProductDTO) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        setupButtons();
        fillProductInfo();
        loadProviderInfo();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvDescription = findViewById(R.id.tvDescription);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvProviderEmail = findViewById(R.id.tvProviderEmail);
        tvCategory = findViewById(R.id.tvCategory);
        tvEventType = findViewById(R.id.tvEventType);
        btnContactProvider = findViewById(R.id.btnContactProvider);
        btnEditProduct = findViewById(R.id.btnEditProduct);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "");
        currentUserId = prefs.getString("user_id", "");
    }

    private void setupButtons() {
        // Contact Provider button
        btnContactProvider.setOnClickListener(v -> {
            // TODO: Implement contact provider functionality
            Toast.makeText(this, "Contact provider functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        // Edit Product button - only visible for product owner
        if (product.getServiceProviderId() != null && 
            currentUserId != null && 
            product.getServiceProviderId().toString().equals(currentUserId) &&
            ("SPProvider".equals(userRole) || "SERVICE_PROVIDER".equals(userRole))) {
            btnEditProduct.setVisibility(View.VISIBLE);
            btnEditProduct.setOnClickListener(v -> {
                // TODO: Navigate to edit product activity
                Toast.makeText(this, "Edit product functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void fillProductInfo() {
        // Set product name
        tvProductName.setText(product.getName());

        // Set price
        if (product.getPrice() != null) {
            tvPrice.setText("Price: " + product.getPrice().intValue() + " RSD");
        } else {
            tvPrice.setText("Price: N/A");
        }
        
        // Set discount
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            tvDiscount.setText("Discount: " + product.getDiscount().intValue() + "%");
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }

        // Set availability
        if (product.getAvailable() != null && product.getAvailable()) {
            tvAvailability.setText("Available");
            tvAvailability.setTextColor(getResources().getColor(android.R.color.white));
            tvAvailability.setBackgroundResource(R.drawable.availability_background);
        } else {
            tvAvailability.setText("Unavailable");
            tvAvailability.setTextColor(getResources().getColor(android.R.color.white));
            tvAvailability.setBackgroundResource(R.drawable.unavailable_background);
        }

        // Set description
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            tvDescription.setText(product.getDescription());
        } else {
            tvDescription.setText("No description available");
        }

        // Set provider info
        if (product.getServiceProviderName() != null && !product.getServiceProviderName().trim().isEmpty()) {
            tvProviderName.setText(product.getServiceProviderName());
        } else if (product.getProviderId() != null) {
            tvProviderName.setText("Provider ID: " + product.getProviderId());
        } else if (product.getServiceProviderId() != null) {
            tvProviderName.setText("Provider ID: " + product.getServiceProviderId());
        } else {
            tvProviderName.setText("Unknown Provider");
        }

        // Provider email - not available in ProductDTO, so we'll show placeholder
        tvProviderEmail.setText("Contact information not available");

        // Set category
        if (product.getCategory() != null && product.getCategory().getName() != null && !product.getCategory().getName().trim().isEmpty()) {
            tvCategory.setText(product.getCategory().getName());
        } else if (product.getCategoryName() != null && !product.getCategoryName().trim().isEmpty()) {
            tvCategory.setText(product.getCategoryName());
        } else {
            tvCategory.setText("-");
        }

        // Set event types
        if (product.getEventTypes() != null && !product.getEventTypes().isEmpty()) {
            StringBuilder eventTypesText = new StringBuilder();
            for (int i = 0; i < product.getEventTypes().size(); i++) {
                if (i > 0) {
                    eventTypesText.append(", ");
                }
                eventTypesText.append(product.getEventTypes().get(i).getName());
            }
            tvEventType.setText(eventTypesText.toString());
        } else if (product.getEventTypeName() != null && !product.getEventTypeName().trim().isEmpty()) {
            tvEventType.setText(product.getEventTypeName());
        } else {
            tvEventType.setText("-");
        }

        // Set product images
        if (product.getImageURLs() != null && !product.getImageURLs().isEmpty()) {
            ProductImageAdapter imageAdapter = new ProductImageAdapter(product.getImageURLs());
            viewPagerImages.setAdapter(imageAdapter);
            viewPagerImages.setVisibility(View.VISIBLE);
            
            // Setup indicators if more than one image
            if (product.getImageURLs().size() > 1) {
                setupIndicators(product.getImageURLs().size());
                layoutIndicators.setVisibility(View.VISIBLE);
            } else {
                layoutIndicators.setVisibility(View.GONE);
            }
        } else {
            // Show default image if no images
            viewPagerImages.setVisibility(View.GONE);
            layoutIndicators.setVisibility(View.GONE);
        }
    }
    
    private void loadProviderInfo() {
        final Long providerId;
        if (product.getProviderId() != null) {
            providerId = product.getProviderId();
        } else if (product.getServiceProviderId() != null) {
            providerId = product.getServiceProviderId();
        } else {
            providerId = null;
        }
        
        if (providerId == null) {
            tvProviderName.setText("Unknown Provider");
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            tvProviderName.setText("Provider ID: " + providerId);
            return;
        }
        
        ProfileService profileService = ApiClient.getClient(this).create(ProfileService.class);
        profileService.getProfileById("Bearer " + token, providerId).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileDTO profile = response.body();
                    // Get provider name based on role
                    String providerName = getProviderName(profile);
                    tvProviderName.setText(providerName);
                } else {
                    tvProviderName.setText("Provider ID: " + providerId);
                }
            }
            
            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                tvProviderName.setText("Provider ID: " + providerId);
            }
        });
    }
    
    private String getProviderName(ProfileDTO profile) {
        if (profile == null) {
            return "Unknown Provider";
        }
        
        // Check user role and get appropriate name
        String role = profile.getRole();
        if ("EventOrganizer".equals(role)) {
            // For Event Organizer, use name + last name
            String name = profile.getName();
            String lastName = profile.getLastName();
            if (name != null && lastName != null) {
                return name + " " + lastName;
            } else if (name != null) {
                return name;
            } else if (lastName != null) {
                return lastName;
            }
        } else if ("SPProvider".equals(role) || "SERVICE_PROVIDER".equals(role)) {
            // For Service Provider, use name (company name)
            String name = profile.getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
        }
        
        // Fallback to email if no name is available
        String email = profile.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        
        return "Unknown Provider";
    }
    
    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.indicator_size),
                getResources().getDimensionPixelSize(R.dimen.indicator_size)
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_unselected);
            layoutIndicators.addView(indicator);
        }
        
        // Set first indicator as selected
        if (count > 0) {
            layoutIndicators.getChildAt(0).setBackgroundResource(R.drawable.indicator_selected);
        }
        
        // Update indicators when page changes
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
                    View indicator = layoutIndicators.getChildAt(i);
                    if (i == position) {
                        indicator.setBackgroundResource(R.drawable.indicator_selected);
                    } else {
                        indicator.setBackgroundResource(R.drawable.indicator_unselected);
                    }
                }
            }
        });
    }
}
