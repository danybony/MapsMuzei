package net.bonysoft.mapsmuzei;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class defining a style for the map, as described in
 * https://developers.google.com/maps/documentation/staticmaps/#StyledMaps
 *
 * Created by Daniele Bonaldo on 2/26/14.
 */
public class MapTheme {
    private static final String TAG = MapTheme.class.getSimpleName();

    public static final int MODE_MAP = 0;
    public static final int MODE_SATELLITE = 1;
    public static final int MODE_TERRAIN = 2;
    public static final int MODE_HYBRID = 3;
    public static final int MODE_CUSTOM = -1;

    public static final String[] MODES = {"roadmap", "satellite", "terrain", "hybrid"};

    public static final int SOURCE_GOOGLE = 0;
    public static final int SOURCE_MAPBOX = 1;

    public static final String[] SOURCES = {"google", "mapbox"};

    public static final String XML_TAG_STYLE = "MapStyle";
    public static final String XML_TAG_THEME = "MapTheme";
    public static final String XML_ATTRIBUTE_NAME = "name";
    public static final String XML_ATTRIBUTE_MAP_TYPE = "mapType";
    public static final String XML_ATTRIBUTE_MAP_SOURCE = "mapSource";
    public static final String XML_ATTRIBUTE_MAP_ID = "mapId";

    private static final String MODE_PREFIX = "&maptype=";
    private static final String STYLE_PREFIX = "&style=";
    private static final String STYLE_INVERT_LIGHTNESS = "invert_lightness:true";

    private boolean isInverted = false;
    int mode = MODE_MAP;
    int source = SOURCE_GOOGLE;
    String mapId;
    private ArrayList<String> styles = new ArrayList<String>();

    public void setMapMode(int mode) {
        this.mode = mode;
    }
    public void setMapSource(int source) { this.source = source; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getMapId() { return this.mapId; }

    public void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MODE_PREFIX).append(MODES[mode]);
        for(String style : styles) {
            sb.append(STYLE_PREFIX).append(style);
        }
        if (isInverted) {
            sb.append(STYLE_PREFIX).append(STYLE_INVERT_LIGHTNESS);
        }

        return sb.toString();
    }

    /**
     * Check id the selected map theme is one of the standard ones (roadmap, satellite, terrain, hybrid)
     * @param mapMode the index of the selected map type
     * @return true if the theme is between roadmap, satellite, terrain or hybrid; false otherwise
     */
    public static boolean isStandardTheme(int mapMode) {
        return mapMode == MODE_MAP || mapMode == MODE_SATELLITE || mapMode == MODE_TERRAIN || mapMode == MODE_HYBRID;
    }

    /**
     * Load the selected theme from the themes XML file, starting from the index of the theme in the all-themes list
     * @param context the context from which access the resources for the themes XML file
     * @param themeId the id of the theme to load
     * @return the loaded theme or a default theme (roadmap) if there was any problem during the loading
     */
    public static MapTheme loadCustomTheme(Context context, int themeId) {
        String[] themesNames = context.getResources().getStringArray(R.array.map_types_titles);
        if (themesNames.length > themeId) {
            return loadFromXml(context, themesNames[themeId]);
        }
        return new MapTheme();
    }

    /**
     * Creates a new Theme loading it from the themes XML file
     * @param context the context from which access the resources for the themes XML file
     * @param themeName the name of the theme to load
     * @return the loaded theme or a default theme (roadmap) if a theme with the given name does not exist
     */
    private static MapTheme loadFromXml(Context context, String themeName) {
        MapTheme newTheme = new MapTheme();
        XmlResourceParser xrp = context.getResources().getXml(R.xml.map_themes);

        try {
            assert xrp != null;

            if (findThemeFromXml(themeName, xrp)) {
                int mapSource = readMapSource(newTheme, xrp);
                if (mapSource == SOURCE_GOOGLE) {
                    readMapType(newTheme, xrp);
                } else if (mapSource == SOURCE_MAPBOX) {
                    readMapId(newTheme, xrp);
                }
                // Load all the styles associated to the theme
                readThemeStyles(newTheme, xrp);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to load theme \"" + themeName + "\" from XML resources", e);
        }

        return newTheme;
    }

    private static void readMapType(MapTheme newTheme, XmlResourceParser xrp) {
        String mapType = xrp.getAttributeValue(null, XML_ATTRIBUTE_MAP_TYPE);
        if (mapType != null) {
            newTheme.setMapMode(getMapTypeIdFromString(mapType));
        }
    }

    private static int readMapSource(MapTheme newTheme, XmlResourceParser xrp) {
        String mapSource = xrp.getAttributeValue(null, XML_ATTRIBUTE_MAP_SOURCE);
        if (mapSource != null) {
            newTheme.setMapSource(getMapSourceIdFromString(mapSource));
            return getMapSourceIdFromString(mapSource);
        }
        return SOURCE_GOOGLE;
    }

    private static void readMapId(MapTheme newTheme, XmlResourceParser xrp) {
        String mapId = xrp.getAttributeValue(null, XML_ATTRIBUTE_MAP_ID);
        if (mapId != null) {
            newTheme.setMapId(mapId);
        }
    }

    private static int getMapTypeIdFromString(String mapTypeName) {
        int i=0;
        for (String s : MODES) {
            if (s.equals(mapTypeName)) {
                return i;
            }
            i++;
        }
        return MODE_MAP;
    }

    private static int getMapSourceIdFromString(String mapSourceName) {
        int i=0;
        for (String s : SOURCES) {
            if (s.equals(mapSourceName)) {
                return i;
            }
            i++;
        }
        return SOURCE_GOOGLE;
    }

    /**
     * Find the node associated to the selected theme in the loaded XML
     *
     * @param themeName the name of the theme to look for
     * @param xrp the XmlResourceParser used to load the themes XML file
     * @return true if atheme with the given name has been found, false otherwise
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static boolean findThemeFromXml(String themeName, XmlResourceParser xrp) throws XmlPullParserException, IOException {
        int eventType = xrp.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG
                && xrp.getName().equalsIgnoreCase(XML_TAG_THEME)
                && themeName.equals(xrp.getAttributeValue(null, XML_ATTRIBUTE_NAME))) {
                break;
            }
            eventType = xrp.next();
        }
        return eventType != XmlPullParser.END_DOCUMENT;
    }

    /**
     * Read all the styles contained in the node of the current theme
     * @param newTheme the target style in which the loaded styles will be saved
     * @param xrp  the XmlResourceParser used to load the themes XML file
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void readThemeStyles(MapTheme newTheme, XmlResourceParser xrp) throws XmlPullParserException, IOException {
        int eventType = xrp.next();
        while (!(eventType == XmlPullParser.END_TAG && xrp.getName().equalsIgnoreCase(XML_TAG_THEME))) {
            if (eventType == XmlPullParser.START_TAG
                && xrp.getName().equalsIgnoreCase(XML_TAG_STYLE)) {
                eventType = xrp.next();
                if (eventType == XmlPullParser.TEXT) {
                    String style = xrp.getText();
                    newTheme.styles.add(style);
                }
            }
            eventType = xrp.next();
        }
    }

}
