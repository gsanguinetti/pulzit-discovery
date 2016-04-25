package com.pulzit.discovery.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.github.nitrico.mapviewpager.MapViewPager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.pulzit.discovery.R;

import java.util.List;

import me.alexrs.wavedrawable.WaveDrawable;
import se.walkercrou.places.Place;

public class DiscoveryActivity extends AppCompatActivity implements MapViewPager.Callback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SearchFragment.OnFragmentInteractionListener,
        PlaceFragment.OnFragmentInteractionListener {

    private MapViewPager mapViewPager;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private DiscoveryAdapter adapter;

    private View targetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapViewPager = (MapViewPager) findViewById(R.id.mapViewPager);
        targetView = findViewById(R.id.targetView);

        initAppBar();
        if (isGooglePlayServicesAvailable()) {
            startLocation();
        }

    }

    private void initAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    public void startViewMap(Location location) {
        if (adapter == null || adapter.getUserPosition() == null
                && mapViewPager.getViewPager().getCurrentItem() == 0) {
            adapter = new DiscoveryAdapter(getSupportFragmentManager(), this);
            adapter.setUserPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            mapViewPager.start(this, adapter, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onMapViewPagerReady() {
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int status = googleAPI.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            googleAPI.getErrorDialog(this, status, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void resetSearch() {
        targetView.setBackground(ContextCompat.getDrawable(this, R.drawable.oval_shape));
        mapViewPager.getMap().getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        startViewMap(location);
    }

    @Override
    public void onStartSearch() {
        WaveDrawable waveDrawable = new WaveDrawable(ContextCompat.getColor(this, R.color.colorWave),
                500);
        waveDrawable.setWaveInterpolator(new AccelerateInterpolator());
        targetView.setBackground(waveDrawable);
        waveDrawable.startAnimation();
        mapViewPager.getMap().getUiSettings().setAllGesturesEnabled(false);
    }

    @Override
    public void onFinishSearchFailed() {
        resetSearch();
    }

    @Override
    public void onFinishSearchSuccessful(List<Place> places) {
        if (places != null && places.size() > 0) {
            adapter.setPlaces(places);
            adapter.notifyDataSetChanged();
            mapViewPager.onMapReady(mapViewPager.getMap());
        } else {
        }
        resetSearch();
    }

    @Override
    public LatLngBounds getSearchableArea() {
        return mapViewPager.getMap().getProjection().getVisibleRegion().latLngBounds;

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
