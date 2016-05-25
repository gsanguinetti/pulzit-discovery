package com.pulzit.discovery.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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

import com.pulzit.discovery.global.UtilConstants;
import com.pulzit.discovery.services.PulzitService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

import java.util.List;

import me.alexrs.wavedrawable.WaveDrawable;
import se.walkercrou.places.Place;

public class DiscoveryActivity extends AppCompatActivity implements MapViewPager.Callback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SearchPlacesFragment.OnFragmentInteractionListener,
        ShowPlaceFragment.OnFragmentInteractionListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "gu5ZVXvQEu1jcVFv0G7b7atGi";
    private static final String TWITTER_SECRET = "cjjVkzlQB0atspS1ks60l9FK019Tczxt88SaoD7aB7JvUhJJFp";


    private MapViewPager mapViewPager;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private DiscoveryAdapter adapter;
    private View twitterLoginView;
    private View mapSearchContainer;
    private TwitterLoginButton loginButton;

    private ProgressDialog processingPlaceDialog;
    private View targetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
        mapViewPager = (MapViewPager) findViewById(R.id.mapViewPager);
        targetView = findViewById(R.id.targetView);
        twitterLoginView = findViewById(R.id.twitterLoginView);
        mapSearchContainer = findViewById(R.id.mapSearchContainer);

        initAppBar();
        if (isGooglePlayServicesAvailable()) {
            startLocation();
        }

        if (Twitter.getSessionManager().getActiveSession() == null)
            twitterLoginView.setVisibility(View.VISIBLE);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                if (result.data.getUserName().equals(UtilConstants.TW_USER)) {
                    twitterLoginView.setVisibility(View.GONE);
                } else {
                    Toast.makeText(DiscoveryActivity.this, R.string.twitter_login_bad_account,
                            Toast.LENGTH_LONG).show();
                    logoutTwitter();
                }
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(DiscoveryActivity.this, R.string.twitter_login_error,
                        Toast.LENGTH_LONG).show();
            }
        });

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

    public void onRestartSearch() {
        mapViewPager.getViewPager().setCurrentItem(0);
        mapSearchContainer.setVisibility(View.VISIBLE);
        resetSearch();
    }

    @Override
    public void onFinishSearchSuccessful(List<Place> places) {
        if (places != null && places.size() > 0) {
            adapter.setPlaces(places);
            adapter.notifyDataSetChanged();
            mapViewPager.onMapReady(mapViewPager.getMap());
            mapSearchContainer.setVisibility(View.GONE);
            mapViewPager.getViewPager().setCurrentItem(places.size());
            mapViewPager.getViewPager().beginFakeDrag();
        } else {
        }
        resetSearch();
    }

    @Override
    public LatLngBounds getSearchableArea() {
        return mapViewPager.getMap().getProjection().getVisibleRegion().latLngBounds;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SearchUsersActivity.USER_PICK_REQUEST) {
            if (resultCode == RESULT_OK) {
                onPlaceProcessed();
            }
        }

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void logoutTwitter() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            ClearCookies(getApplicationContext());
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
    }

    public static void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void onPlaceProcessed() {
        adapter.removeItem(mapViewPager.getViewPager().getCurrentItem() - 1);
        mapViewPager.onMapReady(mapViewPager.getMap());
        mapViewPager.getViewPager().setCurrentItem(mapViewPager.getViewPager().getAdapter().getCount());
        Toast okToast = Toast.makeText(this, R.string.place_processed, Toast.LENGTH_SHORT);
        okToast.setGravity(Gravity.CENTER, 0, -200);
        okToast.show();
        if (mapViewPager.getViewPager().getCurrentItem() == 0) {
            onRestartSearch();
        }

    }

    @Override
    public void onBackPressed() {

        if (adapter != null && adapter.size() > 0 && mapViewPager.getViewPager().getCurrentItem() > 0) {
            onRestartSearch();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onPlaceDismissed() {
        showProgressDialog();
        Place place = adapter.getPlaceAt(mapViewPager.getViewPager().getCurrentItem() - 1);
        PulzitService.getInstance().addPlaceToBlockedList(place.getPlaceId(), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if(firebaseError==null) {
                    dismissProgressDialog();
                    onPlaceProcessed();
                } else {
                    Toast.makeText(DiscoveryActivity.this, getString(R.string.error_firebase),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMissingAccountForPlace() {
        showProgressDialog();
        Place place = adapter.getPlaceAt(mapViewPager.getViewPager().getCurrentItem() - 1);
        PulzitService.getInstance().addPlaceToAccountNotFoundList(place.getPlaceId(), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if(firebaseError==null) {
                    dismissProgressDialog();
                    onPlaceProcessed();
                } else {
                    Toast.makeText(DiscoveryActivity.this, getString(R.string.error_firebase),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showProgressDialog() {
        if (processingPlaceDialog == null || !processingPlaceDialog.isShowing()) {
            processingPlaceDialog = new ProgressDialog(this);
            processingPlaceDialog.setMessage(getString(R.string.progress_msg));
            processingPlaceDialog.setIndeterminate(true);
            processingPlaceDialog.setCanceledOnTouchOutside(false);
            processingPlaceDialog.setCancelable(false);
            processingPlaceDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if(processingPlaceDialog!= null && processingPlaceDialog.isShowing()){
            processingPlaceDialog.dismiss();
            processingPlaceDialog = null;
        }
    }
}
