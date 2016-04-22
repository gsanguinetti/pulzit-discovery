package com.pulzit.discovery.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.nitrico.mapviewpager.MapViewPager;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.pulzit.discovery.R;

public class DiscoveryAdapter extends MapViewPager.Adapter {

    private LatLng userPosition;
    private Context context;

    public DiscoveryAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public CameraPosition getCameraPosition(int position) {
        if(position == 0 && userPosition != null) {
             return CameraPosition.fromLatLngZoom(userPosition, 16f);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0)
            return context.getString(R.string.my_location);
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return SearchFragment.newInstance();
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    public LatLng getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(LatLng userPosition) {
        this.userPosition = userPosition;
    }
}
