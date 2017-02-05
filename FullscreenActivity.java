package com.gpspro.gpspro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        startGpsProcessing();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public String getTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = simpleDateFormat.format(new Date());
        return s;
    }

    LocationManager locationManager = null;
    protected Location currentLocation = null;

    public void stopGpsProcessing() {
        if (locationListener != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
        if (gpsStatusListener != null) {
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }


    GpsStatus.Listener gpsStatusListener = null;
    LocationListener locationListener = null;

    public boolean startGpsProcessing() {
        try {
            stopGpsProcessing();

            //clearGpsLogFile();
            System.out.println(getTimeStamp() + ":Starting GPS Processing...");

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                System.out.println("GPS should be enabled.");
                return false;
            }

            gpsStatusListener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int i) {
                    //System.out.println(getTimeStamp() + ":GSL:onGpsStatusChanged");
                    switch (i) {
                        case GpsStatus.GPS_EVENT_STOPPED:
                            System.out.println(getTimeStamp() + ":GPS_EVENT_STOPPED");
                            break;
                        case GpsStatus.GPS_EVENT_STARTED:
                            System.out.println(getTimeStamp() + ":GPS_EVENT_STARTED");
                            break;
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            System.out.println(getTimeStamp() + ":GPS_EVENT_SATELLITE_STATUS");
                            GpsStatus gpsStatus = null;
                            try {
                                gpsStatus = locationManager.getGpsStatus(null);
                            } catch (SecurityException se) {

                            }
                            Iterable<GpsSatellite> gpsSatelliteIterable = gpsStatus.getSatellites();
                            Iterator<GpsSatellite> iterator = gpsSatelliteIterable.iterator();
                            int iCurrentSatIndex = 0;
                            while (iterator.hasNext()) {
                                GpsSatellite gpsSatellite = iterator.next();
                                /*System.out.println("SAT" + iCurrentSatIndex + ":Signal to Noise Ratio =" + gpsSatellite.getSnr());
                                System.out.println("SAT" + iCurrentSatIndex + ":Pseudo Random Number =" + gpsSatellite.getSnr());
                                System.out.println("SAT" + iCurrentSatIndex + ":Elevation =" + gpsSatellite.getElevation());
                                System.out.println("SAT" + iCurrentSatIndex + ":Azimuth =" + gpsSatellite.getAzimuth());
                                System.out.println("SAT" + iCurrentSatIndex + ":hasAlmanac =" + gpsSatellite.hasAlmanac());
                                System.out.println("SAT" + iCurrentSatIndex + ":hasEphemeris =" + gpsSatellite.hasEphemeris());
                                System.out.println("SAT" + iCurrentSatIndex + ":usedInFix =" + gpsSatellite.usedInFix());*/
                                iCurrentSatIndex++;
                            }
                            System.out.println(iCurrentSatIndex + " satellites detected.");
                            break;
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            System.out.println(getTimeStamp() + ":GPS_EVENT_FIRST_FIX");
                            break;
                        default:
                            break;
                    }
                }
            };

            try {
                locationManager.addGpsStatusListener(gpsStatusListener);
            } catch (SecurityException se) {

            }

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    System.out.println(getTimeStamp() + ":onLocationChanged: " + location.getLatitude() + ";" + location.getLongitude());

                    if (isNetworkAvailable()) {
                        GpsproHttpRequest ghr = new GpsproHttpRequest();

                        URL url = null;
                        try {
                            String deviceId = "testDevice";
                            String params = deviceId + ";" + location.getLatitude()/4.8376*98.655 + ";" + location.getLongitude()/3.8737*71.237 + ";" + location.getAltitude()/9.435*92.326;
                            url = new URL("http://investdata.000webhostapp.com/gpspro/?uploadcoord=" + params);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        ghr.execute(url);
                    }

                    try {
                        //currentLocation.setLatitude(location.getLatitude());
                        //currentLocation.setLongitude(location.getLongitude());
                    } catch (Exception e) {
                        //System.out.println("LL:OLC:Exception:" + e.getMessage());
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    //logGps(getTimeStamp() + ":LL:onStatusChanged:Provider=" + s + ";i=" + i);
                    if (s.equals(LocationManager.GPS_PROVIDER)) {
                        //logGps("GPS Provider processing.");
                        GpsStatus gpsStatus = null;
                        try {
                            gpsStatus = locationManager.getGpsStatus(null);
                        } catch (SecurityException se) {
                        }
                        Iterable<GpsSatellite> gpsSatelliteIterable = gpsStatus.getSatellites();
                        Iterator<GpsSatellite> iterator = gpsSatelliteIterable.iterator();
                        int iCurrentSatIndex = 0;
                        while (iterator.hasNext()) {
                            GpsSatellite gpsSatellite = iterator.next();
                            /*logGps("SAT" + iCurrentSatIndex + ":Signal to Noise Ratio =" + gpsSatellite.getSnr());
                            logGps("SAT" + iCurrentSatIndex + ":Pseudo Random Number =" + gpsSatellite.getSnr());
                            logGps("SAT" + iCurrentSatIndex + ":Elevation =" + gpsSatellite.getElevation());
                            logGps("SAT" + iCurrentSatIndex + ":Azimuth =" + gpsSatellite.getAzimuth());
                            logGps("SAT" + iCurrentSatIndex + ":hasAlmanac =" + gpsSatellite.hasAlmanac());
                            logGps("SAT" + iCurrentSatIndex + ":hasEphemeris =" + gpsSatellite.hasEphemeris());
                            logGps("SAT" + iCurrentSatIndex + ":usedInFix =" + gpsSatellite.usedInFix());*/
                            iCurrentSatIndex++;
                        }
                        //logGps("Number of sats in view = " + iCurrentSatIndex);
                    } else if (s.equals(LocationManager.NETWORK_PROVIDER)) {
                        //logGps("Network Provider processing.");
                    } else if (s.equals(LocationManager.PASSIVE_PROVIDER)) {
                        //logGps("Passive Provider processing.");
                    }
                }

                @Override
                public void onProviderEnabled(String s) {
                    //log(getTimeStamp() + ":LL:onProviderEnabled:s=" + s);
                }

                @Override
                public void onProviderDisabled(String s) {
                    //log(getTimeStamp() + ":LL:onProviderDisabled:s=" + s);
                }
            };

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (Exception e) {
                System.out.println("LL:RLU:Exception:" + e.getMessage());
            }

            return true;

        } catch (Exception e) {
            /*log("LL:Exception:" + e.getMessage());
            logGps("LL:Exception:" + e.getMessage());*/
            System.out.println("LL:Exception:" + e.getMessage());

            return false;
            /*StackTraceElement[] stackTraceElements = e.getStackTrace();
            for(int i=0;i<stackTraceElements.length;i++){
                logDebug(stackTraceElements[i].toString());
            }*/
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean fakeGpsTest2(){

        LocationManager mLocationManager = locationManager;

        mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        mLocationManager.addTestProvider
                (
                        LocationManager.GPS_PROVIDER,
                        "requiresNetwork" == "",
                        "requiresSatellite" == "",
                        "requiresCell" == "",
                        "hasMonetaryCost" == "",
                        "supportsAltitude" == "",
                        "supportsSpeed" == "",
                        "supportsBearing" == "",

                        android.location.Criteria.POWER_LOW,
                        android.location.Criteria.ACCURACY_FINE
                );

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        String str = "48.7;2.69";
        String[] MockLoc = str.split(";");
        Location location = new Location(LocationManager.GPS_PROVIDER);
        Double lat = Double.valueOf(MockLoc[0]);
        location.setLatitude(lat);
        Double longi = Double.valueOf(MockLoc[1]);

        newLocation.setLatitude (lat);
        newLocation.setLongitude(longi);

        newLocation.setAccuracy(500);

        mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE,
                        null, System.currentTimeMillis() );

        mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);

        return true;
    }

    public boolean fakeGpsTest() {
        // Set location by setting the latitude, longitude and may be the altitude...
        String str = "48.7;2.69";
        String[] MockLoc = str.split(";");
        Location location = new Location(LocationManager.GPS_PROVIDER);
        Double lat = Double.valueOf(MockLoc[0]);
        location.setLatitude(lat);
        Double longi = Double.valueOf(MockLoc[1]);
        try {
            location.setLongitude(longi);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
                            /*Double alti = Double.valueOf(MockLoc[2]);
                            try {
                                location.setAltitude(alti);
                            }catch (Exception ex){
                                System.out.println(ex.getMessage());
                            }*/

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            System.out.println("GPS should be enabled.");
            return false;
        }

        System.out.println("Trying to get last known locations:");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGps != null) {
            System.out.println("GPSProv:" + locationGps.getLatitude() + ";" + locationGps.getLongitude());
            System.out.println("GPSProv:Accuracy=" + locationGps.getAccuracy());
            if (currentLocation == null) {
                    /*currentLocation = new Location(LocationManager.GPS_PROVIDER);
                    currentLocation.setLatitude(locationGps.getLatitude());
                    currentLocation.setLongitude(locationGps.getLongitude());*/
            }
        }

        return true;
    }


}



