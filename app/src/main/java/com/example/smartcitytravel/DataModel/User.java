package com.example.smartcitytravel.DataModel;

import com.google.firebase.firestore.Exclude;

public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private String image_url;
    private Boolean google_account;

    public User() {

    }

    public User(String name, String email, String password, String image_url, Boolean google_account) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.image_url = image_url;
        this.google_account = google_account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Boolean getGoogle_account() {
        return google_account;
    }

    public void setGoogle_account(Boolean google_account) {
        this.google_account = google_account;
    }
}
