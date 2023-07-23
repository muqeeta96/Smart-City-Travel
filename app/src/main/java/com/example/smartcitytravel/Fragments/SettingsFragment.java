package com.example.smartcitytravel.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import com.example.smartcitytravel.Activities.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentSettingsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private FirebaseFirestore db;
    private Connection connection;

    public SettingsFragment() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        db = FirebaseFirestore.getInstance();
        connection = new Connection();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setToolBarTheme();
        deleteAccount();
    }

    public void deleteAccount() {
        PreferenceFragment preferenceFragment = (PreferenceFragment) getChildFragmentManager().findFragmentById(R.id.preferenceFragment);
        preferenceFragment.getDeleteAccountPreference().setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                showDeleteAccountDialog("Delete Account",
                        "Do you want to permanently delete your account?");
                return false;
            }
        });
    }

    public void setToolBarTheme() {
        util.setStatusBarColor(requireActivity(), R.color.theme_light);
        util.addToolbarAndNoUpButton((AppCompatActivity) requireActivity(), binding.toolbarLayout.toolbar, "       Settings");
    }

    public void showDeleteAccountDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkConnectionAndDeleteAccount();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    public void checkConnectionAndDeleteAccount() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(requireContext());
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(requireContext());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            delete();
                        } else {
                            hideLoadingBar();
                            Toast.makeText(requireContext(), "Unable to delete account", Toast.LENGTH_SHORT).show();
                            Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void delete() {
        db
                .collection("user")
                .document(preferenceHandler.getUserIdPreference(requireContext()))
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        logout();
                    }
                });
    }

    public void logout() {
        Boolean account_type = preferenceHandler.getAccountTypePreference(requireContext());
        if (!account_type) {
            preferenceHandler.clearLoggedInAccountPreference(requireContext());

            moveToLoginActivity();

        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        moveToLoginActivity();
                    } else if (task.isCanceled()) {
                        Toast.makeText(requireContext(), "Unable to logout", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            preferenceHandler.clearLoggedInAccountPreference(requireContext());
        }
        hideLoadingBar();
    }

    public void moveToLoginActivity() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
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