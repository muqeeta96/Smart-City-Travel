package com.example.smartcitytravel.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.databinding.FragmentNavigationBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {
    private FragmentNavigationBinding binding;
    private PlaceDetail placeDetail;
    private LatLng placeLatLng;
    private boolean locationPermissionAllowed;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BTW_UPDATES = 60000;
    private LocationManager locationManager;
    private Location currentLocation;
    private GoogleMap googleMap;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (locationPermissionAllowed) {
                currentLocation = location;
                setLocationOnMap();
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
                        getLastKnownLocation();

                    } else {
                        locationPermissionAllowed = false;
                    }
                    checkLocationPermission();
                }
            });


    public NavigationFragment(PlaceDetail placeDetail, LocationManager locationManager) {
        this.placeDetail = placeDetail;
        this.locationManager = locationManager;
        placeLatLng = new LatLng(Double.parseDouble(placeDetail.getLatitude()), Double.parseDouble(placeDetail.getLongitude()));
        locationPermissionAllowed = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requestLocationPermission();
        setMapFragment();
        openNavigation();
        openLocationSetting();
    }

    @Override
    public void onResume() {
        checkLocationPermission();
        registerForLocationUpdates();
        setLocationOnMap();
        super.onResume();
    }

    public void setMapFragment() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        getLastKnownLocation();

        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);

        setLocationOnMap();
    }

    @SuppressLint("MissingPermission")
    public void setLocationOnMap() {
        if (currentLocation != null && googleMap != null) {
            googleMap.clear();

            MarkerOptions currentLocationMarker = getCurrentLocationMarker();
            MarkerOptions destinationPlaceMarker = getDestinationPlaceMarker();

            configureMap(currentLocationMarker, destinationPlaceMarker);
        }

    }

    public MarkerOptions getCurrentLocationMarker() {
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        MarkerOptions currentLocationMarker = new MarkerOptions()
                .position(currentLocationLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.ic_my_location, 100, 100)))
                .title("Current Location");
        googleMap.addMarker(currentLocationMarker);
        return currentLocationMarker;
    }

    public MarkerOptions getDestinationPlaceMarker() {
        MarkerOptions destinationPlaceMarker = new MarkerOptions()
                .position(placeLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.ic_destination_location, 100, 100)))
                .title(placeDetail.getName());
        googleMap.addMarker(destinationPlaceMarker).showInfoWindow();
        return destinationPlaceMarker;
    }

    public void configureMap(MarkerOptions currentLocationMarker, MarkerOptions destinationPlaceMarker) {
        LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
        latLngBoundsBuilder.include(currentLocationMarker.getPosition());
        latLngBoundsBuilder.include(destinationPlaceMarker.getPosition());

        LatLngBounds latLngBounds = latLngBoundsBuilder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 2;
        int padding = (int) (height * 0.20);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, width, height, padding);
        googleMap.animateCamera(cameraUpdate);
    }

    public Bitmap resizeMapIcons(int res, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), res);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    public void openNavigation() {
        binding.navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + placeDetail.getLatitude() + "," + placeDetail.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

            }
        });

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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

        } else {
            locationPermissionAllowed = true;
        }

    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionAllowed = true;
            hideLocationPermissionButton();
        } else {
            locationPermissionAllowed = false;
            showLocationPermissionButton();
        }
    }

    public void openLocationPermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    public void showLocationPermissionButton() {
        binding.mapFragment.setVisibility(View.GONE);
        binding.navigateBtn.setVisibility(View.GONE);
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
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            binding.mapFragment.setVisibility(View.VISIBLE);
            binding.navigateBtn.setVisibility(View.VISIBLE);
        }

    }

    void showLocationSettingsButton() {
        binding.mapFragment.setVisibility(View.GONE);
        binding.navigateBtn.setVisibility(View.GONE);
        binding.locationSettingBtn.setVisibility(View.VISIBLE);
    }

    void hideLocationSettingsButton() {
        binding.mapFragment.setVisibility(View.VISIBLE);
        binding.navigateBtn.setVisibility(View.VISIBLE);
        binding.locationSettingBtn.setVisibility(View.GONE);
    }

    @SuppressLint("MissingPermission")
    public void registerForLocationUpdates() {
        checkLocationPermission();
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

    @SuppressLint("MissingPermission")
    public void getLastKnownLocation() {
        if (locationPermissionAllowed) {
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
    }

    @Override
    public void onDestroyView() {
        locationManager.removeUpdates(locationListener);
        binding = null;
        super.onDestroyView();
    }


}