package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.Activities.PlaceDetailActivity;
import com.example.smartcitytravel.Activities.ShowMorePlaceActivity;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.databinding.PlaceViewBinding;
import com.example.smartcitytravel.databinding.ShowMorePlacesBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Place> placeArrayList;
    private Context context;
    public static final int PLACE_VIEW = 0;
    public static final int SHOW_MORE_VIEW = 1;
    private boolean enableShowMoreOption;
    private String title;
    private String placeType;
    private String city;

    public PlaceAdapter(Context context, ArrayList<Place> placeArrayList, String city) {
        this.context = context;
        this.placeArrayList = placeArrayList;
        this.city = city;
        this.enableShowMoreOption = false;
    }

    public PlaceAdapter(Context context, ArrayList<Place> placeArrayList, boolean enableShowMoreOption, String title, String placeType, String city) {
        this.context = context;
        this.placeArrayList = placeArrayList;
        this.enableShowMoreOption = enableShowMoreOption;
        this.title = title;
        this.placeType = placeType;
        this.city = city;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PLACE_VIEW) {
            return new PlaceAdapter.PlaceViewHolder(PlaceViewBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new PlaceAdapter.ShowMoreViewHolder(ShowMorePlacesBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == PLACE_VIEW) {
            PlaceViewHolder placeHolder = (PlaceViewHolder) holder;
            Place place = placeArrayList.get(position);

            setPlaceUI(placeHolder, place);
            moveToPlaceDetailActivity(placeHolder, place);

            setScaleAnimation(placeHolder.binding.getRoot());

        } else {
            ShowMoreViewHolder showMoreHolder = (ShowMoreViewHolder) holder;
            moveToShowMorePlaceActivity(showMoreHolder);
        }


    }

    @Override
    public int getItemCount() {
        if (enableShowMoreOption) {
            return placeArrayList.size() + 1;
        } else {
            return placeArrayList.size();

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() - 1 == position && enableShowMoreOption) {
            return SHOW_MORE_VIEW;
        } else {
            return PLACE_VIEW;
        }
    }

    // set data in place UI
    public void setPlaceUI(PlaceAdapter.PlaceViewHolder holder, Place place) {
        holder.binding.placeName.setText(place.getName());
        holder.binding.placeRatingTxt.setText(place.getRating().toString());

        Picasso.get().load(place.getImage1()).into(holder.binding.placeImg);

        holder.binding.placeLoadingBar.setVisibility(View.GONE);

    }

    //move to place detail activity when click on place ui
    public void moveToPlaceDetailActivity(PlaceAdapter.PlaceViewHolder holder, Place place) {
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("placeId", place.getPlaceId());
                context.startActivity(intent);
            }
        });
    }

    //move to show more place activity when click on show more button
    public void moveToShowMorePlaceActivity(ShowMoreViewHolder holder) {
        holder.binding.showMorePlaceImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("placeList", placeArrayList);

                Intent intent = new Intent(context, ShowMorePlaceActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("placeType", placeType);
                intent.putExtra("city", city);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final PlaceViewBinding binding;

        public PlaceViewHolder(@NonNull PlaceViewBinding placeViewBinding) {
            super(placeViewBinding.getRoot());
            this.binding = placeViewBinding;

        }
    }

    // zoom in animation
    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(300);
        view.startAnimation(anim);
    }

    public class ShowMoreViewHolder extends RecyclerView.ViewHolder {
        private final ShowMorePlacesBinding binding;

        public ShowMoreViewHolder(@NonNull ShowMorePlacesBinding showMorePlacesBinding) {
            super(showMorePlacesBinding.getRoot());
            this.binding = showMorePlacesBinding;

        }
    }


}
