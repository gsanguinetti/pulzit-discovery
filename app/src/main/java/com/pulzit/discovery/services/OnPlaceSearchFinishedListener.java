package com.pulzit.discovery.services;

import java.util.List;

import se.walkercrou.places.Place;

/**
 * Created by gastonsanguinetti on 07/05/16.
 */
public interface OnPlaceSearchFinishedListener {
    void onPlacesSearchFinished(List<Place> places);
    void onPlacesSearchFailed();
    void onPlacesSearchCancelled(List<Place> places);
}
