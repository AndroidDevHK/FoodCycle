package com.nextgen.wastefoodmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class LoginActivity extends AppCompatActivity {

    private EditText userIDTextView, passwordTextView;
    private MaterialButton loginButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String ERROR_EMPTY_FIELD = "Please fill in all fields.";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email address.";
    private static final String ERROR_INVALID_CREDENTIALS = "Invalid email or password.";
    private ImageView visibilityOnIcon, visibilityOffIcon;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFirebase();
        initializeViews();
        setupLoginButton();
        setupPasswordVisibilityToggle();

    }

    private void setupPasswordVisibilityToggle() {
        visibilityOnIcon.setOnClickListener(v -> togglePasswordVisibility());

        visibilityOffIcon.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordTextView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            visibilityOnIcon.setVisibility(View.GONE);
            visibilityOffIcon.setVisibility(View.VISIBLE);
        } else {
            passwordTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            visibilityOnIcon.setVisibility(View.VISIBLE);
            visibilityOffIcon.setVisibility(View.GONE);
        }
        isPasswordVisible = !isPasswordVisible;

        passwordTextView.setSelection(passwordTextView.getText().length());
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        visibilityOnIcon = findViewById(R.id.visibilityOnIcon);
        visibilityOffIcon = findViewById(R.id.visibilityOffIcon);
        userIDTextView = findViewById(R.id.userIDTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        loginButton = findViewById(R.id.LoginBtn);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        String email = userIDTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (isInputValid(email, password)) {
            authenticateUser(email, password);
        }
    }

    private boolean isInputValid(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            userIDTextView.setError(ERROR_EMPTY_FIELD);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userIDTextView.setError(ERROR_INVALID_EMAIL);
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordTextView.setError(ERROR_EMPTY_FIELD);
            return false;
        }

        return true;
    }

    private void authenticateUser(String email, String password) {
        ProgressDialogHelper.showProgressDialog(this, "Authenticating....");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(user.getUid());
                        }
                    } else {
                        showToast(ERROR_INVALID_CREDENTIALS);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void fetchUserData(String userId) {
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            handleUserData(document);
                        } else {
                            showToast("User data not found.");
                            ProgressDialogHelper.dismissProgressDialog();
                        }
                    } else {
                        showToast("Failed to fetch user data.");
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void handleUserData(DocumentSnapshot document) {
        String email = document.getString("UserEmail");
        String name = document.getString("name");
        String userType = document.getString("UserType");

        if (userType == null) {
            showToast("User type not found.");
            ProgressDialogHelper.dismissProgressDialog();
            return;
        }

        UserSessionDetails session = UserSessionDetails.getInstance(this);
        session.setUserEmail(email);
        session.setUserName(name);
        session.setUserType(userType);
        session.setUserDbRef(document.getId()); // Save the UserDatabase reference

        if (userType.equals("Food Provider")) {
            handleFoodProviderUser(document, session);
        } else if (userType.equals("Community Helper")) {
            navigateToMainActivity();
        } else {
            showToast("Unknown user type.");
            ProgressDialogHelper.dismissProgressDialog();
        }
    }

    private void handleFoodProviderUser(DocumentSnapshot document, UserSessionDetails session) {
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

        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        ProgressDialogHelper.dismissProgressDialog();
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void OpenSignUpActivity(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
