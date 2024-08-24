package com.nextgen.wastefoodmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainMenuActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initializeNavigationComponents();
        setupBottomNavigation();
        handleCommunityHelperVisibility();
        setupNavigationListener();
    }

    private void initializeNavigationComponents() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void handleCommunityHelperVisibility() {
        if ("Community Helper".equals(UserSessionDetails.getInstance(this).getUserType())) {
            bottomNavigationView.getMenu().removeItem(R.id.listingManagementFragment);
        }
    }

    private void setupNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> handleNavigationItemSelected(item.getItemId()));
    }

    private boolean handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.homeFragment) {
            navController.navigate(R.id.homeFragment);
            return true;
        } else if (itemId == R.id.listingManagementFragment) {
            navController.navigate(R.id.listingManagementFragment);
            return true;
        } else if (itemId == R.id.profileFragment) {
            navController.navigate(R.id.profileFragment);
            return true;
        } else {
            return false;
        }
    }
}
