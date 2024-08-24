package com.nextgen.wastefoodmanagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private LinearLayout phoneLayout, whatsappLayout, cityLayout, addressLayout, organizationLocationLayout;
    private TextView nameTextView, emailTextView, phoneTextView, whatsappTextView, cityTextView, addressTextView, organizationLocationTextView, userTypeTextView;
    private MaterialButton logoutButton;
    private ImageView viewLocationButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeViews(view);
        populateData();
        setupLogoutButton();
        setupViewLocationButton();
        return view;
    }

    private void initializeViews(View view) {
        phoneLayout = view.findViewById(R.id.phone_number_layout);
        whatsappLayout = view.findViewById(R.id.whatsapp_layout);
        cityLayout = view.findViewById(R.id.city_layout);
        addressLayout = view.findViewById(R.id.address_layout);
        organizationLocationLayout = view.findViewById(R.id.ViewLocationLayout);

        nameTextView = view.findViewById(R.id.food_provider_name_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        phoneTextView = view.findViewById(R.id.phone_number_text_view);
        whatsappTextView = view.findViewById(R.id.whatsapp_text_view);
        cityTextView = view.findViewById(R.id.city_text_view);
        addressTextView = view.findViewById(R.id.address_text_view);
        organizationLocationTextView = view.findViewById(R.id.selected_location_TextView);
        userTypeTextView = view.findViewById(R.id.user_type_text_view);

        logoutButton = view.findViewById(R.id.view_location_button);
        viewLocationButton = view.findViewById(R.id.select_location_picker_IMG_Button);
    }

    private void populateData() {
        UserSessionDetails userDetails = UserSessionDetails.getInstance(getActivity());

        nameTextView.setText(userDetails.getUserName());
        emailTextView.setText(userDetails.getUserEmail());
        userTypeTextView.setText(userDetails.getUserType());

        if ("Community Helper".equals(userDetails.getUserType())) {
            phoneLayout.setVisibility(View.GONE);
            whatsappLayout.setVisibility(View.GONE);
            cityLayout.setVisibility(View.GONE);
            addressLayout.setVisibility(View.GONE);
            organizationLocationLayout.setVisibility(View.GONE);
        } else {
            phoneTextView.setText(userDetails.getUserPhone());
            whatsappTextView.setText(userDetails.getUserWhatsapp());
            cityTextView.setText(userDetails.getUserCity());
            addressTextView.setText(userDetails.getUserAddress());
            organizationLocationTextView.setText(userDetails.getLocation().toString());
            organizationLocationLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void setupViewLocationButton() {
        viewLocationButton.setOnClickListener(v -> {
            UserSessionDetails userDetails = UserSessionDetails.getInstance(getActivity());
            if (userDetails.getLocation() != null) {
                Intent intent = new Intent(getActivity(), DisplayFoodLocationActivity.class);
                intent.putExtra("latitude", userDetails.getLocation().getLatitude());
                intent.putExtra("longitude", userDetails.getLocation().getLongitude());
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "No location data available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
