package com.pulzit.discovery.domain;

/**
 * Created by gastonsanguinetti on 07/05/16.
 */
public class PlacesQuery {

    private double lat;
    private double lng;
    private double radius;
    private String types;
    private String lang;
    private String orderBy;
    private String keywords;

    public PlacesQuery() {}

    public PlacesQuery(double lat, double lng, double radius, String types, String lang, String orderBy, String keywords) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.types = types;
        this.lang = lang;
        this.orderBy = orderBy;
        this.keywords = keywords;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
