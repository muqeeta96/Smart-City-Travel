package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.GridSpaceItemDecoration;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityShowMorePlaceBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowMorePlaceActivity extends AppCompatActivity {
    private ActivityShowMorePlaceBinding binding;
    private Util util;
    private GridPlaceAdapter gridPlaceAdapter;
    private String placeType;
    private Connection connection;
    private FirebaseFirestore db;
    private String city;
    private int intentPlaceListSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowMorePlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        createRecyclerView();
        checkConnectionAndGetPlaces();
    }

    public void initialize() {
        util = new Util();
        placeType = getIntent().getExtras().getString("placeType");
        city = getIntent().getExtras().getString("city");
        connection = new Connection();
        db = FirebaseFirestore.getInstance();

    }

    public void setToolBarTheme() {
        String title = getIntent().getExtras().getString("title");

        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, title);
    }

    public void createRecyclerView() {
        ArrayList<Place> placeList = (ArrayList<Place>) getIntent().getExtras().getSerializable("placeList");
        intentPlaceListSize = placeList.size();

        gridPlaceAdapter = new GridPlaceAdapter(this, placeList);
        binding.placeRecyclerView.setAdapter(gridPlaceAdapter);
        binding.placeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.placeRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));
    }

    public void checkConnectionAndGetPlaces() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(ShowMorePlaceActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            getPlaces();
                        } else {

                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    public void getPlaces() {
        db.collection("place")
                .whereEqualTo("City", city)
                .whereEqualTo("Place_type", placeType)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> placeList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                boolean differentPlace = true;
                                for (Place adapterPlace : gridPlaceAdapter.getData()) {
                                    if (adapterPlace.getPlaceId().equals(place.getPlaceId())) {
                                        differentPlace = false;
                                        break;
                                    }
                                }
                                if (differentPlace) {
                                    placeList.add(place);
                                }
                            }
                            gridPlaceAdapter.setData(placeList);
                            binding.placeRecyclerView.setVisibility(View.VISIBLE);

                            if (gridPlaceAdapter.getData().size() >= intentPlaceListSize + 2) {
                                binding.placeRecyclerView.smoothScrollToPosition(intentPlaceListSize + 2);
                            } else {
                                binding.placeRecyclerView.smoothScrollToPosition(intentPlaceListSize);
                            }

                        }
                        binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);

                    }
                });
    }

    public void retryConnection() {
        binding.CheckConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetPlaces();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
}