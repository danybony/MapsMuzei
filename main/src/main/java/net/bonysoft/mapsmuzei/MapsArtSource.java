/*
 * Copyright 2014 Bonysoft (Daniele Bonaldo)
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bonysoft.mapsmuzei;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

public class MapsArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = MapsArtSource.class.getSimpleName();
    private static final String SOURCE_NAME = "MapsArtSource";

    private static final int COMMAND_SHARE_ARTWORK = 1337;
    public static final String ACTION_SETTINGS_MODIFIED = "settings_modified_action";

    public MapsArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK),
                        new UserCommand(COMMAND_SHARE_ARTWORK, getString(R.string.share)));
    }

    @Override
    protected void onCustomCommand(int id) {
        if (id == COMMAND_SHARE_ARTWORK) {
            Artwork currentArtwork = getCurrentArtwork();
            if (currentArtwork == null) {
                Log.w(TAG, "No current artwork, can't share.");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapsArtSource.this,
                                       R.string.source_error_no_artwork_to_share,
                                       Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            String detailUrl = currentArtwork.getViewIntent().getDataString();
            String description = currentArtwork.getByline().trim();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "My Android wallpaper today is the map '"
                                                    + currentArtwork.getTitle().trim()
                                                    + "' on " + description
                                                    + ". #Muzei\n\n"
                                                    + detailUrl);
            shareIntent = Intent.createChooser(shareIntent, "Share artwork");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shareIntent);
        }
        else {
            super.onCustomCommand(id);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            super.onHandleIntent(intent);
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Handle intent: " + intent.getAction());

        String action = intent.getAction();
        if (ACTION_SETTINGS_MODIFIED.equals(action)) {
            scheduleUpdate(System.currentTimeMillis() + 1000);
            return;
        }

        super.onHandleIntent(intent);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isInverted = prefs.getBoolean(Constants.PREF_INVERTED, Constants.PREF_INVERTED_DEFAULT);
        int mapMode = prefs.getInt(Constants.PREF_MAP_TYPE, Constants.PREF_MAP_TYPE_DEFAULT);
        int zoom = prefs.getInt(Constants.PREF_ZOOM, Constants.PREF_ZOOM_DEFAULT);

        MapStyle style = new MapStyle();
        style.setInverted(isInverted);

        MapImage map = new MapImage(this, mapMode, zoom, style);

        if (BuildConfig.DEBUG) Log.d(TAG, "Publishing map: " + map.getTitle() + " URL:" + map.getImageUrl());
        publishArtwork(new Artwork.Builder()
                           .title(map.getTitle())
                           .byline(map.getDescription())
                           .imageUri(Uri.parse(map.getImageUrl()))
                           .token(map.getToken())
                           .viewIntent(new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse(map.getIntentUrl())))
                           .build());

        int updateTimeIndex = prefs.getInt(Constants.PREF_UPDATE_INTERVAL, Constants.PREF_UPDATE_INTERVAL_DEFAULT);
        int updateMinutes = getResources().getIntArray(R.array.update_frequency_values)[updateTimeIndex];
        if (BuildConfig.DEBUG) Log.d(TAG, "Scheduling update in " + updateMinutes + " minutes");
        scheduleUpdate(System.currentTimeMillis() + updateMinutes * 1000 * 60);
    }

}

