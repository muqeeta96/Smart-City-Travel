package com.example.smartcitytravel.DataModel;

public class PlaceDetail extends Place {
    private String Description;
    private String Image2;
    private String Image3;
    private String Latitude;
    private String Longitude;
    private String Timing;
    private String City;

    public PlaceDetail() {
        super();
    }

    public PlaceDetail(String name, String city, String description, String image1, String image2, String image3, Float rating, String latitude, String longitude, String timing, String sub_type, String place_type) {
        super(name, image1, rating, sub_type, place_type);
        City = city;
        Description = description;
        Image2 = image2;
        Image3 = image3;
        Latitude = latitude;
        Longitude = longitude;
        Timing = timing;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImage2() {
        return Image2;
    }

    public void setImage2(String image2) {
        Image2 = image2;
    }

    public String getImage3() {
        return Image3;
    }

    public void setImage3(String image3) {
        Image3 = image3;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getTiming() {
        return Timing;
    }

    public void setTiming(String timing) {
        Timing = timing;
    }
}
