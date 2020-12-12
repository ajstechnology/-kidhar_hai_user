
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

package in.dibc.kidharhaiuser;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.firebase.jobdispatcher.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import in.dibc.kidharhaiuser.utils.Constants;

public class LocationBackground_serviceQ extends Service implements LocationListener {
    private static final String TAG = "LocationService.....";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    //    private GoogleApiClient googleApiClient;
    String latlngJson, json;
    String android_id;
    int b_level;
    String b_status;
    int deviceStatus;
    BroadcastReceiver batteryLevelReceiver;

    // Location classes
    private boolean isTracking;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    // Location Manager APIs
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private boolean canGetLocation = false;

    private Location location;

    private double latitude;
    private double longitude;

    private static final long MIN_DISTANCE = 1;    // 10 Meters distance
    private static final long MIN_TIME = 1000;      // 1 Sec interval

    private LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constants.createLogData("Location service started...");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                Constants.s_fb_lat = lastLocation.getLatitude();
                Constants.s_fb_lng = lastLocation.getLongitude();
                Constants.S_fb_accuracy = lastLocation.getAccuracy();
                location = locationResult.getLastLocation();
                sendLocationDataToWebsite();
            }
        };

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                location = getLocation();

                if (location != null) {
                    Constants.s_fb_lat = location.getLatitude();
                    Constants.s_fb_lng = location.getLongitude();
                    Constants.S_fb_accuracy = location.getAccuracy();
                    sendLocationDataToWebsite();
                } else {
                    fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper());
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0, 10000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }

        return START_STICKY;
    }

    private LocationRequest getLocationRequest() {
        int MAX_INTERVAL = 10000;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(MAX_INTERVAL);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    protected void sendLocationDataToWebsite() {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), String.format("Lat: %s\nLng: %s", Constants.s_fb_lat, Constants.s_fb_lng), Toast.LENGTH_SHORT).show());
        @SuppressLint("SimpleDateFormat") String logString = Constants.s_fb_lat +
                "," +
                Constants.s_fb_lng + "@accuracy: " + Constants.S_fb_accuracy +
                "@time: " +
                new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date());
        Constants.createLogData(logString);

        if (Constants.isConnectedWithNetwork(this)) {
            Constants.createLogData("Internet available");
            Constants.sendLocationData(
                    getApplicationContext(),
                    Constants.S_fb_accuracy,
                    Constants.s_fb_altitude,
                    String.valueOf(location.getAccuracy()),
                    "96.486",
                    Constants.s_fb_lat,
                    Constants.s_fb_lng,
                    String.valueOf(location.getSpeed())
            );
        } else {
            Constants.createLogData("Internet not available");
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
        Log.d(TAG, "onDestroy: service stopped");
        Constants.createLogData("Location service destroyed...");


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Works upto android 7
            // Restart the service itself after killed form Background
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), LocationBackground_serviceQ.class);
                startService(intent);
            }, 10000);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService() {
        String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID;
        String channelName = "LocService";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"MissingPermission", "DefaultLocale"})
    private Location getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            if (locationManager != null) {

                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
//                    Log.d("NetworkManager", "GPS and Network is disabled");
                } else {
                    this.canGetLocation = true;

                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME,
                                MIN_DISTANCE,
                                this,
                                Looper.getMainLooper()
                        );

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                this.latitude = location.getLatitude();
                                this.longitude = location.getLongitude();

                                Constants.s_fb_lat = latitude;
                                Constants.s_fb_lng = longitude;
                            }
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this, Looper.getMainLooper());

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(
                                            LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Constants.s_fb_lat = latitude;
                                Constants.s_fb_lng = longitude;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    public double getLatitude() {
        if (location != null) {
            this.latitude = location.getLatitude();
        }

        return this.latitude;
    }

    public double getLongitude() {
        if (location != null) {
            this.longitude = location.getLongitude();
        }

        return this.longitude;
    }


    public void stopLocationTracker() {
        if (locationManager != null) {
//            locationManager.removeUpdates(LocationBackground_service.this);
        }
    }

    public boolean isCanGetLocation() {
        return this.canGetLocation;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "onLocationChanged: updated");
            Log.d(TAG, "onLocationChanged: lat " + location.getLatitude());
            Log.d(TAG, "onLocationChanged: lng " + location.getLongitude());
            this.location = location;
//            Constants.sendLocationData(
//                    getApplicationContext(),
//                    Constants.S_fb_accuracy,
//                    Constants.s_fb_altitude,
//                    String.valueOf(location.getAccuracy()),
//                    "96.486",
//                    Constants.s_fb_lat,
//                    Constants.s_fb_lng,
//                    String.valueOf(location.getSpeed())
//            );
        }
    }
}
