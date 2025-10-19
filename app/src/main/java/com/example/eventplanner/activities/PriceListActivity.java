package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.PriceListAdapter;
import com.example.eventplanner.dto.ProductDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.ProductService;
import com.example.eventplanner.network.service.ServiceService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListActivity extends BaseActivity implements PriceListAdapter.OnItemClickListener {

    
    private RecyclerView recyclerView;
    private PriceListAdapter adapter;
    private List<Object> priceListItems;
    private ProgressBar progressBar;
    private Button btnExportPDF;
    private TextView emptyView;
    private DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate the content layout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_price_list, contentFrame, true);
        
        setTitle("Price List");
        
        initializeViews();
        setupRecyclerView();
        loadPriceList();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_price_list);
        progressBar = findViewById(R.id.progress_bar);
        btnExportPDF = findViewById(R.id.btn_export_pdf);
        emptyView = findViewById(R.id.empty_view);
        
        btnExportPDF.setOnClickListener(v -> exportToPDF());
    }

    private void setupRecyclerView() {
        priceListItems = new ArrayList<>();
        adapter = new PriceListAdapter(priceListItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadPriceList() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        Long providerId = getCurrentUserId();
        if (providerId == -1L) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Load both services and products
        loadServices(providerId);
        loadProducts(providerId);
    }

    private void loadServices(Long providerId) {
        ServiceService service = ApiClient.getClient(this).create(ServiceService.class);
        service.getServicesByProviderId(getAuthHeader(), providerId).enqueue(new Callback<List<ServiceDTO>>() {
            @Override
            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> services = response.body();
                    for (ServiceDTO service : services) {
                        priceListItems.add(service);
                    }
                    updateAdapter();
                } else {
                    Log.e("PriceListActivity", "Error loading services: " + response.code());
                }
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                Log.e("PriceListActivity", "Error loading services: " + t.getMessage());
                checkLoadingComplete();
            }
        });
    }

    private void loadProducts(Long providerId) {
        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        productService.getProductsByProviderId(getAuthHeader(), providerId).enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductDTO> products = response.body();
                    for (ProductDTO product : products) {
                        priceListItems.add(product);
                    }
                    updateAdapter();
                } else {
                    Log.e("PriceListActivity", "Error loading products: " + response.code());
                }
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e("PriceListActivity", "Error loading products: " + t.getMessage());
                checkLoadingComplete();
            }
        });
    }

    private void updateAdapter() {
        adapter.notifyDataSetChanged();
        if (priceListItems.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void checkLoadingComplete() {
        // This is a simple approach - in a real app you might want to track loading states
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onEditClick(Object item, int position) {
        showEditDialog(item, position);
    }

    private void showEditDialog(Object item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_price, null);
        
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etDiscount = dialogView.findViewById(R.id.et_discount);
        TextView tvItemName = dialogView.findViewById(R.id.tv_item_name);
        
        double price = 0;
        double discount = 0;
        String itemName = "";
        
        if (item instanceof ServiceDTO) {
            ServiceDTO service = (ServiceDTO) item;
            price = service.getPrice();
            discount = service.getDiscount();
            itemName = service.getName();
        } else if (item instanceof ProductDTO) {
            ProductDTO product = (ProductDTO) item;
            price = product.getPrice() != null ? product.getPrice() : 0;
            discount = product.getDiscount() != null ? product.getDiscount() : 0;
            itemName = product.getName();
        }
        
        tvItemName.setText(itemName);
        etPrice.setText(String.valueOf(price));
        etDiscount.setText(String.valueOf(discount));
        
        builder.setView(dialogView)
                .setTitle("Edit Price & Discount")
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        double newPrice = Double.parseDouble(etPrice.getText().toString());
                        double newDiscount = Double.parseDouble(etDiscount.getText().toString());
                        
                        if (newPrice < 0 || newDiscount < 0 || newDiscount > 100) {
                            Toast.makeText(this, "Invalid values", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        updatePriceAndDiscount(item, newPrice, newDiscount, position);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePriceAndDiscount(Object item, double newPrice, double newDiscount, int position) {
        if (item instanceof ServiceDTO) {
            updateServicePrice((ServiceDTO) item, newPrice, newDiscount, position);
        } else if (item instanceof ProductDTO) {
            updateProductPrice((ProductDTO) item, newPrice, newDiscount, position);
        }
    }

    private void updateServicePrice(ServiceDTO service, double newPrice, double newDiscount, int position) {
        ServiceService serviceService = ApiClient.getClient(this).create(ServiceService.class);
        
        ServiceService.PriceDiscountUpdateDTO dto = new ServiceService.PriceDiscountUpdateDTO(newPrice, newDiscount);
        serviceService.updatePriceAndDiscount(getAuthHeader(), service.getId(), dto)
                .enqueue(new Callback<ServiceDTO>() {
                    @Override
                    public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            priceListItems.set(position, response.body());
                            adapter.notifyItemChanged(position);
                            Toast.makeText(PriceListActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PriceListActivity.this, "Error updating", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ServiceDTO> call, Throwable t) {
                        Toast.makeText(PriceListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProductPrice(ProductDTO product, double newPrice, double newDiscount, int position) {
        ProductService productService = ApiClient.getClient(this).create(ProductService.class);
        
        ProductService.PriceDiscountUpdateDTO dto = new ProductService.PriceDiscountUpdateDTO(newPrice, newDiscount);
        productService.updatePriceAndDiscount(getAuthHeader(), product.getId(), dto)
                .enqueue(new Callback<ProductDTO>() {
                    @Override
                    public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            priceListItems.set(position, response.body());
                            adapter.notifyItemChanged(position);
                            Toast.makeText(PriceListActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PriceListActivity.this, "Error updating", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductDTO> call, Throwable t) {
                        Toast.makeText(PriceListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportToPDF() {
        if (priceListItems.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // For Android 11+ (API 30+), we don't need WRITE_EXTERNAL_STORAGE permission
        // for writing to Downloads directory
        generatePDF();
    }


    private void generatePDF() {
        try {
            // Create file name with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String fileName = "price_list_" + timestamp + ".pdf";

            // Save to Documents directory for easy access
            File pdfFile;
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            
            if (documentsDir != null) {
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs();
                }
                pdfFile = new File(documentsDir, fileName);
            } else {
                // Fallback to app's external files directory
                File appDir = new File(getExternalFilesDir(null), "PDFs");
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                pdfFile = new File(appDir, fileName);
            }

            // Create PDF document
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph title = new Paragraph("PRICE LIST")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold();
            document.add(title);

            // Add date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
            Paragraph date = new Paragraph("Date: " + dateFormat.format(new Date()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(date);

            // Add some space
            document.add(new Paragraph("\n"));

            // Create table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 1, 2}))
                    .useAllAvailableWidth();

            // Add table headers
            table.addHeaderCell(new Cell().add(new Paragraph("#").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Name").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Price (RSD)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Discount (%)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Discounted Price (RSD)").setBold()));

            // Add data rows
            for (int i = 0; i < priceListItems.size(); i++) {
                Object item = priceListItems.get(i);
                
                double price = 0;
                double discount = 0;
                String name = "";
                String type = "";

                if (item instanceof ServiceDTO) {
                    ServiceDTO service = (ServiceDTO) item;
                    price = service.getPrice();
                    discount = service.getDiscount();
                    name = service.getName();
                    type = "SERVICE";
                } else if (item instanceof ProductDTO) {
                    ProductDTO product = (ProductDTO) item;
                    price = product.getPrice() != null ? product.getPrice() : 0;
                    discount = product.getDiscount() != null ? product.getDiscount() : 0;
                    name = product.getName();
                    type = "PRODUCT";
                }

                double discountedPrice = price * (1 - discount / 100);

                table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1))));
                table.addCell(new Cell().add(new Paragraph(name + " (" + type + ")")));
                table.addCell(new Cell().add(new Paragraph(priceFormat.format(price))));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f", discount))));
                table.addCell(new Cell().add(new Paragraph(priceFormat.format(discountedPrice))));
            }

            document.add(table);

            // Close document
            document.close();

            // Show success message with full path
            String fullPath = pdfFile.getAbsolutePath();
            String location = fullPath.contains("Documents") ? "Documents folder" : "App folder";
            Toast.makeText(this, "PDF saved successfully!\n" + location + ":\n" + fileName, Toast.LENGTH_LONG).show();
            
            // Also log the path for debugging
            Log.d("PriceListActivity", "PDF saved to: " + fullPath);

        } catch (IOException e) {
            Log.e("PriceListActivity", "Error generating PDF", e);
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getLong("user_id", -1L);
    }

    private String getAuthHeader() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");
        return "Bearer " + token;
    }
}
