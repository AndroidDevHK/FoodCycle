package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PersonalFoodItemAdapter extends RecyclerView.Adapter<PersonalFoodItemAdapter.FoodViewHolder> {

    private List<FoodItem> foodItemList;
    private Context context;
    private FirebaseFirestore firestore;
    private AdapterCallback callback;

    public PersonalFoodItemAdapter(Context context, List<FoodItem> foodItemList, AdapterCallback callback) {
        this.context = context;
        this.foodItemList = foodItemList;
        this.callback = callback;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.personal_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);
        bindData(holder, foodItem);
        setItemButtons(holder, foodItem);
    }

    private void bindData(FoodViewHolder holder, FoodItem foodItem) {
        holder.foodNameTextView.setText(foodItem.getName());
        holder.priceTextView.setText(String.format("%d PKR", (int) foodItem.getPrice()));
        holder.descriptionTextView.setText(foodItem.getDescription());
        holder.statusTextView.setText(foodItem.getStatus());

        Date expirationDate = foodItem.getExpirationDate().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(expirationDate);
        holder.expireAtTextView.setText(formattedDate);

        loadImage(holder, foodItem);

        if ("Sold".equals(foodItem.getStatus())) {
            holder.markAsSoldButton.setVisibility(View.GONE);
        } else {
            holder.markAsSoldButton.setVisibility(View.VISIBLE);
        }
    }

    private void loadImage(FoodViewHolder holder, FoodItem foodItem) {
        if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .timeout(10000); // 10 seconds timeout

            Glide.with(context)
                    .load(foodItem.getImageUrl())
                    .apply(requestOptions)
                    .error(R.mipmap.ic_launcher) // Placeholder image on error
                    .into(holder.foodImageView);
        } else {
            holder.foodImageView.setImageResource(R.mipmap.ic_launcher); // Placeholder image
        }
    }

    private void setItemButtons(FoodViewHolder holder, FoodItem foodItem) {
        holder.deleteItemButton.setOnClickListener(v -> showConfirmationDialog(foodItem, "delete"));
        holder.markAsSoldButton.setOnClickListener(v -> showConfirmationDialog(foodItem, "sold"));
    }

    private void showConfirmationDialog(FoodItem foodItem, String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        if ("delete".equals(action)) {
            builder.setMessage("Are you sure you want to delete " + foodItem.getName() + " (" + foodItem.getPrice() + " PKR)?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                ProgressDialogHelper.showProgressDialog(context, "Deleting...");
                deleteFoodItem(foodItem);
            });
        } else if ("sold".equals(action)) {
            builder.setMessage("Are you sure you want to mark " + foodItem.getName() + " (" + foodItem.getPrice() + " PKR) as sold?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                ProgressDialogHelper.showProgressDialog(context, "Marking as sold...");
                markAsSold(foodItem);
            });
        }
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void markAsSold(FoodItem foodItem) {
        firestore.collection("FoodItems").document(foodItem.getFoodItemID())
                .update("status", "Sold")
                .addOnSuccessListener(aVoid -> {
                    foodItem.setStatus("Sold");
                    notifyDataSetChanged();
                    ProgressDialogHelper.dismissProgressDialog();
                })
                .addOnFailureListener(e -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    // Handle the error
                });
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    private void deleteFoodItem(FoodItem foodItem) {
        firestore.collection("FoodItems").document(foodItem.getFoodItemID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    foodItemList.remove(foodItem);
                    notifyDataSetChanged();
                    checkAndUpdateUI();
                    ProgressDialogHelper.dismissProgressDialog();
                })
                .addOnFailureListener(e -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    // Handle the error
                });
    }

    private void checkAndUpdateUI() {
        if (foodItemList.size() == 0 && callback != null) {
            callback.onUpdateUI(0);
        }
    }

    public void updateList(List<FoodItem> filteredList) {
        this.foodItemList = filteredList;
        notifyDataSetChanged();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView, priceTextView, descriptionTextView, expireAtTextView, statusTextView;
        ImageView foodImageView;
        com.google.android.material.button.MaterialButton deleteItemButton;
        com.google.android.material.button.MaterialButton markAsSoldButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            expireAtTextView = itemView.findViewById(R.id.expireAtTextView);
            statusTextView = itemView.findViewById(R.id.StatusTextView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            deleteItemButton = itemView.findViewById(R.id.deleteItemButton);
            markAsSoldButton = itemView.findViewById(R.id.MarkAsSoldButton);
        }
    }

    public interface AdapterCallback {
        void onUpdateUI(int itemCount);
    }
}
