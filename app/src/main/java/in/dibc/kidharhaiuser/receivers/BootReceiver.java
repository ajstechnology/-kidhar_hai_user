package in.dibc.kidharhaiuser.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.dibc.kidharhaiuser.LocationBackground_serviceQ;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, LocationBackground_serviceQ.class));
        }
    }
}
