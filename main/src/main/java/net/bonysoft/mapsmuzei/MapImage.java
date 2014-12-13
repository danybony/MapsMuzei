package net.bonysoft.mapsmuzei;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapImage {

    private static final String TAG = MapImage.class.getSimpleName();

    private static final String GOOGLE_BASE_IMAGE_URL = "https://maps.googleapis.com/maps/api/staticmap?center=";
    private static final String GOOGLE_BASE_INTENT_URL = "https://www.google.it/maps/@";

    private static final String MAPBOX_BASE_IMAGE_URL = "http://api.tiles.mapbox.com/v4/";

    private final Context context;
    private final LocationInfo location;
    private final String token;
    private final int zoom;
    private final MapTheme style;
    private String title = "";
    private String description = "";

    public MapImage(Context context, int zoom, MapTheme style) {
        this.context = context;
        this.zoom = zoom;
        this.style = style;

        location = new LocationInfo(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "currentLocation = " + location.lastLat + "," + location.lastLong);
        if (!isValidLocation()) {
            createFakeLocation();
        }

        createImageTitleAndDescription();

        token = location.lastLat + "," + location.lastLong;
    }

    private boolean isValidLocation() {
        return location.lastLat >= -90f && location.lastLat <= 90f &&
               location.lastLong >= -180f && location.lastLong <= 180f;
    }

    private void createFakeLocation() {
        Resources res = context.getResources();
        String[] fakeLocations = res.getStringArray(R.array.fake_locations);
        Random rng = new Random();
        String selectedLocation = fakeLocations[rng.nextInt(fakeLocations.length)];
        String[] coordinates = selectedLocation.split(",");
        location.lastLat = Float.parseFloat(coordinates[0]);
        location.lastLong = Float.parseFloat(coordinates[1]);
    }

    /**
     * Create title and desctiption from the address of the current location
     */
    private void createImageTitleAndDescription() {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = new ArrayList<Address>();
        try {
            addresses = geocoder.getFromLocation(location.lastLat, location.lastLong, 1);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception in getFromLocation(). Lat=" + location.lastLat + ", Long=" + location.lastLong, e);
        }

        if (!addresses.isEmpty()) {
            Address address = addresses.get(0);
            title = address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "";
            description = address.getLocality();
        }
    }

    /**
     * This URL will be used by Muzei to fetch the actual image
     */
    public String getImageUrl() {
        if (style.source == style.SOURCE_MAPBOX) {
            return MAPBOX_BASE_IMAGE_URL + style.getMapId() + "/" + location.lastLong + "," + location.lastLat +
                    "," + zoom + "/1024x1024.png?access_token=" + Config.MAPBOX_API_KEY;
        } else {
            return GOOGLE_BASE_IMAGE_URL + location.lastLat + ',' + location.lastLong + "&zoom=" + zoom +
                    "&size=1024x1024&scale=2&sensor=false" + style.toString() +
                    "&key=" + Config.GOOGLE_API_KEY;
        }
    }

    /**
     * This URL will be used by Muzei when someone click on the description of the map, opening Google Maps
     */
    public String getIntentUrl() {
        return GOOGLE_BASE_INTENT_URL + location.lastLat + "," + location.lastLong
               + "," + zoom+"z";
    }

    public String getToken() {
        return token;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
