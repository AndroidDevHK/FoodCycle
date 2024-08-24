package com.nextgen.wastefoodmanagement;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.GeoPoint;

public class UserSessionDetails {

    private static final String USER_SHARED_PREFS = "USER_SHARED_PREFS";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_WHATSAPP = "user_whatsapp";
    private static final String KEY_USER_LATITUDE = "user_latitude";
    private static final String KEY_USER_LONGITUDE = "user_longitude";
    private static final String KEY_USER_ADDRESS = "user_address";
    private static final String KEY_USER_CITY = "user_city";
    private static final String KEY_USER_DB_REF = "user_db_ref";

    private static UserSessionDetails instance;
    private Context context;

    private String userEmail;
    private String userName;
    private String userType;
    private String userPhone;
    private String userWhatsapp;
    private GeoPoint location;
    private String userAddress;
    private String userCity;
    private String userDbRef;

    private UserSessionDetails(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized UserSessionDetails getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionDetails(context);
        }
        return instance;
    }

    public String getUserEmail() {
        if (userEmail == null || userEmail.isEmpty()) {
            loadUserEmailFromPrefs();
        }
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        saveUserEmailToPrefs(userEmail);
    }

    public String getUserName() {
        if (userName == null || userName.isEmpty()) {
            loadUserNameFromPrefs();
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        saveUserNameToPrefs(userName);
    }

    public String getUserType() {
        if (userType == null || userType.isEmpty()) {
            loadUserTypeFromPrefs();
        }
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
        saveUserTypeToPrefs(userType);
    }

    public String getUserPhone() {
        if (userPhone == null || userPhone.isEmpty()) {
            loadUserPhoneFromPrefs();
        }
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
        saveUserPhoneToPrefs(userPhone);
    }

    public String getUserWhatsapp() {
        if (userWhatsapp == null || userWhatsapp.isEmpty()) {
            loadUserWhatsappFromPrefs();
        }
        return userWhatsapp;
    }

    public void setUserWhatsapp(String userWhatsapp) {
        this.userWhatsapp = userWhatsapp;
        saveUserWhatsappToPrefs(userWhatsapp);
    }

    public GeoPoint getLocation() {
        if (location == null) {
            loadLocationFromPrefs();
        }
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
        saveLocationToPrefs(location);
    }

    public String getUserAddress() {
        if (userAddress == null || userAddress.isEmpty()) {
            loadUserAddressFromPrefs();
        }
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        saveUserAddressToPrefs(userAddress);
    }

    public String getUserCity() {
        if (userCity == null || userCity.isEmpty()) {
            loadUserCityFromPrefs();
        }
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
        saveUserCityToPrefs(userCity);
    }

    public String getUserDbRef() {
        if (userDbRef == null || userDbRef.isEmpty()) {
            loadUserDbRefFromPrefs();
        }
        return userDbRef;
    }

    public void setUserDbRef(String userDbRef) {
        this.userDbRef = userDbRef;
        saveUserDbRefToPrefs(userDbRef);
    }

    private void saveUserEmailToPrefs(String userEmail) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    private void loadUserEmailFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    private void saveUserNameToPrefs(String userName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    private void loadUserNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userName = sharedPreferences.getString(KEY_USER_NAME, "");
    }

    private void saveUserTypeToPrefs(String userType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }

    private void loadUserTypeFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userType = sharedPreferences.getString(KEY_USER_TYPE, "");
    }

    private void saveUserPhoneToPrefs(String userPhone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.apply();
    }

    private void loadUserPhoneFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    private void saveUserWhatsappToPrefs(String userWhatsapp) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_WHATSAPP, userWhatsapp);
        editor.apply();
    }

    private void loadUserWhatsappFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userWhatsapp = sharedPreferences.getString(KEY_USER_WHATSAPP, "");
    }

    private void saveLocationToPrefs(GeoPoint location) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_LATITUDE, String.valueOf(location.getLatitude()));
        editor.putString(KEY_USER_LONGITUDE, String.valueOf(location.getLongitude()));
        editor.apply();
    }

    private void loadLocationFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        double latitude = Double.parseDouble(sharedPreferences.getString(KEY_USER_LATITUDE, "0"));
        double longitude = Double.parseDouble(sharedPreferences.getString(KEY_USER_LONGITUDE, "0"));
        location = new GeoPoint(latitude, longitude);
    }

    private void saveUserAddressToPrefs(String userAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ADDRESS, userAddress);
        editor.apply();
    }

    private void loadUserAddressFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userAddress = sharedPreferences.getString(KEY_USER_ADDRESS, "");
    }

    private void saveUserCityToPrefs(String userCity) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_CITY, userCity);
        editor.apply();
    }

    private void loadUserCityFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userCity = sharedPreferences.getString(KEY_USER_CITY, "");
    }

    private void saveUserDbRefToPrefs(String userDbRef) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_DB_REF, userDbRef);
        editor.apply();
    }

    private void loadUserDbRefFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userDbRef = sharedPreferences.getString(KEY_USER_DB_REF, "");
    }

    public void clearSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        userEmail = null;
        userName = null;
        userType = null;
        userPhone = null;
        userWhatsapp = null;
        location = null;
        userAddress = null;
        userCity = null; // Clear city
        userDbRef = null;
    }
}
