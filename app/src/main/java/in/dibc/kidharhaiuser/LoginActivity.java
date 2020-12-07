package in.dibc.kidharhaiuser;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import in.dibc.kidharhaiuser.base.Base;
import in.dibc.kidharhaiuser.models.MOTPRes;
import in.dibc.kidharhaiuser.models.MResVerify;
import in.dibc.kidharhaiuser.utils.APIService;
import in.dibc.kidharhaiuser.utils.ApiClient;
import in.dibc.kidharhaiuser.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Base {

    private TextInputLayout tilPhone;
    private TextInputLayout tilOTP;

    private TextInputEditText tietPhone;
    private TextInputEditText tietOTP;

    private ImageView imgHeader;

    private boolean gotOTP;

    private APIService apiService;

    private FirebaseJobDispatcher jobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadElements();
        initValues();
        setListeners();

    }

    @Override
    public void loadElements() {
        tilPhone = findViewById(R.id.act_login_til_phone);
        tilOTP = findViewById(R.id.act_login_til_otp);

        tietPhone = findViewById(R.id.act_login_tiet_phone);
        tietOTP = findViewById(R.id.act_login_tiet_otp);

        imgHeader = findViewById(R.id.act_login_header);
    }

    @Override
    public void initValues() {
        Constants.createLogData("Login screen started...");
        Glide.with(LoginActivity.this).load(R.drawable.logo).into(imgHeader);

        gotOTP = false;
        apiService = ApiClient.getAPIService();
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void setListeners() {

    }

    public void callWeb(View view) {
        String mobile = Objects.requireNonNull(tietPhone.getText()).toString();
        String otp = Objects.requireNonNull(tietOTP.getText()).toString();

        if (gotOTP) {
            // verify otp and go to the dashboard
            if (mobile.isEmpty() && otp.isEmpty()) {
                showDlg("Invalid input");
            } else {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("otp", otp);

                apiService.verifyOTP(params).enqueue(new Callback<MResVerify>() {
                    @Override
                    public void onResponse(@NonNull Call<MResVerify> call, @NonNull Response<MResVerify> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MResVerify mResVerify = response.body();
                            SharedPreferences prefs = getSharedPreferences(Constants.LOGIN_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString(Constants.AUTH_KEY, mResVerify.getAuthkey());
                            editor.putString(Constants.CLIENT_ID, mResVerify.getClientid());
                            editor.putString(Constants.SECRET, mResVerify.getSecret());

                            editor.apply();
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(getResources().getString(R.string.app_name))
                                    .setMessage(mResVerify.getMsg())
                                    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            callingServices();
                                            startLocationService();
                                            startActivity(new Intent(LoginActivity.this, in.dibc.kidharhaiuser.DashboardActivity.class));
                                            finish();
                                        }
                                    }).create().show();
                        } else {
                            showDlg("Invalid OTP");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MResVerify> call, @NonNull Throwable t) {

                    }
                });
            }
        } else {
            // get the otp

            if (!mobile.isEmpty()) {
                // call web
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                apiService.getOTP(params).enqueue(new Callback<MOTPRes>() {
                    @Override
                    public void onResponse(@NonNull Call<MOTPRes> call, @NonNull Response<MOTPRes> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            gotOTP = true;

                            tilOTP.setVisibility(View.VISIBLE);

                            String msg = response.body().getMsg();
                            showDlg(msg);
                        } else {
                            showDlg("Failed to send OTP");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MOTPRes> call, @NonNull Throwable t) {
                        showDlg(getResources().getString(R.string.app_name));
                    }
                });
            } else {
                showDlg(getResources().getString(R.string.enter_ten_num));
            }
        }
    }

    private void startLocationService() {
        Intent bgServiceIntent = new Intent(LoginActivity.this, LocationBackground_serviceQ.class);
        startService(bgServiceIntent);
    }


    private void showDlg(String msg) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(LoginActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}