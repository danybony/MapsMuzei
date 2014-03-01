package net.bonysoft.mapsmuzei;

import android.app.Application;
import android.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

/**
 * The base Application class. Needed to initialize the location library
 * Created by Daniele Bonaldo on 2/25/14.
 */
public class MapsMuzeiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            LocationLibrary.initialiseLibrary(getBaseContext(), "net.bonysoft.mapsmuzei");
        }
        catch (UnsupportedOperationException e) {
            Log.e(MapsMuzeiApplication.class.getSimpleName(), "No location providers available", e);
        }
    }
}
