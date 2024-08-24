package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private LatLng currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Toolbar toolbar;
    private View emptyStateContainer;
    private MenuItem searchItem;
    private SearchView searchView;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupToolbar(view);
        setupRecyclerView();
        getLocationPermission();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupToolbar(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            // Disable the back button
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setHomeButtonEnabled(false);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Items");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFoodItems(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFoodItems(newText);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupRecyclerView() {
        foodItems = new ArrayList<>();
        adapter = new FoodItemAdapter(foodItems, getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void fetchFoodItems() {
        showProgressBar();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = getFoodItemsQuery(db);

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            hideProgressBar();
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                if (queryDocumentSnapshots.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    handleDocumentChanges(queryDocumentSnapshots.getDocumentChanges());
                }
            }
        });
    }

    private Query getFoodItemsQuery(FirebaseFirestore db) {
        String userType = UserSessionDetails.getInstance(getActivity()).getUserType();
        Query query = db.collection("FoodItems").whereEqualTo("status", "Available");

        if ("Community Helper".equals(userType)) {
            query = query.whereEqualTo("price", 0);
        } else {
            query = query.whereGreaterThan("price", 0); // Exclude 0 price items for non-community users
        }

        return query;
    }


    private void handleDocumentChanges(List<DocumentChange> documentChanges) {
        for (DocumentChange dc : documentChanges) {
            FoodItem item = dc.getDocument().toObject(FoodItem.class);
            switch (dc.getType()) {
                case ADDED:
                    if (!isExpired(item.getExpirationDate())) {
                        fetchUserDetails(item);
                    }
                    break;
                case MODIFIED:
                    handleModifiedItem(item);
                    break;
                case REMOVED:
                    handleRemovedItem(item);
                    break;
            }
        }
    }

    private void handleModifiedItem(FoodItem item) {
        for (int i = 0; i < foodItems.size(); i++) {
            if (foodItems.get(i).getFoodItemID().equals(item.getFoodItemID())) {
                if ("Sold".equals(item.getStatus())) {
                    foodItems.remove(i);
                    adapter.notifyItemRemoved(i);
                } else {
                    foodItems.set(i, item);
                    fetchUserDetails(item);
                }
                break;
            }
        }
    }

    private void handleRemovedItem(FoodItem item) {
        for (int i = 0; i < foodItems.size(); i++) {
            if (foodItems.get(i).getFoodItemID().equals(item.getFoodItemID())) {
                foodItems.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void fetchUserDetails(FoodItem item) {
        DocumentReference userRef = item.getUserRef();
        if (userRef != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = createUserFromSnapshot(documentSnapshot);
                    item.setUserDetails(user);
                    handleFetchedUserDetails(item, user);
                } else {
                    Log.w("Firestore", "User document does not exist.");
                }
            }).addOnFailureListener(e -> {
                Log.w("Firestore", "Failed to fetch user details", e);
            });
        } else {
            Log.w("UserRef", "User reference is null for item: " + item.getName());
        }
    }

    private User createUserFromSnapshot(DocumentSnapshot documentSnapshot) {
        User user = new User();
        user.setGeoPoint(documentSnapshot.getGeoPoint("GeoPoint"));
        user.setUuid(documentSnapshot.getString("UUID"));
        user.setUserEmail(documentSnapshot.getString("UserEmail"));
        user.setUserType(documentSnapshot.getString("UserType"));
        user.setAddress(documentSnapshot.getString("address"));
        user.setCity(documentSnapshot.getString("city"));
        user.setName(documentSnapshot.getString("name"));
        user.setPhone(documentSnapshot.getString("phone"));
        user.setWhatsapp(documentSnapshot.getString("whatsapp"));
        return user;
    }

    private void handleFetchedUserDetails(FoodItem item, User user) {
        if (user.getGeoPoint() != null) {
            if (!foodItems.contains(item)) {
                foodItems.add(item);
                adapter.notifyItemInserted(foodItems.size() - 1);
            } else {
                int index = foodItems.indexOf(item);
                foodItems.set(index, item);
                adapter.notifyItemChanged(index);
            }
            startExpirationTimer(item);
            sortFoodItemsByDistance();
            adapter.setCurrentLocation(currentLocation);
            adapter.notifyDataSetChanged();
            Log.d("FetchUserDetails", "Food item added: " + item.getName());
        } else {
            Log.w("GeoPoint", "GeoPoint is null for user: " + user.getName());
        }
    }

    private boolean isExpired(Timestamp expirationTimestamp) {
        Date expirationDate = expirationTimestamp.toDate();
        return expirationDate.before(new Date());
    }

    private void sortFoodItemsByDistance() {
        if (currentLocation != null) {
            Collections.sort(foodItems, (item1, item2) -> {
                GeoPoint loc1 = item1.getUserDetails().getGeoPoint();
                GeoPoint loc2 = item2.getUserDetails().getGeoPoint();
                return compareDistances(loc1, loc2);
            });
        } else {
            Log.w("Location", "currentLocation is null, cannot sort food items by distance.");
        }
    }

    private int compareDistances(GeoPoint loc1, GeoPoint loc2) {
        if (loc1 == null || loc2 == null) {
            return 0;
        }

        float[] results1 = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                loc1.getLatitude(), loc1.getLongitude(), results1);
        float distance1 = results1[0];

        float[] results2 = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                loc2.getLatitude(), loc2.getLongitude(), results2);
        float distance2 = results2[0];

        return Float.compare(distance1, distance2);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            getCurrentLocation();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Log.w("Permission", "Location permission denied.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("CurrentLocation", "Lat: " + currentLocation.latitude + ", Lon: " + currentLocation.longitude);
                fetchFoodItems();
            } else {
                Log.w("Location", "Failed to get current location.");
                if (task.getException() != null) {
                    Log.e("Location", "Exception: ", task.getException());
                }
            }
        });
    }

    private void filterFoodItems(String query) {
        List<FoodItem> filteredList = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
        updateEmptyState(); // Update empty state after filtering
    }

    private void startExpirationTimer(FoodItem item) {
        long expirationTime = item.getExpirationDate().toDate().getTime();
        long currentTime = System.currentTimeMillis();
        long delay = expirationTime - currentTime;

        if (delay > 0) {
            new CountDownTimer(delay, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {}

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onFinish() {
                    foodItems.remove(item);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }.start();
        } else {
            foodItems.remove(item);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (foodItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
    }
}
