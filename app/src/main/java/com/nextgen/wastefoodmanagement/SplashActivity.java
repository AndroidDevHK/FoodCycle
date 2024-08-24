package com.nextgen.wastefoodmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeComponents();

        if (permissionHelper.hasAllPermissions()) {
            checkUserAuthentication();
        } else {
            permissionHelper.requestPermissions();
        }
    }

    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        permissionHelper = new PermissionHelper(this);
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserData(currentUser.getUid());
        } else {
            navigateToLogin();
        }
    }


    private void fetchUserData(String userId) {
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            processUserData(document);
                        } else {
                            handleUserNotFound();
                        }
                    } else {
                        handleUserFetchFailure();
                    }
                });
    }

    private void processUserData(DocumentSnapshot document) {
        String email = document.getString("UserEmail");
        String name = document.getString("name");
        String userType = document.getString("UserType");

        if (userType == null) {
            showToast("User type not found.");
            navigateToLogin();
            return;
        }

        saveUserSessionDetails(document, email, name, userType);

        if (userType.equals("Food Provider")) {
            saveFoodProviderDetails(document);
            navigateToMain();
        } else if (userType.equals("Community Helper")) {
            navigateToMain();
        } else {
            handleUnknownUserType();
        }
    }

    private void saveUserSessionDetails(DocumentSnapshot document, String email, String name, String userType) {
        UserSessionDetails session = UserSessionDetails.getInstance(this);
        session.setUserEmail(email);
        session.setUserName(name);
        session.setUserType(userType);
        session.setUserDbRef(document.getId());
    }

    private void saveFoodProviderDetails(DocumentSnapshot document) {
        UserSessionDetails session = UserSessionDetails.getInstance(this);
        String phone = document.getString("phone");
        String whatsapp = document.getString("whatsapp");
        GeoPoint geoPoint = document.getGeoPoint("GeoPoint");
        String address = document.getString("address");
        String city = document.getString("city");

        session.setUserPhone(phone);
        session.setUserWhatsapp(whatsapp);
        session.setLocation(geoPoint);
        session.setUserAddress(address);
        session.setUserCity(city);
    }

    private void handleUserNotFound() {
        showToast("User data not found.");
        navigateToLogin();
    }

    private void handleUserFetchFailure() {
        showToast("Failed to fetch user data.");
        navigateToLogin();
    }

    private void handleUnknownUserType() {
        showToast("Unknown user type.");
        navigateToLogin();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper.onRequestPermissionsResult(requestCode, grantResults)) {
            checkUserAuthentication();
        } else {
            if (permissionHelper.shouldShowRequestPermissionRationale()) {
                permissionHelper.showPermissionsRequiredToast();
                permissionHelper.requestPermissions();
            } else {
                showToast("Permissions denied.");
                navigateToLogin();
            }
        }
    }
}
