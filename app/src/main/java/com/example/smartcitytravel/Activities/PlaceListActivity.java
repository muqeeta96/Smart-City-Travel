package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.DataModel.Favorite;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.PlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.GridSpaceItemDecoration;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceListBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceListActivity extends AppCompatActivity {
    private ActivityPlaceListBinding binding;
    private Util util;
    private String userId;
    private Connection connection;
    private String city;
    private PreferenceHandler preferenceHandler;
    private ArrayList<Place> placeList;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolbar();
        checkConnectionAndGetPlaces();
    }

    public void initialize() {
        connection = new Connection();
        util = new Util();
        db = FirebaseFirestore.getInstance();
        city = getIntent().getExtras().getString("destination_name");
        preferenceHandler = new PreferenceHandler();
        userId = preferenceHandler.getUserIdPreference(this);
        placeList = new ArrayList<>();
    }

    public void setToolbar() {
        util.setStatusBarColor(PlaceListActivity.this, R.color.theme_light);
        util.addToolbar(PlaceListActivity.this, binding.toolbarLayout.toolbar, city);
    }

    public void checkConnectionAndGetPlaces() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(PlaceListActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            binding.popularLayout.setVisibility(View.VISIBLE);
                            binding.restaurantLayout.setVisibility(View.VISIBLE);
                            binding.famousSpotLayout.setVisibility(View.VISIBLE);
                            binding.hotelLayout.setVisibility(View.VISIBLE);

                            getPlaceList();
                        } else {
                            binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                        binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);

                    }
                });
            }
        });
        executor.shutdown();
    }

    public void retryConnection() {
        binding.checkConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.checkConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetPlaces();
            }
        });

    }

    public void getPlaceList() {
        db.collection("place")
                .whereEqualTo("City", city)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());
                                placeList.add(place);
                            }

                            getRecommendedPlaces();
                            getPopularPlaces();
                            getRestaurantPlaces();
                            getFamousSpots();
                            getHotelPlaces();

                        }
                    }
                });
    }

    public void getPopularPlaces() {
        Collections.sort(placeList, new Comparator<Place>() {
            @Override
            public int compare(Place place1, Place place2) {
                return place1.getRating().compareTo(place2.getRating());
            }
        });
        Collections.reverse(placeList);

        ArrayList<Place> popularPlaceList = new ArrayList<>(placeList.subList(0, 50));
        Collections.shuffle(popularPlaceList);

        showPopularPlaces(new ArrayList<>(popularPlaceList.subList(0, 15)));
        binding.popularLoadingBar.setVisibility(View.GONE);
    }

    public void showPopularPlaces(ArrayList<Place> popularPlaceList) {
        PlaceAdapter placeAdapter = new PlaceAdapter(this, popularPlaceList, city);

        binding.popularRecyclerView.setAdapter(placeAdapter);
        binding.popularRecyclerView.setHasFixedSize(true);
        binding.popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.popularRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 8, 8));
    }

    public void getRestaurantPlaces() {
        ArrayList<Place> restaurantPlaceList = new ArrayList<>();
        String placeType = "Restaurant";
        for (Place place : placeList) {
            if (place.getPlace_type().equals(placeType)) {
                restaurantPlaceList.add(place);
            }
        }
        Collections.shuffle(restaurantPlaceList);

        if (restaurantPlaceList.size() > 15) {
            showRestaurantPlaces(new ArrayList<>(restaurantPlaceList.subList(0, 15)), placeType);
        } else {
            showRestaurantPlaces(restaurantPlaceList, placeType);
        }

        binding.restaurantLoadingBar.setVisibility(View.GONE);
    }

    public void showRestaurantPlaces(ArrayList<Place> restaurantPlaceList, String placeType) {

        PlaceAdapter placeAdapter = new PlaceAdapter(this,
                restaurantPlaceList, true, "Restaurants", placeType, city);

        binding.restaurantRecyclerView.setAdapter(placeAdapter);
        binding.restaurantRecyclerView.setHasFixedSize(true);
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.restaurantRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 8, 8));
    }

    public void getFamousSpots() {
        ArrayList<Place> famousSpotList = new ArrayList<>();
        String placeType = "Tourism_spot";
        for (Place place : placeList) {
            if (place.getPlace_type().equals(placeType)) {
                famousSpotList.add(place);
            }
        }
        Collections.shuffle(famousSpotList);

        if (famousSpotList.size() > 15) {
            showFamousSpots(new ArrayList<>(famousSpotList.subList(0, 15)), placeType);
        } else {
            showFamousSpots(famousSpotList, placeType);
        }
        binding.famousSpotLoadingBar.setVisibility(View.GONE);
    }

    public void showFamousSpots(ArrayList<Place> famousSpotList, String placeType) {
        PlaceAdapter placeAdapter = new PlaceAdapter(this,
                famousSpotList, true, "Famous Spots", placeType, city);

        binding.famousSpotRecyclerView.setAdapter(placeAdapter);
        binding.famousSpotRecyclerView.setHasFixedSize(true);
        binding.famousSpotRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.famousSpotRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 8, 8));
    }

    public void getHotelPlaces() {
        ArrayList<Place> hotelPlaceList = new ArrayList<>();
        String placeType = "Hotel";
        for (Place place : placeList) {
            if (place.getPlace_type().equals(placeType)) {
                hotelPlaceList.add(place);
            }
        }
        Collections.shuffle(hotelPlaceList);

        if (hotelPlaceList.size() > 15) {
            showHotelPlaces(new ArrayList<>(hotelPlaceList.subList(0, 15)), placeType);
        } else {
            showHotelPlaces(hotelPlaceList, placeType);

        }
        binding.hotelLoadingBar.setVisibility(View.GONE);
    }

    public void showHotelPlaces(ArrayList<Place> hotelPlaceList, String placeType) {
        PlaceAdapter placeAdapter = new PlaceAdapter(this,
                hotelPlaceList, true, "Hotels", placeType, city);

        binding.hotelRecyclerView.setAdapter(placeAdapter);
        binding.hotelRecyclerView.setHasFixedSize(true);
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.hotelRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 8, 8));
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

    public void getRecommendedPlaces() {
        getUserFavoritePlaces();
    }

    public void getUserFavoritePlaces() {
        ArrayList<String> favoritePlacesIdList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("favorite")
                .whereEqualTo("userId", userId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Favorite favoritePlace = querySnapshot.toObject(Favorite.class);
                                favoritePlacesIdList.add(favoritePlace.getPlaceId());

                            }
                            getUserTopRatedPlaces(favoritePlacesIdList);
                        }
                    }
                });
    }

    public void getUserTopRatedPlaces(ArrayList<String> favoritePlacesIdList) {
        ArrayList<String> topRatedPlacesIdList = new ArrayList<>();
        db.collection("review")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Review place = querySnapshot.toObject(Review.class);
                                topRatedPlacesIdList.add(place.getPlaceId());
                            }
                            createRecommendation(favoritePlacesIdList, topRatedPlacesIdList);

                        }
                    }
                });
    }

    public void createRecommendation(ArrayList<String> favoritePlacesIdList,
                                     ArrayList<String> topRatedPlacesIdList) {

        HashSet<String> placeSubTypeHashSet = new HashSet<>();
        for (Place place : placeList) {
            for (String favoritePlaceId : favoritePlacesIdList) {
                if (place.getPlaceId().equals(favoritePlaceId)) {
                    placeSubTypeHashSet.add(place.getSub_type());
                    break;
                }
            }
        }
        for (Place place : placeList) {
            for (String topRatedPlaceId : topRatedPlacesIdList) {
                if (place.getPlaceId().equals(topRatedPlaceId)
                        && place.getRating() >= 3.5) {
                    placeSubTypeHashSet.add(place.getSub_type());
                    break;
                }
            }
        }

        ArrayList<Place> recommendedPlaceList = new ArrayList<>();
        for (Place place : placeList) {
            for (String placeSubType : placeSubTypeHashSet) {
                if (place.getSub_type().equals(placeSubType)) {
                    recommendedPlaceList.add(place);
                    break;
                }

            }
        }

        if (!recommendedPlaceList.isEmpty()) {
            Collections.shuffle(recommendedPlaceList);
            if (recommendedPlaceList.size() > 15) {
                showRecommendedPlaces(new ArrayList<>(recommendedPlaceList.subList(0, 15)));
            } else {
                showRecommendedPlaces(recommendedPlaceList);
            }
            binding.recommendationLayout.setVisibility(View.VISIBLE);
        }

    }

    public void showRecommendedPlaces(ArrayList<Place> recommendedPlaceList) {
        PlaceAdapter placeAdapter = new PlaceAdapter(this, recommendedPlaceList, city);

        binding.recommendationRecyclerView.setAdapter(placeAdapter);
        binding.recommendationRecyclerView.setHasFixedSize(true);
        binding.recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recommendationRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 8, 8));
    }

}