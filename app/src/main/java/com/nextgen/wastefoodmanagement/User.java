package com.nextgen.wastefoodmanagement;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

public class User implements Parcelable {
    private GeoPoint geoPoint;
    private String uuid;
    private String userEmail;
    private String userType;
    private String address;
    private String city;
    private String name;
    private String phone;
    private String whatsapp;

    public User() {}

    public User(GeoPoint geoPoint, String uuid, String userEmail, String userType, String address, String city, String name, String phone, String whatsapp) {
        this.geoPoint = geoPoint;
        this.uuid = uuid;
        this.userEmail = userEmail;
        this.userType = userType;
        this.address = address;
        this.city = city;
        this.name = name;
        this.phone = phone;
        this.whatsapp = whatsapp;
    }

    // Getters and setters for all fields
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        double lat = in.readDouble();
        double lon = in.readDouble();
        geoPoint = new GeoPoint(lat, lon);
        uuid = in.readString();
        userEmail = in.readString();
        userType = in.readString();
        address = in.readString();
        city = in.readString();
        name = in.readString();
        phone = in.readString();
        whatsapp = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(geoPoint != null ? geoPoint.getLatitude() : 0);
        dest.writeDouble(geoPoint != null ? geoPoint.getLongitude() : 0);
        dest.writeString(uuid);
        dest.writeString(userEmail);
        dest.writeString(userType);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(whatsapp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
