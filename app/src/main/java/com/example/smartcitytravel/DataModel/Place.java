package com.example.smartcitytravel.DataModel;

import java.io.Serializable;

public class Place implements Serializable {
    private String placeId;
    private String Name;
    private String Image1;
    private Float Rating;
    private String Sub_type;
    private String Place_type;

    public Place() {
    }

    public Place(String name, String image1, Float rating, String sub_type, String place_type) {
        Name = name;
        Image1 = image1;
        Rating = rating;
        Sub_type = sub_type;
        Place_type = place_type;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage1() {
        return Image1;
    }

    public void setImage1(String image1) {
        Image1 = image1;
    }

    public Float getRating() {
        return Rating;
    }

    public String getSub_type() {
        return Sub_type;
    }

    public void setSub_type(String sub_type) {
        Sub_type = sub_type;
    }

    public void setRating(Float rating) {
        Rating = rating;
    }

    public String getPlace_type() {
        return Place_type;
    }

    public void setPlace_type(String place_type) {
        Place_type = place_type;
    }
}
