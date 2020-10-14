package com.ohadshai.mipmap.ui.activities;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.dialogs.AboutDialog;
import com.ohadshai.mipmap.ui.fragments.LastSearchPlacesFragment;
import com.ohadshai.mipmap.ui.fragments.MapContainerFragment;
import com.ohadshai.mipmap.ui.views.FavoritesRevealHelper;
import com.ohadshai.mipmap.ui.views.LastSearchPlacesBottomSheet;
import com.ohadshai.mipmap.ui.views.SearchHelper;
import com.ohadshai.mipmap.ui.views.SnackbarManager;
import com.ohadshai.mipmap.utils.PermissionsHelper;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;
import com.ohadshai.mipmap.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PlaceSynchronizationListener {

    //region Private Members

    /**
     * Holds the SharedPreferences control.
     */
    private SharedPreferences _preferences;

    /**
     * Holds the floating action button for locating the user location on the map.
     */
    private FloatingActionButton _fabLocate;

    /**
     * Holds the broadcast receiver for the battery charging.
     */
    private BatteryChargingReceiver _batteryChargingReceiver;

    /**
     * Holds the Bottom Sheet control for the last search places list (in portrait state).
     */
    private LastSearchPlacesBottomSheet _bottomSheet;

    //endregion

    //region Activity Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enables activity transition:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initControls();

        if (savedInstanceState == null)
            this.firstInitControls(); // Initializes controls for the first load of the application.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSearch:
                this.actionSearch();
                return true;
            case R.id.actionFavoritePlaces:
                this.actionFavoritePlaces();
                return true;
            case R.id.actionSettings:
                this.actionSettings();
                return true;
            case R.id.actionAbout:
                new AboutDialog().show(getFragmentManager(), UIConsts.Fragments.ABOUT_DIALOG_TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Transfers the "onActivityResult" event to the fragment if found:
        MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
        if (mapContainerFragment != null)
            mapContainerFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Fires the "onRequestPermissionsResult" event in the "PermissionsHelper", to let the "PermissionsHelper" deal with permissions:
        PermissionsHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //region [Place Synchronization Listener] Events

    @Override
    public void onSearchListAdd(ArrayList<Place> places) {
        // Checks the orientation of the device, to show different states for portrait / landscape:
        if (isTwoPaneMode()) {
            // Shows the places list:
            LastSearchPlacesFragment lastSearchPlacesFragment = (LastSearchPlacesFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.LAST_SEARCH_PLACES_FRAGMENT_TAG);
            if (lastSearchPlacesFragment != null)
                lastSearchPlacesFragment.onSearchListAdd(places);

            // Clears the map:
            MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
            if (mapContainerFragment != null)
                mapContainerFragment.onSearchListAdd(places);
        } else {
            // Shows the places list in a bottom sheet mode:
            _bottomSheet.onSearchListAdd(places);

            // Clears the map:
            MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
            if (mapContainerFragment != null)
                mapContainerFragment.onSearchListAdd(places);
        }
    }

    @Override
    public void onSearchListShowOnMap(ArrayList<Place> places) {
        // Checks the orientation of the device, to show different states for portrait / landscape:
        if (isTwoPaneMode()) {
            // Shows the places list:
            LastSearchPlacesFragment lastSearchPlacesFragment = (LastSearchPlacesFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.LAST_SEARCH_PLACES_FRAGMENT_TAG);
            lastSearchPlacesFragment.onSearchListShowOnMap(places);

            // Shows the places markers on the map:
            MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
            mapContainerFragment.onSearchListShowOnMap(places);
        } else {
            // Shows the places list in a bottom sheet mode:
            _bottomSheet.onSearchListShowOnMap(places);

            // Shows the places markers on the map:
            MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
            mapContainerFragment.onSearchListShowOnMap(places);
        }
    }

    @Override
    public void onPlaceShowOnMap(Place place) {
        // Shows the place marker on the map:
        MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
        mapContainerFragment.onPlaceShowOnMap(place);
    }

    //endregion

    @Override
    public void onBackPressed() {
        if (SearchHelper.with(this).handleBackPress())
            return;
        else if (FavoritesRevealHelper.with(this).handleBackPress())
            return;

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (FavoritesRevealHelper.isRevealed())
            FavoritesRevealHelper.with(this).refresh();

        if (isTwoPaneMode()) {
            LastSearchPlacesFragment lastSearchPlacesFragment = (LastSearchPlacesFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.LAST_SEARCH_PLACES_FRAGMENT_TAG);
            if (lastSearchPlacesFragment != null)
                lastSearchPlacesFragment.refreshList();
        } else {
            if (_bottomSheet != null)
                _bottomSheet.refreshList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregisters the battery charging broadcast receiver:
        if (_batteryChargingReceiver != null)
            unregisterReceiver(_batteryChargingReceiver);

        SnackbarManager.with(this).removeRelation();
    }

    //endregion

    //region Public API

    /**
     * Gets the FloatingActionButton control for locating the user location on the map.
     *
     * @return Returns the FloatingActionButton control.
     */
    public FloatingActionButton getFabLocate() {
        return _fabLocate;
    }

    /**
     * Gets the "last search places" bottom sheet.
     *
     * @return Returns the "last search places" bottom sheet.
     */
    public LastSearchPlacesBottomSheet getBottomSheet() {
        return _bottomSheet;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _fabLocate = (FloatingActionButton) findViewById(R.id.fabLocate);
        _fabLocate.hide();

        // Registers the battery charging broadcast receiver:
        _batteryChargingReceiver = new BatteryChargingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(_batteryChargingReceiver, filter);

        // Checks to initial the right orientation state:
        MapContainerFragment mapContainerFragment = (MapContainerFragment) getFragmentManager().findFragmentByTag(UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
        if (isTwoPaneMode()) {
            // Hides the bottom sheet:
            findViewById(R.id.sheetLastSearchPlaces).setVisibility(View.GONE);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (mapContainerFragment == null)
                transaction.replace(R.id.fragmentMapContainer, new MapContainerFragment(), UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG);
            transaction.replace(R.id.fragmentLastSearchPlaces, new LastSearchPlacesFragment(), UIConsts.Fragments.LAST_SEARCH_PLACES_FRAGMENT_TAG);
            transaction.commit();
        } else {
            if (mapContainerFragment == null)
                getFragmentManager().beginTransaction().replace(R.id.fragmentMapContainer, new MapContainerFragment(), UIConsts.Fragments.MAP_CONTAINER_FRAGMENT_TAG).commit();

            // Portrait state gets a bottom sheet, to display the last search places list:
            _bottomSheet = new LastSearchPlacesBottomSheet(findViewById(R.id.sheetLastSearchPlaces), this);
        }

        SearchHelper.with(this).initialize(new SearchHelper.SearchHelperCallback() {
            @Override
            public void onSearchEnter() {
                _fabLocate.hide();

                if (_bottomSheet != null)
                    _bottomSheet.hide();
            }

            @Override
            public void onSearchExited() {
                _fabLocate.show();

                if (_bottomSheet != null)
                    _bottomSheet.peek();
            }
        });

        FavoritesRevealHelper.with(this).initialize(new FavoritesRevealHelper.RevealCallback() {
            @Override
            public void onReveal() {
                _fabLocate.hide();

                if (_bottomSheet != null)
                    _bottomSheet.hide();
            }

            @Override
            public void onConcealed() {
                _fabLocate.show();

                if (_bottomSheet != null)
                    _bottomSheet.peek();
            }
        });

    }

    /**
     * First initializations for controls.
     */
    private void firstInitControls() {

        DBHandler.getInstance(this).initialize(); // Initializes the database to make faster interactions for others.

        // Checks the availability of Google Play Services:
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available:
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 10);
            dialog.show();
        }

    }

    /**
     * Indicates whether the state of the activity is in two pane mode or not.
     *
     * @return Returns true if the state of the activity is in two pane mode, otherwise false.
     */
    private boolean isTwoPaneMode() {
        return getResources().getBoolean(R.bool.isTwoPaneMode);
    }

    /**
     * Validates the map exists and loaded.
     *
     * @return Returns true if the map exists, otherwise false.
     */
    private boolean validateMapExists() {
        // Validates "Google Play Services" is available:
        if (!Utils.GPS.isPlayServicesAvailable(this)) {
            SnackbarManager.with(this).make(findViewById(R.id.coordinatorAboveBottomSheet), getString(R.string.general_msg_update_play_services), Snackbar.LENGTH_LONG).show();
            return false;
        }
        // Validates the map resources loaded:
        else if (!_preferences.getBoolean(Utils.Preferences.Keys.MAP_RESOURCES_LOADED, false)) {
            Utils.UI.showNoConnectionDialogForMap(this);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Method procedure for menu action: "Search".
     */
    private void actionSearch() {
        if (!this.validateMapExists())
            return;

        if (SnackbarManager.with(this).isShowing())
            SnackbarManager.with(this).dismiss();

        SearchHelper.with(this).enter();
    }

    /**
     * Method procedure for menu action: "Favorite Places".
     */
    private void actionFavoritePlaces() {
        if (!this.validateMapExists())
            return;

        if (SnackbarManager.with(this).isShowing())
            SnackbarManager.with(this).dismiss();

        FavoritesRevealHelper.with(this).reveal();
    }

    /**
     * Method procedure for menu action: "Settings".
     */
    private void actionSettings() {
        if (!this.validateMapExists())
            return;

        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    //endregion

    //region Broadcast Receivers

    /**
     * Represents a broadcast receiver for battery charging.
     */
    private class BatteryChargingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                // Displays a charging message:
                SnackbarManager.with(MainActivity.this).make(findViewById(R.id.coordinatorAboveBottomSheet), R.string.general_msg_charging, Snackbar.LENGTH_SHORT).show();
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                // Displays a not charging message:
                SnackbarManager.with(MainActivity.this).make(findViewById(R.id.coordinatorAboveBottomSheet), R.string.general_msg_not_charging, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    //endregion

}
