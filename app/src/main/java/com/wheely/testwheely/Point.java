package com.wheely.testwheely;


import com.orm.dsl.Table;

@Table
public class Point{
    private Long id;
    private double lon;
    private double lat;

    public Point() {
    }

    public Point(Long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {

        return id;
    }

    public void setLon(double lon) {

        this.lon = lon;
    }
}
