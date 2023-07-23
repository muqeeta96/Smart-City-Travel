package com.example.smartcitytravel.DataModel;

public class PlaceLocation {
    private String placeId;
    private String Latitude;
    private String Longitude;

    public PlaceLocation() {
        super();
    }

    public PlaceLocation(String latitude, String longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
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
}
