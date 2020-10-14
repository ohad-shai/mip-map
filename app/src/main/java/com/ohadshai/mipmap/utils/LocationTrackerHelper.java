package com.ohadshai.mipmap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.R;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Represents a GPS location tracker helper.
 * Created by Ohad on 12/22/2016.
 */
public class LocationTrackerHelper {

    //region Private Members

    /**
     * Holds the instance of the "LocationTrackerHelper" for all the application (in order to implement a singleton manner).
     */
    private static LocationTrackerHelper _instance;

    /**
     * Holds the location tracker for the "LocationTrackerHelper".
     */
    private LocationTrackerHelper.Tracker _tracker;

    /**
     * Holds an indicator indicating whether a location track is opened or not.
     */
    private boolean _isOpened;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a GPS location tracker helper.
     */
    private LocationTrackerHelper() {
        // Disables the initialization of a new instance from the outside,
        // in order to implement a singleton manner.
    }

    //endregion

    //region Public Static API

    /**
     * Gets the "LocationTrackerHelper" instance of the application, or creates a new instance if null.
     *
     * @return Returns the "LocationTrackerHelper" instance of the application.
     */
    public static LocationTrackerHelper getInstance() {
        if (_instance == null)
            _instance = new LocationTrackerHelper();

        return _instance;
    }

    //endregion

    //region Public API

    /**
     * Opens a tracker to track the location, if not opened already.
     *
     * @param context  The context owner of the tracker.
     * @param callback The tracker callback.
     */
    public void open(@NonNull Context context, LocationTrackerHelper.TrackerCallback callback) {
        if (_isOpened)
            return;

        try {
            _tracker = new LocationTrackerHelper.Tracker(context, callback);
        } catch (Exception ex) {
            Toast.makeText(context, context.getString(R.string.general_msg_gps_doesnt_exist), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Closes the tracker of the location track, if opened.
     */
    public void close() {
        if (!_isOpened)
            return;

        _tracker.close();
        _tracker = null;
    }

    /**
     * Checks if a location track is opened or not.
     *
     * @return Returns true if a location track is opened, otherwise false.
     */
    public boolean isOpened() {
        return _isOpened;
    }

    /**
     * Checks if currently tracking the location or not.
     *
     * @return Returns true if currently tracking the location, otherwise false.
     */
    public boolean isTracking() {
        return _tracker != null && _tracker._isTracking;
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a location tracker for the "LocationTrackerHelper".
     */
    private class Tracker implements LocationListener {

        //region Private Members

        /**
         * Holds the context owner of the tracker.
         */
        private Context _context;

        /**
         * Holds an indicator indicating whether currently tracking the location.
         */
        private boolean _isTracking;

        /**
         * Holds the location manager control.
         */
        private LocationManager _locationManager;

        /**
         * Holds the number of tracks (GPS signals received with a location).
         */
        private int _tracks;

        /**
         * Holds the tracker callbacks.
         */
        private LocationTrackerHelper.TrackerCallback _callback;

        //endregion

        //region C'tor

        /**
         * Initializes a new instance of a location tracker for the "LocationTrackerHelper".
         *
         * @param context  The context owner of the tracker.
         * @param callback The tracker callback.
         */
        Tracker(@NonNull Context context, LocationTrackerHelper.TrackerCallback callback) {
            this._context = context;
            this._callback = callback;
            this._tracks = 0;

            if (PermissionsHelper.checkPermission(PermissionsHelper.LOCATION, _context)) {
                _locationManager = (LocationManager) _context.getSystemService(LOCATION_SERVICE);
                _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
                _isOpened = true;

                if (_callback != null)
                    _callback.onLocationTrackerEnabled();
            }
        }

        //endregion

        //region Events

        @Override
        public void onLocationChanged(Location location) {
            _isTracking = true;
            _tracks++;
            this.saveLocationToPreferences(location);

            if (_tracks == 1 && _callback != null)
                _callback.onFirstLocationTrack();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (!_isTracking) {
                if (_callback != null)
                    _callback.onLocationTrackerEnabled();
            }

            _isTracking = true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (_isTracking) {
                if (_callback != null)
                    _callback.onLocationTrackDisabled();
            }

            _isTracking = false;
        }

        //endregion

        /**
         * Closes the tracker.
         */
        void close() {
            _tracks = 0;
            _isTracking = false;
            _isOpened = false;
            _callback = null;

            if (PermissionsHelper.checkPermission(PermissionsHelper.LOCATION, _context) && _locationManager != null)
                _locationManager.removeUpdates(this);
        }

        //region Private Methods

        /**
         * Saves the location to the shared preferences.
         *
         * @param location The location object to save.
         */
        private void saveLocationToPreferences(Location location) {
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(_context).edit();
            edit.putString(Utils.Preferences.Keys.LAST_LOCATION, Utils.LatLng.serialize(new LatLng(location.getLatitude(), location.getLongitude())));
            edit.apply();
        }

        //endregion

    }

    /**
     * Represents the callbacks for the location tracker helper.
     */
    public interface TrackerCallback {

        /**
         * Event occurs when the location tracker is enabled.
         */
        void onLocationTrackerEnabled();

        /**
         * Event occurs when the first track signal received with a location.
         */
        void onFirstLocationTrack();

        /**
         * Event occurs when the location tracker is disabled.
         */
        void onLocationTrackDisabled();

    }

    //endregion

}
