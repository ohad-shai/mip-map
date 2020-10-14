package com.ohadshai.mipmap.ui.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.FavoritePlacesAdapter;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.loaders.FavoritePlacesLoader;
import com.ohadshai.mipmap.ui.views.FavoritesRevealHelper;

import java.util.ArrayList;

/**
 * Represents a {@link Fragment} for the favorite places.
 */
public class FavoritePlacesFragment extends Fragment {

    //region Constants

    /**
     * Holds a constant for the favorite places loader id.
     */
    private static final int FAVORITE_PLACES_LOADER_ID = 13117317;

    //endregion

    //region Private Members

    /**
     * Holds an indicator indicating whether it's the first instance of the application or not.
     */
    private static boolean _isFirstInstanceForApp = true;

    /**
     * Holds the view of this fragment.
     */
    private View _view;

    /**
     * Holds an indicator indicating whether it's the first instance of the fragment or not.
     */
    private boolean _isFirstInstance;

    /**
     * Holds the favorite places list.
     */
    private ArrayList<Place> _places = new ArrayList<>();

    /**
     * Holds the RecyclerView for the favorite places list.
     */
    private RecyclerView _rvFavPlaces;

    /**
     * Holds the favorite places adapter list for the recycler view.
     */
    private FavoritePlacesAdapter _adapter;

    /**
     * Holds the ProgressBar control.
     */
    private ProgressBar _progressBar;

    /**
     * Holds the View layout to display when the favorite places list is empty.
     */
    private View _layoutFavPlacesEmpty;

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the favorite places.
     */
    public FavoritePlacesFragment() {
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
        _view = inflater.inflate(R.layout.fragment_favorite_places, container, false);
        _isFirstInstance = (savedInstanceState == null);
        this.initControls();
        return _view;
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
            _rvFavPlaces.setVisibility(View.GONE);
            _layoutFavPlacesEmpty.setVisibility(View.VISIBLE);
        } else {
            _layoutFavPlacesEmpty.setVisibility(View.GONE);
            _rvFavPlaces.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Loads the favorite places list from the DB.
     */
    public void loadFavoritePlacesList() {
        getLoaderManager().initLoader(FAVORITE_PLACES_LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Place>>() {
            @Override
            public Loader<ArrayList<Place>> onCreateLoader(int id, Bundle args) {
                return new FavoritePlacesLoader(getContext());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Place>> loader, ArrayList<Place> data) {
                if (data == null)
                    return;

                // Updates the selected positions with new place references:
                ArrayList<Place> selection = FavoritesRevealHelper.with((AppCompatActivity) getActivity()).getSelection();
                if (selection != null) {
                    ArrayList<Integer> selectedPositions = new ArrayList<>();
                    for (Place place : selection)
                        selectedPositions.add(_places.indexOf(place));

                    selection.clear();
                    for (int position : selectedPositions)
                        selection.add(data.get(position));
                }

                _places.clear();
                for (Place place : data)
                    _places.add(place);

                _adapter.notifyDataSetChanged();
                displayListState();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Place>> loader) {
            }
        }).forceLoad();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _progressBar = (ProgressBar) _view.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);

        _layoutFavPlacesEmpty = _view.findViewById(R.id.layoutFavPlacesEmpty);

        _rvFavPlaces = (RecyclerView) _view.findViewById(R.id.rvFavPlaces);
        _rvFavPlaces.setLayoutManager(new LinearLayoutManager(getContext()));

        // Checks if it's the first instance of the fragment:
        if (_isFirstInstance) {
            _adapter = new FavoritePlacesAdapter(_places, getActivity());
            _rvFavPlaces.setAdapter(_adapter);

            if (_isFirstInstanceForApp) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFavoritePlacesList();
                    }
                }, 700);
                _isFirstInstanceForApp = false;
            } else {
                this.loadFavoritePlacesList();
            }
        } else {
            _adapter.setActivity(getActivity());
            _rvFavPlaces.setAdapter(_adapter);
            displayListState();
        }

    }

    //endregion

}
