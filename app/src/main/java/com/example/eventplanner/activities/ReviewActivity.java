package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.CreateReviewDTO;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.ReviewDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.ReviewService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends BaseActivity {
    
    private ProductDTO product;
    private Long userId;
    
    private TextView tvProductName;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitReview;
    private Button btnSkipReview;
    private ProgressBar progressBar;
    
    private ReviewService reviewService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_review, findViewById(R.id.content_frame), true);
        
        product = (ProductDTO) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        initializeServices();
        loadUserInfo();
    }
    
    private void initializeViews() {
        tvProductName = findViewById(R.id.tvProductName);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSkipReview = findViewById(R.id.btnSkipReview);
        progressBar = findViewById(R.id.progressBar);
        
        tvProductName.setText("Rate: " + product.getName());
        
        // Set rating bar to 0-10 scale (multiply by 2 to get 0-10 range)
        ratingBar.setMax(10);
        ratingBar.setStepSize(1);
        ratingBar.setRating(5); // Default rating
        
        btnSubmitReview.setOnClickListener(v -> submitReview());
        btnSkipReview.setOnClickListener(v -> skipReview());
    }
    
    private void initializeServices() {
        reviewService = ApiClient.getClient(this).create(ReviewService.class);
    }
    
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userIdLong = prefs.getLong("user_id", -1L);
        userId = userIdLong != -1L ? userIdLong : null;
        
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void submitReview() {
        String comment = etComment.getText().toString().trim();
        float rating = ratingBar.getRating();
        
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (rating < 1) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setUserId(userId);
        dto.setRating((double) rating);
        dto.setComment(comment);
        dto.setSolutionId(product.getId());
        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        reviewService.createReview("Bearer " + token, dto).enqueue(new Callback<ReviewDTO>() {
            @Override
            public void onResponse(Call<ReviewDTO> call, Response<ReviewDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ReviewActivity.this, "Thank you for your review!", Toast.LENGTH_SHORT).show();
                    navigateBackToProducts();
                } else {
                    Toast.makeText(ReviewActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ReviewDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReviewActivity.this, "Error submitting review: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void skipReview() {
        Toast.makeText(this, "Review skipped", Toast.LENGTH_SHORT).show();
        navigateBackToProducts();
    }
    
    private void navigateBackToProducts() {
        Intent intent = new Intent(this, AllProductsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
