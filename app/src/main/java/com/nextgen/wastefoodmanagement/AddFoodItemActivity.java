package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class AddFoodItemActivity extends AppCompatActivity {

    private static final int LOCATION_PICKER_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 3;

    private EditText foodNameEditText, foodDescriptionEditText, priceEditText;
    private MaterialButton saveButton;
    private GeoPoint selectedLocation;
    private Uri pictureUri;
    private File photoFile;
    private ImageView DateAndTimeSelector, selectImagePickerButton;
    private TextView expiration_date_text, SelectedDateTxtView, SelectedPictureTxtView, selectedPictureText;

    private Calendar expirationCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_item);
        FirebaseApp.initializeApp(this);

        initializeViews();
        configureToolbar();
        setListeners();

        expirationCalendar = Calendar.getInstance();
    }

    private void initializeViews() {
        foodNameEditText = findViewById(R.id.food_name);
        foodDescriptionEditText = findViewById(R.id.food_description);
        DateAndTimeSelector = findViewById(R.id.expiration_date_picker_button);
        priceEditText = findViewById(R.id.price);
        expiration_date_text = findViewById(R.id.expiration_date_text);
        SelectedDateTxtView= findViewById(R.id.SelectedDateTxtView);
        saveButton = findViewById(R.id.save_button);
        selectImagePickerButton = findViewById(R.id.select_image_picker_button);
        SelectedPictureTxtView = findViewById(R.id.SelectedPictureTxtView);
        selectedPictureText = findViewById(R.id.selected_picture_text);
        selectImagePickerButton = findViewById(R.id.select_image_picker_button);


    }

    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle the back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        DateAndTimeSelector.setOnClickListener(v -> showDateTimePickerDialog());


        saveButton.setOnClickListener(v -> saveFoodItemToFirestore());

        selectImagePickerButton.setOnClickListener(v -> showPicturePickerDialog());
    }

    private void showDateTimePickerDialog() {
        final Calendar currentCalendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            expirationCalendar.set(year, month, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                expirationCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                expirationCalendar.set(Calendar.MINUTE, minute);

                if (expirationCalendar.before(currentCalendar)) {
                    Toast.makeText(this, "Cannot select past date/time", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat localDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    expiration_date_text.setVisibility(View.VISIBLE);
                    expiration_date_text.setText(localDateFormat.format(expirationCalendar.getTime()));
                    SelectedDateTxtView.setText("Selected : ");
                }
            }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), false);

            timePickerDialog.show();
        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(currentCalendar.getTimeInMillis());
        datePickerDialog.show();
    }


    private void startLocationPickerActivity() {
        Intent intent = new Intent(AddFoodItemActivity.this, MapActivity.class);
        startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            handleLocationPickerResult(data);
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            handleGalleryResult(data);
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            handleCameraResult();
        }
    }

    private void handleLocationPickerResult(@Nullable Intent data) {
        if (data != null) {
            LatLng selectedLatLng = data.getParcelableExtra("selectedLatLng");
            selectedLocation = new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude);
        }
    }

    private void handleGalleryResult(@Nullable Intent data) {
        if (data != null) {
            pictureUri = data.getData();
            String fileName = getFileNameFromUri(pictureUri);
            copyImageToAppSpecificDirectory(pictureUri);
            updateSelectedPictureText(pictureUri, fileName);
        }
    }

    private void handleCameraResult() {
        if (photoFile != null) {
            pictureUri = Uri.fromFile(photoFile);
            String fileName = photoFile.getName();
            copyImageToAppSpecificDirectory(pictureUri);
            updateSelectedPictureText(pictureUri, fileName);
        }
    }
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }
    @SuppressLint("SetTextI18n")
    private void updateSelectedPictureText(Uri imageUri, String fileName) {
        String imagePath = imageUri.getPath();
        if (imagePath != null) {
            SelectedPictureTxtView.setText("Selected Image : ");
            selectedPictureText.setText(fileName);
            selectedPictureText.setVisibility(View.VISIBLE);
        }
    }

    private void showPicturePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Picture");
        builder.setItems(new CharSequence[]{"Select from Gallery", "Take Photo"}, (dialog, which) -> {
            switch (which) {
                case 0: // Select from Gallery
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
                    break;
                case 1: // Take Photo
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (photoFile != null) {
                            pictureUri = FileProvider.getUriForFile(this, "com.nextgen.wastefoodmanagement.fileprovider", photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                        }
                    }
                    break;
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void copyImageToAppSpecificDirectory(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File newFile = new File(storageDir, UUID.randomUUID().toString() + ".jpg");
            OutputStream outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            pictureUri = Uri.fromFile(newFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy image.", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveFoodItemToFirestore() {
        String name = foodNameEditText.getText().toString();
        String description = foodDescriptionEditText.getText().toString();
        String priceStr = priceEditText.getText().toString();

        if (!validateInput(name, description, priceStr)) {
            return;
        }

        double price = Double.parseDouble(priceStr);

        // Convert the expiration date and time to UTC
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(expirationCalendar.getTimeInMillis());
        Date expirationDateUTC = utcCalendar.getTime();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> foodItem;
        foodItem = createFoodItemMapResturantUser(name, description, expirationDateUTC, price);

        if (pictureUri != null) {
            ProgressDialogHelper.showProgressDialog(this,"Saving Food Item Details...");
            compressAndUploadImage(pictureUri, foodItem, db);
        } else {
            ProgressDialogHelper.showProgressDialog(this,"Saving Food Item Details...");
            saveFoodItemToFirestore(foodItem, db);
        }
    }


    private boolean validateInput(String name, String description, String price) {
        if (name.isEmpty()) {
            foodNameEditText.setError("Food name is required");
            foodNameEditText.requestFocus();
            return false;
        }
        if (description.isEmpty()) {
            foodDescriptionEditText.setError("Description is required");
            foodDescriptionEditText.requestFocus();
            return false;
        }
        if (price.isEmpty()) {
            priceEditText.setError("Price is required");
            priceEditText.requestFocus();
            return false;
        }
        if (expiration_date_text.getText().toString().isEmpty()) {
            showAlert("Please select an expiry date and time");
            return false;
        }
        if (pictureUri == null) {
            showAlert("Please select a food image");
            return false;
        }

        return true;
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private Map<String, Object> createFoodItemMapResturantUser(String name, String description, Date expirationDate, double price) {
        Map<String, Object> foodItem = new HashMap<>();
        foodItem.put("name", name);
        foodItem.put("description", description);
        foodItem.put("expirationDate", new Timestamp(expirationDate));
        foodItem.put("price", price);
        foodItem.put("status", "Available");
        foodItem.put("createdAt", FieldValue.serverTimestamp());
        foodItem.put("updatedAt", FieldValue.serverTimestamp());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(UserSessionDetails.getInstance(this).getUserDbRef());
        foodItem.put("userRef", userRef);
        foodItem.put("postedBy", UserSessionDetails.getInstance(this).getUserDbRef());

        return foodItem;
    }
    private void compressAndUploadImage(Uri imageUri, Map<String, Object> foodItem, FirebaseFirestore db) {
        ImageCompressHelper imageCompressHelper = new ImageCompressHelper(this);
        Uri compressedImageUri = imageCompressHelper.compressImage(imageUri);

        if (compressedImageUri != null) {
            uploadImageToFirebaseStorage(compressedImageUri, foodItem, db);
        } else {
            Toast.makeText(this, "Failed to compress image.", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImageToFirebaseStorage(Uri fileUri, Map<String, Object> foodItem, FirebaseFirestore db) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images/" + UUID.randomUUID().toString());

        UploadTask uploadTask = imagesRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            foodItem.put("imageUrl", downloadUri.toString());
            saveFoodItemToFirestore(foodItem, db);
            deleteImageFile(fileUri);  // Delete the image file after successful upload
        })).addOnFailureListener(exception -> Toast.makeText(AddFoodItemActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void deleteImageFile(Uri fileUri) {
        File file = new File(fileUri.getPath());
        if (file.exists()) {
            if (!file.delete()) {
                Log.e("Delete Image", "Failed to delete image file");
            }
        }
    }

    private void saveFoodItemToFirestore(Map<String, Object> foodItem, FirebaseFirestore db) {
        String foodItemID = UUID.randomUUID().toString();

        foodItem.put("foodItemID", foodItemID);

        db.collection("FoodItems").document(foodItemID)
                .set(foodItem)
                .addOnSuccessListener(documentReference -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Toast.makeText(AddFoodItemActivity.this, "Food item added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                    Toast.makeText(AddFoodItemActivity.this, "Failed to add food item", Toast.LENGTH_SHORT).show();
                });
    }
}
