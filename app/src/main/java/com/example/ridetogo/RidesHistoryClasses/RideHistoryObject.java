package com.example.ridetogo.RidesHistoryClasses;

public class RideHistoryObject {
    private String rideId;
    private String date;

    public RideHistoryObject(String rideId, String date) {
        this.rideId = rideId;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

}
