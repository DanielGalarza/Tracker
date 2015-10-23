package com.example.danielgalarza.tracker;

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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;

    private Button mStartButton, mStopButton, mClearButton;

    private NumberPicker millisecondPicker;

    private TextView mStateTextView, mLatitudeTextView,
            mLongitudeTextView, mDistanceTextView, mShowInfo;

    private LocationManager locationManager;
    private Criteria criteria;
    private Location myLocation;
    private String provider;

    private double longitude;
    private double latitude;
    private double distance;
    private double oldDistance;
    private double newDistance;
    private boolean toggle = true;

    private int time = 5;

    Location startLoc;
    Location endLoc;


    /**********************************************************************************************/
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
        mClearButton = (Button) findViewById(R.id.run_clearButton);
        mShowInfo = (TextView) findViewById(R.id.show_info);
        mShowInfo.setMovementMethod(new ScrollingMovementMethod());
        millisecondPicker = (NumberPicker) findViewById(R.id.editTime);

        /**************************    LOCATION LISTENER    ***************************************/
        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                calcDist(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                //
            }

            @Override
            public void onProviderEnabled(String s) {
                //
            }

            @Override
            public void onProviderDisabled(String s) {
                //
            }
        };

        /************************    START BUTTON LISTENER    *************************************/
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }

                locationManager.requestLocationUpdates(provider, time, 0, listener);
                Log.d("start button", " Pressed!!");
            }

        });

        /*************************    STOP BUTTON LISTENER    *************************************/
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }
                //removing location updates
                locationManager.removeUpdates(listener);

                mStateTextView.setText("Stopped");

                toggle = true;
            }

        });
        /*************************    CLEAR BUTTON LISTENER    *************************************/
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distance = 0;
                mLatitudeTextView.setText(" ");
                mLongitudeTextView.setText(" ");
                mDistanceTextView.setText("0" + " meters");
                mShowInfo.setText(" ");
            }
        });
        /*************************    MILLISECOND PICKER LISTENER    *************************************/
        millisecondPicker.setMaxValue(9999);
        millisecondPicker.setMinValue(0);
        millisecondPicker.setWrapSelectorWheel(true);
        millisecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                time = newVal;
                // TODO: MAKE time INTERVAL VALUE CHANGE ON CLICK
            }
        });

    } // End of onCreate()



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
    }// End of setUpMapIfNeeded()


    /**
     * This method sets up the Google Map.
     * This should only be called once and when we are sure that mMap is not null.
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

        if(myLocation != null) {

            // Get the lat and lng of current location
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();

            // mark my location on map with marker
            markMyLocation(latitude, longitude);
        }

    }// End of setUpMap()

    /**
     * This marks the last know location the chosen the provider on the map.
     * @param lat Latitude
     * @param lng Longitude
     */
    public void markMyLocation(double lat, double lng) {

        // Add Marker with title of current location
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("You are here! (Or you were here at some point)"));
        Log.d("Marker:", "adding marker!");

    }// End of markMyLocation()

    /**
     * This method calculates the distance between two location objects.
     * @param loc the location passed in by the location listener
     */
    public void calcDist(Location loc) {

        //debugging
        Log.d("calc dist" , "calculating distance");

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

        //debugging
        Log.d("LocationListener: ", "Lat: " + startLoc.getLatitude() + "     Lng: " + startLoc.getLongitude());
        Log.d("Distance", " distance result: " + distance);

        //Sending info to UI
        mStateTextView.setText("Started");
        mLatitudeTextView.setText(String.format("%.4f", new Double(startLoc.getLatitude())));
        mLongitudeTextView.setText(String.format("%.4f", new Double(startLoc.getLongitude())));
        mDistanceTextView.setText(String.format("%.2f",new Double(distance)) + " meters");

        //new lng and lat values are prepended to front of TextView mShowInfo
        mShowInfo.setText("\nCurrent Longitude: " + String.format("%.4f", new Double(startLoc.getLongitude())) +
                "\nCurrent Latitude: " + String.format("%.2f", new Double(startLoc.getLatitude())) +
                "\nDistance Traveled: " + String.format("%.2f", new Double(distance)) + " meters\n" +
                mShowInfo.getText());

    }// End of calcDist()


    /**
     *  saves the distance just in case orientation changes or screen is locked.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Displays distance in TextView
        //mShowInfo.setText(new Double(distance).toString());

    }

    /**
     * Sets up the map again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


}
