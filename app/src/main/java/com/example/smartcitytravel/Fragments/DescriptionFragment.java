package com.example.smartcitytravel.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.databinding.FragmentDescriptionBinding;

public class DescriptionFragment extends Fragment {
    private FragmentDescriptionBinding binding;
    private PlaceDetail placeDetail;

    public DescriptionFragment(PlaceDetail placeDetail) {
        this.placeDetail = placeDetail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDescriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (placeDetail.getTiming() != null) {
            binding.timeTxt.setText(placeDetail.getTiming());
        }
        binding.descriptionTxt.setText(placeDetail.getDescription());

    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}