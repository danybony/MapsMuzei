package net.bonysoft.mapsmuzei;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class SettingsActivity extends Activity {

    private ViewGroup mMainView;
    private TextView mZoomValue;
    private Spinner mUpdateInterval;
    private Spinner mMapType;
    private CheckBox mInvertLightness;
    private SeekBar mZoom;
    private Switch mWiFiOnly;

    private SharedPreferences mPrefs;
    private boolean isSomethingModified = false;
    private boolean isVisibilityInitDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mMainView = (ViewGroup) findViewById(R.id.layout_main);
        mMapType = (Spinner) findViewById(R.id.map_type_spinner);
        mUpdateInterval = (Spinner) findViewById(R.id.update_interval_spinner);
        mInvertLightness = (CheckBox) findViewById(R.id.check_inverse);
        mZoom = (SeekBar) findViewById(R.id.zoom_bar);
        mZoomValue = (TextView) findViewById(R.id.zoom_value);
        mWiFiOnly = (Switch) findViewById(R.id.wifi_only_switch);

        ArrayAdapter<CharSequence> mapTypesAdapter =
            ArrayAdapter.createFromResource(this,
                                            R.array.map_types_titles,
                                            android.R.layout.simple_spinner_item);
        mapTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMapType.setAdapter(mapTypesAdapter);
        mMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePreference(Constants.PREF_MAP_TYPE, position);
                boolean goingToVisible = position == MapImage.MODE_MAP || position == MapImage.MODE_TERRAIN;
                mInvertLightness.setVisibility(goingToVisible ? View.VISIBLE : View.GONE);
                enableAnimateLayoutChanges();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<CharSequence> intervalsAdapter =
            ArrayAdapter.createFromResource(this,
                                            R.array.update_frequency_titles,
                                            android.R.layout.simple_spinner_item);

        intervalsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUpdateInterval.setAdapter(intervalsAdapter);
        mUpdateInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePreference(Constants.PREF_UPDATE_INTERVAL, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mZoomValue.setText(String.valueOf(progress));
                savePreference(Constants.PREF_ZOOM, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mInvertLightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanPreference(Constants.PREF_INVERTED, isChecked);
            }
        });

        mWiFiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                saveBooleanPreference(Constants.PREF_WIFI_ONLY, checked);
            }
        });

        initFromPreferences();
    }

    /**
     * Enable the layout animations for the container layout
     */
    private void enableAnimateLayoutChanges() {
        if (!isVisibilityInitDone) {
            isVisibilityInitDone = true;
            mMainView.setLayoutTransition(new LayoutTransition());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSomethingModified) {
            Intent intent = new Intent(MapsArtSource.ACTION_SETTINGS_MODIFIED);
            intent.setClass(this, MapsArtSource.class);
            startService(intent);
        }
    }

    /**
     * Load the the settings from SharedPreferencies and set the views accordingly
     */
    private void initFromPreferences() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        boolean isInverted = mPrefs.getBoolean(Constants.PREF_INVERTED, Constants.PREF_INVERTED_DEFAULT);
        int mapMode = mPrefs.getInt(Constants.PREF_MAP_TYPE, Constants.PREF_MAP_TYPE_DEFAULT);
        int zoom = mPrefs.getInt(Constants.PREF_ZOOM, Constants.PREF_ZOOM_DEFAULT);
        int updateInterval = mPrefs.getInt(Constants.PREF_UPDATE_INTERVAL, Constants.PREF_UPDATE_INTERVAL_DEFAULT);
        boolean wifiOnly = mPrefs.getBoolean(Constants.PREF_WIFI_ONLY, Constants.PREF_WIFI_ONLY_DEFAULT);

        mInvertLightness.setChecked(isInverted);
        mMapType.setSelection(mapMode);
        mUpdateInterval.setSelection(updateInterval);
        mZoom.setProgress(zoom);
        mZoomValue.setText(String.valueOf(zoom));
        mWiFiOnly.setChecked(wifiOnly);
    }

    private void saveBooleanPreference(String key, boolean value) {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        isSomethingModified = true;
        mPrefs.edit().putBoolean(key, value).apply();
    }

    private void savePreference(String key, int value) {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        isSomethingModified = true;
        mPrefs.edit().putInt(key, value).apply();
    }

}
