package ph.locko.locky.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

import ph.locko.locky.R;
import ph.locko.locky.receiver.UserActiveReceiver;
import ph.locko.locky.receiver.MyAdminReceiver;

public class NotificationService extends Service /*implements TimerStopperInterface*/ {

    /*Use only one notification id*/
    private final static int NOTIFICATION_ID = 1;
    /*Our time format hours minutes and second*/
    public final String TIME_FORMAT = "%02d:%02d:%02d";
    private SharedPreferences.Editor sharedPreferencesEditor;
    /*Static so other class can use this*/
    private static NotificationManager notificationManager;
    private static CountDownTimer timer;

    /*Constructor*/
    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*Always called when starting a service*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*Get the length of how long the lock will be*/
        long wait = intent.getLongExtra(getString(R.string.EXTRA_WAIT), 60000);
        /*SharedPreference*/
        sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        /*Get the notification service*/
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        /*Notification Builder reuse*/
        final NotificationCompat.Builder builders = new NotificationCompat.Builder(NotificationService.this)
                .setContentTitle(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_notification_message),"FOCUS"))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Started Locky")
                .setOngoing(true)
                .setContentText("Started");
        /*Our timer*/
        timer = new CountDownTimer(wait, 1000) {
            @Override
            public void onTick(long timeLeft) {

                String hms = String.format(TIME_FORMAT,
                        TimeUnit.MILLISECONDS.toHours(timeLeft),
                        TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                        TimeUnit.MILLISECONDS.toSeconds(timeLeft) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));

                /*SET A NEW CONTENT TEXT*/
                builders.setContentText(hms);
                /*UPDATE OUR SHARED PREFERENCE*/
                /*COMMIT OR APPLY*/
                sharedPreferencesEditor.putLong(getString(R.string.EXTRA_WAIT_KEY), timeLeft).apply();
                /*NOTIFY*/
                notificationManager.notify(NOTIFICATION_ID, builders.build());

            }

            @Override
            public void onFinish() {

                /*UPDATE THE INSTRUCTION TO STOP*/
                PreferenceManager.getDefaultSharedPreferences(NotificationService.this).edit().putString(getString(R.string.pref_instruction), getString(R.string.pref_instruction_stop)).apply();

                /*SET AUDIO STREAM TO NORMAL*/
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                /*NOTIFICATION SETUP*/
                builders.setContentText("YOU CAN NOW USE YOUR PHONE!")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setOngoing(false)
                        .setContentTitle("LOCKY TIMER FINISHED").setTicker("LOCKY NOTIFICATION!");

                /*NOTIFY*/
                notificationManager.notify(NOTIFICATION_ID, builders.build());

                /*Wake phone*/
                UserActiveReceiver.wake(NotificationService.this);

                /*Stop the service*/
                stopSelf();

            }
        };

        timer.start();


        return super.onStartCommand(intent, flags, startId);
    }

    /* @Override*/
    /*This method is called on the AdminReceiver if disabled */
    public static void stopTimer() {
        /*Check if timer is not equals null*/
        if (timer != null) {
            /*Remove the notification from the top and cancel the timer*/
            notificationManager.cancel(NOTIFICATION_ID);
            timer.cancel();
        }

    }
}
