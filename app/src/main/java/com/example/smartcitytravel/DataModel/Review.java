package com.example.smartcitytravel.DataModel;

import com.google.firebase.firestore.Exclude;

public class Review {
    private String reviewId;
    private String userId;
    private String placeId;
    private Float rating;
    private String feedback;

    public Review() {
    }

    public Review(String userId, String placeId, Float rating, String feedback) {
        this.userId = userId;
        this.placeId = placeId;
        this.rating = rating;
        this.feedback = feedback;
    }

    @Exclude
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
