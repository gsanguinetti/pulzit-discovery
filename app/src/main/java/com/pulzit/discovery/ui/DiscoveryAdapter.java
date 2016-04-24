package com.pulzit.discovery.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.nitrico.mapviewpager.MapViewPager;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.pulzit.discovery.R;

import java.util.List;

import se.walkercrou.places.Place;

public class DiscoveryAdapter extends MapViewPager.Adapter {

    private LatLng userPosition;
    private Context context;
    private List<Place> places;

    public DiscoveryAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    @Override
    public CameraPosition getCameraPosition(int position) {
        if (position == 0 && userPosition != null) {
            return CameraPosition.fromLatLngZoom(userPosition, 14f);
        }
        LatLng placeLatLng = new LatLng(places.get(position-1).getLatitude(),
                places.get(position-1).getLongitude());
        return CameraPosition.fromLatLngZoom(placeLatLng, 16f);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return context.getString(R.string.my_location);
        return places.get(position - 1).getName();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return SearchFragment.newInstance();
        return SearchFragment.newInstance();
    }

    @Override
    public int getCount() {
        if (places != null) {
            return places.size() + 1;
        } else {
            return 1;
        }
    }

    public LatLng getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(LatLng userPosition) {
        this.userPosition = userPosition;
    }
}