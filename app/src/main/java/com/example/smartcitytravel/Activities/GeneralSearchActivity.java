package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.GridSpaceItemDecoration;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityGeneralSearchBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneralSearchActivity extends AppCompatActivity {
    private ActivityGeneralSearchBinding binding;
    private Util util;
    private FirebaseFirestore db;
    private ArrayList<String> placeNameList;
    private boolean firstSearchResult;
    private GridPlaceAdapter gridPlaceAdapter;
    private Connection connection;
    private boolean noConnectionMSGCalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGeneralSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        getAllPlaceNames();
    }

    public void initialize() {
        util = new Util();
        db = FirebaseFirestore.getInstance();
        placeNameList = new ArrayList<>();
        firstSearchResult = true;
        connection = new Connection();
        noConnectionMSGCalled = false;
    }

    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.getRoot(), "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        setSearchViewUI(searchView);

        onPlaceNameEntered(searchView);

        return true;
    }

    public void setSearchViewUI(SearchView searchView) {
        searchView.setQueryHint("Search Place by Name");
        searchView.setIconified(false);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });

        TextView searchEditText = searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.trans_white));

        ImageView closeIcon = searchView.findViewById(R.id.search_close_btn);
        closeIcon.setColorFilter(getResources().getColor(R.color.white));

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

    public void onPlaceNameEntered(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                checkConnectionAndOnWriteQuery(query);
                return true;
            }
        });
    }

    public void getAllPlaceNames() {
        db
                .collection("place")
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null || !queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                placeNameList.add((String) querySnapshot.get("Name"));
                            }
                        }
                    }
                });
    }

    public void getMatchedPlaces(ArrayList<String> matchPlaceNameList) {
        db
                .collection("place")
                .whereIn("Name", matchPlaceNameList)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null || !queryDocumentSnapshots.isEmpty()) {
                            binding.noResultTxt.setVisibility(View.GONE);

                            ArrayList<Place> resultPlaceList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                resultPlaceList.add(place);

                            }
                            if (firstSearchResult) {
                                setAdapter(resultPlaceList);
                                firstSearchResult = false;
                            } else {
                                gridPlaceAdapter.setNewData(resultPlaceList);
                            }
                            binding.loadingBar.setVisibility(View.GONE);
                            binding.searchPlaceRecyclerView.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    public void setAdapter(ArrayList<Place> resultPlaceList) {
        gridPlaceAdapter = new GridPlaceAdapter(this, resultPlaceList);

        binding.searchPlaceRecyclerView.setAdapter(gridPlaceAdapter);
        binding.searchPlaceRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));

    }

    public void onQueryWrite(String query) {
        query = query.trim();

        if (query.isEmpty()) {
            binding.noResultTxt.setVisibility(View.VISIBLE);
            binding.loadingBar.setVisibility(View.GONE);

            if (!firstSearchResult) {
                gridPlaceAdapter.clearData();
            }

        } else {
            binding.searchPlaceRecyclerView.setVisibility(View.GONE);

            binding.noResultTxt.setVisibility(View.GONE);
            binding.loadingBar.setVisibility(View.VISIBLE);

            ArrayList<String> matchPlaceNameList = new ArrayList<>();

            for (String placeName : placeNameList) {
                if (placeName.toLowerCase().matches(query.toLowerCase() + ".*")) {
                    matchPlaceNameList.add(placeName);
                }
                if (matchPlaceNameList.size() == 10) {
                    break;
                }
            }

            if (matchPlaceNameList.size() == 0) {
                binding.noResultTxt.setVisibility(View.VISIBLE);
                binding.loadingBar.setVisibility(View.GONE);

                if (!firstSearchResult) {
                    gridPlaceAdapter.clearData();
                }

            } else {
                getMatchedPlaces(matchPlaceNameList);
            }
        }
    }

    public void checkConnectionAndOnWriteQuery(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(GeneralSearchActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            binding.noConnectionTxt.setVisibility(View.GONE);
                            noConnectionMSGCalled = false;
                            onQueryWrite(query);
                        } else {
                            if (!noConnectionMSGCalled) {
                                Toast.makeText(GeneralSearchActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                noConnectionMSGCalled = true;
                            }
                            binding.noResultTxt.setVisibility(View.GONE);
                            binding.loadingBar.setVisibility(View.GONE);
                            binding.noConnectionTxt.setVisibility(View.VISIBLE);

                            if (!firstSearchResult) {
                                gridPlaceAdapter.clearData();
                            }


                        }
                    }
                });

            }
        });
        executor.shutdown();
    }
}