package com.example.smartcitytravel.DataModel;

import com.google.firebase.firestore.Exclude;


public class Favorite {
    private String favoriteId;
    private String userId;
    private String placeId;

    public Favorite() {
    }

    public Favorite(String userId, String placeId) {
        this.userId = userId;
        this.placeId = placeId;
    }

    @Exclude
    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
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
}
