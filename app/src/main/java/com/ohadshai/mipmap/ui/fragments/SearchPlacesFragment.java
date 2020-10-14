package com.ohadshai.mipmap.ui.fragments;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.SearchPlacesAdapter;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.SearchInfo;
import com.ohadshai.mipmap.services.DeleteLastSearchPlacesPhotosService;
import com.ohadshai.mipmap.services.SaveSearchPlacesPhotosService;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.views.SearchHelper;
import com.ohadshai.mipmap.utils.NoNetworkException;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;

import java.util.ArrayList;

/**
 * Represents a {@link Fragment} for the search places in search mode.
 */
public class SearchPlacesFragment extends Fragment {

    //region Private Members

    /**
     * Holds the view of this fragment.
     */
    private View _view;

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the ProgressBar control.
     */
    private ProgressBar _progressBar;

    /**
     * Holds the layout to display when the places list is empty.
     */
    private LinearLayout _layoutNoPlaces;

    /**
     * Holds the list of places as a result from a search.
     */
    private ArrayList<Place> _places = new ArrayList<>();

    /**
     * Holds the RecyclerView control for the list of places.
     */
    private RecyclerView _rvPlaces;

    /**
     * Holds the adapter for the places list.
     */
    private SearchPlacesAdapter _adapter;

    /**
     * Holds the task for searching places.
     */
    private SearchPlacesTask _searchPlacesTask;

    /**
     * Holds the FloatingActionButton control for the "show in map" action.
     */
    private FloatingActionButton _fabShowInMap;

    /**
     * Holds an indicator indicating whether it's the first instance of the fragment or not.
     */
    private boolean _isFirstInstance;

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the search places in search mode.
     */
    public SearchPlacesFragment() {
        // Required empty public constructor.
    }

