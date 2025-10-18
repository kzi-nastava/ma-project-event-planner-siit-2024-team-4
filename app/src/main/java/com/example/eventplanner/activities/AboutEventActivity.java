package com.example.eventplanner.activities;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.EventDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.EventService;
import com.example.eventplanner.network.service.FavoriteService;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AboutEventActivity extends BaseActivity {

    private EventDTO event;
    private boolean isLoading = true;
    private boolean isFavorite = false;
    private String userId;
    private FavoriteService favoriteService;

    private ProgressBar progressBar;
    private TextView eventName, eventDate, eventDescription, maxParticipants, locationText;
    private ImageView favoriteIcon;
    private Button downloadPdfBtn;
    private WebView webViewMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("AboutEventActivity", "onCreate() called");
        
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_about_event, contentFrame, true);

        initViews();
        setupClickListeners();
        
        int eventId = getIntent().getIntExtra("event_id", -1);
        if (eventId != -1) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        setupMap();

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        
        favoriteService = ApiClient.getClient(this).create(FavoriteService.class);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        eventName = findViewById(R.id.eventName);
        eventDate = findViewById(R.id.eventDate);
        eventDescription = findViewById(R.id.eventDescription);
        maxParticipants = findViewById(R.id.maxParticipants);
        locationText = findViewById(R.id.locationText);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        webViewMap = findViewById(R.id.webViewMap);
    }

    private void setupClickListeners() {
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        downloadPdfBtn.setOnClickListener(v -> generatePDF());
    }
    
    private void setupMap() {
        android.util.Log.d("AboutEventActivity", "setupMap() called");
        if (webViewMap != null) {
            android.util.Log.d("AboutEventActivity", "WebView is not null, setting up map");
            webViewMap.getSettings().setJavaScriptEnabled(true);
            webViewMap.getSettings().setLoadWithOverviewMode(true);
            webViewMap.getSettings().setUseWideViewPort(true);
            
            webViewMap.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    android.util.Log.d("AboutEventActivity", "Map loaded successfully");
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    android.util.Log.e("AboutEventActivity", "WebView error: " + description + " for URL: " + failingUrl);
                }
            });
            
            android.util.Log.d("AboutEventActivity", "WebView configured successfully");
        } else {
            android.util.Log.e("AboutEventActivity", "WebView is null in setupMap()");
        }
    }
    
    private void fetchEventDetails(int eventId) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        EventService eventService = ApiClient.getClient(this).create(EventService.class);
        Call<EventDTO> call = eventService.getEventById(eventId);
        

        call.enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    event = response.body();
                    
                    
                    displayEventDetails();
                    
                    if (userId != null) {
                        checkIfFavorite();
                    }
                } else {
                    Log.e("AboutEventActivity", "Failed to load event details. Response code: " + response.code());
                    Toast.makeText(AboutEventActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AboutEventActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayEventDetails() {
        android.util.Log.d("AboutEventActivity", "displayEventDetails() called");
        if (event == null) {
            android.util.Log.d("AboutEventActivity", "Event is null, returning");
            return;
        }

        android.util.Log.d("AboutEventActivity", "Displaying event: " + event.getName());
        eventName.setText(event.getName());
        
        String formattedDate = formatDate(event.getStartDate());
        eventDate.setText("Date: " + formattedDate);
        
        eventDescription.setText(event.getDescription());
        maxParticipants.setText("Max. participants: " + event.getParticipants());
        
        if (event.getLocation() != null) {
            locationText.setText("Location: " + event.getLocation().getAddress());
        } else {
            locationText.setText("Location: Not specified");
        }
        
        updateMapLocation();
    }

    private String formatDate(String dateString) {
        try {
            return dateString.split("T")[0];
        } catch (Exception e) {
            return dateString;
        }
    }

    private void toggleFavorite() {
        if (userId == null) {
            Toast.makeText(this, "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event == null) {
            Toast.makeText(this, "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavorite = !isFavorite;
        updateFavoriteIcon();

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            isFavorite = !isFavorite;
            updateFavoriteIcon();
            return;
        }

        String authHeader = "Bearer " + token;
        Call<Void> call;

        if (isFavorite) {
            call = favoriteService.addEventToFavorites(userId, event.getId(), authHeader);
        } else {
            call = favoriteService.removeEventFromFavorites(userId, event.getId(), authHeader);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(AboutEventActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Revert local change if API call failed
                    isFavorite = !isFavorite;
                    updateFavoriteIcon();
                    Toast.makeText(AboutEventActivity.this, "Failed to update favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isFavorite = !isFavorite;
                updateFavoriteIcon();
                Toast.makeText(AboutEventActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon() {
        if (favoriteIcon != null) {
            favoriteIcon.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart_empty);
        }
    }

    private void checkIfFavorite() {
        if (userId == null || event == null || favoriteService == null) {
            updateFavoriteIcon();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        
        if (token == null) {
            updateFavoriteIcon();
            return;
        }

        String authHeader = "Bearer " + token;
        Call<Boolean> call = favoriteService.checkIfEventIsFavorite(userId, event.getId(), authHeader);
        
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body();
                    updateFavoriteIcon();
                } else {
                    isFavorite = false;
                    updateFavoriteIcon();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                isFavorite = false;
                updateFavoriteIcon();
            }
        });
    }

    private void generatePDF() {
        if (event == null) {
            Toast.makeText(this, "Event data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create PDF file using Scoped Storage
            String fileName = event.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            
            Uri pdfUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+ (API 29+)
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                
                pdfUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (pdfUri == null) {
                    Toast.makeText(this, "Failed to create PDF file", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Fallback for older Android versions
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File pdfFile = new File(downloadsDir, fileName);
                pdfUri = Uri.fromFile(pdfFile);
            }

            // Create PDF content
            PdfWriter writer = new PdfWriter(getContentResolver().openOutputStream(pdfUri));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph title = new Paragraph("Event: " + event.getName())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold();
            document.add(title);

            // Add event details
            document.add(new Paragraph("\nEvent Type: " + (event.getEventTypeName() != null ? event.getEventTypeName() : "N/A")));
            
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                document.add(new Paragraph("Description: " + event.getDescription()));
            }
            
            document.add(new Paragraph("Max. participants: " + event.getParticipants()));
            document.add(new Paragraph("Privacy type: " + (event.isPublic() ? "Public" : "Private")));
            
            if (event.getLocation() != null) {
                document.add(new Paragraph("Location: " + event.getLocation().getAddress()));
            } else {
                document.add(new Paragraph("Location: Not specified"));
            }
            
            String formattedDate = formatDate(event.getStartDate());
            document.add(new Paragraph("Date: " + formattedDate));

            // Add activities table if available
            if (event.getActivities() != null && !event.getActivities().isEmpty()) {
                document.add(new Paragraph("\nActivities:").setBold());
                
                Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 4, 3}))
                        .useAllAvailableWidth();
                
                // Add table headers
                table.addHeaderCell(new Cell().add(new Paragraph("Time").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Activity Name").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Location").setBold()));
                
                // Add activity data
                for (com.example.eventplanner.dto.ActivityDTO activity : event.getActivities()) {
                    String timeRange = "";
                    if (activity.getStartTime() != null && activity.getEndTime() != null) {
                        
                        try {
                            // Parse the time strings - they might be in different formats
                            String startTime = activity.getStartTime();
                            String endTime = activity.getEndTime();
                            
                            // If the time contains 'T' (ISO format), extract just the time part
                            if (startTime.contains("T")) {
                                startTime = startTime.split("T")[1].substring(0, 5); // Get HH:mm part
                            }
                            if (endTime.contains("T")) {
                                endTime = endTime.split("T")[1].substring(0, 5); // Get HH:mm part
                            }
                            
                            timeRange = startTime + " - " + endTime;
                        } catch (Exception e) {
                            timeRange = "N/A";
                        }
                    }
                    
                    table.addCell(new Cell().add(new Paragraph(timeRange)));
                    table.addCell(new Cell().add(new Paragraph(activity.getName() != null ? activity.getName() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(activity.getDescription() != null ? activity.getDescription() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(activity.getLocation() != null ? activity.getLocation() : "N/A")));
                }
                
                document.add(table);
            }

            document.close();

            Toast.makeText(this, "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e("PDF Generation", "Error creating PDF", e);
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateMapLocation() {
        android.util.Log.d("AboutEventActivity", "updateMapLocation() called");
        if (webViewMap == null) {
            android.util.Log.d("AboutEventActivity", "WebView is null, cannot update location");
            return;
        }

        double latitude, longitude;
        
        if (event != null && event.getLocation() != null) {
            latitude = event.getLocation().getLatitude();
            longitude = event.getLocation().getLongitude();
            android.util.Log.d("AboutEventActivity", "Using event location: " + latitude + ", " + longitude);
        } else {
            latitude = 44.7866;
            longitude = 20.4489;
            android.util.Log.d("AboutEventActivity", "Using default location (Belgrade)");
        }
        
        String mapUrl = "https://www.openstreetmap.org/export/embed.html?bbox=" + 
                       (longitude - 0.01) + "," + (latitude - 0.01) + "," + 
                       (longitude + 0.01) + "," + (latitude + 0.01) + 
                       "&layer=mapnik&marker=" + latitude + "," + longitude;
        
        android.util.Log.d("AboutEventActivity", "Loading map URL: " + mapUrl);
        webViewMap.loadUrl(mapUrl);
    }

}
