package com.example.smartcitytravel.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.Broadcast.UpdateProfileImageBroadcast;
import com.example.smartcitytravel.Broadcast.UpdateProfileNameBroadcast;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.Fragments.AboutUsFragment;
import com.example.smartcitytravel.Fragments.FavoriteFragment;
import com.example.smartcitytravel.Fragments.HomeFragment;
import com.example.smartcitytravel.Fragments.SettingsFragment;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private UpdateProfileImageBroadcast updateProfileImageBroadcast;
    private UpdateProfileNameBroadcast updateProfileNameBroadcast;

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult
            (new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        requestLocationPermission();
        User user = preferenceHandler.getLoggedInAccountPreference(HomeActivity.this);
        registerUpdateProfileImageBroadcastReceiver();
        registerUpdateProfileNameBroadcastReceiver();
        setLoadingBarColor();
        setUserProfile(user);
        createHomeFragment(savedInstanceState);
        navigationDrawerToggle();
        selectFragmentFromDrawer(savedInstanceState);
        editUserProfile();
        onLogoutButtonClicked();

    }

    public void initialize() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
    }

    public void registerUpdateProfileImageBroadcastReceiver() {
        updateProfileImageBroadcast = new UpdateProfileImageBroadcast(binding);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.smartcitytravel.UPDATE_PROFILE_IMAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateProfileImageBroadcast, intentFilter);
    }

    public void registerUpdateProfileNameBroadcastReceiver() {
        updateProfileNameBroadcast = new UpdateProfileNameBroadcast(binding);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.smartcitytravel.UPDATE_PROFILE_NAME");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateProfileNameBroadcast, intentFilter);
    }

    public void setUserProfile(User user) {

        View headerLayout = binding.navigationView.getHeaderView(0);

        setName(headerLayout, user.getName());
        setEmail(headerLayout, user.getEmail());
        setProfileImage(headerLayout, user.getImage_url());

    }

    public void setName(View headerLayout, String name) {

        TextView nameTxt = headerLayout.findViewById(R.id.profileNameTxt);
        nameTxt.setText(util.capitalizedName(name));

        nameTxt.post(new Runnable() {
            @Override
            public void run() {
                if (nameTxt.getLineCount() > 1) {
                    nameTxt.setMaxLines(1);
                    nameTxt.setEllipsize(TextUtils.TruncateAt.END);
                }
            }
        });
    }

    public void setEmail(View headerLayout, String email) {

        TextView emailTxt = headerLayout.findViewById(R.id.profileEmailTxt);
        emailTxt.setText(email);

        emailTxt.post(new Runnable() {
            @Override
            public void run() {
                if (emailTxt.getLineCount() > 1) {
                    emailTxt.setMaxLines(1);
                    emailTxt.setEllipsize(TextUtils.TruncateAt.END);
                }
            }
        });

    }

    public void setProfileImage(View headerLayout, String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }

    public void selectFragmentFromDrawer(Bundle savedInstanceState) {
        binding.navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.home_menu) {
                            createHomeFragment(savedInstanceState);
                            setNavigationDrawerIcon(R.drawable.ic_light_white_navigation_drawer_menu);
                        } else if (item.getItemId() == R.id.favorite_menu) {
                            createFavoriteFragment(savedInstanceState);
                            setNavigationDrawerIcon(R.drawable.ic_white_navigation_drawer_menu);
                        } else if (item.getItemId() == R.id.settings_menu) {
                            createSettingsFragment(savedInstanceState);
                            setNavigationDrawerIcon(R.drawable.ic_light_white_navigation_drawer_menu);
                        } else if (item.getItemId() == R.id.about_us_menu) {
                            createAboutUsFragment(savedInstanceState);
                            setNavigationDrawerIcon(R.drawable.ic_light_white_navigation_drawer_menu);
                        }
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

    }

    public void setNavigationDrawerIcon(int drawableId) {
        Glide.with(this)
                .load(drawableId)
                .into(binding.navigationDrawerImg);
    }

    public void createFavoriteFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(binding.fragmentContainer.getId(), new FavoriteFragment())
                    .commit();
        }

    }

    public void createSettingsFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(binding.fragmentContainer.getId(), new SettingsFragment())
                    .commit();
        }
    }

    public void createAboutUsFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(binding.fragmentContainer.getId(), new AboutUsFragment())
                    .commit();
        }
    }

    public void navigationDrawerToggle() {
        binding.navigationDrawerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    public void createHomeFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.fragmentContainer.getId(), new HomeFragment())
                    .commit();
        }
        binding.navigationView.setCheckedItem(R.id.home_menu);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    public void onLogoutButtonClicked() {
        binding.logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                showLogoutDialog("Logout", "Do you want to logout?");
            }
        });
    }

    public void showLogoutDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
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

    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    public void logout() {
        Boolean account_type = preferenceHandler.getAccountTypePreference(HomeActivity.this);
        if (!account_type) {
            preferenceHandler.clearLoggedInAccountPreference(HomeActivity.this);
            moveToLoginActivity();

        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        preferenceHandler.clearLoggedInAccountPreference(HomeActivity.this);
                        moveToLoginActivity();
                    } else if (task.isCanceled()) {
                        Toast.makeText(HomeActivity.this, "Unable to logout", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void editUserProfile() {
        View headerLayout = binding.navigationView.getHeaderView(0);
        ShapeableImageView editProfileImg = headerLayout.findViewById(R.id.editProfileImg);

        editProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                    }
                }, 50);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 1000);

            }
        });

    }

    public void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

        }

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateProfileImageBroadcast);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateProfileNameBroadcast);
        super.onDestroy();
    }

}
