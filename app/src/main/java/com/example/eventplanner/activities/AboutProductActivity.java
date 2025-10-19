package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.example.eventplanner.dto.FavoriteSolutionDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.ProfileDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.GlideAuthModule;
import com.example.eventplanner.network.service.ProfileService;
import com.example.eventplanner.network.service.FavoriteService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.eventplanner.config.ApiConfig.BASE_URL;

import java.util.List;

public class AboutProductActivity extends BaseActivity {

    private ProductDTO product;
    private String userRole;
    private String currentUserId;
    private boolean isFavorite = false;
    private boolean isFromFavorites = false;
    private FavoriteService favoriteService;

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
    private ImageView heartIcon;
    private Button btnEditProduct;
    private Button btnProviderProfile;
    private Button btnChatWithProvider;
    private Button btnBuyProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_about_product, contentFrame, true);

        product = (ProductDTO) getIntent().getSerializableExtra("product");
        isFromFavorites = getIntent().getBooleanExtra("isFromFavorites", false);
        
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        initFavoriteService();
        setupButtons();
        fillProductInfo();
        loadProviderInfo();
        checkIfFavorite();
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
        heartIcon = findViewById(R.id.heartIcon);
        btnEditProduct = findViewById(R.id.btnEditProduct);
        btnProviderProfile = findViewById(R.id.btnProviderProfile);
        btnChatWithProvider = findViewById(R.id.btnChatWithProvider);
        btnBuyProduct = findViewById(R.id.btnBuyProduct);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "");
        Long userId = prefs.getLong("user_id", -1L);
        currentUserId = userId != -1L ? userId.toString() : "";
    }

    private void setupButtons() {
        btnProviderProfile.setOnClickListener(v -> {
            // Navigate to provider profile
            Long providerId = product.getServiceProviderId() != null ? product.getServiceProviderId() : product.getProviderId();
            if (providerId != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("userId", providerId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Provider information not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnChatWithProvider.setOnClickListener(v -> {
            openChatWithProvider();
        });

        // Setup buy product button
        btnBuyProduct.setOnClickListener(v -> {
            if (product.getAvailable() != null && product.getAvailable()) {
                Intent intent = new Intent(this, PurchaseActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            } else {
                Toast.makeText(this, "This product is not available for purchase", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup heart icon for favorites
        if (!"SPProvider".equals(userRole) && !"SERVICE_PROVIDER".equals(userRole)) {
            Log.d("AboutProductActivity", "Setting heart icon visible for user role: " + userRole);
            heartIcon.setVisibility(View.VISIBLE);
            heartIcon.setImageResource(R.drawable.heart_empty); // Set initial state
            heartIcon.setOnClickListener(v -> toggleFavorite());
        } else {
            Log.d("AboutProductActivity", "Heart icon not visible for user role: " + userRole);
        }

        // Setup buy product button - only visible for Event Organizers
        if ("EventOrganizer".equals(userRole)) {
            btnBuyProduct.setVisibility(View.VISIBLE);
        } else {
            btnBuyProduct.setVisibility(View.GONE);
        }

        if (product.getServiceProviderId() != null && 
            currentUserId != null && 
            product.getServiceProviderId().toString().equals(currentUserId) &&
            ("SPProvider".equals(userRole) || "SERVICE_PROVIDER".equals(userRole))) {
            btnEditProduct.setVisibility(View.VISIBLE);
            btnEditProduct.setOnClickListener(v -> {
                Toast.makeText(this, "Edit product functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void fillProductInfo() {
        tvProductName.setText(product.getName());

        if (product.getPrice() != null) {
            tvPrice.setText("Price: " + product.getPrice().intValue() + " RSD");
        } else {
            tvPrice.setText("Price: N/A");
        }
        
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            tvDiscount.setText("Discount: " + product.getDiscount().intValue() + "%");
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }

        if (product.getAvailable() != null && product.getAvailable()) {
            tvAvailability.setText("Available");
            tvAvailability.setTextColor(getResources().getColor(android.R.color.white));
            tvAvailability.setBackgroundResource(R.drawable.availability_background);
        } else {
            tvAvailability.setText("Unavailable");
            tvAvailability.setTextColor(getResources().getColor(android.R.color.white));
            tvAvailability.setBackgroundResource(R.drawable.unavailable_background);
        }

        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            tvDescription.setText(product.getDescription());
        } else {
            tvDescription.setText("No description available");
        }

        if (product.getServiceProviderName() != null && !product.getServiceProviderName().trim().isEmpty()) {
            tvProviderName.setText(product.getServiceProviderName());
        } else if (product.getProviderId() != null) {
            tvProviderName.setText("Provider ID: " + product.getProviderId());
        } else if (product.getServiceProviderId() != null) {
            tvProviderName.setText("Provider ID: " + product.getServiceProviderId());
        } else {
            tvProviderName.setText("Unknown Provider");
        }

        tvProviderEmail.setText("Contact information not available");

        if (product.getCategory() != null && product.getCategory().getName() != null && !product.getCategory().getName().trim().isEmpty()) {
            tvCategory.setText(product.getCategory().getName());
        } else if (product.getCategoryName() != null && !product.getCategoryName().trim().isEmpty()) {
            tvCategory.setText(product.getCategoryName());
        } else {
            tvCategory.setText("-");
        }

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

        if (product.getImageURLs() != null && !product.getImageURLs().isEmpty()) {
            ProductImageAdapter imageAdapter = new ProductImageAdapter(product.getImageURLs());
            viewPagerImages.setAdapter(imageAdapter);
            viewPagerImages.setVisibility(View.VISIBLE);
            
            if (product.getImageURLs().size() > 1) {
                setupIndicators(product.getImageURLs().size());
                layoutIndicators.setVisibility(View.VISIBLE);
            } else {
                layoutIndicators.setVisibility(View.GONE);
            }
        } else {
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
        
        String role = profile.getRole();
        if ("SPProvider".equals(role) || "SERVICE_PROVIDER".equals(role)) {
            String name = profile.getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
        }
        
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
        
        if (count > 0) {
            layoutIndicators.getChildAt(0).setBackgroundResource(R.drawable.indicator_selected);
        }
        
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

    private void initFavoriteService() {
        favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
    }

    private void checkIfFavorite() {
        Log.d("AboutProductActivity", "checkIfFavorite called");
        
        // Only check if heart icon is visible (user is not SPP)
        if (heartIcon == null || heartIcon.getVisibility() != View.VISIBLE) {
            Log.d("AboutProductActivity", "Heart icon not visible, skipping check");
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String token = prefs.getString("jwt_token", null);

        Log.d("AboutProductActivity", "checkIfFavorite: userId=" + userId + ", token=" + (token != null) + ", product=" + (product != null));

        if (userId == -1L || token == null || product == null) {
            Log.d("AboutProductActivity", "Missing required data, skipping check");
            return;
        }

        String authHeader = "Bearer " + token;
        String userIdStr = userId.toString();

        Log.d("AboutProductActivity", "Checking if product " + product.getId() + " is favorite for user " + userIdStr);
        // Instead of checkIfProductIsFavorite, get all favorites and check if this product is in the list
        favoriteService.getFavoriteProducts(userIdStr, authHeader).enqueue(new Callback<List<FavoriteSolutionDTO>>() {
            @Override
            public void onResponse(Call<List<FavoriteSolutionDTO>> call, Response<List<FavoriteSolutionDTO>> response) {
                Log.d("AboutProductActivity", "getFavoriteProducts response: success=" + response.isSuccessful() + ", body size=" + (response.body() != null ? response.body().size() : "null"));
                if (response.isSuccessful() && response.body() != null) {
                    // Check if this product is in the favorites list
                    isFavorite = false;
                    for (FavoriteSolutionDTO favoriteSolution : response.body()) {
                        if (favoriteSolution.getSolution() != null && favoriteSolution.getSolution().getId().equals(product.getId())) {
                            isFavorite = true;
                            break;
                        }
                    }
                    Log.d("AboutProductActivity", "Product is favorite: " + isFavorite);
                    updateHeartIcon();
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteSolutionDTO>> call, Throwable t) {
                Log.e("AboutProductActivity", "Error getting favorite products: " + t.getMessage());
            }
        });
    }

    private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String token = prefs.getString("jwt_token", null);

        if (userId == -1L || token == null || product == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        String userIdStr = userId.toString();

        Call<Void> call;
        if (isFavorite) {
            call = favoriteService.removeProductFromFavorites(userIdStr, product.getId(), authHeader);
        } else {
            call = favoriteService.addProductToFavorites(userIdStr, product.getId(), authHeader);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isFavorite = !isFavorite;
                    updateHeartIcon();
                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(AboutProductActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AboutProductActivity.this, "Error updating favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AboutProductActivity", "Error toggling favorite: " + t.getMessage());
                Toast.makeText(AboutProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHeartIcon() {
        Log.d("AboutProductActivity", "updateHeartIcon: heartIcon=" + (heartIcon != null) + ", isFavorite=" + isFavorite + ", visibility=" + (heartIcon != null ? heartIcon.getVisibility() : "null"));
        if (heartIcon != null) {
            if (isFavorite) {
                Log.d("AboutProductActivity", "Setting heart to filled");
                heartIcon.setImageResource(R.drawable.heart_filled);
            } else {
                Log.d("AboutProductActivity", "Setting heart to empty");
                heartIcon.setImageResource(R.drawable.heart_empty);
            }
        }
    }
    
    private void openChatWithProvider() {
        String providerName = tvProviderName.getText().toString();
        if (!TextUtils.isEmpty(providerName) && !providerName.equals("Unknown Provider")) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("recipient_name", providerName);
            intent.putExtra("context_type", "PRODUCT");
            intent.putExtra("context_id", product.getId().toString());
            intent.putExtra("product_name", product.getName());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Provider information not available", Toast.LENGTH_SHORT).show();
        }
    }
}
