package com.ohadshai.mipmap.ui.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.LastSearchPlacesAdapter;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.loaders.LastSearchPlacesLoader;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;

import java.util.ArrayList;

/**
 * Represents a {@link Fragment} for the places list.
 */
public class LastSearchPlacesFragment extends Fragment implements PlaceSynchronizationListener {

    //region Constants

    /**
     * Holds a constant for the last search places loader id.
     */
    private static final int LAST_SEARCH_PLACES_LOADER_ID = 2917126;

    //endregion

    //region Private Members

    /**
     * Holds the view of this fragment.
     */
    private View _view;

    /**
     * Holds the places list.
     */
    private ArrayList<Place> _places = new ArrayList<>();

    /**
     * Holds the RecyclerView control for the places list.
     */
    private RecyclerView _rvPlaces;

    /**
     * Holds the places adapter list for the recycler view.
     */
    private LastSearchPlacesAdapter _adapter;

    /**
     * Holds the ProgressBar control.
     */
    private ProgressBar _progressBar;

    /**
     * Holds the View layout to display when the places list is empty.
     */
    private View _layoutPlacesEmpty;

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the places list.
     */
    public LastSearchPlacesFragment() {
        // Required empty public constructor.
    }

    //region Fragment Events

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_last_search_places, container, false);
        this.initControls();
        return _view;
    }

    //region [Place Synchronization Listener] Events

    @Override
    public void onSearchListAdd(ArrayList<Place> places) {
        _places = places;
        _adapter.setPlaces(_places);
        _adapter.notifyDataSetChanged();
        displayListState();
    }

    @Override
    public void onSearchListShowOnMap(ArrayList<Place> places) {
        _places = places;
        _adapter.setPlaces(_places);
        _adapter.notifyDataSetChanged();
        displayListState();
    }

    @Override
    public void onPlaceShowOnMap(Place place) {
        // Does nothing...
    }

    //endregion

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
            _layoutPlacesEmpty.setVisibility(View.VISIBLE);
        } else {
            _layoutPlacesEmpty.setVisibility(View.GONE);
            _rvPlaces.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Refreshes the last search places list.
     */
    public void refreshList() {
        _adapter.refreshList();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _progressBar = (ProgressBar) _view.findViewById(R.id.progressBar);

        _layoutPlacesEmpty = _view.findViewById(R.id.layoutPlacesEmpty);

        _rvPlaces = (RecyclerView) _view.findViewById(R.id.rvPlaces);
        _adapter = new LastSearchPlacesAdapter(_places, getActivity());
        _rvPlaces.setAdapter(_adapter);
        _rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));

        getLoaderManager().initLoader(LAST_SEARCH_PLACES_LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Place>>() {
            @Override
            public Loader<ArrayList<Place>> onCreateLoader(int id, Bundle args) {
                return new LastSearchPlacesLoader(getContext());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Place>> loader, ArrayList<Place> data) {
                if (data == null)
                    return;

                _places = data;
                _adapter.setPlaces(_places);
                _adapter.notifyDataSetChanged();
                displayListState();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Place>> loader) {
            }
        }).forceLoad();

    }

    //endregion

}
