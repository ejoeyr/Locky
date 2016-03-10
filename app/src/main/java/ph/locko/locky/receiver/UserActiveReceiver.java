package ph.locko.locky.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ph.locko.locky.R;
import ph.locko.locky.service.NotificationService;

public class UserActiveReceiver extends BroadcastReceiver {
    DevicePolicyManager devicePolicyManager;
    private ComponentName mComponentName;

    public UserActiveReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(context, MyAdminReceiver.class);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String instruction = sharedPreferences.getString(context.getString(R.string.pref_instruction), context.getString(R.string.pref_instruction_stop));
        long wait = sharedPreferences.getLong(context.getString(R.string.EXTRA_WAIT_KEY), 60000);

        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) && instruction.equals(context.getString(R.string.pref_instruction_start))) {

            if (devicePolicyManager.isAdminActive(mComponentName)) {

                devicePolicyManager.lockNow();
             /*   wake(context);*/
                Toast.makeText(context, "A notification will be pushed when you've completed the time you've set :)", Toast.LENGTH_LONG).show();
            } else {

                /*Finished waiting!*/
            }
            /*Means user reboot his/her device while the Locky service is running so re-run it again*/
        }
        else if (intent.getAction().equals(context.getString(R.string.string_boot_completed)) && instruction.equals(context.getString(R.string.pref_instruction_start))) {
            Intent extra = new Intent(context, NotificationService.class);
            extra.putExtra(context.getString(R.string.EXTRA_WAIT), wait);
            context.startService(extra);
            devicePolicyManager.lockNow();

        }
    }

    public static void wake(Context context) {


        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = pm.isScreenOn();


        if (isScreenOn == false) {

            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");

            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");

            wl_cpu.acquire(10000);

            wl_cpu.release();
        }


    }
}
