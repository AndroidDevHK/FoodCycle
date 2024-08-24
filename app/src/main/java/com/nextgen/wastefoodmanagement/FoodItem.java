package com.nextgen.wastefoodmanagement;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FoodItem implements Parcelable {
    private String foodItemID;
    private String name;
    private String description;
    private String imageUrl;
    private Timestamp expirationDate;
    private double price;
    private String status;
    private String postedBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private DocumentReference userRef;
    private User userDetails;

    // Default constructor required for calls to DataSnapshot.getValue(FoodItem.class)
    public FoodItem() {}

    public FoodItem(String foodItemID, String name, String description, String imageUrl, Timestamp expirationDate, double price, String status, String postedBy, Timestamp createdAt, Timestamp updatedAt, DocumentReference userRef) {
        this.foodItemID = foodItemID;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.expirationDate = expirationDate;
        this.price = price;
        this.status = status;
        this.postedBy = postedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userRef = userRef;
    }

    // Getters and setters for all fields
    public String getFoodItemID() {
        return foodItemID;
    }

    public void setFoodItemID(String foodItemID) {
        this.foodItemID = foodItemID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public void setUserRef(DocumentReference userRef) {
        this.userRef = userRef;
    }

    public User getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(User userDetails) {
        this.userDetails = userDetails;
    }

    // Parcelable implementation
    protected FoodItem(Parcel in) {
        foodItemID = in.readString();
        name = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        expirationDate = in.readParcelable(Timestamp.class.getClassLoader());
        price = in.readDouble();
        status = in.readString();
        postedBy = in.readString();
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        updatedAt = in.readParcelable(Timestamp.class.getClassLoader());
        String userRefPath = in.readString();
        if (userRefPath != null) {
            userRef = FirebaseFirestore.getInstance().document(userRefPath);
        }
        userDetails = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodItemID);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeParcelable(expirationDate, flags);
        dest.writeDouble(price);
        dest.writeString(status);
        dest.writeString(postedBy);
        dest.writeParcelable(createdAt, flags);
        dest.writeParcelable(updatedAt, flags);
        dest.writeString(userRef != null ? userRef.getPath() : null);
        dest.writeParcelable(userDetails, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodItem> CREATOR = new Creator<FoodItem>() {
        @Override
        public FoodItem createFromParcel(Parcel in) {
            return new FoodItem(in);
        }

        @Override
        public FoodItem[] newArray(int size) {
            return new FoodItem[size];
        }
    };
}
