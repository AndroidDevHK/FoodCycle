package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    private FoodItem foodItem;
    private String imageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        LinearLayout contactLayout = findViewById(R.id.contactLayout);

        contactLayout.setOnClickListener(v -> showContactDialog());
        foodItem = getIntent().getParcelableExtra("foodItem");
        imageUrl = getIntent().getStringExtra("imageUrl");

        loadFoodImage();
        setFoodDetails();
        setupViewLocationButton();
    }

    private void showContactDialog() {
        ContactDialogHelper dialogHelper = new ContactDialogHelper(this,foodItem.getUserDetails().getPhone(),foodItem.getUserDetails().getWhatsapp());
        dialogHelper.showContactDialog();
    }


    private void loadFoodImage() {
        ImageView foodImageView = findViewById(R.id.food_image_view);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions().fitCenter())
                    .into(foodImageView);
        } else {
            foodImageView.setImageResource(R.mipmap.ic_launcher); // Placeholder image
        }
    }

    private void setFoodDetails() {
        TextView foodNameTextView = findViewById(R.id.food_name_text_view);
        TextView foodDescriptionTextView = findViewById(R.id.food_description_text_view);
        TextView foodPriceTextView = findViewById(R.id.food_price_text_view);
        TextView locationTextView = findViewById(R.id.LocationtxtView);
        TextView foodExpirationTextView = findViewById(R.id.food_expiration_text_view);
        TextView ResturanttNameTextView = findViewById(R.id.restaurant_name_text_view);

        if (foodItem != null) {
            foodNameTextView.setText(foodItem.getName());
            foodDescriptionTextView.setText(foodItem.getDescription());
            setFoodPriceText(foodPriceTextView);
            ResturanttNameTextView.setText(foodItem.getUserDetails().getName());
            locationTextView.setText(foodItem.getUserDetails().getAddress());
            setExpirationDateText(foodExpirationTextView);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setFoodPriceText(TextView foodPriceTextView) {
        if (foodItem.getPrice() == 0) {
            foodPriceTextView.setText("Free");
        } else {
            foodPriceTextView.setText(String.format("PKR %d", (int) foodItem.getPrice()));
        }
    }

    @SuppressLint("SetTextI18n")
    private void setExpirationDateText(TextView foodExpirationTextView) {
        Timestamp expirationTimestamp = foodItem.getExpirationDate();
        if (expirationTimestamp != null) {
            Date expirationDate = expirationTimestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(expirationDate);
            foodExpirationTextView.setText(formattedDate);
        } else {
            foodExpirationTextView.setText("No expiration date available");
        }
    }



    private void setupViewLocationButton() {
        Button viewLocationButton = findViewById(R.id.view_location_button);
        viewLocationButton.setOnClickListener(v -> {
            if (foodItem != null && foodItem.getUserDetails().getGeoPoint() != null) {
                GeoPoint location = foodItem.getUserDetails().getGeoPoint();
                Intent intent = new Intent(FoodDetailActivity.this, DisplayFoodLocationActivity.class);
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                startActivity(intent);
            }
        });
    }
}
