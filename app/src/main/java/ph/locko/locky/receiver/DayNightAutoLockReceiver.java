package ph.locko.locky.receiver;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import ph.locko.locky.R;
import ph.locko.locky.fragments.OptionsFragment;
import ph.locko.locky.service.NotificationService;

public class DayNightAutoLockReceiver extends BroadcastReceiver {

    private Context context;
    private int intentType;
    private DevicePolicyManager devicePolicyManager;
    private int FORMULA = 1000 * 60 * 60;

    public DayNightAutoLockReceiver() {


    }

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        intentType = intent.getIntExtra(
                context.getString(R.string.string_request),
                OptionsFragment.NIGHT_REQUEST);

        devicePolicyManager = getDevicePolicyManager(context);

        /*If this app is  admin mode*/
        if (devicePolicyManager!=null) {

            configureOtherOptions();
            /*if device*/
            if (MyAdminReceiver.isMyServiceRunning(NotificationService.class, context))
            {
                context.stopService(new Intent(context, NotificationService.class));
                NotificationService.stopTimer();
                startRunningService(context);

            } else
            {

                startRunningService(context);
            }


        }
        /*This can't happen but let's just put this here
        * for insurance*/
        else
        {
            notifyUserForError();
        }


    }

    private void notifyUserForError() {


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder noti = new NotificationCompat.Builder(context)
                .setTicker("LOCKY CAN\'T LOCK IT")
                .setContentTitle("NIGHT LOCK CAN\'T START")
                .setContentText("ADMIN MODE IS DISABLED").setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(2, noti.build());

    }

    private void configureOtherOptions() {
        /*get the def shared preference*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        /*Both have false initial pref value */
        boolean wifi = sharedPreferences.getBoolean(context.getString(R.string.pref_night_lock_wifi), false);
        boolean silent = sharedPreferences.getBoolean(context.getString(R.string.pref_night_lock_silent), false);
        setManagers(wifi, silent);
    }

    private void setManagers(boolean wifi,boolean silent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(wifi);

        if(silent)
        {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }

    }
    private void startRunningService(Context context) {
        /*Start mode*/
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.pref_instruction), context.getString(R.string.pref_instruction_start)).apply();
        /*Avoid nulls*/
        long waitTime = 60000;

        if (intentType == OptionsFragment.NIGHT_REQUEST) {
            waitTime = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_night_lock_time), 8) * FORMULA;
        } else {
            waitTime = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_day_lock_time), 2) * FORMULA;
        }

        Intent intentNightLock = new Intent(context, NotificationService.class);
        intentNightLock.putExtra(context.getString(R.string.EXTRA_WAIT), waitTime);
        context.startService(intentNightLock);
        devicePolicyManager.lockNow();
    }

    public static DevicePolicyManager getDevicePolicyManager(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, MyAdminReceiver.class);

        if (devicePolicyManager.isAdminActive(componentName)) {
            return devicePolicyManager;
        } else {
            devicePolicyManager = null;
            return devicePolicyManager;

        }

    }
}
