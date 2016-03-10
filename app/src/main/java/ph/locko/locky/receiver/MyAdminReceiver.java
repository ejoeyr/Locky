package ph.locko.locky.receiver;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ph.locko.locky.R;
import ph.locko.locky.fragments.OptionsFragment;
import ph.locko.locky.service.NotificationService;

public class MyAdminReceiver extends DeviceAdminReceiver {
    private AlarmManager alarmManager;
    private Context mContext;
    private SharedPreferences.Editor sharedPreferences;

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mContext = context;
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context).edit();
        /*STOP INSTRUCTION!*/
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.pref_instruction), context.getString(R.string.pref_instruction_stop)).apply();
        cancelAlarmNightLock(context);

        if (isMyServiceRunning(NotificationService.class, context)) {
            /*Stop timer and service*/
            NotificationService.stopTimer();
            context.stopService(new Intent(context, NotificationService.class));
            Toast.makeText(context, "Locky service stopped", Toast.LENGTH_SHORT).show();

        }

    }

    /*Cancels the pending night autolock alarm*/
    private void cancelAlarmNightLock(Context context) {
        /*Set the list pref to no thanks*/
        sharedPreferences.putString(context.getString(R.string.pref_list_hour_night), context.getString(R.string.default_value)).apply();
        sharedPreferences.putString(context.getString(R.string.pref_list_hour_day), context.getString(R.string.default_value)).apply();

        cancelIntentWithRequestCodeOf(OptionsFragment.NIGHT_REQUEST);
        cancelIntentWithRequestCodeOf(OptionsFragment.DAY_REQUEST);

    }

    private void cancelIntentWithRequestCodeOf(int requestCode) {
        /*Both request has the intention to start the daynightlock receiver so it's okto declare it */
        Intent intentNightLock = new Intent(mContext, DayNightAutoLockReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intentNightLock, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    /*HElper method to check if my service is running from <3 stackOverflow*/
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


}
