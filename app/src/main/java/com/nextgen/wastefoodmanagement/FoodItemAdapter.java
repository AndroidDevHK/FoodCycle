package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItems;
    private Context context;
    private LatLng currentLocation;

    public FoodItemAdapter(List<FoodItem> foodItems, Context context) {
        this.foodItems = foodItems;
        this.context = context;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.foodName.setText(foodItem.getName());
        holder.foodPrice.setText(formatPrice(foodItem.getPrice()));


        // Set the posted by name
        if (foodItem.getUserDetails() != null) {
            holder.postedByName.setText(foodItem.getUserDetails().getName());
        } else {
            holder.postedByName.setText("Unknown");
        }

        holder.City.setText(foodItem.getUserDetails().getCity());
        displayDistance(holder, foodItem);
        loadImage(holder, foodItem);
        setCardViewClickListener(holder, foodItem);
    }
    private String formatPrice(double price) {
        if (price == 0) {
            return "Free";
        } else if (price == (int) price) {
            // If the price is a whole number, return it as an integer string
            return "PKR " + (int) price;
        } else {
            // If the price has a fractional part, return it with two decimal places
            return String.format("PKR %.2f", price);
        }
    }
    @SuppressLint("SetTextI18n")
    private void displayDistance(FoodItemViewHolder holder, FoodItem foodItem) {
        if (currentLocation != null && foodItem.getUserDetails() != null && foodItem.getUserDetails().getGeoPoint() != null) {
            GeoPoint geoPoint = foodItem.getUserDetails().getGeoPoint();
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    geoPoint.getLatitude(), geoPoint.getLongitude(), results);
            float distanceInMeters = results[0];

            String distanceText;
            if (distanceInMeters < 1000) {
                distanceText = (int) distanceInMeters + " meters away";
            } else {
                distanceText = (int) (distanceInMeters / 1000) + " km away";
            }

            holder.foodDistance.setText(distanceText);
        } else {
            holder.foodDistance.setText("Distance not available");
        }
    }

    private void loadImage(FoodItemViewHolder holder, FoodItem foodItem) {
        if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .timeout(10000); // 10 seconds timeout

            Glide.with(context)
                    .load(foodItem.getImageUrl())
                    .apply(requestOptions)
                    .error(R.mipmap.ic_launcher) // Placeholder image on error
                    .into(holder.foodImage);
        } else {
            holder.foodImage.setImageResource(R.drawable.ic_na); // Placeholder image
        }
    }

    private void setCardViewClickListener(FoodItemViewHolder holder, FoodItem foodItem) {
        holder.foodCardView.setOnClickListener(v -> {
            // Debug log
            Log.d("FoodItemAdapter", "CardView clicked for item: " + foodItem.getName());

            Intent intent = new Intent(context, FoodDetailActivity.class);
            intent.putExtra("foodItem", foodItem);
            intent.putExtra("imageUrl", foodItem.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public void updateList(List<FoodItem> filteredList) {
        this.foodItems = filteredList;
        notifyDataSetChanged();
    }

    public static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodDistance, foodPrice, postedByName,City;
        ImageView foodImage;
        LinearLayout foodCardView;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.food_name);
            foodDistance = itemView.findViewById(R.id.food_distance);
            foodImage = itemView.findViewById(R.id.food_image);
            postedByName = itemView.findViewById(R.id.posted_by_name);
            foodCardView = itemView.findViewById(R.id.food_card_view);
            foodPrice = itemView.findViewById(R.id.food_price);
            City = itemView.findViewById(R.id.food_location);
        }
    }
}
