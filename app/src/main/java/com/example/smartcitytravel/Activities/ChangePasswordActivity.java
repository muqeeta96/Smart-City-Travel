package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private PreferenceHandler preferenceHandler;
    private Validation validation;
    private Util util;
    private Connection connection;
    private boolean validate_old_password;
    private boolean validate_new_password;
    private boolean validate_confirm_new_password;
    private boolean validate_different_password;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        setToolBarTheme();
        save();

    }

    public void initialize() {
        preferenceHandler = new PreferenceHandler();
        validation = new Validation();
        util = new Util();
        user = preferenceHandler.getLoggedInAccountPreference(this);
        connection = new Connection();

        validate_old_password = false;
        validate_new_password = false;
        validate_different_password = false;
        validate_confirm_new_password = false;

    }


    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Change Password");
    }

    public void validateOldPassword() {
        if (user.getPassword().equals(binding.oldPasswordEdit.getText().toString())) {
            binding.oldPasswordLayout.setError(null);
            validate_old_password = true;
        } else {
            binding.oldPasswordLayout.setError("Error! Incorrect password");
            binding.oldPasswordLayout.setErrorIconDrawable(null);
            validate_old_password = false;
        }
    }

    public void validateNewPassword() {
        String password = binding.newPasswordEdit.getText().toString();
        String errorMessage = validation.validatePassword(password);
        if (errorMessage.isEmpty()) {
            removeNewPasswordError();
        } else {
            showNewPasswordError(errorMessage);
        }
    }

    public void showNewPasswordError(String errorMsg) {
        binding.newPasswordLayout.setError(errorMsg);
        binding.newPasswordLayout.setErrorIconDrawable(null);
        validate_new_password = false;
    }


    public void removeNewPasswordError() {
        binding.newPasswordLayout.setError(null);
        validate_new_password = true;
    }

    public void matchNewPasswordAndConfirmNewPassword() {
        String password = binding.newPasswordEdit.getText().toString();
        String confirmPassword = binding.confirmNewPasswordEdit.getText().toString();
        int errorCode = validation.matchPasswordAndConfirmPassword(password, confirmPassword);

        if (errorCode == 2) {
            showConfirmNewPasswordError("ERROR! Re-enter password");
        } else if (errorCode == 1) {
            showConfirmNewPasswordError("ERROR! Password not matched");
            showNewPasswordError(" ");
        } else if (errorCode == 0) {
            removeConfirmNewPasswordError();
        } else {
            showConfirmNewPasswordError("ERROR! Invalid password");
        }
    }

    public void showConfirmNewPasswordError(String errorMsg) {
        binding.confirmNewPasswordLayout.setError(errorMsg);
        binding.confirmNewPasswordLayout.setErrorIconDrawable(null);
        validate_confirm_new_password = false;
    }


    public void removeConfirmNewPasswordError() {
        binding.confirmNewPasswordLayout.setError(null);
        validate_confirm_new_password = true;
    }

    public void validateDifferentPassword() {
        String oldPassword = binding.oldPasswordEdit.getText().toString();
        String newPassword = binding.newPasswordEdit.getText().toString();
        if (newPassword.equals(oldPassword)) {
            util.createErrorDialog(this, "Password Issue", "New password should not be same as old password");
            validate_different_password = false;
        } else {
            validate_different_password = true;
        }

    }


    public void checkConnectionAndUpdateNewPassword() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean isInternetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable) {
                            updateNewPassword();
                        } else {
                            hideLoadingBar();
                            Toast.makeText(ChangePasswordActivity.this, "Unable to update password", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        executor.shutdown();
    }

    public void updateNewPassword() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .document(user.getUserId())
                .update("password", binding.newPasswordEdit.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingBar();
                        preferenceHandler.updatePasswordPreference(binding.newPasswordEdit.getText().toString(), ChangePasswordActivity.this);
                        user.setPassword(binding.newPasswordEdit.getText().toString());
                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(ChangePasswordActivity.this);
                validateOldPassword();
                validateNewPassword();
                matchNewPasswordAndConfirmNewPassword();

                if (validate_new_password && validate_old_password && validate_confirm_new_password) {
                    validateDifferentPassword();

                    if (validate_different_password) {
                        checkConnectionAndUpdateNewPassword();
                    }
                }
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