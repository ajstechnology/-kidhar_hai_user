package in.dibc.kidharhai.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import in.dibc.kidharhai.models.MResLocation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Constants {
    private static final String TAG = "Constants";
    public static final String LOGIN_PREFS = "login";
    public static final String AUTH_KEY = "auth";
    public static final String CLIENT_ID = "id";
    public static final String SECRET = "secret";
    public static boolean check = true;
    public static double s_fb_lat = 0.00;
    public static double s_fb_lng = 0.00;
    public static double S_fb_accuracy = 0.00;
    public static double s_fb_altitude = 0.00;
    public static double s_fb_speed = 0.00;
    public static double s_fb_time = 0.00;


    public static int s_rem = 0;

    public static int check_n = 0;

    public static class KEYS {
        public static final String LOCATION = "location";
        public static final String LAT = "latitude";
        public static final String LNG = "longitude";
    }

    public static void sendLocationData(Context mContext, double strAccuracy, double strAltitude, String strAltitudeAccuracy, String strHeading, Double strLatitude, Double strLongitude, String strSpeed) {

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.LOGIN_PREFS, mContext.MODE_PRIVATE);
        String strAuthKey = prefs.getString(Constants.AUTH_KEY, "");
        String strClientId = prefs.getString(Constants.CLIENT_ID, "");


        ApiClient.getAPIService().sendLocationData(
                strAuthKey, strClientId, strAccuracy, strAltitude, strAltitudeAccuracy, strHeading, strLatitude, strLongitude, strSpeed, String.valueOf(System.currentTimeMillis())
        ).enqueue(new Callback<MResLocation>() {
            @Override
            public void onResponse(@NonNull Call<MResLocation> call, @NonNull Response<MResLocation> response) {
                if (response.isSuccessful() && response.body() != null) {
//                    showDlg(response.body().getMessage());
                    Log.d(TAG, "onResponse: " + response.body().getMessage());
                } else {
//                    showDlg("Failed to send data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<MResLocation> call, @NonNull Throwable t) {
//                showDlg("Something went wrong. Failed to send data");
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkLocationPermission(Activity activity){
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isConnectedWithNetwork(Context mContext){
        ConnectivityManager cm = (ConnectivityManager)  mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            return false;
        }
    }
}
