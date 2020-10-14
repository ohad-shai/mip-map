package com.ohadshai.mipmap.ui.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.ip_api.IPLocation;
import com.ohadshai.mipmap.services.IpApiFindUserLocationService;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.activities.MainActivity;
import com.ohadshai.mipmap.ui.activities.PlaceDisplayActivity;
import com.ohadshai.mipmap.ui.views.FavoritesRevealHelper;
import com.ohadshai.mipmap.ui.views.SearchHelper;
import com.ohadshai.mipmap.ui.views.SnackbarManager;
import com.ohadshai.mipmap.utils.LocationTrackerHelper;
import com.ohadshai.mipmap.utils.PermissionsHelper;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;
import com.ohadshai.mipmap.utils.Utils;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Represents a {@link Fragment} for the map container.
 */
public class MapContainerFragment extends Fragment implements PlaceSynchronizationListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener, LocationTrackerHelper.TrackerCallback {

    //region Constants

    /**
     * Holds a constant for the request code of the location settings dialog.
     */
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 24171;

    /**
     * Holds the request code for the place display activity.
     */
    public final static int PLACE_DISPLAY_REQUEST_CODE = 24172;

    //endregion

    //region Private Members

    /**
     * Holds the view of this fragment.
     */
    private View _view;

    /**
     * Holds the map fragment control.
     */
    private MapFragment _mapFragment;

    /**
     * Holds the device's location.
     */
    private LatLng _deviceLocation;

    /**
     * Holds the "Google Map" control.
     */
    private GoogleMap _googleMap;

    /**
     * Holds the floating action button for the "Locate Me" action.
     */
    private FloatingActionButton _fabLocate;

    /**
     * Holds the SharedPreferences control.
     */
    private SharedPreferences _preferences;

    /**
     * Holds an indicator indicating whether the instance is first or not.
     */
    private boolean _isFirstInstance;

    /**
     * Holds the saved instance state.
     */
    private Bundle _savedInstanceState;

    /**
     * Holds a list of all the marked places on the map.
     */
    private ArrayList<Place> _markedPlaces = new ArrayList<>();

    /**
     * Holds an indicator indicating whether to notify the user that showing his location or not.
     */
    private boolean _shouldNotifyShowingLocation = false;

    /**
     * Holds an indicator indicating whether currently looking for GPS signal or not.
     */
    private boolean _isLookingForGPS = false;

    /**
     * Holds a place object to add a marker on the map, when it is requested.
     */
    private Place _placeToAddMarker = null;

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the map container.
     */
    public MapContainerFragment() {
        // Required empty public constructor.
    }

