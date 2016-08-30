package com.myexamples.tracker;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Location previousLocation;

    // isFirstAccurateLocation is a a simple counter with
    // which is deduced what location is regarded as starting point
    // (first location sent is not very accurate)
    private Integer firstAccurateLocationCounter;
    private final static Integer LOCATION_ACCURACY = 3;
    private boolean trackingStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firstAccurateLocationCounter = 0;
        trackingStarted = false;
        previousLocation = null;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (isLocationPermissionGranted()) {
            startLocationDependentServices();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isLocationPermissionGranted()) {
            startLocationDependentServices();
        }
    }

    private boolean isLocationPermissionGranted () {
        return ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION")
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationDependentServices() {
        mMap.setMyLocationEnabled(true);
        mGoogleApiClient
                = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(20*1000);
        locationRequest.setSmallestDisplacement(5);
        locationRequest.setFastestInterval(5*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
        Log.d(getClass().getName(), "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(firstAccurateLocationCounter==0)
            zoomToCurrentLocation(location);
        else if (firstAccurateLocationCounter<LOCATION_ACCURACY)
            return;
        else if(firstAccurateLocationCounter==LOCATION_ACCURACY)
            addMarker(location);

        firstAccurateLocationCounter++;
        previousLocation = location;
        if(trackingStarted)
            drawLine(location);
        Log.d(getClass().getName(), location.toString());
    }

    public void startTracking(View view) {
        trackingStarted = true;
    }

    public void addMarker(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Start Location"));
    }

    public void zoomToCurrentLocation(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
    public void drawLine(Location location){
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()),
                        new LatLng(location.getLatitude(), location.getLongitude()))
                .width(5)
                .color(Color.RED));
    }
    //TODO: Add tabbed view with: distance traversed, avg. speed...
    //TODO: (taxy cab fare.. (start + km * pricePerKM + IdleTime * pricePerTimeSpentIdle)
    //TODO: Save and Load existing tracks from DB for example.
}
