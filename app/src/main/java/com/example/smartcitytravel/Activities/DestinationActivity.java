package com.example.smartcitytravel.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityDestinationBinding;

public class DestinationActivity extends AppCompatActivity {
    private ActivityDestinationBinding binding;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();

        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Destination");

        showLahoreDestination();
        showIslamabadDestination();
    }

    public void showLahoreDestination() {
        binding.lahoreCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPlaceListActivity("Lahore");
            }
        });
    }

    public void showIslamabadDestination() {
        binding.islamabadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPlaceListActivity("Islamabad");
            }
        });
    }

    public void moveToPlaceListActivity(String destination_name) {
        Intent intent = new Intent(this, PlaceListActivity.class);
        intent.putExtra("destination_name", destination_name);
        startActivity(intent);
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