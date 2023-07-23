package com.example.smartcitytravel.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.WorkManager.ImageUpdateWorkManager;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private Util util;
    private Color color;
    private Validation validation;
    private PreferenceHandler preferenceHandler;
    private Connection connection;
    private User user;
    private String changeName;
    private boolean newProfileImageSelected;
    private boolean newProfileName;
    private Uri imageUri;
    private Toast noConnectionToast;
    private boolean backPressed;

    private final ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null && result.getData() != null) {
                        imageUri = result.getData().getData();
                        setProfileImage(imageUri.toString());
                        newProfileImageSelected = true;
                        binding.saveBtn.setEnabled(true);
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        setUserProfile();
        openGallery();
        nameChangeListener();
        save();
        changePassword();
    }

    public void initialize() {
        util = new Util();
        color = new Color();
        validation = new Validation();
        preferenceHandler = new PreferenceHandler();
        connection = new Connection();
        noConnectionToast = new Toast(this);

        changeName = "";
        user = preferenceHandler.getLoggedInAccountPreference(this);
        newProfileImageSelected = false;
        newProfileName = false;
        backPressed = false;


    }

    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Edit Profile");
    }

    public void setUserProfile() {
        setProfileImage(user.getImage_url());
        setName();
        setEmail();
    }

    public void setName() {
        String name = util.capitalizedName(user.getName());
        binding.fullNameEdit.setText(name);
    }

    public void setEmail() {
        binding.emailEdit.setText(user.getEmail());
        binding.emailLayout.setEnabled(false);
    }

    public void setProfileImage(String image_url) {
        Picasso.get()
                .load(image_url)
                .into(binding.selectProfileImgLayout.profileImg);
    }

    public void openGallery() {
        binding.selectProfileImgLayout.changeProfileImgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                imagePickerActivityResultLauncher.launch(galleryIntent);


            }
        });
    }

    public void checkConnectionAndUpdateProfileImage(Uri imageUri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(EditProfileActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            startUpdateImageWorkManager(imageUri);
                            Toast.makeText(EditProfileActivity.this, "Profile image take time to update", Toast.LENGTH_SHORT).show();

                            newProfileImageSelected = false;
                            setSaveButtonState();
                        } else {
                            displayNoConnectionMessage();
                        }

                        finishActivity();
                    }
                });
            }
        });
        executor.shutdown();
    }

    public void startUpdateImageWorkManager(Uri imageUri) {
        Data data = new Data.Builder()
                .putString("image_url", imageUri.toString())
                .putString("userId", user.getUserId())
                .putBoolean("update_UI", true)
                .build();

        WorkRequest imageUpdateWorkRequest = new OneTimeWorkRequest.
                Builder(ImageUpdateWorkManager.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(EditProfileActivity.this)
                .enqueue(imageUpdateWorkRequest);
    }

    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EditProfileActivity.this);
                applyChanges();
            }
        });
    }

    public void applyChanges() {
        if (validateFullName() && newProfileName) {
            checkConnectionAndUpdateProfileName();
        }
        if (newProfileImageSelected) {
            checkConnectionAndUpdateProfileImage(imageUri);
        }
    }

    public boolean validateFullName() {
        String fullName = binding.fullNameEdit.getText().toString();
        String errorMessage = validation.validateFullName(fullName);
        if (errorMessage.isEmpty()) {
            removeFullNameError();
            return true;
        } else {
            showFullNameError(errorMessage);
            return false;
        }
    }

    public void showFullNameError(String errorMsg) {
        binding.fullNameLayout.setErrorIconTintList(color.createColor(this, R.color.theme_light));
        binding.fullNameLayout.setError(errorMsg);
    }

    public void removeFullNameError() {
        binding.fullNameLayout.setError(null);
    }

    public void nameChangeListener() {

        binding.fullNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeName = s.toString();
                newProfileName = !changeName.equalsIgnoreCase(user.getName());

                binding.saveBtn.setEnabled(newProfileImageSelected || newProfileName);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void checkConnectionAndUpdateProfileName() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(EditProfileActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            updateProfileName();
                        } else {
                            displayNoConnectionMessage();
                            binding.saveBtn.setEnabled(true);
                            hideLoadingBar();
                            finishActivity();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    public void updateProfileName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user").document(user.getUserId())
                .update("name", changeName.toLowerCase())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.setName(changeName);
                        binding.fullNameEdit.setText(user.getName());
                        preferenceHandler.updateNamePreference(user.getName(), EditProfileActivity.this);
                        sendUpdateProfileNameBroadcast();
                        newProfileName = false;

                        Toast.makeText(EditProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        setSaveButtonState();
                        hideLoadingBar();

                        finishActivity();
                    }
                });

    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(this);
    }

    public void sendUpdateProfileNameBroadcast() {
        Intent updateProfileNameIntent = new Intent("com.example.smartcitytravel.UPDATE_PROFILE_NAME");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateProfileNameIntent);
    }

    public void displayNoConnectionMessage() {
        try {
            noConnectionToast.cancel();
            noConnectionToast.getView().isShown();
        } catch (Exception ignored) {
            noConnectionToast = Toast.makeText(this, "Unable to update profile", Toast.LENGTH_SHORT);
            noConnectionToast.show();
        }
    }

    public void setSaveButtonState() {
        binding.saveBtn.setEnabled(newProfileImageSelected || newProfileName);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (binding.saveBtn.isEnabled()) {
                backPressed = true;
                util.hideKeyboard(EditProfileActivity.this);
                showApplyChangesConfirmationDialog();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        if (binding.saveBtn.isEnabled()) {
            backPressed = true;
            util.hideKeyboard(EditProfileActivity.this);
            showApplyChangesConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void showApplyChangesConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText("Confirm");

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText("Do you want to apply changes?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyChanges();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    public void finishActivity() {
        if (backPressed) {
            finish();
        }
    }

    public void changePassword() {
        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

}