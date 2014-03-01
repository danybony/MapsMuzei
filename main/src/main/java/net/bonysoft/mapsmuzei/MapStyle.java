package net.bonysoft.mapsmuzei;

/**
 * Class defining a style for the map, as described in
 * https://developers.google.com/maps/documentation/staticmaps/#StyledMaps
 *
 * Created by Daniele Bonaldo on 2/26/14.
 */
public class MapStyle {
    private static final String STYLE_BASE = "&style=";
    private static final String STYLE_INVERT_LIGHTNESS = "invert_lightness:true";

    private boolean isInverted = false;

    public void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isInverted) {
            sb.append(STYLE_BASE).append(STYLE_INVERT_LIGHTNESS);
        }

        return sb.toString();
    }

}
