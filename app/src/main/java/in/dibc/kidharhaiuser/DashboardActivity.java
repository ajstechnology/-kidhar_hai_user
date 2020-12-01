package in.dibc.kidharhaiuser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import in.dibc.kidharhaiuser.base.Base;
import in.dibc.kidharhaiuser.utils.APIService;
import in.dibc.kidharhaiuser.utils.ApiClient;
import in.dibc.kidharhaiuser.utils.Constants;

public class DashboardActivity extends AppCompatActivity implements Base {
    private APIService apiService;
    private FirebaseJobDispatcher jobDispatcher;

    private TextView tvSecret;

    // Temp purpose

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
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        SharedPreferences prefs = getSharedPreferences(Constants.LOGIN_PREFS, MODE_PRIVATE);
        String secretString = prefs.getString(Constants.SECRET, "");
        tvSecret.setText("Your secret key is : " + secretString);
        apiService = ApiClient.getAPIService();

//        callingServices();
        startLocationService();

//        updateLocation();

    }

    private void startLocationService() {
        Intent bgServiceIntent = new Intent(DashboardActivity.this, LocationBackground_serviceQ.class);
        startService(bgServiceIntent);
    }

    @Override
    public void setListeners() {

    }

    /*
     *   Getting the Location Updates
     * */
    @SuppressLint("DefaultLocale")
    private void updateLocation() {

    }

    public void callingServices() {
        // Constants.jobDispatcher.cancelAll();
        final Job.Builder builder = jobDispatcher.newJobBuilder()
                .setTag("LocationGet")
                .setRecurring(true)
                .setLifetime(true ? Lifetime.FOREVER : Lifetime.UNTIL_NEXT_BOOT)
                .setService(in.dibc.kidharhaiuser.LocationBackground_service.class)
                .setTrigger(Trigger.executionWindow(0, 90))
                .setReplaceCurrent(true)
                .setRetryStrategy(jobDispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL, 30, 3600));


        if (false) {
            builder.addConstraint(Constraint.DEVICE_CHARGING);
        }
        if (true) {
            builder.addConstraint(Constraint.ON_ANY_NETWORK);
        }
        if (false) {
            builder.addConstraint(Constraint.ON_UNMETERED_NETWORK);
        }

        //  Toast.makeText(getApplicationContext(),"Your Tracking successfully activated !!",Toast.LENGTH_LONG).show();
        Log.i("FJD.JobForm", "scheduling new job");
        jobDispatcher.mustSchedule(builder.build());
        //  counter_display=0;
        // mainss1();

    }
}