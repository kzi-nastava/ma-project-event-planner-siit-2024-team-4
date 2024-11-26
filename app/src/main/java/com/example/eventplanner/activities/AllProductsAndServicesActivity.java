package com.example.eventplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ProductAdapter;
import com.example.eventplanner.activities.Product;

import java.util.ArrayList;
import java.util.List;

public class AllProductsAndServicesActivity extends AppCompatActivity {

    private RecyclerView rvAllProductsServices;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private androidx.appcompat.widget.SearchView searchViewProducts;
    private Spinner spinnerFilterProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products_and_services);

        // Inicijalizacija UI komponenti
        rvAllProductsServices = findViewById(R.id.rvAllProductsServices);
        searchViewProducts = findViewById(R.id.searchViewProducts);
        spinnerFilterProducts = findViewById(R.id.spinnerFilterProducts);

        // Kreiranje liste proizvoda
        productList = new ArrayList<>();
        productList.add(new Product("Wedding Cake", R.drawable.service_band, "Catering"));
        productList.add(new Product("DJ Services", R.drawable.service_band, "Entertainment"));
        productList.add(new Product("Floral Arrangement", R.drawable.service_band, "Decoration"));
        productList.add(new Product("Photography", R.drawable.service_band, "Service"));
        productList.add(new Product("Custom Flags", R.drawable.service_band, "Decoration"));

        // Postavljanje RecyclerView-a
        rvAllProductsServices.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList);
        rvAllProductsServices.setAdapter(productAdapter);

        // Funkcionalnost pretrage
        searchViewProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.filter(newText);
                return false;
            }
        });

        // Postavljanje filter Spinner-a
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.product_filter_options,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterProducts.setAdapter(filterAdapter);

        spinnerFilterProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                filterProducts(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterProducts("All");
            }
        });
    }

    // Metoda za filtriranje proizvoda na osnovu kategorije
    private void filterProducts(String filter) {
        List<Product> filteredProducts = new ArrayList<>();
        if (filter.equals("All")) {
            filteredProducts.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getCategory().equalsIgnoreCase(filter)) {
                    filteredProducts.add(product);
                }
            }
        }
        productAdapter.updateProducts(filteredProducts);
    }
}
