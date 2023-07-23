package com.example.smartcitytravel.DataModel;

import com.google.firebase.firestore.Exclude;

public class Message {

    private String senderID;
    private String senderName;
    private String message;
    private String date;
    private String time;
    private String city;
    private boolean error = false;

    public Message() {
    }

    public Message(String senderID, String senderName, String message, String date, String time, String city) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.message = message;
        this.date = date;
        this.time = time;
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Exclude
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
