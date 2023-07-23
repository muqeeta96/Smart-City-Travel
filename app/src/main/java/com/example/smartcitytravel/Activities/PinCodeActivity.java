package com.example.smartcitytravel.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.WorkManager.ImageUpdateWorkManager;
import com.example.smartcitytravel.databinding.ActivityPinCodeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PinCodeActivity extends AppCompatActivity {
    private ActivityPinCodeBinding binding;
    private Util util;
    private Connection connection;
    private int pin_code;
    private String email;
    private boolean fromSignUpActivity;
    private String uploadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(PinCodeActivity.this, R.color.black);
        setLoadingBarColor();
        getEmail();
        setTitle();
        boldEmail();
        checkConnectionAndSendPinCode();
        continueButtonClickListener();
        resendCode();
    }

    public void initialize() {
        pin_code = 0;
        util = new Util();
        connection = new Connection();
        fromSignUpActivity = false;
        uploadImage = null;
    }

    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }

    public void setTitle() {
        String title = getIntent().getExtras().getString("title");
        binding.titleTxt.setText(title);

    }

    public void getEmail() {
        if (getIntent().getExtras().getString("signup_email") != null) {
            email = getIntent().getExtras().getString("signup_email");
            fromSignUpActivity = true;

        } else {
            email = getIntent().getExtras().getString("email");
            fromSignUpActivity = false;

        }
    }

    public void boldEmail() {
        String normalTxt = binding.pincodeTxt.getText().toString();
        SpannableString styleTxt = new SpannableString(normalTxt + " " + email);
        styleTxt.setSpan(new StyleSpan(Typeface.BOLD), 34, styleTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.pincodeTxt.setText(styleTxt);
    }

    public void validatePinCode() {
        String pinCode = binding.pinCodeEdit.getText().toString();
        if (pinCode.isEmpty()) {
            showPinCodeError(" ");
        } else if (Integer.parseInt(pinCode) == pin_code) {
            removePinCodeError();
            if (fromSignUpActivity) {
                checkConnectionAndCreateAccount();
            } else {
                moveToNewPasswordActivity();
            }

        } else if (Integer.parseInt(pinCode) != pin_code) {
            showPinCodeError(" ");
            util.createErrorDialog(PinCodeActivity.this, "Error", "Incorrect pin code entered");
        }
    }

    public void showPinCodeError(String errorMsg) {
        binding.pincodeLayout.setErrorIconDrawable(null);
        binding.pincodeLayout.setError(errorMsg);
    }

    public void removePinCodeError() {
        binding.pincodeLayout.setError(null);
    }

    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(PinCodeActivity.this);
                validatePinCode();
            }
        });
    }

    public void moveToNewPasswordActivity() {
        Intent intent = new Intent(this, NewPasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);

    }

    public void resendCode() {
        binding.resendCodeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectionAndSendPinCode();
            }
        });
    }

    public void checkConnectionAndSendPinCode() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(PinCodeActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            sendPinCode();
                        } else {
                            Toast.makeText(PinCodeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    public void sendPinCode() {

        Random random = new Random();
        pin_code = random.nextInt(10000 - 1000) + 1000;

        Toast.makeText(this, pin_code + "", Toast.LENGTH_LONG).show();

        String body = "";
        if (fromSignUpActivity) {
            body = pin_code + " is pin code to verify email";
        } else {
            body = pin_code + " is pin code to reset password";
        }

        BackgroundMail.newBuilder(PinCodeActivity.this)
                .withUsername("smartcitytravel011@gmail.com")
                .withPassword("kcfhrubiwizacdeq")
                .withMailto(email)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject("Pin code from smart city travel")
                .withBody(body)
                .withProcessVisibility(false)
                .send();


    }

    public void checkConnectionAndCreateAccount() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(PinCodeActivity.this);
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
                            createAccount();
                        } else {
                            Toast.makeText(PinCodeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    public void createAccount() {

        User user = getCreateAccountInfo();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .add(user)
                .addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String newUserId = documentReference.getId();
                        if (!newUserId.isEmpty()) {
                            if (uploadImage != null) {
                                startUpdateImageWorkManager(newUserId);
                            }
                            moveToSuccessfulAccountMessageActivity();
                        }
                        hideLoadingBar();
                    }
                });

    }

    public User getCreateAccountInfo() {
        String password = getIntent().getExtras().getString("signup_password");
        String name = getIntent().getExtras().getString("signup_name");

        String normalizedFullName = name.toLowerCase();
        normalizedFullName = normalizedFullName.trim().replaceAll("\\s{2,}", " ");

        String imageUrl = getIntent().getExtras().getString("signup_imageUrl");

        User user;
        if (imageUrl.equals(getString(R.string.default_profile_image_url))) {
            user = new User(normalizedFullName, email, password, imageUrl, false);
        } else {
            uploadImage = imageUrl;
            user = new User(normalizedFullName, email, password, "", false);
        }
        return user;
    }

    public void startUpdateImageWorkManager(String newUserId) {
        Data data = new Data.Builder()
                .putString("image_url", uploadImage)
                .putString("userId", newUserId)
                .putBoolean("update_UI", false)
                .build();

        WorkRequest imageUpdateWorkRequest = new OneTimeWorkRequest.
                Builder(ImageUpdateWorkManager.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this)
                .enqueue(imageUpdateWorkRequest);
    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(PinCodeActivity.this);
    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(PinCodeActivity.this);
    }

    public void moveToSuccessfulAccountMessageActivity() {
        Intent intent = new Intent(this, SuccessfulAccountMessageActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("message", "Account Created Successfully");
        startActivity(intent);
    }
}