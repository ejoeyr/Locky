package ph.locko.locky.app;

import android.app.Application;
import android.util.Log;

import java.io.File;

/**
 * Created by joeyramirez on 2/20/2016.
 */
public class LockyApp extends Application {
    private static LockyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static LockyApp getInstance() {
        return instance;
    }


}
