package ph.locko.locky.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ph.locko.locky.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by joeyramirez on 2/17/2016.
 */
public class AboutDeveloperActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setFonts();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*Reload everything from above*/
        NavUtils.navigateUpFromSameTask(this);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setFonts() {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(MainActivity.FONT_NAME)
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }

    public void buttonHandler(View view) {

        int id = view.getId();

        if (id == R.id.button_email) {
            sendMail();
        } else if (id == R.id.button_linkedIn) {
            linkedInMe();
        }else if(id == R.id.button_apps) {
            goToApps();
        }


    }

    private void goToApps() {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=levrsoft&c=apps"));
        startActivity(Intent.createChooser(intent, "View page using..."));

    }

    private void sendMail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "joeyramirez1000@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hola!");
        startActivity(Intent.createChooser(emailIntent, "Send feedback using..."));


    }

    private void linkedInMe() {


        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ph.linkedin.com/in/ramirezjoey"));
        startActivity(Intent.createChooser(intent, "View page using..."));

    }

}
