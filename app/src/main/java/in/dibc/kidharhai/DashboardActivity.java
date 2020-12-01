package in.dibc.kidharhai;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;

import in.dibc.kidharhai.base.Base;
import in.dibc.kidharhai.utils.APIService;
import in.dibc.kidharhai.utils.ApiClient;
import in.dibc.kidharhai.utils.Constants;

public class DashboardActivity extends AppCompatActivity implements Base {
    private APIService apiService;
    private FirebaseJobDispatcher jobDispatcher;

    private TextView tvSecret;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadElements();
        initValues();
        setListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }


    @Override
    public void loadElements() {
        tvSecret = findViewById(R.id.secretText);
    }

    @Override
    public void initValues() {
        SharedPreferences prefs = getSharedPreferences(Constants.LOGIN_PREFS, MODE_PRIVATE);
        String secretString = prefs.getString(Constants.SECRET, "");
        tvSecret.setText("Your secret key is : " + secretString);
        apiService = ApiClient.getAPIService();
//        startLocationService();

//        updateLocation();

    }

    private void startLocationService() {

    }

    @Override
    public void setListeners() {

    }

    /*
     *   Getting the Location Updates
     * */
    @SuppressLint("DefaultLocale")
    private void updateLocation() {

//        Intent locIntentServ = new Intent(this, LocationBackground_service.class);
//        startService(locIntentServ);
//        LocationBackground_service locationBgService = new LocationBackground_service(DashboardActivity.this);
//
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        handler.postDelayed(() -> {
//            TimerTask timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    if (locationBgService.isCanGetLocation()) {
//                        Constants.s_fb_lat = locationBgService.getLatitude();
//                        Constants.s_fb_lng = locationBgService.getLongitude();
//                    }
//                }
//            };
//
//            Timer timer = new Timer();
//            timer.schedule(timerTask, 1000, 10000);
//
//        }, 1000);
    }
}