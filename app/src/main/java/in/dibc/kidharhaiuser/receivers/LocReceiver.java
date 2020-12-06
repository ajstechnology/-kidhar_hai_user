package in.dibc.kidharhaiuser.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import in.dibc.kidharhaiuser.LocationBackground_serviceQ;

public class LocReceiver extends BroadcastReceiver {
    private static final String TAG = "LocReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: receive req to start service");
        context.startService(new Intent(context.getApplicationContext(), LocationBackground_serviceQ.class));
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
    }
}