    //region Fragment Events

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_search_places, container, false);
        _isFirstInstance = (savedInstanceState == null);
        this.initControls();
        return _view;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        // If it's not the first instance, then clears the fragment animations:
        if (!_isFirstInstance)
            return new Animator() {
                @Override
                public long getStartDelay() {
                    return 0;
                }

                @Override
                public void setStartDelay(long startDelay) {

                }

                @Override
                public Animator setDuration(long duration) {
                    return null;
                }

                @Override
                public long getDuration() {
                    return 0;
                }

                @Override
                public void setInterpolator(TimeInterpolator value) {

                }

                @Override
                public boolean isRunning() {
                    return false;
                }
            };
        else
            return super.onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancels the search task if running:
        if (_searchPlacesTask != null) {
            _searchPlacesTask.cancel(true);
            _searchPlacesTask = null;
        }

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
     * Displays the state of the adapter (list / empty message), according to the items in the list.
     */
    public void displayListState() {
        _progressBar.setVisibility(View.GONE);

        if (this._places.size() < 1) {
            _rvPlaces.setVisibility(View.GONE);
            _layoutNoPlaces.setVisibility(View.VISIBLE);
            _fabShowInMap.hide();
        } else {
            _layoutNoPlaces.setVisibility(View.GONE);
            _rvPlaces.setVisibility(View.VISIBLE);

            if (!SearchHelper.isInSelectionMode())
                Utils.UI.showFabWithAnimation(_fabShowInMap, 250);
        }
    }

    /**
     * Gets the list of places.
     *
     * @return Returns the list of places.
     */
    public ArrayList<Place> getPlaces() {
        return _places;
    }

    /**
     * Hides the FloatingActionButton "show in map".
     */
    public void hideFabShowInMap() {
        _fabShowInMap.hide();
    }

    /**
     * Shows the FloatingActionButton "show in map".
     */
    public void showFabShowInMap() {
        Utils.UI.showFabWithAnimation(_fabShowInMap, 250);
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _repository = DBHandler.getInstance(getActivity());

        _fabShowInMap = (FloatingActionButton) _view.findViewById(R.id.fabShowInMap);
        _fabShowInMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Closes the search, and shows the list on the map:
                SearchHelper.with((AppCompatActivity) getActivity()).exit();
                ((PlaceSynchronizationListener) getActivity()).onSearchListShowOnMap(_places);
            }
        });
        // Animates the fab on start:
        _fabShowInMap.hide();

        _progressBar = (ProgressBar) _view.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);

        _layoutNoPlaces = (LinearLayout) _view.findViewById(R.id.layoutNoPlaces);

        _rvPlaces = (RecyclerView) _view.findViewById(R.id.rvPlaces);
        _rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));

        // Checks if it's the first instance of the fragment:
        if (_isFirstInstance) {
            _adapter = new SearchPlacesAdapter(_places, getActivity());
            _rvPlaces.setAdapter(_adapter);

            if (getArguments() != null) {
                SearchInfo searchInfo = getArguments().getParcelable(UIConsts.Intent.SEARCH_INFO_KEY);
                if (searchInfo != null) {
                    _searchPlacesTask = new SearchPlacesTask(searchInfo);
                    _searchPlacesTask.execute();
                }
            }
        } else {
            _adapter.setActivity(getActivity());
            _rvPlaces.setAdapter(_adapter);
            displayListState();
        }

    }

    //endregion

    //region [Tasks]

    /**
     * Represents a task for searching places.
     * Created by Ohad on 1/10/2017.
     */
    private class SearchPlacesTask extends AsyncTask<Void, Void, ArrayList<Place>> {

        //region Private Members

        /**
         * Holds the search info object, which holds all the search fields.
         */
        private SearchInfo searchInfo;

        /**
         * Holds a network exception object if thrown.
         */
        private NoNetworkException networkException;

        /**
         * Holds an exception object if thrown.
         */
        private Exception exception;

        //endregion

        /**
         * Initializes a new instance of a task for searching places.
         *
         * @param searchInfo The search info object, which holds all the search fields.
         */
        SearchPlacesTask(@NonNull SearchInfo searchInfo) {
            this.searchInfo = searchInfo;
        }

        //region Task Events

        @Override
        protected ArrayList<Place> doInBackground(Void... params) {
            try {
                String url;
                // Checks if to search nearby or search by text:
                if (searchInfo.isNearby()) {
                    url = GooglePlacesConsts.Urls.SEARCH_NEARBY_LOCATION + Uri.encode((searchInfo.getLocation().latitude + "," + searchInfo.getLocation().longitude)) +
                            GooglePlacesConsts.Params.APPEND_RADIUS + searchInfo.getRadius() +
                            GooglePlacesConsts.Params.APPEND_LANGUAGE + Uri.encode(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Utils.Preferences.Keys.LANGUAGE, "")) +
                            GooglePlacesConsts.Params.APPEND_KEYWORD + (searchInfo.getText() == null ? "" : Uri.encode(searchInfo.getText())) +
                            GooglePlacesConsts.Params.APPEND_TYPE + (searchInfo.getPlaceType() == null ? "" : Uri.encode(searchInfo.getPlaceType().getValue()));
                } else {
                    url = GooglePlacesConsts.Urls.SEARCH_PLACES_BY_TEXT + Uri.encode(searchInfo.getText()) +
                            GooglePlacesConsts.Params.APPEND_LANGUAGE + Uri.encode(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Utils.Preferences.Keys.LANGUAGE, "")) +
                            GooglePlacesConsts.Params.APPEND_TYPE + (searchInfo.getPlaceType() == null ? "" : Uri.encode(searchInfo.getPlaceType().getValue()));
                }
                String json = Utils.Networking.sendHttpRequest(url, getActivity());

                // Builds the places list by the JSON response:
                return Place.parseJsonToList(json);
            } catch (NoNetworkException e) {
                networkException = e;
                return null;
            } catch (Exception ex) {
                exception = ex;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Place> places) {
            if (networkException != null)
                Utils.UI.showNoConnectionDialog(getActivity());
            else if (exception != null)
                Utils.UI.showServiceErrorDialog(getActivity());

            if (places == null) {
                _places = new ArrayList<>();
                displayListState();
                return;
            }

            _places = places;
            _adapter.setPlaces(_places);
            _adapter.notifyDataSetChanged();
            displayListState();
            ((PlaceSynchronizationListener) getActivity()).onSearchListAdd(_places);

            // Starts a service for deleting the last search places photos from the internal storage:
            Intent deleteIntent = new Intent(getContext(), DeleteLastSearchPlacesPhotosService.class);
            deleteIntent.putParcelableArrayListExtra(UIConsts.Intent.PLACES_LIST_KEY, _repository.places.getLastSearchPlacesNotFavorite());
            getActivity().startService(deleteIntent);

            // Deletes the last search places, and adds the new places:
            _repository.places.removeLastSearchPlaces();
            _repository.places.addLastSearch(places);

            // Starts a service for saving the places photos to the internal storage:
            Intent saveIntent = new Intent(getContext(), SaveSearchPlacesPhotosService.class);
            saveIntent.putParcelableArrayListExtra(UIConsts.Intent.PLACES_LIST_KEY, places);
            getActivity().startService(saveIntent);
        }

        //endregion

    }

    //endregion

}