    //region Fragment Events

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_map_container, container, false);
        _savedInstanceState = savedInstanceState;
        _isFirstInstance = (savedInstanceState == null);
        this.initControls();
        return _view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _googleMap = googleMap;
        _googleMap.setOnInfoWindowClickListener(this);
        _googleMap.setOnMapClickListener(this);

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);

        if (_isFirstInstance)
            this.showDeviceLocation();

        this.restoreOnMapReady();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Place place = (Place) marker.getTag();
        if (place == null)
            throw new NullPointerException("place");

        Intent displayIntent = new Intent(getContext(), PlaceDisplayActivity.class);

        if (place.getId() > 0)
            displayIntent.putExtra(UIConsts.Intent.PLACE_ID_KEY, place.getId());
        else
            displayIntent.putExtra(UIConsts.Intent.GOOGLE_PLACE_ID_KEY, place.getGooglePlaceId());

        startActivityForResult(displayIntent, MapContainerFragment.PLACE_DISPLAY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            this.showDeviceLocation();
        } else if (requestCode == PLACE_DISPLAY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Place place = data.getParcelableExtra(UIConsts.Intent.PLACE_KEY);
            if (place != null) {
                if (_googleMap == null)
                    _placeToAddMarker = place;
                else
                    this.showPlaceMarkerFromPlaceDisplay(place);
            }
        }
    }

    //region [Place Synchronization Listener] Events

    @Override
    public void onSearchListAdd(ArrayList<Place> places) {
        // Clears all the markers on the map, when a new search is entered:
        this.clearAllPlaceMarkersOnMap();
    }

    @Override
    public void onSearchListShowOnMap(ArrayList<Place> places) {
        for (int i = places.size() - 1; i >= 0; i--)
            this.addPlaceMarkerOnMap(places.get(i), true).showInfoWindow();
    }

    @Override
    public void onPlaceShowOnMap(Place place) {
        this.addPlaceMarkerOnMap(place, true).showInfoWindow();
    }

    //endregion

    //region [Location Tracker Helper] Events

    @Override
    public void onLocationTrackerEnabled() {
        if (SearchHelper.isInSearchMode() || FavoritesRevealHelper.isRevealed())
            return;

        // Operates only if it's requested to notify showing location:
        if (_shouldNotifyShowingLocation) {
            // Shows a snackbar "looking for GPS":
            SnackbarManager.with(getActivity())
                    .make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), R.string.general_msg_looking_for_gps, 10000)
                    .setTextColor(getResources().getColor(R.color.colorAccent))
                    .setTextBold(true)
                    .show();
            _isLookingForGPS = true;
        }
    }

    @Override
    public void onFirstLocationTrack() {
        // Operates only if it's not a first instance, and it's requested to notify showing location:
        if (_shouldNotifyShowingLocation && _isLookingForGPS) {
            // Shows a snackbar informing the location found:
            SnackbarManager.with(getActivity())
                    .make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), R.string.general_msg_location_found, Snackbar.LENGTH_SHORT)
                    .setTextColor(Color.rgb(0, 220, 0))
                    .show();

            // Navigates to the location tracked:
            _deviceLocation = Utils.LatLng.parse(_preferences.getString(Utils.Preferences.Keys.LAST_LOCATION, null));
            if (_deviceLocation != null) {
                this.navigateMapToLocation(_deviceLocation, 16);
                this.saveMapZoom(16);
            }
        }

        // For devices below API 21: just shows the location:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Navigates to the location tracked:
            _deviceLocation = Utils.LatLng.parse(_preferences.getString(Utils.Preferences.Keys.LAST_LOCATION, null));
            if (_deviceLocation != null) {
                this.navigateMapToLocation(_deviceLocation, 16);
                this.saveMapZoom(16);
            }
        }

        // Clears indicators:
        _shouldNotifyShowingLocation = false;
        _isLookingForGPS = false;
    }

    @Override
    public void onLocationTrackDisabled() {
        if (SearchHelper.isInSearchMode() || FavoritesRevealHelper.isRevealed())
            return;

        // Location is disabled, shows a nice Snackbar:
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), getString(R.string.general_msg_enable_gps), 10000)
                .setAction(getString(R.string.general_btn_enable), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PermissionsHelper.with(getActivity())
                                .needs(PermissionsHelper.LOCATION)
                                .run(new PermissionsHelper.PermissionResultCallback() {
                                    @Override
                                    public void onPermissionGranted() {
                                        _shouldNotifyShowingLocation = true;
                                        Utils.GPS.displayLocationSettingsRequest(getActivity(), LOCATION_SETTINGS_REQUEST_CODE);
                                    }
                                });
                    }
                })
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        SnackbarManager.with(getActivity()).set(snackbar).show();
    }

    //endregion

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Saves the state of the marked places on the map:
        outState.putParcelableArrayList(UIConsts.Bundles.MARKED_PLACES_LIST, _markedPlaces);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // If the GPS location tracking is open, then closes it:
        LocationTrackerHelper.getInstance().close();
    }

    //endregion

    //region Public API

    @Override
    public Context getContext() {
        if (_view != null)
            return _view.getContext();
        else
            return super.getContext();
    }

    /**
     * Gets the activity owner of the "MapContainerFragment".
     *
     * @return Returns the activity owner of the "MapContainerFragment".
     */
    public MainActivity getActivityOwner() {
        return (MainActivity) getActivity();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        this.initMap();

        if (PermissionsHelper.checkPermission(PermissionsHelper.LOCATION, getContext()))
            LocationTrackerHelper.getInstance().open(getContext(), this);

        _fabLocate = getActivityOwner().getFabLocate();
        _fabLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionLocateMe();
            }
        });
        // Animates the fab on start:
        _fabLocate.hide();
        if (!SearchHelper.isInSearchMode() && !FavoritesRevealHelper.isRevealed()) {
            _fabLocate.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _fabLocate.show();
                }
            }, 250);
        }

        Button btnRefreshMapLoad = (Button) _view.findViewById(R.id.btnRefreshMapLoad);
        btnRefreshMapLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMap();
            }
        });

    }

    /**
     * Initializes the map by the state of the instance, and handles the validation of the map resources load.
     */
    private void initMap() {

        LinearLayout layoutMapLoadError = (LinearLayout) _view.findViewById(R.id.layoutMapLoadError);
        layoutMapLoadError.setVisibility(View.GONE);

        // Checks if the map resources has been loaded to the application before:
        if (_preferences.getBoolean(Utils.Preferences.Keys.MAP_RESOURCES_LOADED, false)) {
            // Map resources loaded:
            if (!_isFirstInstance) {
                // If it's not the first instance, gets the initialized map:
                _mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_FRAGMENT_TAG);
                _mapFragment.getMapAsync(this);
                return;
            }
        } else {
            // Map resources not loaded, so checks for network connection in order to load map resources:
            if (!Utils.Networking.isNetworkAvailable(getContext())) {
                Utils.UI.showNoConnectionDialogForMap(getContext());
                layoutMapLoadError.setVisibility(View.VISIBLE);
                return;
            } else {
                // Network connection is available, so map resources loaded, then saves an indicator to the preferences:
                _preferences.edit().putBoolean(Utils.Preferences.Keys.MAP_RESOURCES_LOADED, true).apply();
            }
        }

        // Here, the map can be initialized:
        _mapFragment = new MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.mapContainer, _mapFragment, UIConsts.Fragments.MAP_FRAGMENT_TAG).commit();
        _mapFragment.getMapAsync(this);
    }

    /**
     * Restores the last instance state when the map is ready.
     */
    private void restoreOnMapReady() {
        if (_savedInstanceState == null)
            return;

        // Restores the state of the marked places on the map:
        ArrayList<Place> places = _savedInstanceState.getParcelableArrayList(UIConsts.Bundles.MARKED_PLACES_LIST);
        if (places != null)
            for (Place place : places)
                this.addPlaceMarkerOnMap(place, false).showInfoWindow();

        if (_placeToAddMarker != null) {
            this.showPlaceMarkerFromPlaceDisplay(_placeToAddMarker);
            _placeToAddMarker = null;
        }
    }

    /**
     * Navigates the map to a location (with animation).
     *
     * @param location The location to navigate the map to.
     * @param zoom     The zoom of the map.
     */
    private void navigateMapToLocation(LatLng location, float zoom) {
        if (_googleMap == null)
            throw new NullPointerException("_googleMap");
        if (location == null)
            throw new NullPointerException("location");

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoom);
        _googleMap.animateCamera(cameraUpdate);
    }

    /**
     * Saves the map zoom in the SharedPreferences.
     *
     * @param zoom The zoom of the map to save.
     */
    private void saveMapZoom(float zoom) {
        _preferences.edit().putFloat(Utils.Preferences.Keys.MAP_ZOOM, zoom).apply();
    }

    /**
     * Gets the saved map zoom from the shared preferences.
     *
     * @return Returns the saved map zoom from the shared preferences.
     */
    private float getMapZoomFromSP() {
        return _preferences.getFloat(Utils.Preferences.Keys.MAP_ZOOM, 10);
    }

    /**
     * Shows the device's location on the map.
     */
    private void showDeviceLocation() {
        if (_googleMap == null)
            throw new NullPointerException("_googleMap");

        // Gets the last location from the SharedPreferences:
        _deviceLocation = Utils.LatLng.parse(_preferences.getString(Utils.Preferences.Keys.LAST_LOCATION, null));

        // Checks if there's no user location, starts a service to get the user's IP location:
        if (_deviceLocation == null) {
            // If there's no last location in the SharedPreferences, gets the device's location by the IP:
            // Registers the finish broadcast receiver:
            IntentFilter filter = new IntentFilter(IpApiFindUserLocationService.FINISH_BROADCAST_NAME);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new IPLocationFindFinishedReceiver(), filter);
            // Starts a service to get the IP location:
            Intent intent = new Intent(getActivity(), IpApiFindUserLocationService.class);
            getActivity().startService(intent);
        }

        // Checks if there's a Location permission, and Location is enabled:
        if (PermissionsHelper.checkPermission(PermissionsHelper.LOCATION, getContext()) && Utils.GPS.checkGpsEnabled(getContext())) {
            _shouldNotifyShowingLocation = true;
            _googleMap.setMyLocationEnabled(true);

            // Checks if the GPS location is not tracking, starts a new location track:
            if (!LocationTrackerHelper.getInstance().isOpened())
                LocationTrackerHelper.getInstance().open(getContext(), this);

            if (_deviceLocation != null)
                this.navigateMapToLocation(_deviceLocation, this.getMapZoomFromSP());
        } else {
            // Location is disabled, shows a nice Snackbar:
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), getString(R.string.general_msg_enable_gps), 10000)
                    .setAction(getString(R.string.general_btn_enable), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PermissionsHelper.with(getActivity())
                                    .needs(PermissionsHelper.LOCATION)
                                    .run(new PermissionsHelper.PermissionResultCallback() {
                                        @Override
                                        public void onPermissionGranted() {
                                            _shouldNotifyShowingLocation = true;
                                            Utils.GPS.displayLocationSettingsRequest(getActivity(), LOCATION_SETTINGS_REQUEST_CODE);
                                        }
                                    });
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            SnackbarManager.with(getActivity()).set(snackbar).show();

            // Checks if there's a last location in the SharedPreferences, then shows the last location:
            if (_deviceLocation != null)
                this.navigateMapToLocation(_deviceLocation, _preferences.getFloat(Utils.Preferences.Keys.MAP_ZOOM, 10));
        }

    }

    /**
     * Method procedure for FAB action: "Locate Me".
     */
    private void actionLocateMe() {
        // Validates "Google Play Services" is available:
        if (!Utils.GPS.isPlayServicesAvailable(getContext())) {
            SnackbarManager.with(getActivity()).make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), getString(R.string.general_msg_update_play_services), Snackbar.LENGTH_LONG).show();
            return;
        }
        // Validates the map is available:
        else if (_mapFragment == null) {
            this.initMap();
            return;
        } else if (_googleMap == null) {
            SnackbarManager.with(getActivity()).make(getActivity().findViewById(R.id.coordinatorAboveBottomSheet), getString(R.string.general_msg_unavailable_map), Snackbar.LENGTH_SHORT).show();
            return;
        }

        PermissionsHelper.with(getActivity())
                .needs(PermissionsHelper.LOCATION)
                .run(new PermissionsHelper.PermissionResultCallback() {
                    @Override
                    public void onPermissionGranted() {
                        showDeviceLocation();
                        _shouldNotifyShowingLocation = true;

                        if (!Utils.GPS.checkGpsEnabled(getContext()))
                            Utils.GPS.displayLocationSettingsRequest(getActivity(), LOCATION_SETTINGS_REQUEST_CODE);
                    }
                });
    }

    /**
     * Adds a place marker on the map.
     *
     * @param place      The place to add the marker on the map.
     * @param moveCamera An indicator indicating whether to move the camera to the marker position or not.
     * @return Returns the marker object created.
     */
    private Marker addPlaceMarkerOnMap(Place place, boolean moveCamera) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(place.getLocation().latitude, place.getLocation().longitude))
                .title(place.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        if (moveCamera)
            _googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLocation(), 16));

        _markedPlaces.add(place);

        Marker marker = _googleMap.addMarker(markerOptions);
        marker.setTag(place);
        return marker;
    }

    /**
     * Clears all the place markers on the map.
     */
    private void clearAllPlaceMarkersOnMap() {
        _googleMap.clear();
        _markedPlaces.clear();
    }

    /**
     * Shows and adds a place marker on the map, after returned from the place display activity.
     *
     * @param place The place object to show.
     */
    private void showPlaceMarkerFromPlaceDisplay(Place place) {
        SearchHelper.with((AppCompatActivity) getActivity()).exitNoAnimation();
        FavoritesRevealHelper.with((AppCompatActivity) getActivity()).concealNoAnimation();
        this.clearAllPlaceMarkersOnMap();
        this.addPlaceMarkerOnMap(place, true).showInfoWindow();
    }

    //endregion

    //region Broadcast Receivers

    /**
     * Represents a broadcast receiver for user IP location find finished.
     */
    private class IPLocationFindFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            IPLocation ipLocation = intent.getParcelableExtra(UIConsts.Intent.IP_LOCATION_KEY);
            if (ipLocation != null) {
                _deviceLocation = new LatLng(ipLocation.getLatitude(), ipLocation.getLongitude());
                _preferences.edit().putString(Utils.Preferences.Keys.LAST_LOCATION, Utils.LatLng.serialize(_deviceLocation)).apply();
                navigateMapToLocation(_deviceLocation, 10);
                saveMapZoom(10);
            }
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
        }
    }

    //endregion

}
