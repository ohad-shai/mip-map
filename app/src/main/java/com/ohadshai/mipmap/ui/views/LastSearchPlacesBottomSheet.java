package com.ohadshai.mipmap.ui.views;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.LastSearchPlacesAdapter;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.loaders.LastSearchPlacesLoader;
import com.ohadshai.mipmap.utils.BottomSheet;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;

import java.util.ArrayList;

/**
 * Represents the "last search places" bottom sheet control.
 * Created by Ohad on 2/11/2017.
 */
public class LastSearchPlacesBottomSheet extends BottomSheet implements PlaceSynchronizationListener {

    //region Constants

    /**
     * Holds a constant for the last search places loader id.
     */
    private static final int LAST_SEARCH_PLACES_LOADER_ID = 2111724;

    //endregion

    //region Private Members

    /**
     * Holds the activity owner of the bottom sheet.
     */
    private Activity _activity;

    /**
     * Holds the ImageButton control for the arrow up.
     */
    private ImageButton _imgbtnArrowUp;

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

    //region C'tor

    /**
     * Initializes a new instance of the "last search places" bottom sheet control.
     *
     * @param view     The view of the bottom sheet.
     * @param activity The activity owner of the bottom sheet.
     */
    public LastSearchPlacesBottomSheet(@NonNull View view, @NonNull Activity activity) {
        super(view, activity.getResources().getDimensionPixelOffset(R.dimen.last_search_places_bottom_sheet_peek));
        this._activity = activity;
        this.initControl();
    }

    //endregion

    //region Events

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

    //region Public API

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

    @Override
    public void peek() {
        getBehavior().setHideable(false);
        super.peek();
    }

    @Override
    public void expend() {
        getBehavior().setHideable(false);
        super.expend();
    }

    @Override
    public void hide() {
        getBehavior().setHideable(true);
        super.hide();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes the control.
     */
    private void initControl() {

        peek();

        getBehavior().setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Hides the arrow up button on sliding:
                ViewGroup.LayoutParams layoutParams = _imgbtnArrowUp.getLayoutParams();
                layoutParams.height = (int) (_activity.getResources().getDimensionPixelOffset(R.dimen.last_search_places_bottom_sheet_peek) * (1 - slideOffset));
                _imgbtnArrowUp.setLayoutParams(layoutParams);
            }
        });

        _imgbtnArrowUp = (ImageButton) getView().findViewById(R.id.imgbtnArrowUp);
        _imgbtnArrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expend();
            }
        });

        Button btnClose = (Button) getView().findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peek();
            }
        });

        _progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

        _layoutPlacesEmpty = getView().findViewById(R.id.layoutPlacesEmpty);

        _rvPlaces = (RecyclerView) getView().findViewById(R.id.rvPlaces);
        _adapter = new LastSearchPlacesAdapter(_places, _activity);
        _rvPlaces.setAdapter(_adapter);
        _rvPlaces.setLayoutManager(new LinearLayoutManager(_activity));

        _activity.getLoaderManager().initLoader(LAST_SEARCH_PLACES_LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Place>>() {
            @Override
            public Loader<ArrayList<Place>> onCreateLoader(int id, Bundle args) {
                return new LastSearchPlacesLoader(_activity);
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
