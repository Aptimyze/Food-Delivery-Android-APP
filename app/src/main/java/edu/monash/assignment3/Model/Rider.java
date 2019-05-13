package edu.monash.assignment3.Model;

import java.util.List;

public class Rider {

    private double Lat;
    private double Lng;

    public Rider(double lat, double lng) {
        Lat = lat;
        Lng = lng;
    }

    public Rider() {
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return Lng;
    }

    public void setLng(double lng) {
        Lng = lng;
    }
}