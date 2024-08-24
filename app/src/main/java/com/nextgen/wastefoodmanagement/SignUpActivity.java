package com.nextgen.wastefoodmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final int MAP_ACTIVITY_REQUEST_CODE = 1;

    private LinearLayout foodProvidersSignupLayout, communitySignupLayout;
    private EditText foodProviderName, emailFoodProvider, phoneFoodProvider, whatsappFoodProvider, cityFoodProvider, addressFoodProvider, passwordFoodProvider, confirmPasswordFoodProvider;
    private EditText communityName, emailCommunity, passwordCommunity, confirmPasswordCommunity;
    private Spinner signupTypeSpinner;
    private MaterialButton signUpButton;
    private TextView selectLocationTextView, selectedLocationTextView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GeoPoint selectedGeoPoint;

    private static final String ERROR_EMPTY_FIELD = "Please fill in all fields.";
    private static final String ERROR_PASSWORDS_MISMATCH = "Passwords do not match.";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email address.";
    private static final String ERROR_PASSWORD_TOO_SHORT = "Password must be at least 6 characters.";
    private static final String ERROR_INVALID_PHONE_FORMAT = "Phone number must be in the format 03XXXXXXXXX.";
    private static final String SUCCESS_SIGNUP = "Sign up successful!";
    private static final String FAILURE_SIGNUP = "Failed to sign up.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupSpinner();
        setupToolbar();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        foodProvidersSignupLayout = findViewById(R.id.FoodProvidersSignupLayout);
        communitySignupLayout = findViewById(R.id.CommunitySignupLayout);

        // Food Provider fields
        foodProviderName = findViewById(R.id.FoodProvidersEditText);
        emailFoodProvider = findViewById(R.id.EmailFoodProviders);
        phoneFoodProvider = findViewById(R.id.FoodProvidersPhoneNo);
        whatsappFoodProvider = findViewById(R.id.FoodProvidersWhatsapp);
        cityFoodProvider = findViewById(R.id.FoodProvidersCity);
        addressFoodProvider = findViewById(R.id.FoodProvidersAddress);
        passwordFoodProvider = findViewById(R.id.passwordInstitute);
        confirmPasswordFoodProvider = findViewById(R.id.confirmPasswordInstitute);
        selectLocationTextView = findViewById(R.id.SelectLocationTxtView);
        selectedLocationTextView = findViewById(R.id.selected_location_TextView);

        // Community Helper fields
        communityName = findViewById(R.id.fullnameCommunity);
        emailCommunity = findViewById(R.id.EmailCommunityEditText);
        passwordCommunity = findViewById(R.id.passwordCommunity);
        confirmPasswordCommunity = findViewById(R.id.confirmPasswordCommunity);

        // Spinner and button
        signupTypeSpinner = findViewById(R.id.spinnerSignupTypes);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> signUp());

        findViewById(R.id.select_location_picker_IMG_Button).setOnClickListener(v -> openMapActivity());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.signup_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        signupTypeSpinner.setAdapter(adapter);

        signupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                handleSpinnerSelection(parentView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void handleSpinnerSelection(String selectedItem) {
        foodProvidersSignupLayout.setVisibility(selectedItem.equals("Food Provider") ? View.VISIBLE : View.GONE);
        communitySignupLayout.setVisibility(selectedItem.equals("Community Helper") ? View.VISIBLE : View.GONE);
        signUpButton.setVisibility(View.VISIBLE);
    }

    private void openMapActivity() {
        Intent intent = new Intent(SignUpActivity.this, MapActivity.class);
        startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE);
    }

    private void signUp() {
        String selectedItem = signupTypeSpinner.getSelectedItem().toString();
        if (selectedItem.equals("Food Provider")) {
            signUpFoodProvider();
        } else if (selectedItem.equals("Community Helper")) {
            signUpCommunityHelper();
        }
    }

    private void signUpFoodProvider() {
        String email = emailFoodProvider.getText().toString().trim();
        String name = foodProviderName.getText().toString().trim();
        String phone = phoneFoodProvider.getText().toString().trim();
        String whatsapp = whatsappFoodProvider.getText().toString().trim();
        String city = cityFoodProvider.getText().toString().trim();
        String address = addressFoodProvider.getText().toString().trim();
        String password = passwordFoodProvider.getText().toString().trim();
        String confirmPassword = confirmPasswordFoodProvider.getText().toString().trim();

        if (!isInputValidFoodProvider(email, name, phone, whatsapp, city, address, password, confirmPassword)) {
            return;
        }

        if (selectedGeoPoint == null) {
            selectLocationTextView.setError("Please select a location");
            return;
        }

        showConfirmationDialogForFoodProvider(email, name, phone, whatsapp, city, address, password, selectedGeoPoint);
    }

    private void signUpCommunityHelper() {
        String email = emailCommunity.getText().toString().trim();
        String name = communityName.getText().toString().trim();
        String password = passwordCommunity.getText().toString().trim();
        String confirmPassword = confirmPasswordCommunity.getText().toString().trim();

        if (!isInputValidCommunityHelper(email, name, password, confirmPassword)) {
            return;
        }

        showConfirmationDialogForCommunityHelper(email, name, password);
    }

    private boolean isInputValidFoodProvider(String email, String name, String phone, String whatsapp, String city, String address, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            foodProviderName.setError("Food provider name can't be empty");
            foodProviderName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailFoodProvider.setError("Email can't be empty");
            emailFoodProvider.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailFoodProvider.setError(ERROR_INVALID_EMAIL);
            emailFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneFoodProvider.setError("Phone number can't be empty");
            phoneFoodProvider.requestFocus();
            return false;
        } else if (!phone.matches("^03[0-9]{9}$")) {
            phoneFoodProvider.setError(ERROR_INVALID_PHONE_FORMAT);
            phoneFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(whatsapp)) {
            whatsappFoodProvider.setError("Whatsapp number can't be empty");
            whatsappFoodProvider.requestFocus();
            return false;
        } else if (!whatsapp.matches("^03[0-9]{9}$")) {
            whatsappFoodProvider.setError(ERROR_INVALID_PHONE_FORMAT);
            whatsappFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(city)) {
            cityFoodProvider.setError("City can't be empty");
            cityFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            addressFoodProvider.setError("Address can't be empty");
            addressFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordFoodProvider.setError("Password can't be empty");
            passwordFoodProvider.requestFocus();
            return false;
        } else if (password.length() < 6) {
            passwordFoodProvider.setError(ERROR_PASSWORD_TOO_SHORT);
            passwordFoodProvider.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordFoodProvider.setError("Confirm password can't be empty");
            confirmPasswordFoodProvider.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordFoodProvider.setError(ERROR_PASSWORDS_MISMATCH);
            confirmPasswordFoodProvider.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isInputValidCommunityHelper(String email, String name, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            communityName.setError("Community helper name can't be empty");
            communityName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailCommunity.setError("Email can't be empty");
            emailCommunity.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailCommunity.setError(ERROR_INVALID_EMAIL);
            emailCommunity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordCommunity.setError("Password can't be empty");
            passwordCommunity.requestFocus();
            return false;
        } else if (password.length() < 6) {
            passwordCommunity.setError(ERROR_PASSWORD_TOO_SHORT);
            passwordCommunity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordCommunity.setError("Confirm password can't be empty");
            confirmPasswordCommunity.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordCommunity.setError(ERROR_PASSWORDS_MISMATCH);
            confirmPasswordCommunity.requestFocus();
            return false;
        }

        return true;
    }

    private void showConfirmationDialogForFoodProvider(String email, String name, String phone, String whatsapp, String city, String address, String password, GeoPoint geoPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Sign Up");

        String message = "<b>Email:</b> " + email +
                "<br><b>Name:</b> " + name +
                "<br><b>Phone:</b> " + phone +
                "<br><b>WhatsApp:</b> " + whatsapp +
                "<br><b>City:</b> " + city +
                "<br><b>Address:</b> " + address +
                "<br><b>User Type:</b> Food Provider" +
                "<br><b>Location:</b> " + geoPoint.getLatitude() + ", " + geoPoint.getLongitude();

        builder.setMessage(android.text.Html.fromHtml(message));

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            ProgressDialogHelper.showProgressDialog(SignUpActivity.this, "Creating Account...");
            registerUserForFoodProvider(email, password, name, phone, whatsapp, city, address, geoPoint);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmationDialogForCommunityHelper(String email, String name, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Sign Up");

        String message = "<b>Email:</b> " + email +
                "<br><b>Name:</b> " + name +
                "<br><b>User Type:</b> Community Helper";

        builder.setMessage(android.text.Html.fromHtml(message));

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            ProgressDialogHelper.showProgressDialog(SignUpActivity.this, "Creating Account...");
            registerUserForCommunityHelper(email, password, name);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerUserForFoodProvider(String email, String password, String name, String phone, String whatsapp, String city, String address, GeoPoint geoPoint) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserData(userId, email, name, phone, whatsapp, city, address, "Food Provider", geoPoint, password);
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void registerUserForCommunityHelper(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserData(userId, email, name, "Community Helper", password);
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void saveUserData(String userId, String email, String name, String phone, String whatsapp, String city, String address, String userType, GeoPoint geoPoint, String password) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("UserEmail", email);
        userMap.put("UUID", userId);
        userMap.put("name", name);
        userMap.put("phone", phone);
        userMap.put("whatsapp", whatsapp);
        userMap.put("city", city);
        userMap.put("address", address);
        userMap.put("UserType", userType);
        userMap.put("GeoPoint", geoPoint);

        db.collection("Users")
                .document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    handleUserDataSaveResult(task.isSuccessful());
                });
    }

    private void saveUserData(String userId, String email, String name, String userType, String password) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("UserEmail", email);
        userMap.put("name", name);
        userMap.put("UUID", userId);
        userMap.put("UserType", userType);

        db.collection("Users")
                .document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    handleUserDataSaveResult(task.isSuccessful());
                });
    }

    private void handleUserDataSaveResult(boolean isSuccess) {
        if (isSuccess) {
            showToast(SUCCESS_SIGNUP);
            ProgressDialogHelper.dismissProgressDialog();
            finish();
        } else {
            showToast(FAILURE_SIGNUP);
            ProgressDialogHelper.dismissProgressDialog();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            handleMapActivityResult(data);
        }
    }

    private void handleMapActivityResult(Intent data) {
        LatLng selectedLatLng = data.getParcelableExtra("selectedLatLng");
        if (selectedLatLng != null) {
            selectedGeoPoint = new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude);
            selectLocationTextView.setVisibility(View.GONE);
            selectedLocationTextView.setText("Location Selected");
            selectedLocationTextView.setVisibility(View.VISIBLE);
        }
    }
}
