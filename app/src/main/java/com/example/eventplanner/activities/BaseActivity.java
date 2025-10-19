package com.example.eventplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.eventplanner.R;
import com.google.android.material.navigation.NavigationView;

import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavigationMenu();
        setupDrawerContent();
    }

    private void updateNavigationMenu() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        String userRole = prefs.getString("user_role", null);

        navigationView.getMenu().clear();

        if (token == null) {
            navigationView.inflateMenu(R.menu.nav_menu_guest);
        } else {
            navigationView.inflateMenu(R.menu.nav_menu_logged_in);

            MenuItem myEventsItem = navigationView.getMenu().findItem(R.id.nav_my_events);
            MenuItem favoriteEventsItem = navigationView.getMenu().findItem(R.id.nav_favorite_events);
            MenuItem myServicesItem = navigationView.getMenu().findItem(R.id.nav_my_services);
            MenuItem myProductsItem = navigationView.getMenu().findItem(R.id.nav_my_products);
            MenuItem categoriesItem = navigationView.getMenu().findItem(R.id.nav_categories);
            MenuItem eventTypesItem = navigationView.getMenu().findItem(R.id.nav_event_types);
            
            if (myEventsItem != null) {
                myEventsItem.setVisible("EventOrganizer".equals(userRole));
            }
            if (favoriteEventsItem != null) {
                favoriteEventsItem.setVisible(true);
            }
            if (myServicesItem != null) {
                myServicesItem.setVisible("SPProvider".equals(userRole));
            }

            if (myProductsItem != null) {
                myProductsItem.setVisible("SPProvider".equals(userRole));
            }
            if (categoriesItem != null) {
                categoriesItem.setVisible(true);
            }
            if (eventTypesItem != null) {
                eventTypesItem.setVisible(true);
            }
        }
    }

    private void setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_homepage) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_login) {
                startActivity(new Intent(this, LogInActivity.class));
            } else if (id == R.id.nav_registration) {
                startActivity(new Intent(this, ChooseRoleActivity.class));
            } else if (id == R.id.nav_profile) {
                openProfile();
            } else if (id == R.id.nav_logout) {
                logout();
            } else if (id == R.id.nav_all_events) {
                startActivity(new Intent(this, AllEventsActivity.class));
            } else if (id == R.id.nav_my_events) {
                startActivity(new Intent(this, MyEventsActivity.class));
            } else if (id == R.id.nav_favorite_events) {
                startActivity(new Intent(this, FavoriteEventsActivity.class));
            } else if (id == R.id.nav_favorite_solutions) {
                startActivity(new Intent(this, FavoriteSolutionsActivity.class));
            } else if (id == R.id.nav_all_services) {
                Intent intent = new Intent(this, ServicesActivity.class);
                intent.putExtra("isMyServices", false);
                startActivity(intent);
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, CategoriesActivity.class));
            } else if (id == R.id.nav_event_types) {
                startActivity(new Intent(this, EventTypeManagementActivity.class));
            } else if (id == R.id.nav_my_services) {
                Intent intent = new Intent(this, ServicesActivity.class);
                intent.putExtra("isMyServices", true);
                startActivity(intent);
            } else if (id == R.id.nav_all_products) {
                Intent intent = new Intent(this, AllProductsActivity.class);
                intent.putExtra("isMyProducts", false);
                startActivity(intent);
            } else if (id == R.id.nav_my_products) {
                Intent intent = new Intent(this, AllProductsActivity.class);
                intent.putExtra("isMyProducts", true);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    private void openProfile() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            return;
        }

        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("jwt_token");
        editor.remove("user_role");
        editor.remove("user_id");
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LogInActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationMenu();
    }
}