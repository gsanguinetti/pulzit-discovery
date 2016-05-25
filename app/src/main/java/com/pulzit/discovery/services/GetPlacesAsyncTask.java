package com.pulzit.discovery.services;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pulzit.discovery.R;
import com.pulzit.discovery.domain.PlaceDiscovered;
import com.pulzit.discovery.domain.PlacesQuery;
import com.pulzit.discovery.global.UtilConstants;

import java.util.ArrayList;
import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

/**
 * Created by gastonsanguinetti on 07/05/16.
 */
public class GetPlacesAsyncTask extends AsyncTask<PlacesQuery, Integer, List<Place>> {

    GooglePlaces googlePlacesClient;
    OnPlaceSearchFinishedListener onPlaceSearchFinishedListener;
    Context context;
    int discoveredQty = 0;
    int undiscoveredQty = 0;

    public GetPlacesAsyncTask(Context context, OnPlaceSearchFinishedListener onPlaceSearchFinishedListener) {
        googlePlacesClient = new GooglePlaces(UtilConstants.PLACES_WEB_API_KEY);
        this.onPlaceSearchFinishedListener = onPlaceSearchFinishedListener;
        this.context = context;
    }

    @Override
    protected List<Place> doInBackground(PlacesQuery... params) {
        PlacesQuery query = params[0];
        List<Place> rowPlaces;
        if (query.getKeywords() != null) {
            rowPlaces = googlePlacesClient.getNearbyPlaces(query.getLat(), query.getLng(), query.getRadius(),
                    Param.name("language").value(query.getLang()),
                    Param.name("types").value(query.getTypes()),
                    Param.name("rankby").value(query.getOrderBy()),
                    Param.name("keyword").value(query.getKeywords()));
        } else {
            rowPlaces = googlePlacesClient.getNearbyPlaces(query.getLat(), query.getLng(), query.getRadius(),
                    Param.name("language").value(query.getLang()),
                    Param.name("types").value(query.getTypes()),
                    Param.name("rankBy").value(query.getOrderBy()));
        }
        List<Place> undiscoveredPlaces = new ArrayList<>();
        for (Place place : rowPlaces) {
            if(!PulzitService.getInstance().isPlaceDiscovered(place.getPlaceId())) {
                undiscoveredPlaces.add(place);
                undiscoveredQty++;
            } else {
                discoveredQty++;
            }
        }
        return undiscoveredPlaces;
    }

    @Override
    protected void onCancelled(List<Place> places) {
        super.onCancelled(places);
        if (onPlaceSearchFinishedListener != null) {
            onPlaceSearchFinishedListener.onPlacesSearchCancelled(places);
        }
    }

    @Override
    protected void onPostExecute(List<Place> places) {
        super.onPostExecute(places);
        if (onPlaceSearchFinishedListener != null) {
            onPlaceSearchFinishedListener.onPlacesSearchFinished(places);
        }
        Toast.makeText(context, context.getString(R.string.discovery_stats, undiscoveredQty, discoveredQty), Toast.LENGTH_LONG )
                .show();
    }
}