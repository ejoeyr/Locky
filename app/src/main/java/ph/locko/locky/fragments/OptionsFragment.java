package ph.locko.locky.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Calendar;

import ph.locko.locky.R;
import ph.locko.locky.receiver.DayNightAutoLockReceiver;

/*
*
 * Created by joeyramirez on 2/21/2016.
*/
public class OptionsFragment extends PreferenceFragment {
    /*static Request codes this
    * is also accessed on other classes ctrl+b to see usage*/
    public static final int NIGHT_REQUEST = 1;
    public static final int DAY_REQUEST = 2;
    /*AlarmManager*/
    private AlarmManager alarmManager;
    /*PendingIntents*/
    private PendingIntent pendingIntent_nightTimeLock;
    private PendingIntent pendingIntent_dayTimeLock;
    /*Preferences*/
    private PreferenceCategory preferenceCategory_nightAutoLock;
    private ListPreference listPreference_night;
    private ListPreference listPreference_day;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        addPreferencesFromResource(R.xml.options_layout);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        /*NIGHT INTENT*/
        Intent intent_nightTimeLock = new Intent(getActivity(), DayNightAutoLockReceiver.class);
        intent_nightTimeLock.putExtra(getString(R.string.string_request), NIGHT_REQUEST);
        pendingIntent_nightTimeLock = PendingIntent.getBroadcast(getActivity(), NIGHT_REQUEST, intent_nightTimeLock, PendingIntent.FLAG_UPDATE_CURRENT);


        /*DAY INTENT*/
        Intent intent_dayTimeLock = new Intent(getActivity(), DayNightAutoLockReceiver.class);
        intent_dayTimeLock.putExtra(getString(R.string.string_request), DAY_REQUEST);
        pendingIntent_dayTimeLock = PendingIntent.getBroadcast(getActivity(), DAY_REQUEST, intent_dayTimeLock, PendingIntent.FLAG_UPDATE_CURRENT);

        /*category*/
        preferenceCategory_nightAutoLock = (PreferenceCategory) findPreference(getString(R.string.pref_night_category));

        /*List prefs*/
        listPreference_night = (ListPreference) findPreference(getString(R.string.pref_list_hour_night));
        listPreference_day = (ListPreference) findPreference(getString(R.string.pref_list_hour_day));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*If this returns null it means admin mode is not on*/
        if (DayNightAutoLockReceiver.getDevicePolicyManager(getActivity()) != null) {
            enableCategories(true);
        } else {
            enableCategories(false);
        }
    }

    private void enableCategories(boolean value) {

        preferenceCategory_nightAutoLock.setEnabled(value);

    }

    @Override
    public void onStop() {
        super.onStop();
        /*Setup the alarms onStop of this fragment*/
        setUpTime(Integer.parseInt(listPreference_night.getValue()), pendingIntent_nightTimeLock);
        setUpTime(Integer.parseInt(listPreference_day.getValue()), pendingIntent_dayTimeLock);
    }

    /*Function to set up the alarm*/
    private void setUpTime(int value, PendingIntent pendingIntent) {
        Calendar calendar = Calendar.getInstance();
        if (value == 1) {
            alarmManager.cancel(pendingIntent);
        } else {
      
        /*Set the hour for the calendar*/
            calendar.set(Calendar.HOUR_OF_DAY, value);
        /*If the time is before then next day*/
            if (calendar.before(calendar)) {
                calendar.add(Calendar.DATE, 1);
            }

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

        }


  /*      Cancel if the value is equals to 1*/


    }


}
