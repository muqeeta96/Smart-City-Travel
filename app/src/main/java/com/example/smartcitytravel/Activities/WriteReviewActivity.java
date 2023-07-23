package com.example.smartcitytravel.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityWriteReviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteReviewActivity extends AppCompatActivity {
    private ActivityWriteReviewBinding binding;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Connection connection;
    private Toast updateMessageToast;
    private boolean toEdit;
    private boolean updateReview;
    private boolean updateRating;
    private boolean backPressed;
    private Intent returnIntentData;
    private FirebaseFirestore db;
    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        getIntentData();
        setToolbar();
        setUserProfile();
        submitButtonClickListener();
        reviewChangeListener();
        ratingChangeListener();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(this);
        updateMessageToast = new Toast(this);
        returnIntentData = new Intent();
        connection = new Connection();
        db = FirebaseFirestore.getInstance();
        placeId = getIntent().getExtras().getString("placeId");

        updateReview = false;
        updateRating = false;
        backPressed = false;
    }

    public void getIntentData() {
        if (getIntent().getExtras() != null) {
            toEdit = getIntent().getExtras().getBoolean("edit");
            setReviewAndRating();
        } else {
            toEdit = false;
        }
    }

    public void setReviewAndRating() {
        String review = getIntent().getExtras().getString("review");
        float rating = getIntent().getExtras().getFloat("rating");

        binding.reviewEdit.setText(review);
        binding.ratingBar.setRating(rating);
    }

    public void submitButtonClickListener() {
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(WriteReviewActivity.this);

                checkConnectionAndUploadReview();
            }
        });

    }

    public void checkConnectionAndUploadReview() {
        if (updateRating || updateReview) {

            boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(WriteReviewActivity.this);
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
                                uploadReview();
                            } else {
                                Toast.makeText(WriteReviewActivity.this, "Unable to update review", Toast.LENGTH_SHORT).show();
                                hideLoadingBar();
                                finishActivity();
                            }
                        }
                    });

                }
            });
            executor.shutdown();

        } else {
            hideLoadingBar();
        }
    }

    public void uploadReview() {


        if (toEdit) {
            String reviewId = getIntent().getExtras().getString("reviewId");
            if (updateReview) {
                updateReview(reviewId);
            }
            if (updateRating) {
                updateRating(reviewId);
            }
        } else {
            writeReview();
        }


    }

    public void writeReview() {
        Review review = new Review(user.getUserId(), placeId, binding.ratingBar.getRating(),
                binding.reviewEdit.getText().toString());

        db.collection("review")
                .add(review)
                .addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        returnIntentData.putExtra("reviewId", documentReference.getId());
                        returnIntentData.putExtra("review", review.getFeedback());
                        returnIntentData.putExtra("rating", review.getRating());

                        calculateAverageRating(review.getRating());

                        updateReview = false;
                        updateRating = false;
                        finishActivity();
                    }
                });
    }

    public void updateReview(String reviewId) {
        db.collection("review")
                .document(reviewId)
                .update("feedback", binding.reviewEdit.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        returnIntentData.putExtra("reviewId", reviewId);
                        returnIntentData.putExtra("review", binding.reviewEdit.getText().toString());
                        returnIntentData.putExtra("rating", binding.ratingBar.getRating());
                        returnIntentData.putExtra("avgRating", -1.0);

                        if (updateReview && !updateRating) {
                            updateResultIntentData();
                            hideLoadingBar();
                            displayUpdateMessage();
                        }

                        updateReview = false;
                        finishActivity();
                    }
                });
    }

    public void updateRating(String reviewId) {

        db.collection("review")
                .document(reviewId)
                .update("rating", binding.ratingBar.getRating())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        returnIntentData.putExtra("reviewId", reviewId);
                        returnIntentData.putExtra("review", binding.reviewEdit.getText().toString());
                        returnIntentData.putExtra("rating", binding.ratingBar.getRating());

                        updateRating = false;
                        finishActivity();
                        calculateAverageRating(binding.ratingBar.getRating());
                    }
                });
    }

    void updateResultIntentData() {
        setResult(100, returnIntentData);
    }

    public void reviewChangeListener() {
        binding.reviewEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateReview = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void ratingChangeListener() {
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRating = true;
            }
        });

    }

    public void setToolbar() {
        util.setStatusBarColor(this, R.color.theme_light);
        if (toEdit) {
            util.addToolbar(this, binding.toolbarLayout.toolbar, "Edit Review");
        } else {
            util.addToolbar(this, binding.toolbarLayout.toolbar, "Write A Review");

        }

    }

    public void setUserProfile() {
        binding.nameTxt.setText(util.capitalizedName(user.getName()));

        Picasso.get()
                .load(user.getImage_url())
                .into(binding.profileImg);

    }

    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(this);
    }

    public void displayUpdateMessage() {
        try {
            updateMessageToast.cancel();
            updateMessageToast.getView().isShown();
        } catch (Exception ignored) {
            updateMessageToast = Toast.makeText(this, "Review updated", Toast.LENGTH_SHORT);
            updateMessageToast.show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (updateReview || updateRating) {
                backPressed = true;
                util.hideKeyboard(this);
                showUpdateReviewConfirmationDialog();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    public void showUpdateReviewConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText("Confirm");

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText("Do you want to save changes?");

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
                        checkConnectionAndUploadReview();
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

    @Override
    public void onBackPressed() {
        if (updateReview || updateRating) {
            backPressed = true;
            util.hideKeyboard(this);
            showUpdateReviewConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void calculateAverageRating(double userRating) {
        db.collection("review")
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int rating_count = queryDocumentSnapshots.size();

                            if (rating_count > 1) {
                                double all_rating = 0;
                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    Review review = querySnapshot.toObject(Review.class);

                                    all_rating += review.getRating();
                                }
                                double newAvgRating = all_rating / rating_count;

                                newAvgRating = roundDoubleValue(newAvgRating);

                                updateAverageRating(newAvgRating);
                            } else {
                                updateAverageRating(userRating);
                            }
                            updateVotes(rating_count);
                        }

                    }
                });
    }

    public void updateAverageRating(double newAvgRating) {
        db.collection("place")
                .document(placeId)
                .update("Rating", newAvgRating)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingBar();
                        returnIntentData.putExtra("avgRating", newAvgRating);

                        updateResultIntentData();

                        if (toEdit) {
                            displayUpdateMessage();
                        } else {
                            Toast.makeText(WriteReviewActivity.this, "Review posted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    public void updateVotes(int no_of_votes) {
        db.collection("place")
                .document(placeId)
                .update("Vote", no_of_votes);
    }

    public double roundDoubleValue(double value) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(value));
    }

}