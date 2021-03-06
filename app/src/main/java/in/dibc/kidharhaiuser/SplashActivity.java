package in.dibc.kidharhaiuser;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import in.dibc.kidharhaiuser.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private ImageView splashImage;

    private static final int REQ_PERMISSION = 11;
    private static final int REQ_PERMISSION_Q = 12;

    private String[] locationPermission = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private String[] locationPermissionQ = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Constants.createLogData("SplashActivity started");

        splashImage = findViewById(R.id.splash_image);
        Glide.with(SplashActivity.this).load(R.drawable.logo).into(splashImage);


        if(Constants.isConnectedWithNetwork(this)) {
            Constants.createLogData("Internet connected");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (Constants.checkLocationPermission(this) && Constants.checkSDWritePermission(this)) {
                    Constants.createLogData("SplashActivity redirection on version code >= M && <=Q");
                    redirect(3000);
                } else {
                    Constants.createLogData("SplashActivity request for version code >= M && <=Q");
                    ActivityCompat.requestPermissions(this, locationPermission, REQ_PERMISSION);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Constants.checkLocationPermissionQ(this) && Constants.checkSDWritePermission(this)) {
                    Constants.createLogData("SplashActivity redirection on version code >= Q");
                    redirect(3000);
                } else {
                    Constants.createLogData("SplashActivity request for version code >= Q");
                    ActivityCompat.requestPermissions(this, locationPermissionQ, REQ_PERMISSION_Q);
                }
            } else {
                Constants.createLogData("SplashActivity redirection on version code >= L");
                redirect(3000);
            }
        } else {
            Toast.makeText(this, "You're not connected", Toast.LENGTH_SHORT).show();
            Constants.createLogData("Internet not connected");
        }
    }

    private void redirect(long millis) {
        SharedPreferences prefs = getSharedPreferences(Constants.LOGIN_PREFS, MODE_PRIVATE);
        String authKey = prefs.getString(Constants.AUTH_KEY, null);
        String secret = prefs.getString(Constants.SECRET, null);

        Log.d(TAG, "redirect: authKey => " + authKey);
        Log.d(TAG, "redirect: secret => " + secret);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authKey == null || secret == null) {
                Intent loginIntent = new Intent(SplashActivity.this, in.dibc.kidharhaiuser.LoginActivity.class);
                startActivity(loginIntent);
            } else {
                Intent dashIntent = new Intent(SplashActivity.this, in.dibc.kidharhaiuser.DashboardActivity.class);
                startActivity(dashIntent);
            }
            finish();
        }, millis);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQ_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Constants.createLogData("SplashActivity permission granted [checked test]");
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    redirect(1000);
                } else {
                    Constants.createLogData("SplashActivity rationale permission dialog");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, locationPermission[0])) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Location Permission");
                        builder.setMessage("It is important to have location permission to function the app properly. Please allow it to access all features");
                        builder.setPositiveButton("GIVE PERMISSION", (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(this, locationPermission, 11);
                            Constants.createLogData("SplashActivity rationale permission revoked");
                        });
                        builder.setNegativeButton("DISMISS", (dialogInterface, i) -> {
                            Constants.createLogData("SplashActivity rationale permission dismissed");
                            finish();
                        });
                        builder.create().show();
                    }
                }
                break;
            case REQ_PERMISSION_Q:
                Constants.createLogData("SplashActivity req permission for Q support");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    redirect(1000);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, locationPermissionQ[0])) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Location Permission");
                        builder.setMessage("It is important to have location permission to function the app properly. Please allow it to access all features");
                        builder.setPositiveButton("GIVE PERMISSION", (dialogInterface, i) -> {
                            Constants.createLogData("SplashActivity rationale permission revoked");
                            ActivityCompat.requestPermissions(this, locationPermissionQ, 11);
                        });
                        builder.setNegativeButton("DISMISS", (dialogInterface, i) -> {
                            Constants.createLogData("SplashActivity rationale permission dismissed");
                            finish();
                        });
                        builder.create().show();
                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}