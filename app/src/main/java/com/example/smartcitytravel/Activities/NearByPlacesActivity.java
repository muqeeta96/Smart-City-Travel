package com.example.smartcitytravel.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.DataModel.PlaceLocation;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.GridSpaceItemDecoration;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityNearByPlacesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NearByPlacesActivity extends AppCompatActivity {
    private ActivityNearByPlacesBinding binding;
    private Util util;
    private FirebaseFirestore db;
    private boolean firstTimeNearbyPlaces;
    private GridPlaceAdapter gridPlaceAdapter;
    private boolean locationPermissionAllowed;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BTW_UPDATES = 60000;
    private LocationManager locationManager;
    private Location currentLocation;
    private boolean lastKnownLocationAccess;
    private Connection connection;
    private String city;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (locationPermissionAllowed) {
                currentLocation = location;
                city = getCityName();
                if (lastKnownLocationAccess) {
                    lastKnownLocationAccess = false;
                } else {
                    checkConnectionAndGetNearByPlaces();
                }
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            if (locationPermissionAllowed) {
                hideLocationSettingsButton();
            }
            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            if (locationPermissionAllowed && currentLocation == null) {
                showLocationSettingsButton();
            }
            LocationListener.super.onProviderDisabled(provider);
        }
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult
            (new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean fineLocationPermission = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationPermission = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (fineLocationPermission != null && fineLocationPermission
                            || coarseLocationPermission != null && coarseLocationPermission) {
                        locationPermissionAllowed = true;
                        getNearByPlacesWithLastKnownLocation();
                    } else {
                        locationPermissionAllowed = false;
                    }
                    checkLocationPermission();
                }
            });

    @Override
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearByPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        requestLocationPermission();
        setToolBarTheme();
        openLocationSetting();
        changeRange();
        getNearByPlacesWithLastKnownLocation();
    }

    @Override
    protected void onResume() {
        checkLocationPermission();
        registerForLocationUpdates();

        super.onResume();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        db = FirebaseFirestore.getInstance();
        firstTimeNearbyPlaces = true;
        locationPermissionAllowed = false;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastKnownLocationAccess = false;
        connection = new Connection();
    }

    public void selectRange() {
        String selectedRange = binding.rangeTxt.getText().toString();
        switch (selectedRange) {
            case "3":
                binding.rangeTxt.setText("5");
                break;
            case "5":
                binding.rangeTxt.setText("7");
                break;
            case "7":
                binding.rangeTxt.setText("3");
                break;
        }
    }

    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Nearby Places");
    }

    public void changeRange() {
        binding.rangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRange();
                checkConnectionAndGetNearByPlaces();
            }
        });
    }

    public void getNearByPlaces() {
        if (locationPermissionAllowed) {
            db.collection("place")
                    .whereEqualTo("City", city)
                    .get()
                    .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    binding.noPlaceTxt.setVisibility(View.VISIBLE);
                                    binding.PlaceRecyclerView.setVisibility(View.GONE);
                                    if (!firstTimeNearbyPlaces) {
                                        gridPlaceAdapter.clearData();
                                    }
                                } else {
                                    double selectedRange = Double.parseDouble(binding.rangeTxt.getText().toString());
                                    ArrayList<Place> nearByPlaceList = new ArrayList<>();
                                    for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                        PlaceLocation placeLocation = querySnapshot.toObject(PlaceLocation.class);
                                        placeLocation.setPlaceId(querySnapshot.getId());

                                        double distanceBetween = calculateDistance(Double.parseDouble(placeLocation.getLatitude()), Double.parseDouble(placeLocation.getLongitude()));
                                        if (distanceBetween <= selectedRange) {
                                            Place place = querySnapshot.toObject(Place.class);
                                            place.setPlaceId(querySnapshot.getId());
                                            nearByPlaceList.add(place);
                                        }

                                    }
                                    if (firstTimeNearbyPlaces) {
                                        setAdapter(nearByPlaceList);
                                        firstTimeNearbyPlaces = false;
                                    } else {
                                        gridPlaceAdapter.setNewData(nearByPlaceList);
                                    }
                                    binding.noPlaceTxt.setVisibility(View.GONE);
                                    binding.PlaceRecyclerView.setVisibility(View.VISIBLE);
                                }
                                binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            }
                        }
                    });

        }

    }

    @SuppressLint("MissingPermission")
    public void getNearByPlacesWithLastKnownLocation() {
        if (locationPermissionAllowed) {
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lastKnownLocationAccess = true;
                city = getCityName();
                checkConnectionAndGetNearByPlaces();
            } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lastKnownLocationAccess = true;
                city = getCityName();
                checkConnectionAndGetNearByPlaces();
            }
        }
    }

    public void checkConnectionAndGetNearByPlaces() {
        binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        binding.noPlaceTxt.setVisibility(View.GONE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(NearByPlacesActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            binding.rangeLayout.setVisibility(View.VISIBLE);
                            getNearByPlaces();
                        } else {
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            binding.rangeLayout.setVisibility(View.GONE);
                            retryConnection();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    public void retryConnection() {
        binding.CheckConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetNearByPlaces();
            }
        });

    }

    public void setAdapter(ArrayList<Place> nearByPlaceList) {
        gridPlaceAdapter = new GridPlaceAdapter(this, nearByPlaceList);

        binding.PlaceRecyclerView.setAdapter(gridPlaceAdapter);
        binding.PlaceRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));

    }

    public double calculateDistance(double placeLatitude, double placeLongitude) {
        Location placeLocation = new Location("place_location");
        placeLocation.setLatitude(placeLatitude);
        placeLocation.setLongitude(placeLongitude);

        double distanceInMeters = currentLocation.distanceTo(placeLocation);
        return distanceInMeters * 0.001; //convert distance from meters into kilometers

    }

    public String getCityName() {
        Geocoder geocoder = new Geocoder(NearByPlacesActivity.this, Locale.getDefault());
        try {
            ArrayList<Address> cityList = (ArrayList<Address>) geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (cityList.get(0).getLocality() != null) {
                return cityList.get(0).getLocality();

            } else {
                return "";
            }
        } catch (IOException e) {
            return "";
        }
    }

    public void openLocationSetting() {
        binding.locationSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
    }

    public void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

        } else {
            locationPermissionAllowed = true;
        }

    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionAllowed = true;
            hideLocationPermissionButton();
        } else {
            locationPermissionAllowed = false;
            showLocationPermissionButton();
        }
    }

    public void showLocationPermissionButton() {
        binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        binding.rangeLayout.setVisibility(View.GONE);
        binding.locationPermissionBtn.setVisibility(View.VISIBLE);
        binding.locationPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationPermission();
            }
        });
    }

    public void hideLocationPermissionButton() {
        binding.locationPermissionBtn.setVisibility(View.GONE);
        binding.PlaceRecyclerView.setVisibility(View.VISIBLE);
        binding.rangeBtn.setVisibility(View.VISIBLE);
    }

    public void openLocationPermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    void showLocationSettingsButton() {
        binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        binding.rangeLayout.setVisibility(View.GONE);
        binding.locationSettingBtn.setVisibility(View.VISIBLE);
    }

    void hideLocationSettingsButton() {
        binding.locationSettingBtn.setVisibility(View.GONE);
    }

    @SuppressLint("MissingPermission")
    public void registerForLocationUpdates() {
        if (locationPermissionAllowed) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            hideLocationPermissionButton();
        } else {
            showLocationPermissionButton();
        }
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

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }
}