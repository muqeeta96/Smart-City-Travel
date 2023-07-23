package com.example.smartcitytravel.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityEmailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailActivity extends AppCompatActivity {
    private ActivityEmailBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private Validation validation;
    private boolean validate_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(EmailActivity.this, R.color.black);
        setLoadingBarColor();
        continueButtonClickListener();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();
        validate_email = false;
    }

    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EmailActivity.this);
                validateEmail();
                if (validate_email) {
                    checkConnectionAndVerifyEmail();
                }


            }
        });
    }

    public void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String errorMessage = validation.validateEmail(email);
        if (errorMessage.isEmpty()) {
            hideEmailError();

        } else {
            showEmailError(errorMessage);
        }
    }

    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
        validate_email = false;
    }

    public void hideEmailError() {
        binding.emailLayout.setError(null);
        validate_email = true;
    }

    public void checkConnectionAndVerifyEmail() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(EmailActivity.this);
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
                            Toast.makeText(EmailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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
                                    util.createErrorDialog(EmailActivity.this, "Account", "Account exist with google. Google account password can only change through google");
                                } else {
                                    moveToPinCodeActivity();
                                }
                            }
                        } else {
                            util.createErrorDialog(EmailActivity.this, "Account", "No account exist with this email");
                        }
                        hideLoadingBar();
                    }
                });
    }

    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(EmailActivity.this);

    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(EmailActivity.this);

    }

    public void moveToPinCodeActivity() {
        Intent intent = new Intent(this, PinCodeActivity.class);
        intent.putExtra("email", binding.emailEdit.getText().toString());
        intent.putExtra("title", "Reset Your Password");
        startActivity(intent);
    }

}