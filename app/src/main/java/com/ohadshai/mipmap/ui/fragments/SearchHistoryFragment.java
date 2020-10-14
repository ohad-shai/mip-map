package com.ohadshai.mipmap.ui.fragments;

import android.animation.ObjectAnimator;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.SearchHistoryAdapter;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Search;
import com.ohadshai.mipmap.loaders.SearchHistoryLoader;

import java.util.ArrayList;

/**
 * Represents a {@link Fragment} for the search history in search mode.
 */
public class SearchHistoryFragment extends Fragment {

    //region Constants

    /**
     * Holds a constant for the search history loader id.
     */
    private static final int SEARCH_HISTORY_LOADER_ID = 394;

    //endregion

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
     * Holds the layout to display when the search history is empty.
     */
    private LinearLayout _layoutNoSearchHistory;

    /**
     * Holds the list of the search history.
     */
    private ArrayList<Search> _searchHistory = new ArrayList<>();

    /**
     * Holds the RecyclerView control for the search history list.
     */
    private RecyclerView _rvSearchHistory;

    /**
     * Holds the adapter for the search history list.
     */
    private SearchHistoryAdapter _adapter;

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the search history in search mode.
     */
    public SearchHistoryFragment() {
        // Required empty public constructor.
    }

    //region Fragment Events

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_search_history, container, false);
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
        if (_searchHistory == null)
            throw new NullPointerException("_searchHistory");

        _progressBar.setVisibility(View.GONE);

        if (this._searchHistory.size() < 1) {
            _rvSearchHistory.setVisibility(View.GONE);
            _layoutNoSearchHistory.setVisibility(View.VISIBLE);
        } else {
            _layoutNoSearchHistory.setVisibility(View.GONE);
            _rvSearchHistory.setVisibility(View.VISIBLE);
            // Animates the search history list:
            _rvSearchHistory.setAlpha(0);
            ObjectAnimator.ofFloat(_rvSearchHistory, "alpha", 1).setDuration(250).start();
        }
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _repository = DBHandler.getInstance(getActivity());

        _progressBar = (ProgressBar) _view.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);

        _layoutNoSearchHistory = (LinearLayout) _view.findViewById(R.id.layoutNoSearchHistory);

        _rvSearchHistory = (RecyclerView) _view.findViewById(R.id.rvSearchHistory);
        _adapter = new SearchHistoryAdapter(_searchHistory, this);
        _rvSearchHistory.setAdapter(_adapter);
        _rvSearchHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Loads the search history list:
        getLoaderManager().initLoader(SEARCH_HISTORY_LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Search>>() {
            @Override
            public Loader<ArrayList<Search>> onCreateLoader(int id, Bundle args) {
                return new SearchHistoryLoader(getContext());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Search>> loader, ArrayList<Search> data) {
                if (data == null)
                    return;

                _searchHistory = data;
                _adapter.setSearchHistory(_searchHistory);
                _adapter.notifyDataSetChanged();
                displayListState();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Search>> loader) {
            }
        }).forceLoad();

    }

    //endregion

}
