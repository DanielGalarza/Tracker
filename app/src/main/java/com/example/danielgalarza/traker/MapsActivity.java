package com.example.danielgalarza.traker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Button mStartButton, mStopButton;
    private TextView mStateTextView, mLatitudeTextView,
            mLongitudeTextView, mDistanceTextView, mShowInfo;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location myLocation;
    private double longitude;
    private double latitude;
    private double distance;
    private double initialLat;
    private double initialLng;
    double endLat;
    double endLng;
    private boolean toggle = true;

    private final int TIME = 5;


    //New
    Location startLoc;
    Location endLoc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mStateTextView = (TextView) findViewById(R.id.run_stateTextView);
        mLatitudeTextView = (TextView) findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView) findViewById(R.id.run_longitudeTextView);
        mDistanceTextView = (TextView) findViewById(R.id.run_distanceTextView);
        mStartButton = (Button) findViewById(R.id.run_startButton);
        mStopButton = (Button) findViewById(R.id.run_stopButton);
        mShowInfo = (TextView) findViewById(R.id.show_info);

        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //calcDistance(location);
                calcDist(location);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("LocationListener: ", "Lat: " + location.getLatitude() + "     Lng: " + location.getLongitude());

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                //
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("provider", "provider enabled");
            }

            @Override
            public void onProviderDisabled(String s) {
                //
            }
        };

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }

                locationManager.requestLocationUpdates(provider, TIME, 0, listener);
                Log.d("start button", " Pressed!!");
            }

        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }

                locationManager.removeUpdates(listener);
                mStateTextView.setText("Stopped");
                Log.d("stop button", " Pressed!!");
                toggle = true;
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    /**
     *
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     *
     */

    private void setUpMap() {

        //Prompt the user to enable location if location isn't enabled.

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // checking to permissions to access location. (ACCESS_FINE_LOCATION gives us the most accurate location.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }

        // New criteria object to retrieve provider
        criteria = new Criteria();

        //name of the best provider
        provider = locationManager.getBestProvider(criteria, true);

        // Get last known location
        myLocation = locationManager.getLastKnownLocation(provider);

        //Log.d("location: ", " Loc: " + myLocation);


        if(myLocation != null) {

            // Get the lat and lng of current location
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();

            // mark my location on map with marker
            markMyLocation(latitude, longitude);
        }

    }

    public void markMyLocation(double lat, double lng) {

        // Add Marker with title of current location
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("You are here! (Rough Estimate)"));
        Log.d("Marker:", "adding marker!");
    }


    public void calcDistance(Location loc) {

        // START HERE.....  try to get distance between locations

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }

        float [] results = new float[1];

        // Dealing with the initial lat and lng because they are  equal to 0 by default
        if(toggle) {

            initialLat = loc.getLatitude();
            initialLng = loc.getLongitude();
            toggle = false;
        }


        endLat = loc.getLatitude();
        endLng = loc.getLongitude();

        //test
        results[0] = 1;

        // Calculating distance: when we start tracking, at every second.
        loc.distanceBetween(initialLat, initialLng, endLat, endLng, results);

        distance = results[0];
        //float prevlat = results[0];
        //float prevlng = results[1];

        initialLat = endLat;
        initialLng = endLng;

        Log.d("LocationListener: ", "Lat: " + loc.getLatitude() + "     Lng: " + loc.getLongitude());
        Log.d("Distance", " distance result: " + distance);
        //Log.d("Location Prev: ", "Prev Lat: " + prevlat + "     Prev Lng: " + prevlng);

        //return results;

    }

    public void calcDist(Location loc) {

        Log.d("calc dist" , "calc distance");
        // Dealing with the initial lat and lng because they are  equal to 0 by default
        if(toggle) {

            startLoc = loc;
            toggle = !toggle;

        }

        // setting up the "end" location as the location that the listener passes in.
        endLoc = loc;

        // Calculating distance: when we start tracking, at every second.
        distance += startLoc.distanceTo(endLoc);

        startLoc = endLoc;

        Log.d("LocationListener: ", "Lat: " + startLoc.getLatitude() + "     Lng: " + startLoc.getLongitude());
        Log.d("Distance", " distance result: " + distance);

        //Sending info to UI
        String lat = new Double(startLoc.getLatitude()).toString();
        String lng = new Double(startLoc.getLongitude()).toString();
        String dist = new Double(distance).toString();

        mLatitudeTextView.setText(lat);
        mLongitudeTextView.setText(lng);
        mDistanceTextView.setText(dist + " meters");
        mStateTextView.setText("Started");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mShowInfo.setText(new Double(distance).toString());

    }


}
