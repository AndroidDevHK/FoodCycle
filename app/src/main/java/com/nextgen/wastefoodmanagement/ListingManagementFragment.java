package com.nextgen.wastefoodmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListingManagementFragment extends Fragment implements PersonalFoodItemAdapter.AdapterCallback {

    private RecyclerView recyclerView;
    private PersonalFoodItemAdapter foodItemAdapter;
    private List<FoodItem> foodItemList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private TextInputEditText searchView;
    private View emptyStateContainer;
    private TextView noResultTextView;
    private View searchCard;
    private ImageView addFoodItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listing_management, container, false);
        initializeViews(view);
        setupToolbar(view);
        setupRecyclerView();
        setupSearchView();
        setupAddFoodItemButton();
        initializeFirestore();
        fetchFoodItems();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.simpleSearchView);
        searchCard = view.findViewById(R.id.searchcard);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        noResultTextView = view.findViewById(R.id.noResultTextView);
        addFoodItem = view.findViewById(R.id.addFoodItemBtn);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("My Foods");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        foodItemAdapter = new PersonalFoodItemAdapter(getActivity(), foodItemList, this);
        recyclerView.setAdapter(foodItemAdapter);
    }

    private void setupSearchView() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoodItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });
    }

    private void setupAddFoodItemButton() {
        addFoodItem.setOnClickListener(v -> handleAddNewItemClick());
    }

    private void initializeFirestore() {
        firestore = FirebaseFirestore.getInstance();
    }

    private void handleAddNewItemClick() {
        Intent intent = new Intent(getActivity(), AddFoodItemActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchFoodItems() {
        firestore.collection("FoodItems")
                .whereEqualTo("postedBy", UserSessionDetails.getInstance(getActivity()).getUserDbRef())
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (querySnapshot != null) {
                        foodItemList.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            FoodItem foodItem = document.toObject(FoodItem.class);
                            foodItemList.add(foodItem);
                        }
                        updateUI(foodItemList.size());
                    }
                });
    }

    private void filterFoodItems(String query) {
        List<FoodItem> filteredList = new ArrayList<>();
        for (FoodItem item : foodItemList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        foodItemAdapter.updateList(filteredList);
        updateSearchUI(filteredList.size());
    }

    private void updateUI(int itemCount) {
        if (itemCount == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            searchCard.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            searchCard.setVisibility(View.VISIBLE);
        }
        foodItemAdapter.notifyDataSetChanged();
    }

    private void updateSearchUI(int itemCount) {
        if (itemCount == 0) {
            noResultTextView.setVisibility(View.VISIBLE);
        } else {
            noResultTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUpdateUI(int itemCount) {
        updateUI(itemCount);
    }
}
