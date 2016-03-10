package ph.locko.locky.activity;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import ph.locko.locky.R;
import ph.locko.locky.receiver.MyAdminReceiver;
import ph.locko.locky.service.NotificationService;
import ph.locko.locky.util.IabHelper;
import ph.locko.locky.util.IabResult;
import ph.locko.locky.util.Inventory;
import ph.locko.locky.util.Purchase;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    /*Request code*/
    private static final int ADMIN_INTENT = 1;
    /*Donate sku*/
    /*In-App Billing helper*/
    private IabHelper mHelper;
    private static final String DONATE_SKU = "donate_locky";
    private static final String TAG = "BILLING-SERVICES";
    /*For the lock button*/
    private Button button_lock;
    /*Admin button*/
    private Button button_enableAdmin;
    /*Dev manager*/
    private DevicePolicyManager mDevicePolicyManager;
    /*Invoke what component*/
    private ComponentName mComponentName;
    /*Any text for our description*/
    private String description = "Lock your phone and focus :)";
    /*Font name*/
    public static final String FONT_NAME = "fonts/primer.ttf";
    /*Choose between second minute or hour*/
    private Spinner spinner_time;
    /*Handle time input*/
    private EditText editText_time;
    /*Other options*/
    private CheckBox checkBox_wifi;
    private CheckBox checkBox_silent;
    /*Check if the device has lock on it*/
   /* private boolean lock;*/
    /*Colors*/
    private int RED_COLOR = android.R.color.holo_red_light;
    private int GREEN_COLOR = android.R.color.holo_green_light;


    /*From Google*/
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                return;
            } else if (purchase.getSku().equals(DONATE_SKU)) {

                mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        if (result.isSuccess()) {

                            sayThanks(true);
                        }
                    }
                });
            } else {

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFonts();
        setUpBilling();
        setContentView(R.layout.activity_main);
        /*Initialization Phase*/
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdminReceiver.class);
        button_enableAdmin = (Button) findViewById(R.id.button_enableAdmin);
        button_lock = (Button) findViewById(R.id.button_lock);
        editText_time = (EditText) findViewById(R.id.editText_time);
        spinner_time = (Spinner) findViewById(R.id.spinner_time);
        checkBox_silent = (CheckBox) findViewById(R.id.checkBox_silent);
        checkBox_wifi = (CheckBox) findViewById(R.id.checkBox_wifi);
        spinner_time.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"second/s", "minute/s", "hour/s"}));
      /*  checkAdminActive();*/


    }


    private void setUpBilling() {

        mHelper = new IabHelper(this, getString(R.string.google_billing_key));
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpBilling();
        checkAdminActive();
        setChecked();

        /*hasLock();*/

    }

    private void setChecked() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        checkBox_wifi.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_night_lock_wifi), false));
        checkBox_silent.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_night_lock_silent), false));


    }

    public void checkAdminActive() {
        if (mDevicePolicyManager.isAdminActive(mComponentName)) {
            configAdminActive(getResources().getDrawable(R.drawable.ic_action_lock_outline),
                              true,getString(R.string.string_disable_admin),RED_COLOR);
        } else {
            configAdminActive(null, false, getString(R.string.string_enable_admin), GREEN_COLOR);
        }
    }

    private void configAdminActive(Drawable o, boolean b, String string, int color) {
        configureButtonLockDrawableAndText(o);
        enableLockButton(b);
        setAdminButtonText(string);
        setAdminButtonBackgroundColor(color);

    }


    private void configureButtonLockDrawableAndText(Drawable drawable) {

        button_lock.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);

    }


    private void setAdminButtonBackgroundColor(int color) {

        button_enableAdmin.setBackgroundColor(getResources().getColor(color));

    }

    private void setFonts() {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(FONT_NAME)
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void buttonHandler(View view) {

        if (view == button_enableAdmin) {
            String button_text = button_enableAdmin.getText().toString();
            if (button_text.equals(getString(R.string.string_enable_admin))) {

                requestAdminPrivilege();

            } else if (button_text.equals(getString(R.string.string_disable_admin))) {
                configureButtonLockDrawableAndText(null);
                disableAdminMode();
                setAdminButtonText(getString(R.string.string_enable_admin));
                enableLockButton(false);
                setAdminButtonBackgroundColor(android.R.color.holo_green_light);

            }


        } else if (view == button_lock) {
                /*If there is no unlock screen of any type show this dialog*/
          /*  if (!lock) {*/
            if (!true) {
                new AlertDialog.Builder(this, R.style.AlertDialogStyle).setTitle(getString(R.string.string_no_unlock_screen_set))
                        .setMessage(getString(R.string.string_no_unlock_screen))
                        .setPositiveButton(getString(R.string.string_add_lock_screen), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.string_lock_screen), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stillContinue();
                            }
                        })
                        .setNeutralButton(getString(R.string.string_cancel), null)
                        .create()
                        .show();
            } else {
                /*user has unlock pattern so continue now!*/
                stillContinue();
            }

        }
    }


    private void stillContinue() {


        if (!editText_time.getText().toString().isEmpty()) {

            String time = editText_time.getText().toString();
            String timeType = spinner_time.getSelectedItem().toString();
                    /*If the input is less than orr equal to zero show error*/
            if (Integer.parseInt(time) <= 0) {
                editText_time.setError(getString(R.string.string_desired_time));

            } else {

                timeType = getTimeType(timeType, time);

                if (mDevicePolicyManager.isAdminActive(mComponentName)) {
                    /*SHOW WARNING */
                    showWarning(time, timeType);
                }
            }

        }
                /*If it is empty show error*/
        else {
            editText_time.setError(getString(R.string.string_desired_time));
        }
    }

    private String getTimeType(String timeType, String time) {

        int lastIndexOfSlash = timeType.lastIndexOf("/");
        if (Integer.parseInt(time) > 1) {

            timeType = timeType.substring(0, lastIndexOfSlash) + "s";
        } else {
            timeType = timeType.substring(0, lastIndexOfSlash);
        }

        return timeType;
    }

    private void showWarning(final String time, final String timeType) {
        String htmlTime = String.format("%s %s", time, timeType);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_show_warning), true)) {
            new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle(getString(R.string.string_warning))
                    .setIcon(getResources().getDrawable(R.drawable.ic_alert_warning))
                    .setMessage(Html.fromHtml("This app will block all device usage attempts for <font color=\"#e62e00\">" + htmlTime + "</font> which is the time you've set. <br><br>You will receive a notification once the time has finished<br><br>Do you still want to continue?"))
                    .setNegativeButton(getString(R.string.string_cancel), null)
                    .setPositiveButton(getString(R.string.string_continue), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            startLock(time, timeType);
                        }
                    }).create()
                    .show();
        } else {

            startLock(time, timeType);

        }
    }

    private void startLock(String time, String timeType) {


                        /*Get millis value of the time*/
        long waitValue = convertThisTime(time, timeType);
                        /*User pressed continue then apply the preference to start mode */


                        /*Configure the preferences*/
        configureInstructionPreference(getString(R.string.pref_instruction), getString(R.string.pref_instruction_start));
        configureWaitPreference(getString(R.string.EXTRA_WAIT_KEY), waitValue);


                        /*Configure if wifi or silent mode checkboxes are checked*/
        configureOtherOptions();

                               /*Start the service*/
        Intent intent = new Intent(MainActivity.this, NotificationService.class);
        intent.putExtra(getString(R.string.EXTRA_WAIT), waitValue);
        startService(intent);
        mDevicePolicyManager.lockNow();
        finish();


    }


    private void configureWaitPreference(String key, long waitValue) {

        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putLong(key, waitValue).apply();

    }

    private void configureInstructionPreference(String instruction, String value) {
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString(instruction, value).apply();
    }

    private void configureOtherOptions() {
        if (checkBox_wifi.isChecked()) {
            turnOffWifi();
        }
        if (checkBox_silent.isChecked()) {
            silentModeOn();
        }


    }


    private void turnOffWifi() {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

    }

    private void silentModeOn() {
        AudioManager am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public long convertThisTime(String time, String timeType) {
        int raw = Integer.parseInt(time);
        long returnValue = 0;
        if (timeType.contains(getString(R.string.string_sec))) {

            returnValue = raw * 1000;

        } else if (timeType.contains(getString(R.string.string_min))) {

            returnValue = raw * 1000 * 60;


        } else if (timeType.contains(getString(R.string.string_hour))) {

            returnValue = raw * 1000 * 60 * 60;


        }

        return returnValue;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADMIN_INTENT && resultCode == RESULT_OK) {
            enableLockButton(true);
            setAdminButtonText(getString(R.string.string_disable_admin));
        }
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*Inte*/
    private void requestAdminPrivilege() {

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, description);
        startActivityForResult(intent, ADMIN_INTENT);
    }

    /*Field helper methods*/
    public void disableAdminMode() {

        mDevicePolicyManager.removeActiveAdmin(mComponentName);
    }

    public void enableLockButton(boolean enable) {
        button_lock.setEnabled(enable);
    }


    public void setAdminButtonText(String text) {

        button_enableAdmin.setText(text);
    }

    public void buttonExtraHandler(View view) {

        int id = view.getId();

        if (id == R.id.buttonExtra_about) {

            about();

        } else if (id == R.id.buttonExtra_donate) {

            donate();

        } else if (id == R.id.buttonExtra_share) {

            share();
        } else if (id == R.id.buttonExtra_feedback) {

            feedback();
        } else if (id == R.id.buttonExtra_rate) {

            rate();

        } else if (id == R.id.buttonExtra_options) {

            startActivity(new Intent(this, OptionsActivity.class));

        }


    }

    /*Invoked when share button is tapped*/
    private void share() {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Focus and avoid the distractions! #LockyApp Link: https://play.google.com/store/apps/details?id=" + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Where to share?"));
    }

    /*Invoked when feedback button is tapped*/
    private void feedback() {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "joeyramirez1000@yahoo.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Locky app");
        startActivity(Intent.createChooser(emailIntent, "Send feedback using..."));

    }

    /*Invoked when rate button is tapped*/
    private void rate() {

        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }


    private void about() {


        startActivity(new Intent(this, AboutDeveloperActivity.class));


    }

    /*invoked when donate button is tapped*/
    private boolean donate() {
        if (alreadyPurchased()) {

            sayThanks(true);

        } else {

            new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle("Donate & Support")
                    .setMessage("If you find this app helpful, you can donate a penny  for continued development and improvement of this software")
                    .setPositiveButton("DONATE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            continueDonation();

                        }
                    }).setNegativeButton("NO WAY!", null)
                    .create()
                    .show();


        }


        return true;
    }

    /*Continue donation method*/
    private void continueDonation() {

        if (!mHelper.isAsyncOperation()) {


            mHelper.launchPurchaseFlow(this, DONATE_SKU, 10001,
                    mPurchaseFinishedListener);
        } else {

            mHelper.flagEndAsync();


            mHelper.launchPurchaseFlow(this, DONATE_SKU, 10001,
                    mPurchaseFinishedListener);
        }

    }

    boolean hasDonated = false;

    /*Check if the user has already donated*/
    private boolean alreadyPurchased() {
        if (mHelper != null) {
            mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    if (result.isSuccess()) {
                        hasDonated = inv.hasPurchase(DONATE_SKU);
                    } else {
                        Toast.makeText(MainActivity.this, "Failed getting data", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


        return hasDonated;
    }

    private void sayThanks(boolean b) {
        String message;

        if (b) {
            message = getString(R.string.string_thank_you_already);
        } else {
            message = getString(R.string.string_thank_you);
        }
        new android.app.AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.string_thanks_for_donation)
                .setMessage(message)
                .setPositiveButton("No problem!", null)
                .create()
                .show();
    }

/*    public boolean hasLock() {

        lock = LockTypeUtil.checkLock(this);

        return lock;
    }*/

}
