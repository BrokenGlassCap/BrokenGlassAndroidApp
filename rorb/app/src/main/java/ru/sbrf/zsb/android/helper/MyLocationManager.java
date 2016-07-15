package ru.sbrf.zsb.android.helper;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyLocationManager {
    /** The minimum distance to GPS change Updates in meters **/
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES_FOR_GPS = 2; // 2
    // meters
    /** The minimum time between GPS updates in milliseconds **/
    private final long MIN_TIME_BW_UPDATES_OF_GPS = 1000 * 5 * 1; // 5
    // seconds

    /** The minimum distance to NETWORK change Updates in meters **/
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES_FOR_NETWORK = 5; // 5
    // meters
    /** The minimum time between NETWORK updates in milliseconds **/
    private final long MIN_TIME_BW_UPDATES_OF_NETWORK = 1000 * 10 * 1; // 10
    // seconds

    /**
     * Lets just say i don't trust the first location that the is found. This is
     * to avoid that
     **/

    private int NetworkLocationCount = 0, GPSLocationCount = 0;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    /**
     * Don't do anything if location is being updated by Network or by GPS
     */
    private boolean isLocationManagerBusy;
    private LocationManager locationManager;
    private Location currentLocation;
    private Context mContext;
    private OnLocationDetectectionListener mListener;

    public MyLocationManager(Context mContext,
                             OnLocationDetectectionListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    /**
     * Start the location manager to find my location
     */
    public void startLocating() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);

            checkProviders();


            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                showSettingsAlertDialog();
            } else {
                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES_OF_GPS,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES_FOR_GPS,
                            gpsLocationListener);

                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES_OF_NETWORK,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES_FOR_NETWORK,
                            networkLocationListener);

                }
            }
            /**
             * My 30 seconds plan to get myself a location
             */
            restartDetecting(10);

        } catch (Exception e) {
            Log.e("Error Fetching Location", e.getMessage());
            Toast.makeText(mContext,
                    "Error Fetching Location" + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void checkProviders() {
        // Getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void restartDetecting(int delay) {
        if (delay <= 0)
        {
            delay = 30;
        }
        ScheduledExecutorService se = Executors
                .newSingleThreadScheduledExecutor();
        se.schedule(new Runnable() {

            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                checkProviders();

                //if (currentLocation == null) {
                    if (isGPSEnabled) {

                        currentLocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } else if (isNetworkEnabled) {
                        currentLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }
                    if (currentLocation != null && mListener != null) {
                        locationManager.removeUpdates(gpsLocationListener);
                        locationManager
                                .removeUpdates(networkLocationListener);
                        mListener.onLocationDetected(currentLocation);
                    }
                    if (currentLocation == null)
                    {
                        mListener.onLocationDetected(currentLocation);
                    }
                //}
            }
        }, delay, TimeUnit.SECONDS);
    }


    /**
     * Handle GPS location listener callbacks
     */
    private LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (GPSLocationCount != 0 && !isLocationManagerBusy) {
                Log.d("GPS Enabled", "GPS Enabled");
                isLocationManagerBusy = true;
                currentLocation = location;
                locationManager.removeUpdates(gpsLocationListener);
                locationManager.removeUpdates(networkLocationListener);
                isLocationManagerBusy = false;
                if (currentLocation != null && mListener != null) {
                    mListener.onLocationDetected(currentLocation);
                }
            }
            GPSLocationCount++;
        }
    };
    /**
     * Handle Network location listener callbacks
     */
    private LocationListener networkLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(Location location) {
            if (NetworkLocationCount != 0 && !isLocationManagerBusy) {
                Log.d("Network", "Network");
                isLocationManagerBusy = true;
                currentLocation = location;
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(gpsLocationListener);
                locationManager.removeUpdates(networkLocationListener);
                isLocationManagerBusy = false;
                if (currentLocation != null && mListener != null) {
                    mListener.onLocationDetected(currentLocation);
                }
            }
            NetworkLocationCount++;
        }
    };

    /**
     * Function to show settings alert dialog. On pressing the Settings button
     * it will launch Settings Options.
     * */
    public void showSettingsAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Настройки GPS");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS не доступна. Открыть меню с настройками?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Настройки",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

}
