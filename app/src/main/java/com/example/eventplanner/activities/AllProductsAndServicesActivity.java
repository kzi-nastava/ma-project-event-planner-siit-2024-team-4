package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.OldProductAdapter;
import com.example.eventplanner.activities.Product;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class AllProductsAndServicesActivity extends AppCompatActivity {

    private RecyclerView rvAllProductsServices;
    private OldProductAdapter productAdapter;
    private List<Product> productList;
    private androidx.appcompat.widget.SearchView searchViewProducts;
    private Spinner spinnerFilterProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                Intent intent = new Intent(AllProductsAndServicesActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_service) {
                Intent intent = new Intent(AllProductsAndServicesActivity.this, ServiceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(AllProductsAndServicesActivity.this, CategoriesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_login) {
                Intent intent = new Intent(AllProductsAndServicesActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_registration) {
                Intent intent = new Intent(AllProductsAndServicesActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_all_products_and_services, contentFrame, true);

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
        productAdapter = new OldProductAdapter(productList);
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
