package com.example.smartcitytravel.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private Validation validation;
    private String imageUrl;
    private boolean validate_full_name;
    private boolean validate_email;
    private boolean validate_password;
    private boolean validate_confirm_password;

    private final ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result != null && result.getData() != null) {
                        imageUrl = result.getData().getData().toString();
                        setProfileImage(imageUrl);
                    }

                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(SignUpActivity.this, R.color.grey);
        initializeValidator();
        setDefaultProfileImage();
        setLoadingBarColor();
        openGallery();
        registerAccount();

    }

    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();
        imageUrl = getString(R.string.default_profile_image_url);
    }

    public void initializeValidator() {
        validate_full_name = false;
        validate_email = false;
        validate_password = false;
        validate_confirm_password = false;
    }

    public void setDefaultProfileImage() {
        Picasso.get()
                .load(R.drawable.default_profile_image)
                .into(binding.selectProfileImgLayout.profileImg);

    }

    public void setProfileImage(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
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

    public void registerAccount() {
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFullName();
                validateEmail();
                validatePassword();
                matchPasswordAndConfirmPassword();

                if (validate_full_name && validate_email && validate_password && validate_confirm_password) {
                    checkConnectionAndVerifyEmail();
                }
            }
        });
    }

    public void validateFullName() {
        String fullName = binding.fullNameEdit.getText().toString();
        String errorMessage = validation.validateFullName(fullName);
        if (errorMessage.isEmpty()) {
            removeFullNameError();

        } else {
            showFullNameError(errorMessage);
        }
    }

    public void showFullNameError(String errorMsg) {
        binding.fullNameLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.fullNameLayout.setError(errorMsg);
        validate_full_name = false;
    }

    public void removeFullNameError() {
        binding.fullNameLayout.setError(null);
        validate_full_name = true;
    }

    public void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String errorMessage = validation.validateEmail(email);
        if (errorMessage.isEmpty()) {
            removeEmailError();

        } else {
            showEmailError(errorMessage);
        }
    }

    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
        validate_email = false;
    }

    public void removeEmailError() {
        binding.emailLayout.setError(null);
        validate_email = true;
    }

    public void validatePassword() {
        String password = binding.passwordEdit.getText().toString();
        String errorMessage = validation.validatePassword(password);
        if (errorMessage.isEmpty()) {
            removePasswordError();

        } else {
            showPasswordError(errorMessage);
        }
    }

    public void showPasswordError(String errorMsg) {
        binding.passwordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
        validate_password = false;
    }

    public void removePasswordError() {
        binding.passwordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.passwordLayout.setError(null);
        validate_password = true;
    }

    public void matchPasswordAndConfirmPassword() {
        String password = binding.passwordEdit.getText().toString();
        String confirmPassword = binding.confirmPasswordEdit.getText().toString();
        int errorCode = validation.matchPasswordAndConfirmPassword(password, confirmPassword);

        if (errorCode == 2) {
            showConfirmPasswordError("ERROR! Re-enter password");
        } else if (errorCode == 1) {
            showConfirmPasswordError("ERROR! Password not matched");
            showPasswordError(" ");
        } else if (errorCode == 0) {
            removeConfirmPasswordError();
        } else {
            showConfirmPasswordError("ERROR! Invalid password");
        }
    }

    public void showConfirmPasswordError(String errorMsg) {
        binding.confirmPasswordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.confirmPasswordLayout.setError(errorMsg);
        binding.confirmPasswordLayout.setErrorIconDrawable(null);
        validate_confirm_password = false;
    }

    public void removeConfirmPasswordError() {
        binding.confirmPasswordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.confirmPasswordLayout.setError(null);
        validate_confirm_password = true;
    }

    public void moveToPinCodeActivity() {

        Intent intent = new Intent(this, PinCodeActivity.class);
        intent.putExtra("signup_name", binding.fullNameEdit.getText().toString());
        intent.putExtra("signup_email", binding.emailEdit.getText().toString());
        intent.putExtra("signup_password", binding.passwordEdit.getText().toString());
        intent.putExtra("signup_imageUrl", imageUrl);
        intent.putExtra("title", "Verify Your Email");
        startActivity(intent);

    }

    public void checkConnectionAndVerifyEmail() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(SignUpActivity.this);
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
                            verifyEmail();
                        } else {
                            Toast.makeText(SignUpActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    public void verifyEmail() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("email", binding.emailEdit.getText().toString().toLowerCase())
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                boolean google_account = querySnapshot.getBoolean("google_account");
                                if (google_account) {
                                    util.createErrorDialog(SignUpActivity.this, "Account", "Account already exist with google. Try sign in with google");
                                } else {
                                    util.createErrorDialog(SignUpActivity.this, "Account", "Account already exist with this email");
                                }
                            }

                        } else {
                            moveToPinCodeActivity();
                        }
                        hideLoadingBar();
                    }
                });

    }


    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(SignUpActivity.this);
    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(SignUpActivity.this);
    }

    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_white));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }
}