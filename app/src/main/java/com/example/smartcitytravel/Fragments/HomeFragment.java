package com.example.smartcitytravel.Fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.Activities.DestinationActivity;
import com.example.smartcitytravel.Activities.GeneralSearchActivity;
import com.example.smartcitytravel.Activities.LiveChatActivity;
import com.example.smartcitytravel.Activities.NearByPlacesActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Util util;

    public HomeFragment() {
        util = new Util();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        util.setStatusBarColor(requireActivity(), R.color.black);
        setLoadingBarColor();
        openLiveChat();
        moveToDestinationActivity();
        moveToNearByPlacesActivity();
        moveToSearchActivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoadingBar();
    }

    public void openLiveChat() {
        binding.liveChatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLiveChatActivity();
            }
        });
    }


    public void moveToSearchActivity() {
        binding.generalSearchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), GeneralSearchActivity.class);
                startActivity(intent);
            }
        });

    }

    public void moveToLiveChatActivity() {
        Intent intent = new Intent(getActivity(), LiveChatActivity.class);
        startActivity(intent);
    }

    public void moveToDestinationActivity() {
        binding.destinationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingBar();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), DestinationActivity.class);
                        startActivity(intent);
                    }
                }, 100);

            }
        });
    }

    public void moveToNearByPlacesActivity() {
        binding.nearbyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), NearByPlacesActivity.class);
                        startActivity(intent);
                    }
                }, 100);

            }
        });
    }

    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(requireActivity());
    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(requireActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}