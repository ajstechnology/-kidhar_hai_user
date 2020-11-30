
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package in.dibc.kidharhai;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

import in.dibc.kidharhai.utils.Constants;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class LocationBackground_service extends JobService implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "DemoJobService.....";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    String latlngJson, json;
    String android_id;
    double fb_lat = 0.00;
    double fb_lng = 0.00;
    int b_level;
    String b_status;
    int deviceStatus;
    BroadcastReceiver batteryLevelReceiver;

    @Override
    public boolean onStartJob(JobParameters job) {

        if (Constants.check) {
            Log.e(TAG, "onStartJob called");
            Constants.check = false;
            android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            try {
                batteryLevel();
            } catch (Exception e) {
                Log.e(TAG, "error in getting batter Info");
            }

            if (!currentlyProcessingLocation) {
                currentlyProcessingLocation = true;
                startTracking();

            }

        }

        return false; // No more work to do

    }


    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        if (Constants.check_n == 1) {
            stopLocationUpdates();
        } else {
            Constants.s_fb_lat = location.getLatitude();    /*  get Latitude  in current  location*/
            Constants.s_fb_lng = location.getLongitude();   /* get longitude in current Location */
            Constants.S_fb_accuracy = location.getAccuracy();  /*get accurancy  in current Location*/
            Constants.s_fb_altitude = location.getAltitude();  /*get altitude in current location*/
            Constants.s_fb_speed = location.getSpeed();        /*get current speed */
            Constants.s_fb_time = location.getTime();          /*get current time*/
            Constants.s_rem = 1;

            sendLocationDataToWebsite();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "onConnected");
        registerRequestUpdate(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
        stopSelf();
    }

    public void registerRequestUpdate(final LocationListener listener) {

        int interval = 1000;  // 15 mins (millis)
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval); // every 10 second
//        locationRequest.setSmallestDisplacement(1.0f);
        locationRequest.setFastestInterval(10000); // the fastest rate in milliseconds at which your app can handle location updates

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            if (!isGoogleApiClientConnected()) {
                googleApiClient.connect();
            }
            registerRequestUpdate(listener);
        }


    }

    public boolean isGoogleApiClientConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    private void startTracking() {
        Log.e(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            googleApiClient = new Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }

        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }


    protected void sendLocationDataToWebsite() {
        fb_lat = Constants.s_fb_lat;
        fb_lng = Constants.s_fb_lng;
        Toast.makeText(getApplicationContext(), "Lat :" + fb_lat + "Lng" + fb_lng, Toast.LENGTH_LONG).show();
        Log.d(TAG, "sendLocationDataToWebsite: " + fb_lat);
        Log.d(TAG, "sendLocationDataToWebsite: " + fb_lng);

        @SuppressLint("SimpleDateFormat") String logString = Constants.s_fb_lat +
                "," +
                Constants.s_fb_lng +
                "@" +
                new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        Constants.createLogData(logString);

        Constants.sendLocationData(
                getApplicationContext(),
                Constants.S_fb_accuracy,
                Constants.s_fb_altitude,
                "96.486",
                "96.486",
                Constants.s_fb_lat,
                Constants.s_fb_lng,
                "23"
        );
        //postlocationapi();

        stopSelf();
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Constants.check_n = 0;
            googleApiClient.disconnect();
        }
    }

    private void batteryLevel() throws Exception {

        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                try {
                    unregisterReceiver(this);
                    int rawlevel = intent.getIntExtra("level", -1);
                    int scale = intent.getIntExtra("scale", -1);
                    b_level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        b_level = (rawlevel * 100) / scale;
                    }

                    deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                    if (deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                        b_status = "Charging";
                    } else if (deviceStatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                        b_status = "Discharging";
                    } else if (deviceStatus == BatteryManager.BATTERY_STATUS_FULL) {
                        b_status = " Battery Full";
                    } else if (deviceStatus == BatteryManager.BATTERY_STATUS_UNKNOWN) {

                        b_status = "Unknown";
                    } else if (deviceStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                        b_status = "Not Charging";


                    }

                } catch (Exception e) {

                }

                // textview.setText("Battery Status = "+b_status+"  "+b_level+" %");
                // Toast.makeText(getApplicationContext(),"Battery Level Remaining: " + b_level + "%",Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

    }

    @Override
    public void onDestroy() {

        try {
            unregisterReceiver(batteryLevelReceiver);

        } catch (Exception e) {

        }
        super.onDestroy();

    }

}
