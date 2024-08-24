package com.nextgen.wastefoodmanagement;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnSelectLocation;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeMap();
        initializeSelectLocationButton();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeSelectLocationButton() {
        btnSelectLocation = findViewById(R.id.btn_select_location);
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectLocationClick();
            }
        });
    }

    private void handleSelectLocationClick() {
        if (selectedLatLng != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedLatLng", selectedLatLng);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(MapActivity.this, "Please select a location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        moveToCurrentLocation();
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                handleMapClick(latLng);
            }
        });
    }

    private void handleMapClick(LatLng latLng) {
        selectedLatLng = latLng;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
    }

    private void moveToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            } else {
                moveToDefaultLocation();
            }
        }
    }

    private void moveToDefaultLocation() {
        LatLng defaultLocation = new LatLng(40.7128, -74.0060); // Default location (New York)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }
}
